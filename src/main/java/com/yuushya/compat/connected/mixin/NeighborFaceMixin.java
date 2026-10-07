package com.yuushya.compat.connected.mixin;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import com.mojang.logging.LogUtils;
import com.yuushya.compat.connected.BoundaryCoverage;
import com.yuushya.compat.connected.MaterialDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.math.box.LittleBox;

/** Verify LT's request to hide an ordinary neighbour, not LT's own render faces. */
@Mixin(value = BlockTile.class, remap = false)
public abstract class NeighborFaceMixin {
    @Unique private static final AtomicInteger yuushyaConnected$neighborReports = new AtomicInteger();

    @Inject(method = "hidesNeighborFace", at = @At("RETURN"), cancellable = true, require = 1)
    private void yuushyaConnected$verifyBoundary(BlockGetter view, BlockPos pos, BlockState state,
            BlockState neighborState, Direction direction, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) return;
        boolean full = false;
        try {
          var entity = view.getBlockEntity(pos);
          if (entity instanceof BETiles tiles && tiles.hasLoaded()) {
            Facing face = Facing.get(direction);
            var rectangles = new ArrayList<BoundaryCoverage.Rect>();
            for (var pair : tiles.allTiles()) {
                if (!pair.value.doesProvideSolidFace()) continue;
                double count = pair.key.getGrid().count;
                for (var box : pair.value) {
                    // Unknown/deformed geometry cannot prove full coverage from its bounding box.
                    if (box.getClass() != LittleBox.class || !box.isFaceAtEdge(pair.key.getGrid(), face)) continue;
                    if (box.getMin(face.one()) <= 0 && box.getMin(face.two()) <= 0
                            && box.getMax(face.one()) >= count && box.getMax(face.two()) >= count) return;
                    rectangles.add(new BoundaryCoverage.Rect(box.getMin(face.one()) / count, box.getMin(face.two()) / count,
                        box.getMax(face.one()) / count, box.getMax(face.two()) / count));
                }
            }
            full = BoundaryCoverage.full(rectangles);
          }
        } catch (java.util.ConcurrentModificationException changedDuringRead) {
            // Do not acquire neighbouring BE locks from render workers; keep the face instead.
            full = false;
        }
        if (!full) {
            callback.setReturnValue(false);
            if (MaterialDiagnostics.ENABLED && yuushyaConnected$neighborReports.getAndIncrement() < 64)
                LogUtils.getLogger().info("LT neighbor face restored: container={} side={} ordinary={}", pos, direction, neighborState);
        }
    }
}
