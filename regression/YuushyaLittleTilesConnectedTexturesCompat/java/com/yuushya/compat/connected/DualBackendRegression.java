package com.yuushya.compat.connected;

public final class DualBackendRegression {
    private static int checks;
    public static void main(String[] args) {
        check(BackendMode.select(false, false) == BackendMode.NONE, "no backend");
        check(BackendMode.select(false, true) == BackendMode.NEO_CONTINUITY, "continuity only");
        check(BackendMode.select(true, false) == BackendMode.FUSION, "fusion only");
        check(BackendMode.select(true, true) == BackendMode.BOTH, "both installed enables both");
        String fusion = "com.yuushya.compat.fusion.mixin.";
        String ctm = "com.yuushya.compat.ctm.mixin.";
        String combined = "com.yuushya.compat.connected.mixin.CombinedRenderBoxMixin";
        for (BackendMode mode : BackendMode.values()) {
            for (boolean nativeLookup : new boolean[]{false, true}) {
                for (String view : new String[]{"VanillaRenderViewMixin", "BoundaryRefreshMixin"})
                    check(AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin." + view, nativeLookup) == (mode != BackendMode.NONE), "full-block boundary policy: " + view);
                check(!AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.SodiumRenderViewMixin", nativeLookup, false), "Sodium hooks absent without Sodium");
                check(AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.SodiumRenderViewMixin", nativeLookup, true) == (mode != BackendMode.NONE), "Sodium boundary policy");
                check(AutoBackendMixinPlugin.applies(mode, "com.yuushya.compat.connected.mixin.MaterialStateMixin", nativeLookup) == (mode != BackendMode.NONE), "state fix applies to either installed backend");
                int redirects = 0;
                for (String name : new String[]{fusion + "RenderBoxMixin", ctm + "RenderBoxMixin", combined})
                    if (AutoBackendMixinPlugin.applies(mode, name, nativeLookup)) redirects++;
                check(redirects == (mode == BackendMode.NONE ? 0 : 1), "exactly one RenderBox redirect owner: " + mode);
                check(AutoBackendMixinPlugin.applies(mode, combined, nativeLookup) == (mode == BackendMode.BOTH), "combined only with both dependencies");
                check(AutoBackendMixinPlugin.applies(mode, fusion + "RenderBoxMixin", nativeLookup) == (mode == BackendMode.FUSION), "standalone Fusion redirect");
                check(AutoBackendMixinPlugin.applies(mode, ctm + "RenderBoxMixin", nativeLookup) == (mode == BackendMode.NEO_CONTINUITY), "standalone CTM redirect");
                for (String name : new String[]{"BlockTileMixin", "LevelAccessorFakeAccessor"})
                    check(AutoBackendMixinPlugin.applies(mode, fusion + name, nativeLookup) == (mode.hasFusion() && !nativeLookup), "legacy lookup policy: " + name);
                for (String name : new String[]{"RenderingThreadMixin", "BERenderManagerMixin"})
                    check(AutoBackendMixinPlugin.applies(mode, fusion + name, nativeLookup) == mode.hasFusion(), "Fusion processing: " + name);
                for (String name : new String[]{"LevelAccessorFakeAccessor", "BakedModelMixin", "EmissiveBakedModelMixin", "ProcessingContextMixin"})
                    check(AutoBackendMixinPlugin.applies(mode, ctm + name, nativeLookup) == mode.hasContinuity(), "CTM support: " + name);
                check(!AutoBackendMixinPlugin.applies(mode, "example.UnrelatedMixin", nativeLookup), "unknown mixin");
            }
        }
        System.out.println("Dual backend regression passed: " + checks + " assertions; independent backends with one redirect owner.");
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
}
