package com.yuushya.compat.connected;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = YuushyaLittleTilesConnectedTexturesCompat.MOD_ID, dist = Dist.CLIENT)
public final class YuushyaLittleTilesConnectedTexturesCompat {
    public static final String MOD_ID = "yuushya_lt_connected_textures_compat";

    public YuushyaLittleTilesConnectedTexturesCompat() {
        if (AutoBackendMixinPlugin.selectedMode() != BackendMode.NONE)
            NeoForge.EVENT_BUS.addListener(BoundaryRefresh::tick);
        LogUtils.getLogger().info("Loaded Yuushya/LittleTiles connected-texture compatibility; selected backend: {}",
            AutoBackendMixinPlugin.selectedMode());
    }
}
