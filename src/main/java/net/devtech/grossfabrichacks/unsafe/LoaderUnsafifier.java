package net.devtech.grossfabrichacks.unsafe;

import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;

public class LoaderUnsafifier implements Opcodes {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/LoaderUnsafifier");

    public static <T extends ClassLoader> T unsafifyLoader(final ClassLoader victim) {
        final String superName = victim.getClass().getName();

        LOGGER.warn("{}, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('.') + 1).replace('$', '.'));

        return UnsafeUtil.unsafeCast(victim, UnsafeUtil.getKlassFromClass(findAndDefineClass("net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader", victim.getClass().getClassLoader())));
    }

    public static <T> Class<T> findAndDefineClass(final String binaryName, final ClassLoader loader) {
        try {
            final InputStream stream = LoaderUnsafifier.class.getClassLoader().getResourceAsStream(binaryName.replace('.', '/') + ".class");
            final byte[] bytecode = new byte[stream.available()];

            while (stream.read(bytecode) != -1);

            return UnsafeUtil.defineClass(binaryName, bytecode, loader, LoaderUnsafifier.class.getProtectionDomain());
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
            System.exit(768);

            throw new RuntimeException(throwable);
        }
    }
}
