package com.yuushya.compat.connected;

public enum BackendMode {
    BOTH,
    FUSION,
    NEO_CONTINUITY,
    NONE;

    public static BackendMode select(boolean fusionLoaded, boolean continuityLoaded) {
        if (fusionLoaded && continuityLoaded)
            return BOTH;
        if (fusionLoaded)
            return FUSION;
        if (continuityLoaded)
            return NEO_CONTINUITY;
        return NONE;
    }

    public boolean hasFusion() {
        return this == FUSION || this == BOTH;
    }

    public boolean hasContinuity() {
        return this == NEO_CONTINUITY || this == BOTH;
    }
}
