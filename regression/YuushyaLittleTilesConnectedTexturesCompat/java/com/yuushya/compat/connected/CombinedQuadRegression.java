package com.yuushya.compat.connected;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.yuushya.compat.connected.mixin.CombinedRenderBoxMixin;
import com.yuushya.compat.fusion.UnculledQuadCache;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Exercises the combined redirect fallback and transformed unculled cache without a game. */
public final class CombinedQuadRegression {
    private static int checks;
    public static void main(String[] args) throws Exception {
        var north = new BakedQuad(new int[32], -1, Direction.NORTH, null, true);
        var extra = new BakedQuad(new int[32], -1, Direction.NORTH, null, true);
        var east = new BakedQuad(new int[32], -1, Direction.EAST, null, true);
        var calls = new AtomicInteger();
        BakedModel model = (BakedModel) Proxy.newProxyInstance(BakedModel.class.getClassLoader(), new Class<?>[]{BakedModel.class}, (proxy, method, values) -> {
            if (method.getName().equals("getQuads") && values.length == 5) {
                calls.incrementAndGet();
                return values[1] == null ? List.of(extra, east) : List.of(north);
            }
            throw new AssertionError("Unexpected model call: " + method);
        });
        var redirect = java.util.Arrays.stream(CombinedRenderBoxMixin.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("yuushyaConnected$getQuads")).findFirst().orElseThrow();
        redirect.setAccessible(true);
        var instance = new CombinedRenderBoxMixin() {};
        Object[] inputs = {model, null, Direction.NORTH, RandomSource.create(1), ModelData.EMPTY, null,
            null, null, null, null, null, null, null, null, null, null, false, -1};
        check(redirect.invoke(instance, inputs).equals(List.of(north)), "no cache keeps ordinary model output");
        inputs[4] = ModelData.builder().with(UnculledQuadCache.PROPERTY, new UnculledQuadCache(1)).build();
        check(redirect.invoke(instance, inputs).equals(List.of(north, extra)), "combined fallback retains Fusion nonculled faces");
        int before = calls.get();
        check(redirect.invoke(instance, inputs).equals(List.of(north, extra)), "combined fallback cache preserves output");
        check(calls.get() == before + 1, "unculled bucket is not fetched again");

        var transformed = new UnculledQuadCache(1);
        var transforms = new AtomicInteger();
        check(transformed.append(Direction.NORTH, null, List.of(), () -> {
            transforms.incrementAndGet(); return List.of(extra, east);
        }).equals(List.of(extra)), "empty directional CTM output still includes transformed unculled north face");
        check(transformed.append(Direction.EAST, null, List.of(), () -> {
            throw new AssertionError("transformed unculled faces must be reused");
        }).equals(List.of(east)), "transformed faces partition by direction");
        check(transforms.get() == 1, "one transform per unculled layer");
        check(transformed.append(null, null, List.of(north), () -> {
            throw new AssertionError("null direction must not append the bucket to itself");
        }).equals(List.of(north)), "no duplication for null direction");
        System.out.println("Combined quad regression passed: " + checks + " assertions (no full CTM/Mixin runtime claim).");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
