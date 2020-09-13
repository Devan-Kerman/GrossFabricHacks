package net.devtech.grossfabrichacks.transformer;

import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.HackedMixinTransformer;

/**
 * The API class for getting access to transforming any and all classes loaded by the KnotClassLoader (or whatever classloader happens to calls mixin)
 */
public class TransformerApi {
	/**
	 * manually load the class, causing it to inject itself into the class loading pipe.
	 */
	public static void manualLoad() {
		if (GrossFabricHacks.State.mixinLoaded) {
			try {
				Class.forName("org.spongepowered.asm.mixin.transformer.HackedMixinTransformer");
			} catch (final ClassNotFoundException exception) {
				throw new RuntimeException(exception);
			}
		} else {
			GrossFabricHacks.State.manualLoad = true;
		}
	}

	/**
	 * listeners are called before mixins are applied, and gives you raw access to the class' bytecode, allowing you to fiddle with things ASM normally doens't let you.
	 */
	public static void registerPreMixinRawClassTransformer(RawClassTransformer transformer) {
		if (GrossFabricHacks.State.preMixinRawClassTransformer == null) {
			GrossFabricHacks.State.preMixinRawClassTransformer = transformer;
			GrossFabricHacks.State.transformPreMixinRawClass = true;
		} else {
			GrossFabricHacks.State.preMixinRawClassTransformer = GrossFabricHacks.State.preMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformers are called before mixin application with the class' classnode
	 */
	public static void registerPreMixinAsmClassTransformer(AsmClassTransformer transformer) {
		if (GrossFabricHacks.State.preMixinAsmClassTransformer == null) {
			GrossFabricHacks.State.preMixinAsmClassTransformer = transformer;
			GrossFabricHacks.State.transformPreMixinAsmClass = true;
			GrossFabricHacks.State.shouldWrite = true;
		} else {
			GrossFabricHacks.State.preMixinAsmClassTransformer = GrossFabricHacks.State.preMixinAsmClassTransformer.andThen(transformer);
		}
	}

	/**
	 * these are the last transformers to be called, and are fed the output of the classwritten classnode after mixin and postmixinasmtransformers.
	 */
	public static void registerPostMixinRawClassTransformer(RawClassTransformer transformer) {
		if (GrossFabricHacks.State.postMixinRawClassTransformer == null) {
			GrossFabricHacks.State.postMixinRawClassTransformer = transformer;
			GrossFabricHacks.State.transformPostMixinRawClass = true;
			GrossFabricHacks.State.shouldWrite = true;
		} else {
			GrossFabricHacks.State.postMixinRawClassTransformer = GrossFabricHacks.State.postMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformer is called right after mixin application.
	 */
	public static void registerPostMixinAsmClassTransformer(AsmClassTransformer transformer) {
		if (GrossFabricHacks.State.postMixinAsmClassTransformer == null) {
			GrossFabricHacks.State.postMixinAsmClassTransformer = transformer;
			GrossFabricHacks.State.transformPostMixinAsmClass = true;
			GrossFabricHacks.State.shouldWrite = true;
		} else {
			GrossFabricHacks.State.postMixinAsmClassTransformer = GrossFabricHacks.State.postMixinAsmClassTransformer.andThen(transformer);
		}
	}

	public static byte[] transformClass(final ClassNode node) {
		return HackedMixinTransformer.instance.transform(MixinEnvironment.getCurrentEnvironment(), node, null);
	}

	static {
		manualLoad();
	}
}
