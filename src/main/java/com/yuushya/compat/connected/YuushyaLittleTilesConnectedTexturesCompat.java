package com.yuushya.compat.connected;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = YuushyaLittleTilesConnectedTexturesCompat.MOD_ID, dist = Dist.CLIENT)
public final class YuushyaLittleTilesConnectedTexturesCompat {
    public static final String MOD_ID = "yuushya_lt_connected_textures_compat";

    public YuushyaLittleTilesConnectedTexturesCompat() {
        LogUtils.getLogger().info("Loaded Yuushya/LittleTiles connected-texture compatibility; selected backend: {}",
            AutoBackendMixinPlugin.selectedMode());
    }
}
