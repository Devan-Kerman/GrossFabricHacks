package net.fabricmc.loader.launch.knot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.game.GameProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnsafeKnotClassLoader extends KnotClassLoader {
    public static final ConcurrentHashMap<String, Class<?>> DEFINED_CLASSES = new ConcurrentHashMap<>();
    public static final Class<KnotClassLoader> SUPERCLASS = KnotClassLoader.class;

    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/UnsafeKnotClassLoader");

    private static final KnotClassDelegate DELEGATE;

    public UnsafeKnotClassLoader(final boolean isDevelopment, final EnvType envType, final GameProvider provider) {
        super(isDevelopment, envType, provider);
    }

    public Class<?> defineClass(final String name, final byte[] bytes) {
        final Class<?> klass = UnsafeUtil.defineClass(name, bytes, null, null);

        DEFINED_CLASSES.put(name, klass);

        return klass;
    }

    public Class<?> defineClass(final byte[] bytes, final String name) {
        final Class<?> klass = UnsafeUtil.defineClass(name, bytes, null, null);

        DEFINED_CLASSES.put(name, klass);

        return klass;
    }

    public Class<?> getLoadedClass(final String name) {
        final Class<?> klass = super.findLoadedClass(name);

        if (klass == null) {
            return DEFINED_CLASSES.get(name);
        }

        return klass;
    }

    @Override
    public boolean isClassLoaded(final String name) {
        synchronized (super.getClassLoadingLock(name)) {
            return super.findLoadedClass(name) != null || DEFINED_CLASSES.get(name) != null;
        }
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        synchronized (super.getClassLoadingLock(name)) {
            Class<?> klass = super.findLoadedClass(name);

            if (klass == null) {
                klass = DEFINED_CLASSES.get(name);

                if (klass == null) {
                    try {
                        return super.loadClass(name, resolve);
                    } catch (final ClassFormatError error) {
                        LOGGER.info("Class {} has an illegal format; unsafely defining it.", name);

                        DEFINED_CLASSES.put(name, klass = UnsafeUtil.defineClass(name, DELEGATE.getPostMixinClassByteArray(name)));
                    }
                }
            }

            if (resolve) {
                super.resolveClass(klass);
            }

            return klass;
        }
    }

    static {
        try {
            final Class<UnsafeKnotClassLoader> thisClass = UnsafeKnotClassLoader.class;
            final ClassLoader loader = thisClass.getClassLoader();
            final ClassLoader knotClassLoader = Thread.currentThread().getContextClassLoader();
            final Class<? extends ClassLoader> knotClassLoaderClass = knotClassLoader.getClass();
            final String loaderUnsafifierName = "net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier";
            final Method forName = Class.forName(loaderUnsafifierName, false, knotClassLoader).getMethod("findAndDefineClass", String.class, ClassLoader.class);

            DEFINED_CLASSES.put(knotClassLoaderClass.getName(), knotClassLoaderClass);
            DEFINED_CLASSES.put(thisClass.getName(), thisClass);

            final String[] names = {
                    "net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer",
                    "net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer",
                    "org.spongepowered.asm.mixin.transformer.HackedMixinTransformer"
            };
            final int classCount = names.length;

            for (int i = 0; i < classCount; i++) {
                DEFINED_CLASSES.put(names[i], (Class<?>) forName.invoke(null, names[i], loader));
            }

            final Field delegate = knotClassLoaderClass.getDeclaredField("delegate");

            delegate.setAccessible(true);

            DELEGATE = (KnotClassDelegate) delegate.get(knotClassLoader);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
