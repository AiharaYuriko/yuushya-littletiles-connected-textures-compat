package com.yuushya.compat.connected.mixin;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import team.creative.creativecore.common.level.LevelAccessorFake;

@Mixin(value = LevelAccessorFake.class, remap = false)
public interface CutFaceLevelAccessor {
    @Accessor("level") Level yuushyaConnected$cutFaceLevel();
}
