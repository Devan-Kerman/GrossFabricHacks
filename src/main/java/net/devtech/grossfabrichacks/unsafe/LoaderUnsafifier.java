package net.devtech.grossfabrichacks.unsafe;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import net.devtech.grossfabrichacks.reflection.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class LoaderUnsafifier implements Opcodes {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/LoaderUnsafifier");

    private static final Method defineClass = ReflectionUtil.getDeclaredMethod(
            ClassLoader.class,
            "defineClass",
            String.class,
            byte[].class,
            int.class,
            int.class,
            ProtectionDomain.class
    );

    public static <T extends ClassLoader> T unsafifyLoader(final ClassLoader victim) {
        final String superName = victim.getClass().getName();

        LOGGER.warn("{}, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('.') + 1).replace('$', '.'));

        return UnsafeUtil.unsafeCast(victim, UnsafeUtil.getKlassFromClass(getUnsafeLoaderClass(victim)));
    }

    public static <T extends ClassLoader, U extends T> Class<U> getUnsafeLoaderClass(final T victim) {
        return getUnsafeLoaderClass(victim.getClass());
    }

    public static <T extends ClassLoader, U extends T> Class<U> getUnsafeLoaderClass(final Class<? extends T> victim) {
        return forName("net/fabricmc/loader/launch/knot/UnsafeKnotClassLoader", victim.getClassLoader());
    }

    public static <T> Class<T> forName(final String name, final ClassLoader loader) {
        try {
            final ClassReader reader = new ClassReader(LoaderUnsafifier.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class"));
            final ClassNode node = new ClassNode();
            final ClassWriter writer = new ClassWriter(0);
            reader.accept(node, 0);
            node.accept(writer);

            final byte[] bytecode = writer.toByteArray();

            return (Class<T>) defineClass.invoke(
                    loader,
                    reader.getClassName().replace('/', '.'),
                    bytecode,
                    0,
                    bytecode.length,
                    LoaderUnsafifier.class.getProtectionDomain()
            );
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
            System.exit(768);

            // don't return null
            throw new RuntimeException(throwable);
        }
    }
}
