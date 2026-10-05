package com.yuushya.compat.ctm.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.yuushya.compat.ctm.CtmQuadCollector;
import com.yuushya.compat.ctm.MaterialBlockView;
import com.yuushya.compat.ctm.CtmLog;

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
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.creativecore.common.util.math.base.Facing;

@Mixin(value = RenderBox.class, remap = false)
public abstract class RenderBoxMixin {
    private static boolean yuushyaLtCtmCompat$reportedFailure;

    @Redirect(
            method = "getBakedQuad(Lteam/creative/creativecore/client/render/box/QuadGeneratorContext;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;Lnet/neoforged/neoforge/client/model/data/ModelData;Lteam/creative/creativecore/common/util/math/base/Facing;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/util/RandomSource;ZI)Ljava/util/List;",
            require = 2, allow = 2,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;"))
    private List<BakedQuad> yuushyaLtCtmCompat$emitCtmQuads(BakedModel model, BlockState state, Direction side, RandomSource random,
            ModelData modelData, RenderType renderType, QuadGeneratorContext holder, LevelAccessor level, BlockPos pos, BlockPos offset,
            BlockState outerState, BakedModel outerModel, ModelData outerModelData, Facing facing, RenderType outerLayer, RandomSource outerRandom,
            boolean overrideTint, int defaultColor) {
        BlockAndTintGetter blockView = yuushyaLtCtmCompat$blockView(level);
        if (pos != null && blockView != null) {
            try {
                List<BakedQuad> transformed = CtmQuadCollector.collect(model, blockView, pos, state, side, random, modelData, renderType);
                if (transformed != null)
                    return transformed;
            } catch (RuntimeException | LinkageError error) {
                if (!yuushyaLtCtmCompat$reportedFailure) {
                    yuushyaLtCtmCompat$reportedFailure = true;
                    CtmLog.LOGGER.error("Experimental CTM bridge failed; falling back to ordinary LittleTiles quads", error);
                }
            }
        }
        return model.getQuads(state, side, random, modelData, renderType);
    }

    private static BlockAndTintGetter yuushyaLtCtmCompat$blockView(LevelAccessor level) {
        if (level instanceof BlockAndTintGetter blockView)
            return blockView;
        if (level instanceof LevelAccessorFake && level instanceof LevelAccessorFakeAccessor accessor
                && accessor.yuushyaLtCtmCompat$getParentLevel() instanceof BlockAndTintGetter parentView) {
            return new MaterialBlockView(parentView, accessor.yuushyaLtCtmCompat$getMaterialPos(), accessor.yuushyaLtCtmCompat$getMaterialState());
        }
        return null;
    }
}
