package com.yuushya.compat.connected.mixin;

import com.yuushya.compat.connected.MaterialStateAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;

@Mixin(value = BERenderManager.class, remap = false)
public abstract class MaterialStateMixin {
    @Inject(method = "getRenderingBoxes", at = @At("RETURN"), require = 1)
    private void yuushyaConnected$resolveMaterialState(RenderingBlockContext context,
            CallbackInfoReturnable<Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>>> callback) {
        MaterialStateAdapter.apply(context, callback.getReturnValue());
    }
}
