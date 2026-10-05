package com.yuushya.compat.fusion.mixin;

import com.yuushya.compat.fusion.TargetMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(value = BlockTile.class, remap = false)
public abstract class BlockTileMixin {
    @Redirect(method = "getAppearance", at = @At(value = "INVOKE", target =
            "Lteam/creative/littletiles/common/block/mc/BlockTile;loadBE(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lteam/creative/littletiles/common/block/entity/BETiles;"), require = 1, allow = 1)
    private BETiles yuushyaFusion$loadNeighbor(BlockGetter view, BlockPos pos) {
        if (Thread.currentThread() instanceof RenderingThread
                && view instanceof LevelAccessorFake
                && view instanceof LevelAccessorFakeAccessor accessor
                && TargetMaterials.includes(accessor.yuushyaFusion$getMaterialState())) {
            // Keep the fake view's hidden block entity at the material's own position.
            if (pos.equals(accessor.yuushyaFusion$getMaterialPos()))
                return null;
            Level parent = accessor.yuushyaFusion$getParentLevel();
            // pre227+ requires a real Level here. Keep its CHECK-only entity lookup
            // and hasLoaded filtering instead of calling Level#getBlockEntity ourselves.
            return parent == null ? null : BlockTile.loadBE(parent, pos);
        }
        return BlockTile.loadBE(view, pos);
    }
}
