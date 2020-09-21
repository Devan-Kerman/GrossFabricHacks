package net.devtech.grossfabrichacks.instrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class InstrumentationApi {
    private static final Set<String> TRANSFORMABLE = new HashSet<>();
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/InstrumentationApi");

    public static Instrumentation instrumentation;

    /**
     * adds a transformer that pipes a class through TransformerBootstrap,
     * this allows you to mix into any all classes (including JDK!) classes, with a few caveats.
     * <p>
     * If the class was loaded *before* you get to it, do not call this method!
     * don't pipe classes that may already be transformable by mixin, or they may be called twice.
     *
     * @param cls the internal name of the class
     */
    public static void pipeClassThroughTransformerBootstrap(String cls) {
        TRANSFORMABLE.add(cls);
        Transformable.init();
    }

    /**
     * a convenience method for {@link InstrumentationApi#retransform(Class, AsmClassTransformer)}
     * intended to be used when the target class is not visible
     *
     * @param cls         the binary name (defined in the last section of the {@linkplain ClassLoader ClassLoader javadoc}
     *                    of the class to retransform
     * @param transformer the class transformer
     */
    public static void retransform(final String cls, final AsmClassTransformer transformer) {
        try {
            retransform(Class.forName(cls), transformer);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void retransform(Class<?> cls, AsmClassTransformer transformer) {
        retransform(cls, transformer.asRaw());
    }

    /**
     * a convenience method for {@link InstrumentationApi#retransform(Class, RawClassTransformer)}
     * intended to be used when the target class is not visible
     *
     * @param cls         the binary name
     *                    (defined in the last section of the {@linkplain ClassLoader ClassLoader Javadoc})
     *                    of the class to retransform
     * @param transformer the class transformer
     */
    public static void retransform(final String cls, final RawClassTransformer transformer) {
        try {
            retransform(Class.forName(cls), transformer);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * retransform the class represented by {@code cls} by {@code transformer}.
     * The {@code className} passed to {@code transformer} may be null if {@code cls} is a JDK class.
     *
     * @param cls         the class to retransform.
     * @param transformer the class transformer.
     */
    public static void retransform(Class<?> cls, RawClassTransformer transformer) {
        try {
            CompatibilityClassFileTransformer fileTransformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
                if (cls == classBeingRedefined) {
                    return transformer.transform(className, classfileBuffer);
                }

                return classfileBuffer;
            };

            instrumentation.addTransformer(fileTransformer, true);
            instrumentation.retransformClasses(cls);
            instrumentation.removeTransformer(fileTransformer);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    // to seperate out the static block
    private static class Transformable {
        private static boolean init;
        private static final CompatibilityClassFileTransformer TRANSFORMER = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            if (TRANSFORMABLE.remove(node.name)) {
                if (TRANSFORMABLE.isEmpty()) {
                    deinit();
                }

                return TransformerApi.transformClass(node);
            }

            return classfileBuffer;
        };

        private static void deinit() {
            instrumentation.removeTransformer(TRANSFORMER);
            init = false;
        }

        private static void init() {
            if (!init) {
                instrumentation.addTransformer(TRANSFORMER);
                init = true;
            }
        }

        static {
            // pipe transformer to
            TransformerApi.manualLoad();
        }
    }

    static {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        final String PID = name.substring(0, name.indexOf('@'));

//        if (!InstrumentationApi.class.getClassLoader().getClass().getName().contains("Knot")) {
//            Thread.dumpStack();
//            System.exit(-1);
//        }

        try {
            final URI source = InstrumentationApi.class.getProtectionDomain().getCodeSource().getLocation().toURI();

            LOGGER.info("Attaching instrumentation agent to VM.");

            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                ByteBuddyAgent.attach(new File(source.resolve("jars/gross_agent.jar")), PID);
            } else {
                final File jar = FabricLoader.getInstance().getGameDir().resolve("mods/gross_agent.jar").toFile();

                IOUtils.write(IOUtils.toByteArray(source), new FileOutputStream(jar));
                ByteBuddyAgent.attach(jar, PID);

                jar.delete();
            }

            final Field field = Class.forName("net.devtech.grossfabrichacks.instrumentation.InstrumentationAgent", false, FabricLoader.class.getClassLoader()).getDeclaredField("instrumentation");

            field.setAccessible(true);

            instrumentation = (Instrumentation) field.get(null);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        LOGGER.info("Successfully attached instrumentation agent.");
    }
}
