package net.devtech.grossfabrichacks;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.org.apache.bcel.internal.generic.ILOAD;
import com.sun.org.apache.bcel.internal.generic.IRETURN;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;

public class GrossFabricHacks implements IMixinConfigPlugin {
	private static final Logger LOGGER = Logger.getLogger("Fabric-Transformer");
	static {
		LOGGER.severe("no good? no, this man is definitely up to evil.");
		EntrypointUtils.invoke("gfh:prePreLaunch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
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
