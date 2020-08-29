package net.devtech.grossfabrichacks.unsafe;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class LoaderUnsafifier implements Opcodes {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/LoaderUnsafifier");

    private static final Method defineClass;

    public static <T extends ClassLoader> T unsafifyLoader(final ClassLoader victim) {
        final String superName = victim.getClass().getName();

        LOGGER.warn("{}, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('.') + 1).replace('$', '.'));

        return UnsafeUtil.unsafeCast(victim, UnsafeUtil.getKlassFromClass(findAndDefineClass("net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader", victim.getClass().getClassLoader())));
    }

    public static <T> Class<T> findAndDefineClass(final String binaryName, final ClassLoader loader) {
        try {
            final ClassReader reader = new ClassReader(LoaderUnsafifier.class.getClassLoader().getResourceAsStream(binaryName.replace('.', '/') + ".class"));
            final ClassNode node = new ClassNode();
            final ClassWriter writer = new ClassWriter(0);
            reader.accept(node, 0);
            node.accept(writer);

            final byte[] bytecode = writer.toByteArray();

            return (Class<T>) defineClass.invoke(
                loader,
                binaryName,
                bytecode,
                0,
                bytecode.length,
                LoaderUnsafifier.class.getProtectionDomain()
            );
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
            System.exit(768);

            throw new RuntimeException(throwable);
        }
    }

    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
            defineClass.setAccessible(true);
        } catch (final NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
    }
}
