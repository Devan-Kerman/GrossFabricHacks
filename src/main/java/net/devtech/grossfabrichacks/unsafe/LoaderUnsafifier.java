package net.devtech.grossfabrichacks.unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;

public class LoaderUnsafifier implements Opcodes {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/LoaderUnsafifier");

    public static final UnsafeKnotClassLoader UNSAFE_LOADER = unsafifyLoader(Thread.currentThread().getContextClassLoader());
    public static final MethodHandle addClass = getAddClass();

    /**
     * a convenient alternative to {@link Class#forName}
     */
    public static void init() {}

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

    public static void addClass(final ClassLoader loader, final Class<?> klass) {
        try {
            addClass.invokeExact(loader, klass);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static MethodHandle getAddClass() {
        try {
            final Method addClass = ClassLoader.class.getDeclaredMethod("addClass", Class.class);

            addClass.setAccessible(true);

            return MethodHandles.lookup().unreflect(addClass);
        } catch (final NoSuchMethodException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
