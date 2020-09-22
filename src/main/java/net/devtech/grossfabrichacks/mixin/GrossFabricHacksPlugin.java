package net.devtech.grossfabrichacks.mixin;

import java.util.List;
import java.util.Set;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import user11681.dynamicentry.DynamicEntry;

public class GrossFabricHacksPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    static {
        GrossFabricHacks.State.mixinLoaded = true;

        DynamicEntry.executeOptionalEntrypoint("gfh:prePreLaunch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);

        if (GrossFabricHacks.State.shouldWrite || GrossFabricHacks.State.manualLoad) {
            TransformerApi.manualLoad();
        }
    }
}
