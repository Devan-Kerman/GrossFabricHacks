package org.spongepowered.asm.mixin.transformer;

import net.devtech.grossfabrichacks.GFHState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class HackedMixinTransformer extends MixinTransformer {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/HackedMixinTransformer");

    private static final MixinProcessor PROCESSOR;

    @Override
    public byte[] transformClass(final MixinEnvironment environment, final String name, byte[] classBytes) {
        // raw class patching
        if (GFHState.transformPreMixinRawClass) {
            classBytes = GFHState.preMixinRawClassTransformer.transform(name, classBytes);
        }

        // ASM patching
        return this.transform(environment, this.readClass(classBytes), classBytes);
    }

    public byte[] transform(MixinEnvironment environment, ClassNode classNode, byte[] original) {
        final String name = classNode.name;

        // return immediately to reduce jumps and assignments
        if (GFHState.AppClassLoaded.shouldWrite) {
            if (GFHState.transformPreMixinAsmClass) {
                GFHState.preMixinAsmClassTransformer.transform(name, classNode);
            }

            PROCESSOR.applyMixins(environment, name.replace('/', '.'), classNode);

            if (GFHState.transformPostMixinAsmClass) {
                GFHState.postMixinAsmClassTransformer.transform(name, classNode);
            }

            // post mixin raw patching
            if (GFHState.transformPostMixinRawClass) {
                return GFHState.postMixinRawClassTransformer.transform(name, this.writeClass(classNode));
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
            final Object mixinTransformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
            final Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");

            LOGGER.info("MixinTransformer found! " + mixinTransformer);
            PROCESSOR = (MixinProcessor) mixinTransformerClass.getDeclaredField("processor").get(mixinTransformer);

            // here, we modify the klass pointer in the object to point towards the HackedMixinTransformer class, effectively turning the existing
            // MixinTransformer instance into an instance of HackedMixinTransformer
            final Class<?> unsafeUtil = Class.forName("net.devtech.grossfabrichacks.unsafe.UnsafeUtil", false, Thread.currentThread().getContextClassLoader());
            unsafeUtil.getMethod("unsafeCast", Object.class, long.class).invoke(null, mixinTransformer, unsafeUtil.getMethod("getKlassFromClass", Class.class).invoke(null, Class.forName("org.spongepowered.asm.mixin.transformer.HackedMixinTransformer", true, mixinTransformerClass.getClassLoader())));

            LOGGER.info("Unsafe cast mixin transformer success!");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
