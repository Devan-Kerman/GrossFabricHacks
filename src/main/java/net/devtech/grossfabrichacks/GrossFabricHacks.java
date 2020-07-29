package net.devtech.grossfabrichacks;

import java.util.List;
import java.util.Set;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class GrossFabricHacks implements IMixinConfigPlugin {
	private static final Logger LOGGER = getLogger("main");

	public static final String MOD_ID = "GrossFabricHacks";

	public static Logger getLogger(final String module) {
	    return LogManager.getLogger(String.format("%s/%s", MOD_ID, module));
    }

    /**
     * initialize here instead of static initializer in order to avoid loading twice after unsafifying the class loader
     */
    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.error("no good? no, this man is definitely up to evil.");
        EntrypointUtils.invoke("gfh:prePreLaunch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
    }

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
    public List<String> getMixins() {return null;}

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
