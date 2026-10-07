package com.yuushya.compat.connected.mixin;

import com.yuushya.compat.connected.BoundaryRefresh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(value = BETiles.class, remap = false)
public abstract class BoundaryRefreshMixin {
    @Inject(method = "updateTiles(ZZ)V", at = @At("RETURN"), require = 1)
    private void yuushyaConnected$refreshFullBlocks(boolean neighbors, boolean faces, CallbackInfo callback) {
        BoundaryRefresh.request((BETiles) (Object) this);
    }

    // Chunk updates bypass updateTiles; NBT can also arrive before setLevel.
    @Inject(method = {"loadAdditional", "setLevel"}, at = @At("RETURN"), require = 2)
    private void yuushyaConnected$refreshLoadedBoundary(CallbackInfo callback) {
        BoundaryRefresh.request((BETiles) (Object) this);
    }
}
