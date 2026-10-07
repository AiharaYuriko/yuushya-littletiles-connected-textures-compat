package com.yuushya.compat.connected.mixin;

import java.util.List;
import com.yuushya.compat.ctm.CtmLog;
import com.yuushya.compat.ctm.CtmQuadCollector;
import com.yuushya.compat.ctm.MaterialBlockView;
import com.yuushya.compat.ctm.mixin.LevelAccessorFakeAccessor;
import com.yuushya.compat.fusion.UnculledQuadCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.creativecore.common.util.math.base.Facing;

/** One redirect owns both call sites when both optional backends are installed. */
@Mixin(value = RenderBox.class, remap = false)
public abstract class CombinedRenderBoxMixin {
    private static boolean yuushyaConnected$reportedFailure;

    @Redirect(method = "getBakedQuad(Lteam/creative/creativecore/client/render/box/QuadGeneratorContext;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;Lnet/neoforged/neoforge/client/model/data/ModelData;Lteam/creative/creativecore/common/util/math/base/Facing;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/util/RandomSource;ZI)Ljava/util/List;",
        require = 2, allow = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;"))
    private List<BakedQuad> yuushyaConnected$getQuads(BakedModel model, BlockState state, Direction side, RandomSource random,
            ModelData data, RenderType layer, QuadGeneratorContext holder, LevelAccessor level, BlockPos pos, BlockPos offset,
            BlockState outerState, BakedModel outerModel, ModelData outerData, Facing facing, RenderType outerLayer,
            RandomSource outerRandom, boolean overrideTint, int defaultColor) {
        BlockAndTintGetter view = yuushyaConnected$blockView(level);
        if (pos != null && view != null) {
            try {
                // null means this model is not handled; an empty list is a valid CTM result.
                List<BakedQuad> transformed = CtmQuadCollector.collect(model, view, pos, state, side, random, data, layer);
                if (transformed != null) {
                    UnculledQuadCache cache = data.get(UnculledQuadCache.PROPERTY);
                    return cache == null ? transformed : cache.append(side, layer, transformed, () -> {
                        // Unculled faces must pass through the same CTM transforms, not be appended raw.
                        RandomSource extraRandom = RandomSource.create(state.getSeed(pos));
                        List<BakedQuad> extra = CtmQuadCollector.collect(model, view, pos, state, null, extraRandom, data, layer);
                        return extra != null ? extra : model.getQuads(state, null, extraRandom, data, layer);
                    });
                }
            } catch (RuntimeException | LinkageError error) {
                if (!yuushyaConnected$reportedFailure) {
                    yuushyaConnected$reportedFailure = true;
                    CtmLog.LOGGER.error("Combined CTM bridge failed; falling back to model quads with Fusion unculled faces", error);
                }
            }
        }
        List<BakedQuad> directional = model.getQuads(state, side, random, data, layer);
        UnculledQuadCache cache = data.get(UnculledQuadCache.PROPERTY);
        return cache == null ? directional : cache.append(model, state, side, data, layer, directional);
    }

    private static BlockAndTintGetter yuushyaConnected$blockView(LevelAccessor level) {
        if (level instanceof BlockAndTintGetter view)
            return view;
        if (level instanceof LevelAccessorFake && level instanceof LevelAccessorFakeAccessor accessor
                && accessor.yuushyaLtCtmCompat$getParentLevel() instanceof BlockAndTintGetter parent)
            return new MaterialBlockView(parent, accessor.yuushyaLtCtmCompat$getMaterialPos(), accessor.yuushyaLtCtmCompat$getMaterialState());
        return null;
    }
}
