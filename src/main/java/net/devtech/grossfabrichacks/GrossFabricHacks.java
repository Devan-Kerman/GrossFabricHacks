package net.devtech.grossfabrichacks;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import user11681.jpp.synthesis.Synthesizer;

public class GrossFabricHacks implements IMixinConfigPlugin {
    public static final String MOD_ID = "GrossFabricHacks";

    private static final Logger LOGGER = getLogger("main");

    public static Logger getLogger(final String module) {
        return LogManager.getLogger(String.format("%s/%s", MOD_ID, module));
    }

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
        LOGGER.error("no good? no, this man is definitely up to evil.");

        LoaderUnsafifier.init();

        try {
            Synthesizer.register((final Consumer<ClassNode> transformer) ->
                    TransformerApi.registerPostMixinAsmClassTransformer((final String name, final ClassNode klass) -> transformer.accept(klass))
            );
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
        }

        EntrypointUtils.invoke("gfh:prePreLaunch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
    }
}
