package net.devtech.grossfabrichacks.unsafe;

import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.fabricmc.loader.launch.knot.GFHUnsafeLoader;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;

public class LoaderUnsafifier implements Opcodes {
    private static final Logger LOGGER = GrossFabricHacks.getLogger("LoaderUnsafifier");
    public static final GFHUnsafeLoader UNSAFE_LOADER;

    /**
     * a convenient alternative to {@link Class#forName}
     */
    public static void init() {}

    public static <T extends ClassLoader> T unsafifyLoader(final ClassLoader victim) {
        return UnsafeUtil.unsafeCast(victim, UnsafeUtil.getKlassFromClass(getUnsafeLoaderClass(victim)));
    }

    public static <T extends ClassLoader, U extends T> Class<U> getUnsafeLoaderClass(final T victim) {
        return getUnsafeLoaderClass(victim.getClass());
    }

    public static <T extends ClassLoader, U extends T> Class<U> getUnsafeLoaderClass(final Class<? extends T> victim) {
        try {
            return (Class<U>) Class.forName(victim.getPackage().getName() + ".GFHUnsafeLoader", true, victim.getClassLoader());
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    static {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final String superName = loader.getClass().getName();

        LOGGER.warn("{}, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('.') + 1).replace('$', '.'));

        UNSAFE_LOADER = unsafifyLoader(loader);
    }
}
