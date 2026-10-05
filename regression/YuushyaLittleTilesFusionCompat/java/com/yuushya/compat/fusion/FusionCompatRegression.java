package com.yuushya.compat.fusion;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.face.RenderBoxFace;
import team.creative.creativecore.common.util.math.base.Facing;

/** Headless checks of production cache logic and compiled injection-site ABI, not an in-game test. */
public final class FusionCompatRegression {
    private static int checks;

    public static void main(String[] args) throws Exception {
        partitionAndLayers();
        emptyFastPath();
        isolatedEvaluations();
        dataProperties();
        reproduceStaleQuadAndInvalidate();
        injectionSites();
        System.out.println("Fusion compatibility regression passed: " + checks + " assertions (no visual/Mixin runtime claim).");
    }

    private static BakedQuad quad(Direction direction) {
        return new BakedQuad(new int[32], -1, direction, null, true);
    }

    private static BakedModel model(Function<Object[], List<BakedQuad>> getQuads) {
        return (BakedModel) Proxy.newProxyInstance(BakedModel.class.getClassLoader(), new Class<?>[]{BakedModel.class}, (proxy, method, args) -> {
            if (method.getName().equals("getQuads") && args.length == 5)
                return getQuads.apply(args);
            throw new AssertionError("Unexpected model call: " + method);
        });
    }

    private static void partitionAndLayers() {
        var north = quad(Direction.NORTH);
        var east = quad(Direction.EAST);
        var overlay = quad(Direction.NORTH);
        var solid = RenderType.solid();
        var translucent = RenderType.translucent();
        var calls = new AtomicInteger();
        long seed = 17;
        var cache = new UnculledQuadCache(seed);
        BakedModel model = model(args -> {
            calls.incrementAndGet();
            check(args[1] == null, "only query unculled bucket");
            check(((RandomSource) args[2]).nextLong() == RandomSource.create(seed).nextLong(), "stable seed for each layer");
            return args[4] == translucent ? List.of(overlay) : List.of(north, east);
        });
        for (Direction direction : Direction.values()) {
            List<BakedQuad> result = cache.append(model, null, direction, ModelData.EMPTY, solid, List.of());
            check(result.size() == (direction == Direction.NORTH || direction == Direction.EAST ? 1 : 0), "no six-face duplication: " + direction);
            for (BakedQuad q : result)
                check(q.getDirection() == direction, "matching geometric direction");
        }
        check(calls.get() == 1, "six directions require only one extra model call");
        var original = List.of(quad(Direction.NORTH));
        var merged = cache.append(model, null, Direction.NORTH, ModelData.EMPTY, solid, original);
        check(merged.size() == 2 && merged.get(0) == original.get(0) && merged.get(1) == north, "preserve directional and unculled output order");
        check(original.size() == 1, "never mutate model-owned directional list");
        var transparent = cache.append(model, null, Direction.NORTH, ModelData.EMPTY, translucent, List.of());
        check(transparent.equals(List.of(overlay)) && calls.get() == 2, "layers do not share output");
        cache.append(model, null, Direction.NORTH, ModelData.EMPTY, null, List.of());
        cache.append(model, null, Direction.EAST, ModelData.EMPTY, null, List.of());
        check(calls.get() == 3, "null layer is cached independently");
        check(cache.append(model, null, null, ModelData.EMPTY, solid, original) == original, "null side passes through");
    }

    private static void emptyFastPath() {
        var calls = new AtomicInteger();
        var model = model(args -> { calls.incrementAndGet(); return List.of(); });
        var cache = new UnculledQuadCache(0);
        var original = List.of(quad(Direction.UP));
        for (Direction direction : Direction.values())
            check(cache.append(model, null, direction, ModelData.EMPTY, null, original) == original, "empty extra output reuses original list");
        check(calls.get() == 1, "empty result is cached");
    }

    private static void isolatedEvaluations() {
        var calls = new AtomicInteger();
        var oldQuad = quad(Direction.NORTH);
        var newQuad = quad(Direction.NORTH);
        var model = model(args -> List.of(calls.getAndIncrement() == 0 ? oldQuad : newQuad));
        var first = new UnculledQuadCache(0).append(model, null, Direction.NORTH, ModelData.EMPTY, null, List.of());
        var second = new UnculledQuadCache(0).append(model, null, Direction.NORTH, ModelData.EMPTY, null, List.of());
        check(first.getFirst() == oldQuad && second.getFirst() == newQuad, "rebuild/material evaluations cannot reuse stale extra quads");
    }

