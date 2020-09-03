package net.fabricmc.loader.launch.knot;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.lang.reflect.Method;
import net.devtech.grossfabrichacks.reflection.ReflectionUtil;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.game.GameProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnsafeKnotClassLoader extends KnotClassLoader {
    public static final Object2ReferenceOpenHashMap<String, Class<?>> CLASSES = new Object2ReferenceOpenHashMap<>();
    public static final Class<KnotClassLoader> SUPERCLASS = KnotClassLoader.class;
    public static final String INTERNAL_NAME = "net/fabricmc/loader/launch/knot/UnsafeKnotClassLoader";

    protected static final KnotClassDelegate DELEGATE;
    protected static final boolean DEVELOPMENT;
    protected static final EnvType ENVIRONMENT;
    protected static final GameProvider PROVIDER;

    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/UnsafeKnotClassLoader");

    public UnsafeKnotClassLoader(final boolean isDevelopment, final EnvType envType, final GameProvider provider) {
        super(isDevelopment, envType, provider);
    }

    public Class<?> defineClass(final String name, final byte[] bytes) {
        final Class<?> klass = UnsafeUtil.defineClass(name, bytes, null, null);

        CLASSES.put(name, klass);

        return klass;
    }

    public Class<?> getLoadedClass(final String name) {
        final Class<?> klass = super.findLoadedClass(name);

        if (klass == null) {
            return CLASSES.get(name);
        }

        return klass;
    }

    @Override
    public boolean isClassLoaded(final String name) {
        synchronized (super.getClassLoadingLock(name)) {
            return super.findLoadedClass(name) != null || CLASSES.get(name) != null;
        }
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        synchronized (super.getClassLoadingLock(name)) {
            Class<?> klass = CLASSES.get(name);

            if (klass == null) {
                klass = super.findLoadedClass(name);

                if (klass == null) {
                    try {
                        CLASSES.put(name, klass = super.loadClass(name, resolve));

                        return klass;
                    } catch (final ClassFormatError formatError) {
                        klass = UnsafeUtil.defineClass(name, DELEGATE.getPostMixinClassByteArray(name));
                    }
                }

                CLASSES.put(name, klass);
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
            final ClassLoader knotClassLoader = Thread.currentThread().getContextClassLoader();
            final Class<? extends ClassLoader> knotClassLoaderClass = knotClassLoader.getClass();

            CLASSES.put(knotClassLoaderClass.getName(), knotClassLoaderClass);
            CLASSES.put(thisClass.getName(), thisClass);

            final ClassLoader applicationLoader = thisClass.getClassLoader();
            final String loaderUnsafifierName = "net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier";
            final Method forName = Class.forName(loaderUnsafifierName, false, knotClassLoader).getMethod("findAndDefineClass", String.class, ClassLoader.class);
            final String[] names = {
                "net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer",
                "net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer",
                "net.devtech.grossfabrichacks.transformer.TransformerApi",
                "org.spongepowered.asm.mixin.transformer.HackedMixinTransformer"
            };
            final int classCount = names.length;

            for (int i = 0; i < classCount; i++) {
                CLASSES.put(names[i], (Class<?>) forName.invoke(null, names[i], applicationLoader));
            }

            final FabricLoader fabricLoader = FabricLoader.getInstance();

            DELEGATE = ((KnotClassLoader) knotClassLoader).getDelegate();
            DEVELOPMENT = fabricLoader.isDevelopmentEnvironment();
            ENVIRONMENT = fabricLoader.getEnvironmentType();
            PROVIDER = ReflectionUtil.getDeclaredFieldValue(KnotClassDelegate.class, "provider", DELEGATE);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
