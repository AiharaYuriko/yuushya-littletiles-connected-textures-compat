package com.yuushya.compat.connected.mixin;

import java.util.concurrent.atomic.AtomicInteger;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.math.face.LittleServerFace;

/** Serialized face flags outlive the GPU/quad caches cleared by a terrain reload. */
@Mixin(value = BERenderManager.class, remap = false)
public abstract class OcclusionRefreshMixin {
    @Shadow private BETiles be;
    @Shadow private Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>> boxCache;
    @Unique private static final AtomicInteger yuushyaConnected$occlusionReports = new AtomicInteger();

    @Inject(method = "getRenderingBoxes", at = @At("HEAD"), require = 1)
    private void yuushyaConnected$verifyHiddenFaces(RenderingBlockContext context,
            CallbackInfoReturnable<Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>>> callback) {
        if (boxCache != null) return;
        // Called under LT's existing synchronized(be) rebuild, never on the packet thread.
        for (var parent : be.groups()) for (var tile : parent) {
            // LT's neighbour memoization is keyed only by direction, so don't share across materials.
            LittleServerFace probe = new LittleServerFace(be);
            for (var box : tile) {
                if (!box.hasFaceState()) continue; // LT will calculate a new box normally.
                for (Facing facing : Facing.VALUES) {
                    var old = box.getFaceState(facing);
                    if (!old.coveredFully()) continue;
                    // Missing neighbour chunks are not evidence that an old face is exposed.
                    if (box.isFaceAtEdge(parent.getGrid(), facing)
                            && !context.getLevel().hasChunkAt(be.getBlockPos().relative(facing.toVanilla()))) continue;
                    var current = probe.set(parent, tile, box, facing).calculate();
                    if (current == old) continue;
                    box.setFaceState(facing, current);
                    if (yuushyaConnected$occlusionReports.getAndIncrement() < 64)
                        LogUtils.getLogger().info("LT occlusion revalidated at {} material={} box={} face={} {} -> {}",
                            be.getBlockPos(), tile.getState(), box, facing, old, current);
                }
            }
        }
    }
}
