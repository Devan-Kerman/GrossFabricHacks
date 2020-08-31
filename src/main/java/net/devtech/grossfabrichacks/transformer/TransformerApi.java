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
	public static boolean shouldWrite;
	// micro-optimization: cache transformer presence
	public static boolean transformPreMixinRawClass;
	public static boolean transformPreMixinAsmClass;
	public static boolean transformPostMixinRawClass;
	public static boolean transformPostMixinAsmClass;
	public static RawClassTransformer preMixinRawClassTransformer;
	public static RawClassTransformer postMixinRawClassTransformer;
	public static AsmClassTransformer preMixinAsmClassTransformer;
	public static AsmClassTransformer postMixinAsmClassTransformer;

	/**
	 * manually load the class, causing it to inject itself into the class loading pipe.
	 */
	public static void manualLoad() {
		try {
			Class.forName("org.spongepowered.asm.mixin.transformer.HackedMixinTransformer");
		} catch (final ClassNotFoundException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * listeners are called before mixins are applied, and gives you raw access to the class' bytecode, allowing you to fiddle with things ASM normally doens't let you.
	 */
	public static void registerPreMixinRawClassTransformer(RawClassTransformer transformer) {
		if (preMixinRawClassTransformer == null) {
			preMixinRawClassTransformer = transformer;
			transformPreMixinRawClass = true;
		} else {
			preMixinRawClassTransformer = preMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformers are called before mixin application with the class' classnode
	 */
	public static void registerPreMixinAsmClassTransformer(AsmClassTransformer transformer) {
		if (preMixinAsmClassTransformer == null) {
			preMixinAsmClassTransformer = transformer;
			transformPreMixinAsmClass = true;
			shouldWrite = true;
		} else {
			preMixinAsmClassTransformer = preMixinAsmClassTransformer.andThen(transformer);
		}
	}

	/**
	 * these are the last transformers to be called, and are fed the output of the classwritten classnode after mixin and postmixinasmtransformers.
	 */
	public static void registerPostMixinRawClassTransformer(RawClassTransformer transformer) {
		if (postMixinRawClassTransformer == null) {
			postMixinRawClassTransformer = transformer;
			transformPostMixinRawClass = true;
			shouldWrite = true;
		} else {
			postMixinRawClassTransformer = postMixinRawClassTransformer.andThen(transformer);
		}
	}

	/**
	 * transformer is called right after mixin application.
	 */
	public static void registerPostMixinAsmClassTransformer(AsmClassTransformer transformer) {
		if (postMixinAsmClassTransformer == null) {
			postMixinAsmClassTransformer = transformer;
			transformPostMixinAsmClass = true;
			shouldWrite = true;
		} else {
			postMixinAsmClassTransformer = postMixinAsmClassTransformer.andThen(transformer);
		}
	}

	public static byte[] transformClass(final ClassNode node) {
		MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();

		return ((HackedMixinTransformer) environment.getActiveTransformer()).transform(environment, node, null);
	}

	static {
		if (GrossFabricHacks.mixinLoaded) {
			manualLoad();
		}
	}
}
