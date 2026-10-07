package com.yuushya.compat.connected.mixin;

import java.util.List;
import com.yuushya.compat.connected.ConnectedMaterialState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.tile.LittleRenderBox;

/** Connected Yuushya models can omit end faces entirely. Restore only missing LT cuts. */
@Mixin(value = RenderBox.class, remap = false)
public abstract class CutFaceMixin {
    @Inject(method = "getBakedQuad(Lteam/creative/creativecore/client/render/box/QuadGeneratorContext;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;Lnet/neoforged/neoforge/client/model/data/ModelData;Lteam/creative/creativecore/common/util/math/base/Facing;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/util/RandomSource;ZI)Ljava/util/List;",
        at = @At("RETURN"), cancellable = true, require = 1)
    private void yuushyaConnected$restoreCutFace(QuadGeneratorContext holder, LevelAccessor level, BlockPos pos,
            BlockPos offset, BlockState state, BakedModel model, ModelData data, Facing facing, RenderType layer,
            RandomSource random, boolean overrideTint, int defaultColor, CallbackInfoReturnable<List<BakedQuad>> callback) {
        if (!((Object) this instanceof LittleRenderBox) || pos == null || !callback.getReturnValue().isEmpty()
                || !ConnectedMaterialState.supports(state)
                || facing.toVanilla().getAxis() != ConnectedMaterialState.positiveDirection(state).getAxis()) return;
        BlockState capped = ConnectedMaterialState.key(state);
        // The unconnected variant supplies end faces. This guard also stops reentry.
        if (capped == state) return;
        Level parent = level instanceof Level real ? real
            : level instanceof CutFaceLevelAccessor fake ? fake.yuushyaConnected$cutFaceLevel() : null;
        if (parent == null) return;
        BakedModel cappedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(capped);
        LevelAccessorFake cappedView = new LevelAccessorFake();
        cappedView.set(parent, pos, capped);
        ModelData cappedData = cappedModel.getModelData(cappedView, pos, capped, level.getModelData(pos));
        callback.setReturnValue(((RenderBox) (Object) this).getBakedQuad(holder, cappedView, pos, offset,
            capped, cappedModel, cappedData, facing, layer, RandomSource.create(state.getSeed(pos)), overrideTint, defaultColor));
    }
}
