package com.yuushya.compat.fusion;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public final class TargetMaterials {
    private TargetMaterials() {}

    public static boolean includes(BlockState state) {
        return state != null && "yuushya".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }
}
