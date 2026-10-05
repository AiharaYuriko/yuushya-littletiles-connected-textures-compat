package com.yuushya.compat.fusion.mixin;

import com.yuushya.compat.fusion.TargetMaterials;
import com.yuushya.compat.fusion.UnculledQuadCache;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.littletiles.client.render.cache.build.RenderingThread;

@Mixin(value = RenderingThread.class, remap = false)
public abstract class RenderingThreadMixin {
    @Redirect(method = "run", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/resources/model/BakedModel;getModelData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/neoforged/neoforge/client/model/data/ModelData;"), require = 1, allow = 1)
    private ModelData yuushyaFusion$prepare(BakedModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData input) {
        ModelData data = model.getModelData(level, pos, state, input);
        if (!TargetMaterials.includes(state))
            return data;
        // Retain all Fusion and wrapper properties. No world reference or global cache.
        return data.derive().with(UnculledQuadCache.PROPERTY, new UnculledQuadCache(state.getSeed(pos))).build();
    }
}
