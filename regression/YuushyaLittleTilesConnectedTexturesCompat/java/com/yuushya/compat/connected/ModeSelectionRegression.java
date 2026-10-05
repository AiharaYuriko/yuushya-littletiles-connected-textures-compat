package com.yuushya.compat.connected;

public final class ModeSelectionRegression {
    public static void main(String[] args) {
        check(BackendMode.select(false, false) == BackendMode.NONE, "no backend");
        check(BackendMode.select(false, true) == BackendMode.NEO_CONTINUITY, "continuity fallback");
        check(BackendMode.select(true, false) == BackendMode.FUSION, "fusion only");
        check(BackendMode.select(true, true) == BackendMode.FUSION, "fusion priority");
        String fusionMixin = "com.yuushya.compat.fusion.mixin.RenderBoxMixin";
        String continuityMixin = "com.yuushya.compat.ctm.mixin.RenderBoxMixin";
        check(AutoBackendMixinPlugin.applies(BackendMode.FUSION, fusionMixin), "fusion mode enables fusion mixins");
        check(!AutoBackendMixinPlugin.applies(BackendMode.FUSION, continuityMixin), "fusion mode rejects continuity mixins");
        check(AutoBackendMixinPlugin.applies(BackendMode.NEO_CONTINUITY, continuityMixin), "continuity mode enables continuity mixins");
        check(!AutoBackendMixinPlugin.applies(BackendMode.NEO_CONTINUITY, fusionMixin), "continuity mode rejects fusion mixins");
        check(!AutoBackendMixinPlugin.applies(BackendMode.NONE, fusionMixin)
            && !AutoBackendMixinPlugin.applies(BackendMode.NONE, continuityMixin), "none mode rejects both backends");
        check(!AutoBackendMixinPlugin.applies(BackendMode.FUSION, "example.UnrelatedMixin"), "unknown mixin is rejected");
        for (String name : new String[]{"BlockTileMixin", "LevelAccessorFakeAccessor"}) {
            String mixin = "com.yuushya.compat.fusion.mixin." + name;
            check(AutoBackendMixinPlugin.applies(BackendMode.FUSION, mixin), "fusion enables neighbor fix: " + name);
            check(!AutoBackendMixinPlugin.applies(BackendMode.NEO_CONTINUITY, mixin)
                && !AutoBackendMixinPlugin.applies(BackendMode.NONE, mixin), "other modes exclude neighbor fix: " + name);
        }
        System.out.println("Unified backend selection regression passed: 14 assertions; Fusion wins and backends remain mutually exclusive.");
    }

    private static void check(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
