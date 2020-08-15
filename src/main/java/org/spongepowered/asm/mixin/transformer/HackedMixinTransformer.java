package org.spongepowered.asm.mixin.transformer;

import java.lang.reflect.Field;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class HackedMixinTransformer extends MixinTransformer {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/HackedMixinTransformer");

    private static final MixinProcessor PROCESSOR;

    // micro-optimization: cache transformer presence
    public static boolean shouldWrite;
    public static boolean transformPreMixinRawClass;
    public static boolean transformPreMixinAsmClass;
    public static boolean transformPostMixinRawClass;
    public static boolean transformPostMixinAsmClass;
    public static RawClassTransformer preMixinRawClassTransformer;
    public static RawClassTransformer postMixinRawClassTransformer;
    public static AsmClassTransformer preMixinAsmClassTransformer;
    public static AsmClassTransformer postMixinAsmClassTransformer;

    @Override
    public byte[] transformClass(final MixinEnvironment environment, final String name, byte[] classBytes) {
        // raw class patching
        if (transformPreMixinRawClass) {
            classBytes = preMixinRawClassTransformer.transform(name, classBytes);
        }

        // ASM patching
        return this.transform(environment, this.readClass(classBytes), classBytes);
    }

    public byte[] transform(MixinEnvironment environment, ClassNode classNode, byte[] original) {
        final String name = classNode.name;

        // return immediately to reduce jumps
        if (shouldWrite) {
            if (transformPreMixinAsmClass) {
                preMixinAsmClassTransformer.transform(name, classNode);
            }

            PROCESSOR.applyMixins(environment, name.replace('/', '.'), classNode);

            if (transformPostMixinAsmClass) {
                postMixinAsmClassTransformer.transform(name, classNode);
            }

            // post mixin raw patching
            if (transformPostMixinRawClass) {
                return postMixinRawClassTransformer.transform(name, this.writeClass(classNode));
            }

            return this.writeClass(classNode);
        }

        if (PROCESSOR.applyMixins(environment, name.replace('/', '.'), classNode)) {
            return this.writeClass(classNode);
        }

        return original;
    }

    static {
        try {
            final Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            // define the class in the same class loader as MixinTransformer, so we can override its package-private method
            final Class<?> hacked = Class.forName("org.spongepowered.asm.mixin.transformer.HackedMixinTransformer", true, mixinTransformerClass.getClassLoader());
            final Field processor = mixinTransformerClass.getDeclaredField("processor");
            processor.setAccessible(true);

            final Object mixinTransformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();

            LOGGER.info("MixinTransformer found! " + mixinTransformer);
            PROCESSOR = (MixinProcessor) processor.get(mixinTransformer);

            // here, we modify the klass pointer in the object to point towards the HackedMixinTransformer class, effectively turning the existing
            // MixinTransformer instance into an instance of HackedMixinTransformer
            final Class<?> unsafeUtil = Class.forName("net.devtech.grossfabrichacks.unsafe.UnsafeUtil", false, Thread.currentThread().getContextClassLoader());
            unsafeUtil.getMethod("unsafeCast", Object.class, long.class).invoke(null, mixinTransformer, unsafeUtil.getMethod("getKlassFromClass", Class.class).invoke(null, hacked));

            LOGGER.info("Unsafe cast mixin transformer success!");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
