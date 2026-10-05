package com.yuushya.compat.connected;

public enum BackendMode {
    FUSION,
    NEO_CONTINUITY,
    NONE;

    public static BackendMode select(boolean fusionLoaded, boolean continuityLoaded) {
        if (fusionLoaded)
            return FUSION;
        if (continuityLoaded)
            return NEO_CONTINUITY;
        return NONE;
    }
}