    private static void dataProperties() {
        ModelProperty<Object> fusionProperty = new ModelProperty<>();
        Object marker = new Object();
        ModelData original = ModelData.builder().with(fusionProperty, marker).build();
        var cache = new UnculledQuadCache(0);
        ModelData derived = original.derive().with(UnculledQuadCache.PROPERTY, cache).build();
        check(derived.get(fusionProperty) == marker, "preserve backend properties");
        check(derived.get(UnculledQuadCache.PROPERTY) == cache && !original.has(UnculledQuadCache.PROPERTY), "derive does not mutate backend ModelData");
    }

    private static void reproduceStaleQuadAndInvalidate() {
        RenderBox box = new RenderBox(0, 0, 0, 1, 1, 1, (BlockState) null);
        for (Facing facing : Facing.VALUES)
            box.setQuad(facing, List.of(quad(facing.toVanilla())));
        box.doesNeedQuadUpdate = false;
        box.setFace(Facing.NORTH, RenderBoxFace.RENDER);
        check(box.getQuad(Facing.NORTH) != null && !box.doesNeedQuadUpdate,
            "reproduce upstream: recalculating face visibility alone retains old CTM quads");
        box.deleteQuadCache();
        check(box.doesNeedQuadUpdate, "invalidation requests model-data and quad regeneration");
        for (Facing facing : Facing.VALUES)
            check(box.getQuad(facing) == null, "clear old clipped face: " + facing);
        check(box.getFace(Facing.NORTH) == RenderBoxFace.RENDER, "invalidation retains geometry visibility");
    }

    private static void injectionSites() throws Exception {
        String model = "net/minecraft/client/resources/model/BakedModel";
        check(countCalls("team/creative/littletiles/client/render/cache/build/RenderingThread", "run", model, "getModelData") == 1, "one native model-data site");
        check(countCalls("team/creative/creativecore/client/render/box/RenderBox", "getBakedQuad", model, "getQuads") == 2, "both normal and transparent layer sites");
        ClassNode manager = readClass("team/creative/littletiles/client/render/block/BERenderManager");
        check(manager.fields.stream().anyMatch(f -> f.name.equals("neighbourChanged") && f.desc.equals("Z")), "neighbor flag shadow ABI");
        check(manager.fields.stream().anyMatch(f -> f.name.equals("boxCache") && f.desc.equals("Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;")), "box cache shadow ABI");
        check(manager.methods.stream().anyMatch(m -> m.name.equals("beforeBuilding") && m.desc.equals("(Lteam/creative/littletiles/client/render/cache/build/RenderingBlockContext;)V")), "worker injection ABI");
        String blockTile = "team/creative/littletiles/common/block/mc/BlockTile";
        check(countCalls(blockTile, "getAppearance", blockTile, "loadBE") == 1, "one appearance-only neighbor lookup site");
        check(readClass(blockTile).methods.stream().anyMatch(m -> m.name.equals("loadBE") && m.desc.equals(
            "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lteam/creative/littletiles/common/block/entity/BETiles;")), "neighbor lookup redirect ABI");
        ClassNode fake = readClass("team/creative/creativecore/common/level/LevelAccessorFake");
        check(fake.fields.stream().anyMatch(f -> f.name.equals("level") && f.desc.equals("Lnet/minecraft/world/level/Level;")), "fake parent accessor ABI");
        check(fake.fields.stream().anyMatch(f -> f.name.equals("pos") && f.desc.equals("Lnet/minecraft/core/BlockPos;")), "fake position accessor ABI");
        check(fake.fields.stream().anyMatch(f -> f.name.equals("state") && f.desc.equals("Lnet/minecraft/world/level/block/state/BlockState;")), "fake material accessor ABI");
    }

    private static int countCalls(String owner, String method, String targetOwner, String targetMethod) throws Exception {
        int count = 0;
        for (var m : readClass(owner).methods)
            if (m.name.equals(method))
                for (var instruction : m.instructions)
                    if (instruction instanceof MethodInsnNode call && call.owner.equals(targetOwner) && call.name.equals(targetMethod))
                        count++;
        return count;
    }

    private static ClassNode readClass(String name) throws Exception {
        try (InputStream input = FusionCompatRegression.class.getClassLoader().getResourceAsStream(name + ".class")) {
            if (input == null) throw new AssertionError("Missing class: " + name);
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, 0);
            return node;
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
