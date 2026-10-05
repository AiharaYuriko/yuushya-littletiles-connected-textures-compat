package com.yuushya.compat.ctm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.LevelAccessorFake;

@Mixin(value = LevelAccessorFake.class, remap = false)
public interface LevelAccessorFakeAccessor {
    @Accessor("level")
    Level yuushyaLtCtmCompat$getParentLevel();

    @Accessor("pos")
    BlockPos yuushyaLtCtmCompat$getMaterialPos();

    @Accessor("state")
    BlockState yuushyaLtCtmCompat$getMaterialState();
}

