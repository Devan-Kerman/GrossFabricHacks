package net.fabricmc.loader.impl.launch.knot;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.devtech.grossfabrichacks.Rethrower;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.util.LoaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnsafeKnotClassLoader extends KnotCompatibilityClassLoader {
    public static final Object2ReferenceOpenHashMap<String, Class<?>> classes = new Object2ReferenceOpenHashMap<>();
    public static final Class<KnotClassLoader> superclass = KnotClassLoader.class;
    public static final ClassLoader applicationClassLoader;

    public static final KnotClassDelegate delegate;
    public static final ClassLoader parent;

    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/UnsafeKnotClassLoader");

    public UnsafeKnotClassLoader(final boolean isDevelopment, final EnvType envType, final GameProvider provider) {
        super(isDevelopment, envType, provider);
    }

    public Class<?> defineClass(final String name, final byte[] bytes) {
        final Class<?> klass = UnsafeUtil.defineClass(name, bytes, null, null);

        classes.put(name, klass);

        return klass;
    }

    public Class<?> getLoadedClass(final String name) {
        final Class<?> klass = super.findLoadedClass(name);

        if (klass == null) {
            return classes.get(name);
        }

        return klass;
    }

    @Override
    public boolean isClassLoaded(final String name) {
        synchronized (super.getClassLoadingLock(name)) {
            return super.findLoadedClass(name) != null || classes.get(name) != null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        synchronized (this.getClassLoadingLock(name)) {
            Class<?> klass = classes.get(name);

            if (klass == null) {
                klass = this.findLoadedClass(name);

                if (klass == null) {
//                    try {
                        if (!name.startsWith("com.google.gson.") && !name.startsWith("java.")) {
                            final byte[] input = delegate.getPostMixinClassByteArray(name, false);

                            if (input != null) {
                                final int pkgDelimiterPos = name.lastIndexOf('.');

                                if (pkgDelimiterPos > 0) {
                                    final String pkgString = name.substring(0, pkgDelimiterPos);

                                    if (this.getPackage(pkgString) == null) {
                                        this.definePackage(pkgString, null, null, null, null, null, null, null);
                                    }
                                }

                                klass = super.defineClass(name, input, 0, input.length, delegate.getMetadata(name, parent.getResource(LoaderUtil.getClassFileName(name))).codeSource);
                            } else {
                                klass = applicationClassLoader.loadClass(name);
                            }
                        } else {
                            klass = applicationClassLoader.loadClass(name);
                        }
//                    } catch (final ClassFormatError formatError) {
//                        LOGGER.warn("A ClassFormatError was encountered while attempting to define {}; resorting to unsafe definition.", name);
//
//                        klass = UnsafeUtil.defineClass(name, delegate.getPostMixinClassByteArray(name));
//                    }
                }

                classes.put(name, klass);
            }

            if (resolve) {
                this.resolveClass(klass);
            }

            return klass;
        }
    }

    static {
        try {
            final Class<UnsafeKnotClassLoader> thisClass = UnsafeKnotClassLoader.class;
            final ClassLoader knotClassLoader = Thread.currentThread().getContextClassLoader();
            applicationClassLoader = thisClass.getClassLoader();

            UnsafeUtil.unsafeCast(knotClassLoader, UnsafeUtil.getKlassFromClass(UnsafeKnotClassLoader.class));

            classes.put(superclass.getName(), superclass);
            classes.put(thisClass.getName(), thisClass);

            for (final String name : new String[]{
                "net.devtech.grossfabrichacks.GrossFabricHacks$State",
                "net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt",
                "net.devtech.grossfabrichacks.unsafe.UnsafeUtil"}) {
                classes.put(name, Class.forName(name, false, applicationClassLoader));
            }

            for (final String name : new String[]{
                "net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer",
                "net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer",
                "net.devtech.grossfabrichacks.transformer.TransformerApi",
                "org.spongepowered.asm.mixin.transformer.HackedMixinTransformer"}) {
                classes.put(name, UnsafeUtil.findAndDefineClass(name, applicationClassLoader));
            }

            delegate = ((KnotCompatibilityClassLoader) UnsafeUtil.unsafeCast(knotClassLoader, KnotCompatibilityClassLoader.class)).getDelegate();
            parent = knotClassLoader.getParent();
        } catch (final Throwable throwable) {
            throw Rethrower.rethrow(throwable);
        }
    }
}
