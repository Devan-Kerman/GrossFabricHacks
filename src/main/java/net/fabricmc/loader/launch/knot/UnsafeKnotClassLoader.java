package net.fabricmc.loader.launch.knot;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.game.GameProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnsafeKnotClassLoader extends KnotClassLoader {
    public static final ConcurrentHashMap<String, Class<?>> DEFINED_CLASSES = new ConcurrentHashMap<>();

    private static final Logger LOGGER;

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

                        try {
                            final Field delegate = KnotClassLoader.class.getDeclaredField("delegate");

                            delegate.setAccessible(true);

                            DEFINED_CLASSES.put(name, klass = UnsafeUtil.defineClass(name, ((KnotClassDelegate) delegate.get(this)).getPostMixinClassByteArray(name)));
                        } catch (final NoSuchFieldException | IllegalAccessException exception) {
                            throw new RuntimeException(exception);
                        }
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
            final ClassLoader loader = UnsafeKnotClassLoader.class.getClassLoader();
            final String[] classes = {
                    "net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer",
                    "net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer",
                    "net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader",
                    "org.spongepowered.asm.mixin.transformer.HackedMixinTransformer"
            };
            final int classCount = classes.length;
            Class<?> klass;

            for (int i = 0; i < classCount; i++) {
                klass = Class.forName(classes[i], false, loader);

                DEFINED_CLASSES.put(klass.getName(), klass);
            }
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        LOGGER = LogManager.getLogger("GrossFabricHacks/UnsafeKnotClassLoader");
    }
}
