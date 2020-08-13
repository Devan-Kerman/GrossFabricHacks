package net.devtech.grossfabrichacks.transformer;

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
		//noinspection SillyAssignment,ConstantConditions
		HackedMixinTransformer.shouldWrite = HackedMixinTransformer.shouldWrite;
	}

	/**
	 * listeners are called before mixins are applied, and gives you raw access to the class' bytecode, allowing you to fiddle with things ASM normally doens't let you.
	 */
	public static void registerPreMixinRawClassTransformer(RawClassTransformer transformer) {
		if (HackedMixinTransformer.preMixinRawClassTransformer == null) {
			HackedMixinTransformer.preMixinRawClassTransformer = transformer;
		} else {
			HackedMixinTransformer.preMixinRawClassTransformer = HackedMixinTransformer.preMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformers are called before mixin application with the class' classnode
	 */
	public static void registerPreMixinAsmClassTransformer(AsmClassTransformer transformer) {
		HackedMixinTransformer.shouldWrite = true;

		if (HackedMixinTransformer.preMixinAsmClassTransformer == null) {
			HackedMixinTransformer.preMixinAsmClassTransformer = transformer;
		} else {
			HackedMixinTransformer.preMixinAsmClassTransformer = HackedMixinTransformer.preMixinAsmClassTransformer.andThen(transformer);
		}
	}

	/**
	 * these are the last transformers to be called, and are fed the output of the classwritten classnode after mixin and postmixinasmtransformers.
	 */
	public static void registerPostMixinRawClassTransformer(RawClassTransformer transformer) {
		HackedMixinTransformer.shouldWrite = true;
		if (HackedMixinTransformer.postMixinRawClassTransformer == null) {
			HackedMixinTransformer.postMixinRawClassTransformer = transformer;
		} else {
			HackedMixinTransformer.postMixinRawClassTransformer = HackedMixinTransformer.postMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformer is called right after mixin application.
	 */
	public static void registerPostMixinAsmClassTransformer(AsmClassTransformer transformer) {
		HackedMixinTransformer.shouldWrite = true;
		if (HackedMixinTransformer.postMixinAsmClassTransformer == null) {
			HackedMixinTransformer.postMixinAsmClassTransformer = transformer;
		} else {
			HackedMixinTransformer.postMixinAsmClassTransformer = HackedMixinTransformer.postMixinAsmClassTransformer.andThen(transformer);
		}
	}

	public static byte[] transformClass(final ClassNode node) {
		MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();

		return ((HackedMixinTransformer) environment.getActiveTransformer()).transform(environment, node, null);
	}
}
