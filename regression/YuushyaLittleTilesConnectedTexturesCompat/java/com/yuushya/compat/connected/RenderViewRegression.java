package com.yuushya.compat.connected;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/** Reads the actual supplied Vanilla/Sodium/LT bytecode, without starting the game. */
public final class RenderViewRegression {
    private static int checks;
    public static void main(String[] args) throws Exception {
        String state = "Lnet/minecraft/world/level/block/state/BlockState;";
        String pos = "Lnet/minecraft/core/BlockPos;";
        var vanilla = read("net/minecraft/client/renderer/chunk/RenderChunkRegion");
        method(vanilla, "getBlockState", "(" + pos + ")" + state);
        method(vanilla, "getBlockEntity", "(" + pos + ")Lnet/minecraft/world/level/block/entity/BlockEntity;");
        var sodium = read("net/caffeinemc/mods/sodium/client/world/LevelSlice");
        method(sodium, "getBlockState", "(III)" + state);
        method(sodium, "copyData", "(Lnet/caffeinemc/mods/sodium/client/world/cloned/ChunkRenderContext;)V");
        method(sodium, "reset", "()V");
        check(calls(sodium, "getBlockState", "(" + pos + ")" + state, sodium.name, "getBlockState", "(III)" + state), "BlockPos overload delegates to intercepted integer overload");
        var mesher = read("net/caffeinemc/mods/sodium/client/render/chunk/compile/tasks/ChunkBuilderMeshingTask");
        check(mesher.methods.stream().anyMatch(m -> calls(mesher, m.name, m.desc, sodium.name, "getBlockState", "(III)" + state)), "mesher reads intercepted state");
        method(read("team/creative/littletiles/common/block/entity/BETiles"), "updateTiles", "(ZZ)V");
        var tiles = read("team/creative/littletiles/common/block/entity/BETiles");
        method(tiles, "loadAdditional", "(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V");
        method(tiles, "setLevel", "(Lnet/minecraft/world/level/Level;)V");
        check(calls(tiles, "handleUpdate", "(Lnet/minecraft/nbt/CompoundTag;Z)V", tiles.name,
            "loadAdditional", "(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"),
            "chunk packet reaches new loadAdditional refresh hook");
        var refresh = read("com/yuushya/compat/connected/BoundaryRefresh");
        check(calls(refresh, "tick", "(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Post;)V",
            "team/creative/littletiles/client/render/block/BERenderManager", "queue", "(ZZJ)V"),
            "boundary refresh invalidates LT's independent cache");
        check(NeighborMaterials.of(java.util.List.of("m", "other")).contains("m"), "full block recognizes matching material inside a mixed LT container");
        check(!NeighborMaterials.of(java.util.List.of("other")).contains("m"), "other material does not connect");
        var cutMixin = read("com/yuushya/compat/connected/mixin/CutFaceMixin");
        var cutHook = cutMixin.methods.stream().filter(m -> m.name.equals("yuushyaConnected$restoreCutFace")).findFirst().orElseThrow();
        var inject = cutHook.visibleAnnotations.stream().filter(a -> a.desc.endsWith("/Inject;")).findFirst().orElseThrow();
        int selectorIndex = inject.values.indexOf("method") + 1;
        String selector = (String) ((java.util.List<?>) inject.values.get(selectorIndex)).getFirst();
        method(read("team/creative/creativecore/client/render/box/RenderBox"), "getBakedQuad", selector.substring(selector.indexOf('(')));
        for (BackendMode mode : BackendMode.values())
            check(AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.CutFaceMixin") == (mode != BackendMode.NONE),
                "cut faces only enabled with installed backend: " + mode);
        for (BackendMode mode : BackendMode.values())
            check(!AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.OcclusionRefreshMixin"),
                "unsuccessful hidden LT face experiment is disabled: " + mode);
        for (BackendMode mode : BackendMode.values())
            check(AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.NeighborFaceMixin") == (mode != BackendMode.NONE),
                "ordinary-neighbour protection follows backend gating: " + mode);
        method(read("team/creative/littletiles/common/block/mc/BlockTile"), "hidesNeighborFace",
            "(Lnet/minecraft/world/level/BlockGetter;" + pos + state + state + "Lnet/minecraft/core/Direction;)Z");
        coverageTests();
        method(read("net/minecraft/client/resources/model/SimpleBakedModel"), "getQuads",
            "(" + state + "Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;");
        for (BackendMode mode : BackendMode.values())
            check(AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.ColumnModelFaceMixin") == (mode != BackendMode.NONE),
                "model cap completion follows backend gating: " + mode);
        var box = new team.creative.littletiles.common.math.box.LittleBox(0, 0, 0, 8, 16, 16);
        var east = team.creative.creativecore.common.util.math.base.Facing.EAST;
        var covered = team.creative.littletiles.common.math.face.LittleFaceState.OUTISDE_COVERED;
        box.setFaceState(east, covered);
        check(box.hasOrCreateFaceState(null, null, null), "cached face flags bypass face calculation even on a rebuilt render box");
        check(box.getFaceState(east) == covered, "old occlusion remains until explicitly revalidated");
        box.setFaceState(east, team.creative.littletiles.common.math.face.LittleFaceState.INSIDE_UNCOVERED);
        check(!box.getFaceState(east).coveredFully(), "updated visible face no longer requests render suppression");
        method(read("team/creative/littletiles/common/math/face/LittleServerFace"), "calculate",
            "()Lteam/creative/littletiles/common/math/face/LittleFaceState;");
        System.out.println("Render view boundary regression passed: " + checks + " assertions (ABI and material presence, not live Mixin).");
    }
    private static void coverageTests() {
        check(!BoundaryCoverage.full(java.util.List.of()), "empty boundary exposes ordinary face");
        check(BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, 1, 1))), "full cube still hides face");
        check(!BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, .5, 1))), "half face cannot hide ordinary face");
        check(BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, .5, 1),
            new BoundaryCoverage.Rect(.5, 0, 1, 1))), "two abutting pieces fully cover face");
        check(!BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, .5, 1),
            new BoundaryCoverage.Rect(.500244140625, 0, 1, 1))), "one fine-grid gap stays visible");
        check(!BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, 1, .25),
            new BoundaryCoverage.Rect(0, .75, 1, 1), new BoundaryCoverage.Rect(0, .25, .25, .75),
            new BoundaryCoverage.Rect(.75, .25, 1, .75))), "frame with interior hole must not hide ordinary neighbour");
        check(BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, 1, .75),
            new BoundaryCoverage.Rect(0, .5, 1, 1))), "overlapping pieces can prove full coverage");
        check(!BoundaryCoverage.full(java.util.List.of(new BoundaryCoverage.Rect(0, 0, 1, .5),
            new BoundaryCoverage.Rect(0, 0, 1, .5))), "overlap does not count as extra covered area");
    }
    private static ClassNode read(String name) throws Exception {
        try (var input = RenderViewRegression.class.getClassLoader().getResourceAsStream(name + ".class")) {
            if (input == null) throw new AssertionError("Missing target: " + name);
            ClassNode node = new ClassNode(); new ClassReader(input).accept(node, 0); return node;
        }
    }
    private static void method(ClassNode node, String name, String desc) {
        check(node.methods.stream().anyMatch(m -> m.name.equals(name) && m.desc.equals(desc)), node.name + "." + name + desc);
    }
    private static boolean calls(ClassNode node, String name, String desc, String owner, String target, String targetDesc) {
        for (var method : node.methods) if (method.name.equals(name) && method.desc.equals(desc))
            for (var instruction : method.instructions)
                if (instruction instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(target) && call.desc.equals(targetDesc)) return true;
        return false;
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
