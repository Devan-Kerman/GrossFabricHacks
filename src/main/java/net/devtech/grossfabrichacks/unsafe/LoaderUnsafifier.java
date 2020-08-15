package net.devtech.grossfabrichacks.unsafe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;

public class LoaderUnsafifier implements Opcodes {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/LoaderUnsafifier");

    public static <T extends ClassLoader> T unsafifyLoader(final ClassLoader victim) {
        final String superName = victim.getClass().getName();

        LOGGER.warn("{}, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('.') + 1).replace('$', '.'));

        return UnsafeUtil.unsafeCast(victim, UnsafeUtil.getKlassFromClass(getUnsafeLoaderClass(victim)));
    }

    public static <T extends ClassLoader, U extends T> Class<U> getUnsafeLoaderClass(final T victim) {
        return getUnsafeLoaderClass(victim.getClass());
    }

    public static <T extends ClassLoader, U extends T> Class<U> getUnsafeLoaderClass(final Class<? extends T> victim) {
        try {
            return (Class<U>) Class.forName(victim.getPackage().getName() + ".Unsafe" + victim.getSimpleName(), true, victim.getClassLoader());
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }
}
