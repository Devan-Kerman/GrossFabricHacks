package net.devtech.grossfabrichacks;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.field.FieldSynthesizer;
import net.devtech.grossfabrichacks.loader.LoaderUnsafifier;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class GrossFabricHacks implements IMixinConfigPlugin {
	private static final Logger LOGGER = Logger.getLogger("Fabric-Transformer");

	static {
		LOGGER.severe("no good? no, this man is definitely up to evil.");
		EntrypointUtils.invoke("gfh:prePreLaunch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
        LoaderUnsafifier.init();
        FieldSynthesizer.init();
	}

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {return null;}

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {return null;}

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
