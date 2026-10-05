package com.yuushya.compat.fusion.mixin;

import com.yuushya.compat.fusion.TargetMaterials;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;

@Mixin(value = BERenderManager.class, remap = false)
public abstract class BERenderManagerMixin {
    @Shadow private boolean neighbourChanged;
    @Shadow private Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>> boxCache;

    @Inject(method = "beforeBuilding", at = @At("HEAD"), require = 1, allow = 1)
    private void yuushyaFusion$invalidateTextureQuads(RenderingBlockContext context, CallbackInfo ci) {
        // This is the existing worker rebuild under synchronized(be), not the notification
        // thread. Clear even inward faces: their Fusion state can depend on a neighbor too.
        if (!neighbourChanged || boxCache == null)
            return;
        for (ChunkLayerMapList<LittleRenderBox> layerMap : boxCache.values())
            for (LittleRenderBox box : layerMap)
                if (TargetMaterials.includes(box.state))
                    box.deleteQuadCache();
    }
}
