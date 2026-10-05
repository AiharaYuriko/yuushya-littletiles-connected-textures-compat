package com.yuushya.compat.ctm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;

/** Tests the native copy/output contracts, not CTM neighbour selection or in-game rendering. */
public final class NativeEmitterRegression {
    public static void main(String[] args) throws Exception {
        nativeTransformAndOrder();
        splitAndOutput(2);
        splitAndOutput(4);
        rejectedQuadIsReset();
        exceptionIsReset();
        nativeOutputRemainsNative();
        System.out.println("PASS: 6 native Sodium emitter regression cases (no in-game visual validation)");
    }

    private static MeshBuilder builder() {
        try {
            return (MeshBuilder) Class.forName("net.caffeinemc.mods.sodium.client.render.frapi.mesh.MeshBuilderImpl")
                    .getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Supply the matching Sodium mod and API JARs", e);
        }
    }

    private static void nativeTransformAndOrder() {
        MeshBuilder target = builder();
        Deque<QuadTransform> transforms = new ArrayDeque<>();
        List<Integer> order = new ArrayList<>();
        transforms.addLast(quad -> { order.add(1); quad.tag(quad.tag() + 10); return true; });
        transforms.addLast(quad -> {
            check(!(quad instanceof CtmQuadCollector.TransformingEmitter), "A wrapper leaked into a native transform");
            check(quad.getClass().getName().contains("sodium"), "Not testing a real Sodium quad");
            order.add(2);
            quad.tag(2);
            return true;
        });
        QuadEmitter emitter = new CtmQuadCollector.TransformingEmitter(target, transforms);
        boolean oldCopyFailed = false;
        try {
            builder().getEmitter().copyFrom(emitter);
        } catch (ClassCastException expected) {
            oldCopyFailed = true;
        }
        check(oldCopyFailed, "Expected Sodium's native-only copyFrom contract");
        square(emitter, 0, 1, 0, 1).emit();
        check(order.equals(List.of(2, 1)), "Transforms must run last-pushed first");
        target.build().forEach(quad -> check(quad.tag() == 12, "Native mutation was lost"));
    }

    private static void splitAndOutput(int parts) {
        MeshBuilder target = builder();
        MeshBuilder extras = builder();
        Deque<QuadTransform> transforms = new ArrayDeque<>();
        // Stand-in for an outer transform (such as emissive processing).
        transforms.addLast(quad -> { quad.colorIndex(17); return true; });
        transforms.addLast(quad -> {
            QuadEmitter extra = extras.getEmitter();
            for (int i = 0; i < parts; i++) {
                // This is the native copyFrom operation that threw in the user's log.
                extra.copyFrom(quad);
                float left = (i % 2) * 0.5f;
                float bottom = parts == 4 ? (i / 2) * 0.5f : 0;
                square(extra, left, left + 0.5f, bottom, bottom + (parts == 4 ? 0.5f : 1));
                extra.tag(i + 1).emit();
            }
            return false; // The split fragments replace, rather than duplicate, the original.
        });
        QuadEmitter emitter = new CtmQuadCollector.TransformingEmitter(target, transforms);
        square(emitter, 0, 1, 0, 1).emit();
        transforms.removeLast();
        Mesh fragments = extras.build();

        // Prove the second failure is reachable with the real Sodium release.
        boolean oldOutputFailed = false;
        try {
            fragments.outputTo(emitter);
        } catch (ClassCastException expected) {
            oldOutputFailed = true;
        }
        check(oldOutputFailed, "Expected Sodium's native-only output contract");
        CtmQuadCollector.outputMesh(fragments, emitter);
        List<Integer> tags = new ArrayList<>();
        float[] area = {0};
        target.build().forEach(quad -> {
            tags.add(quad.tag());
            check(quad.colorIndex() == 17, "Split output skipped the outer transform");
            area[0] += (quad.x(1) - quad.x(0)) * (quad.y(3) - quad.y(0));
        });
        check(tags.size() == parts, "Dropped original was emitted or fragments were lost: " + tags);
        for (int i = 0; i < parts; i++)
            check(tags.get(i) == i + 1, "Fragment metadata was lost");
        check(Math.abs(area[0] - 1) < 0.0001f, "Fragment geometry did not cover the original face");
    }

    private static void rejectedQuadIsReset() {
        MeshBuilder target = builder();
        Deque<QuadTransform> transforms = new ArrayDeque<>();
        transforms.addLast(quad -> false);
        QuadEmitter emitter = new CtmQuadCollector.TransformingEmitter(target, transforms);
        emitter.tag(99).colorIndex(42).normal(0, 1, 0, 0).pos(0, 5, 6, 7).emit();
        check(emitter.tag() == 0 && emitter.colorIndex() == -1 && !emitter.hasNormal(0), "Rejected quad leaked metadata");
        check(emitter.x(0) == 0 && emitter.y(0) == 0, "Rejected quad leaked positions");
        transforms.clear();
        square(emitter, 0, 1, 0, 1).emit();
        int[] count = {0};
        target.build().forEach(quad -> count[0]++);
        check(count[0] == 1, "Rejected original must not enter the mesh");
    }

    private static void exceptionIsReset() {
        MeshBuilder target = builder();
        Deque<QuadTransform> transforms = new ArrayDeque<>();
        IllegalStateException failure = new IllegalStateException("test transform failure");
        transforms.addLast(quad -> { quad.tag(98); throw failure; });
        QuadEmitter emitter = new CtmQuadCollector.TransformingEmitter(target, transforms);
        boolean caught = false;
        try {
            emitter.emit();
        } catch (IllegalStateException expected) {
            check(expected == failure, "Unexpected exception");
            caught = true;
        }
        check(caught && emitter.tag() == 0, "Failed transform was swallowed or left dirty data");
        int[] count = {0};
        target.build().forEach(quad -> count[0]++);
        check(count[0] == 0, "Failed transform committed a quad");
    }

    private static void nativeOutputRemainsNative() {
        MeshBuilder source = builder();
        square(source.getEmitter(), 0, 1, 0, 1).tag(123).emit();
        MeshBuilder target = builder();
        CtmQuadCollector.outputMesh(source.build(), target.getEmitter());
        List<Integer> tags = new ArrayList<>();
        target.build().forEach(quad -> tags.add(quad.tag()));
        check(tags.equals(List.of(123)), "Unrelated native rendering path changed");
    }

    private static QuadEmitter square(QuadEmitter emitter, float left, float right, float bottom, float top) {
        emitter.pos(0, left, bottom, 0).pos(1, right, bottom, 0).pos(2, right, top, 0).pos(3, left, top, 0);
        return emitter;
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
