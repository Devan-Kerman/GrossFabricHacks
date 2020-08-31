package net.devtech.grossfabrichacks.mixin;

import java.util.List;
import java.util.Set;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
        GrossFabricHacks.mixinLoaded = true;

        final EntrypointContainer<PrePreLaunch>[] entrypoints = FabricLoader.getInstance().getEntrypointContainers("gfh:prePreLaunch", PrePreLaunch.class).toArray(new EntrypointContainer[0]);
        final int entrypointCount = entrypoints.length;

        for (int i = 0; i < entrypointCount; i++) {
            entrypoints[i].getEntrypoint().onPrePreLaunch();
        }

        if (TransformerApi.shouldWrite) {
            TransformerApi.manualLoad();
        }
    }
}
