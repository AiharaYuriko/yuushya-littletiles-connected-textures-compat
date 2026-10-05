package com.yuushya.compat.connected;

import java.io.InputStream;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/** Reads installed bytes directly; does not confuse workspace source with the target ABI. */
public final class TargetJarRegression {
    private static int checks;

    public static void main(String[] args) throws Exception {
        try (ZipFile core = new ZipFile(args[0]); ZipFile tiles = new ZipFile(args[1]);
                ZipFile continuity = new ZipFile(args[2])) {
            String model = "net/minecraft/client/resources/model/BakedModel";
            String quads = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;";
            ClassNode box = read(core, "team/creative/creativecore/client/render/box/RenderBox");
            String boxArgs = "(Lteam/creative/creativecore/client/render/box/QuadGeneratorContext;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;Lnet/neoforged/neoforge/client/model/data/ModelData;Lteam/creative/creativecore/common/util/math/base/Facing;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/util/RandomSource;ZI)Ljava/util/List;";
            method(box, "getBakedQuad", boxArgs);
            calls(box, "getBakedQuad", model, "getQuads", quads, 2);
            method(box, "deleteQuadCache", "()V");
            field(box, "state", "Lnet/minecraft/world/level/block/state/BlockState;");
            ClassNode fake = read(core, "team/creative/creativecore/common/level/LevelAccessorFake");
            field(fake, "level", "Lnet/minecraft/world/level/Level;");
            field(fake, "pos", "Lnet/minecraft/core/BlockPos;");
            field(fake, "state", "Lnet/minecraft/world/level/block/state/BlockState;");
            ClassNode manager = read(tiles, "team/creative/littletiles/client/render/block/BERenderManager");
            method(manager, "beforeBuilding", "(Lteam/creative/littletiles/client/render/cache/build/RenderingBlockContext;)V");
            field(manager, "neighbourChanged", "Z");
            field(manager, "boxCache", "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;");
            calls(read(tiles, "team/creative/littletiles/client/render/cache/build/RenderingThread"), "run", model, "getModelData",
                "(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/neoforged/neoforge/client/model/data/ModelData;", 1);
            String block = "team/creative/littletiles/common/block/mc/BlockTile";
            ClassNode tile = read(tiles, block);
            String load = "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lteam/creative/littletiles/common/block/entity/BETiles;";
            method(tile, "loadBE", load);
            check(tile.methods.stream().anyMatch(m -> m.name.equals("loadBE") && m.desc.equals(load) && (m.access & 8) != 0), "loadBE is static");
            calls(tile, "getAppearance", block, "loadBE", load, 1);
            String mesh = "net/fabricmc/fabric/api/renderer/v1/mesh/Mesh";
            String output = "(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;)V";
            ClassNode emissive = read(continuity, "me/pepperbell/continuity/client/model/EmissiveBakedModel");
            method(emissive, "emitBlockQuads", "(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Ljava/util/function/Supplier;Lnet/fabricmc/fabric/api/renderer/v1/render/RenderContext;)V");
            calls(emissive, "emitBlockQuads", mesh, "outputTo", output, 1);
            ClassNode processing = read(continuity, "me/pepperbell/continuity/impl/client/ProcessingContextImpl");
            method(processing, "outputTo", output);
            calls(processing, "outputTo", mesh, "outputTo", output, 2);
        }
        System.out.println("Installed target JAR audit passed: " + checks + " assertions (no game/Mixin startup claim).");
    }

    private static ClassNode read(ZipFile jar, String name) throws Exception {
        var entry = jar.getEntry(name + ".class");
        if (entry == null) throw new AssertionError("Missing target " + name + " in " + jar.getName());
        try (InputStream input = jar.getInputStream(entry)) {
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, 0);
            return node;
        }
    }

    private static void method(ClassNode node, String name, String desc) {
        check(node.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).count() == 1, node.name + "." + name + desc);
    }

    private static void field(ClassNode node, String name, String desc) {
        check(node.fields.stream().anyMatch(f -> f.name.equals(name) && f.desc.equals(desc)), node.name + "." + name + ":" + desc);
    }

    private static void calls(ClassNode node, String method, String owner, String name, String desc, int expected) {
        int found = 0;
        for (var m : node.methods) if (m.name.equals(method))
            for (var instruction : m.instructions)
                if (instruction instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(desc)) found++;
        check(found == expected, node.name + "." + method + " -> " + owner + "." + name + ": expected " + expected + ", found " + found);
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) throw new AssertionError(description);
    }
}
