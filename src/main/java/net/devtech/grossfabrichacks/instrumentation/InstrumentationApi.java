package net.devtech.grossfabrichacks.instrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.devtech.grossfabrichacks.mixin.GrossFabricHacksPlugin;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class InstrumentationApi {
    private static final Set<String> TRANSFORMABLE = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger("InstrumentationApi");

    public static final Instrumentation INSTRUMENTATION;

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
            ClassFileTransformer fileTransformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
                if (cls.equals(classBeingRedefined)) {
                    return transformer.transform(className, classfileBuffer);
                }

                return classfileBuffer;
            };

            INSTRUMENTATION.addTransformer(fileTransformer, true);
            INSTRUMENTATION.retransformClasses(cls);
            INSTRUMENTATION.removeTransformer(fileTransformer);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get an instance of Instrumentation, capable of redefinition and redefining of classes.
     */
    public static Instrumentation getInstrumentation() {
        try {
            return (Instrumentation) Class.forName("gross.agent.InstrumentationAgent")
                    .getDeclaredField("instrumentation")
                    .get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void unpack(File file) throws IOException {
        try (InputStream stream = GrossFabricHacksPlugin.class.getResourceAsStream("/jars/gross_agent.jar")) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                byte[] arr = new byte[2048];
                int len;
                while ((len = stream.read(arr)) != -1) {
                    out.write(arr, 0, len);
                }
            }
        }
    }

    // to seperate out the static block
    private static class Transformable {
        private static boolean init;
        private static final ClassFileTransformer TRANSFORMER = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
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
            INSTRUMENTATION.removeTransformer(TRANSFORMER);
            init = false;
        }

        private static void init() {
            if (!init) {
                INSTRUMENTATION.addTransformer(TRANSFORMER);
                init = true;
            }
        }

        static {
            // pipe transformer to
            TransformerApi.manualLoad();
        }
    }

    static {
        File grossHackFolder = new File("gross_hacks");
        //noinspection ResultOfMethodCallIgnored
        grossHackFolder.mkdirs();
        try {
            File grossJar = new File(grossHackFolder, "gross_agent.jar");
            if (!grossJar.exists()) {
                LOGGER.info("no gross_agent.jar found, cloning new one");
                unpack(grossJar);
            } else {
                LOGGER.info("gross_agent.jar located!");
            }

            String name = ManagementFactory.getRuntimeMXBean()
                    .getName();
            LOGGER.info("VM name: " + name);
            String pid = name.substring(0, name.indexOf('@'));
            LOGGER.info("VM PID: " + pid);
            LOGGER.info("Attaching to VM");
            ByteBuddyAgent.attach(grossJar, pid);
        } catch (Throwable e) {
            LOGGER.severe("error in attaching agent to JVM");
            throw new RuntimeException(e);
        }

        INSTRUMENTATION = getInstrumentation();
    }
}
