package com.yuushya.compat.connected;

import java.util.List;
import java.util.Set;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/** Selects once during Mixin configuration; render calls contain no backend-selection branch. */
public final class AutoBackendMixinPlugin implements IMixinConfigPlugin {
    private static final String FUSION_MIXINS = "com.yuushya.compat.fusion.mixin.";
    private static final String CONTINUITY_MIXINS = "com.yuushya.compat.ctm.mixin.";
    private static volatile BackendMode selected;

    public static BackendMode selectedMode() {
        BackendMode mode = selected;
        return mode == null ? BackendMode.NONE : mode;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return applies(selectedMode(), mixinClassName);
    }

    static boolean applies(BackendMode mode, String mixinClassName) {
        if (mixinClassName.startsWith(FUSION_MIXINS))
            return mode == BackendMode.FUSION;
        if (mixinClassName.startsWith(CONTINUITY_MIXINS))
            return mode == BackendMode.NEO_CONTINUITY;
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
            selected = BackendMode.select(fusion, continuity);
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
