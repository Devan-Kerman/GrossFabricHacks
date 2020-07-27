package net.devtech.grossfabrichacks.transformer;

import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;

/**
 * The API class for getting access to transforming any and all classes loaded by the KnotClassLoader (or whatever classloader happens to calls mixin)
 */
public class TransformerApi {
	/**
	 * manually load the class, causing it to inject itself into the class loading pipe.
	 */
	public static void manualLoad() {
		//noinspection SillyAssignment,ConstantConditions
		TransformerBootstrap.shouldWrite = TransformerBootstrap.shouldWrite;
	}

	/**
	 * listeners are called before mixins are applied, and gives you raw access to the class' bytecode, allowing you to fiddle with things ASM normally doens't let you.
	 */
	public static void registerPreMixinRawClassTransformer(RawClassTransformer transformer) {
		if (TransformerBootstrap.preMixinRawClassTransformer == null) {
			TransformerBootstrap.preMixinRawClassTransformer = transformer;
		} else {
			TransformerBootstrap.preMixinRawClassTransformer = TransformerBootstrap.preMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformers are called before mixin application with the class' classnode
	 */
	public static void registerPreMixinAsmClassTransformer(AsmClassTransformer transformer) {
		TransformerBootstrap.shouldWrite = true;

		if (TransformerBootstrap.preMixinAsmClassTransformer == null) {
			TransformerBootstrap.preMixinAsmClassTransformer = transformer;
		} else {
			TransformerBootstrap.preMixinAsmClassTransformer = TransformerBootstrap.preMixinAsmClassTransformer.andThen(transformer);
		}
	}

	/**
	 * these are the last transformers to be called, and are fed the output of the classwritten classnode after mixin and postmixinasmtransformers.
	 */
	public static void registerPostMixinRawClassTransformer(RawClassTransformer transformer) {
		TransformerBootstrap.shouldWrite = true;
		if (TransformerBootstrap.postMixinRawClassTransformer == null) {
			TransformerBootstrap.postMixinRawClassTransformer = transformer;
		} else {
			TransformerBootstrap.postMixinRawClassTransformer = TransformerBootstrap.postMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformer is called right after mixin application.
	 */
	public static void registerPostMixinAsmClassTransformer(AsmClassTransformer transformer) {
		TransformerBootstrap.shouldWrite = true;
		if (TransformerBootstrap.postMixinAsmClassTransformer == null) {
			TransformerBootstrap.postMixinAsmClassTransformer = transformer;
		} else {
			TransformerBootstrap.postMixinAsmClassTransformer = TransformerBootstrap.postMixinAsmClassTransformer.andThen(transformer);
		}
	}
}
