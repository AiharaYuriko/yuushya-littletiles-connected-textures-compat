package com.yuushya.compat.fusion.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import team.creative.creativecore.common.level.LevelAccessorFake;

@Mixin(value = LevelAccessorFake.class, remap = false)
public interface LevelAccessorFakeAccessor {
    @Accessor("level") Level yuushyaFusion$getParentLevel();
    @Accessor("pos") BlockPos yuushyaFusion$getMaterialPos();
    @Accessor("state") BlockState yuushyaFusion$getMaterialState();
}
