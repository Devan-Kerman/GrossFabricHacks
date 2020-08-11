package org.spongepowered.asm.mixin.transformer;

import java.lang.reflect.Field;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class HackedMixinTransformer extends MixinTransformer {
    private static final Logger LOGGER = GrossFabricHacks.getLogger("HackedMixinTransformer");
    private static final MixinProcessor PROCESSOR;

    public static boolean shouldWrite;
    public static RawClassTransformer preMixinRawClassTransformer;
    public static RawClassTransformer postMixinRawClassTransformer;
    public static AsmClassTransformer preMixinAsmClassTransformer;
    public static AsmClassTransformer postMixinAsmClassTransformer;

    public static byte[] transformClass(final ClassNode node) {
        MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();

        return ((HackedMixinTransformer) environment.getActiveTransformer()).transform(environment, node, null);
    }

    // override the transformClass function and call our hook
    @Override
    public byte[] transformClass(final MixinEnvironment environment, final String name, byte[] classBytes) {
        // raw class patching
        if (preMixinRawClassTransformer != null) {
            classBytes = preMixinRawClassTransformer.transform(name, classBytes);
        }

        // ASM patching
        return this.transform(environment, this.readClass(classBytes), classBytes);
    }

    public byte[] transform(MixinEnvironment environment, ClassNode classNode, byte[] original) {
        final String name = classNode.name;

        if (preMixinAsmClassTransformer != null) {
            preMixinAsmClassTransformer.transform(name, classNode);
        }

        if (shouldWrite || PROCESSOR.applyMixins(environment, name.replace('/', '.'), classNode)) {
            if (postMixinAsmClassTransformer != null) {
                postMixinAsmClassTransformer.transform(name, classNode);
            }

            // post mixin raw patching
            if (postMixinRawClassTransformer != null) {
                return postMixinRawClassTransformer.transform(name, this.writeClass(classNode));
            }

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
            PROCESSOR = (MixinProcessor) processor.get(mixinTransformer);
            LOGGER.info("MixinTransformer found! " + mixinTransformer);

            // here, we modify the klass pointer in the object to point towards the HackedMixinTransformer class, effectively turning the existing
            // MixinTransformer instance into an instance of HackedMixinTransformer
            UnsafeUtil.unsafeCast(mixinTransformer, UnsafeUtil.getKlassFromClass(hacked));
            LOGGER.info("Unsafe cast mixin transformer success!");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
