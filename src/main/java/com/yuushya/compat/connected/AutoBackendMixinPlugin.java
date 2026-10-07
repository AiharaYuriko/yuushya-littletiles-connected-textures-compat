package com.yuushya.compat.connected;

import java.util.List;
import java.util.Set;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/** Enables installed backends once during Mixin configuration. */
public final class AutoBackendMixinPlugin implements IMixinConfigPlugin {
    private static final String FUSION_MIXINS = "com.yuushya.compat.fusion.mixin.";
    private static final String CONTINUITY_MIXINS = "com.yuushya.compat.ctm.mixin.";
    private static volatile BackendMode selected;
    private static volatile boolean nativeNeighborLookup;
    private static volatile boolean sodiumRenderView;

    public static BackendMode selectedMode() {
        BackendMode mode = selected;
        return mode == null ? BackendMode.NONE : mode;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return applies(selectedMode(), mixinClassName, nativeNeighborLookup, sodiumRenderView);
    }

    static boolean applies(BackendMode mode, String mixinClassName) {
        return applies(mode, mixinClassName, false);
    }

    static boolean applies(BackendMode mode, String mixinClassName, boolean nativeLookup) {
        return applies(mode, mixinClassName, nativeLookup, true);
    }

    static boolean applies(BackendMode mode, String mixinClassName, boolean nativeLookup, boolean sodiumPresent) {
        if (mixinClassName.equals("com.yuushya.compat.connected.mixin.SodiumRenderViewMixin") && !sodiumPresent)
            return false;
        if (mixinClassName.equals("com.yuushya.compat.connected.mixin.VanillaRenderViewMixin")
                || mixinClassName.equals("com.yuushya.compat.connected.mixin.SodiumRenderViewMixin")
                || mixinClassName.equals("com.yuushya.compat.connected.mixin.BoundaryRefreshMixin"))
            return mode != BackendMode.NONE;
        if (mixinClassName.equals("com.yuushya.compat.connected.mixin.MaterialStateMixin")
                || mixinClassName.equals("com.yuushya.compat.connected.mixin.CutFaceMixin")
                || mixinClassName.equals("com.yuushya.compat.connected.mixin.CutFaceLevelAccessor")
                || mixinClassName.equals("com.yuushya.compat.connected.mixin.NeighborFaceMixin")
                || mixinClassName.equals("com.yuushya.compat.connected.mixin.ColumnModelFaceMixin"))
            return mode != BackendMode.NONE;
        if (mixinClassName.equals("com.yuushya.compat.connected.mixin.CombinedRenderBoxMixin"))
            return mode == BackendMode.BOTH;
        if (mode == BackendMode.BOTH && (mixinClassName.equals(FUSION_MIXINS + "RenderBoxMixin")
                || mixinClassName.equals(CONTINUITY_MIXINS + "RenderBoxMixin")))
            return false;
        if (nativeLookup && (mixinClassName.equals(FUSION_MIXINS + "BlockTileMixin")
                || mixinClassName.equals(FUSION_MIXINS + "LevelAccessorFakeAccessor")))
            return false;
        if (mixinClassName.startsWith(FUSION_MIXINS))
            return mode.hasFusion();
        if (mixinClassName.startsWith(CONTINUITY_MIXINS))
            return mode.hasContinuity();
        return false;
    }

    private static boolean isClassAvailable(String className) {
        try {
            // Use Mixin's launch-aware bytecode provider. Class.forName can miss mod classes
            // at this phase because they are not yet visible to the ordinary class loader.
            MixinService.getService().getBytecodeProvider().getClassNode(className);
            return true;
        } catch (Exception | LinkageError ignored) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        if (selected != null)
            return;
        synchronized (AutoBackendMixinPlugin.class) {
            if (selected != null)
                return;
            boolean fusion = isClassAvailable("com.supermartijn642.fusion.Fusion");
            boolean continuity = isClassAvailable("me.pepperbell.continuity.impl.client.ProcessingContextImpl");
            BackendMode mode = BackendMode.select(fusion, continuity);
            sodiumRenderView = mode != BackendMode.NONE && isClassAvailable("net.caffeinemc.mods.sodium.client.world.LevelSlice");
            if (mode.hasFusion()) {
                try {
                    nativeNeighborLookup = NativeNeighborLookup.isFixed(MixinService.getService().getBytecodeProvider()
                        .getClassNode(NativeNeighborLookup.BLOCK_TILE.replace('/', '.')));
                } catch (Exception | LinkageError error) {
                    LogUtils.getLogger().warn("Cannot inspect LittleTiles native neighbor lookup; retaining the compatibility redirect", error);
                }
                LogUtils.getLogger().info("LittleTiles neighbor lookup: nativeFixed={}, compatibilityRedirect={}; quad refresh and unculled faces remain enabled",
                    nativeNeighborLookup, !nativeNeighborLookup);
            }
            selected = mode;
            LogUtils.getLogger().info("Yuushya/LittleTiles CTM backend selection: fusion={}, continuity={}, selected={}",
                fusion, continuity, selected);
        }
    }
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
