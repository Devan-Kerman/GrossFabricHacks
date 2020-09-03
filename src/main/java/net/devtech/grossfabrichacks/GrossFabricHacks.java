package net.devtech.grossfabrichacks;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.devtech.grossfabrichacks.reflection.access.AccessAllower;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import user11681.smartentrypoints.SmartEntrypoints;

public class GrossFabricHacks implements LanguageAdapter {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks");

    public static final UnsafeKnotClassLoader UNSAFE_LOADER;

    @Override
    public native <T> T create(net.fabricmc.loader.api.ModContainer mod, String value, Class<T> type);

    private static void loadSimpleMethodHandle() {
        try {
            final String internalName = "net/devtech/grossfabrichacks/reflection/SimpleMethodHandle";
            final ClassReader reader = new ClassReader(GrossFabricHacks.class.getClassLoader().getResourceAsStream(internalName + ".class"));
            final ClassNode klass = new ClassNode();
            reader.accept(klass, 0);

            final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);

            for (final MethodNode method : methods) {
                if (method.desc.equals("([Ljava/lang/Object;)Ljava/lang/Object;")) {
                    method.access &= ~Opcodes.ACC_NATIVE;

                    method.visitVarInsn(Opcodes.ALOAD, 0);
                    method.visitFieldInsn(Opcodes.GETFIELD, internalName, "delegate", Type.getDescriptor(MethodHandle.class));
                    method.visitVarInsn(Opcodes.ALOAD, 1);
                    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
                    method.visitInsn(Opcodes.ARETURN);
                }
            }
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static void loadClass(final String name, final Method defineClass, final ClassLoader appClassLoader) throws Throwable {
        final InputStream classStream = GrossFabricHacks.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
        final byte[] bytecode = new byte[classStream.available()];

        while (classStream.read(bytecode) != -1);

        defineClass.invoke(appClassLoader, name, bytecode, 0, bytecode.length);
    }

    public static class State {
        public static boolean mixinLoaded;

        public static boolean shouldWrite;
        // micro-optimization: cache transformer presence
        public static boolean transformPreMixinRawClass;
        public static boolean transformPreMixinAsmClass;
        public static boolean transformPostMixinRawClass;
        public static boolean transformPostMixinAsmClass;
        public static RawClassTransformer preMixinRawClassTransformer;
        public static RawClassTransformer postMixinRawClassTransformer;
        public static AsmClassTransformer preMixinAsmClassTransformer;
        public static AsmClassTransformer postMixinAsmClassTransformer;
    }

    static {
        AccessAllower.init();

        try {
            final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            final ClassLoader applicationClassLoader = FabricLoader.class.getClassLoader();

            loadClass("net.devtech.grossfabrichacks.GrossFabricHacks$State", defineClass, applicationClassLoader);
            loadClass("net.devtech.grossfabrichacks.reflection.ReflectionUtil", defineClass, applicationClassLoader);
            loadClass("net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier", defineClass, applicationClassLoader);
            loadClass("net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt", defineClass, applicationClassLoader);
            loadClass("net.devtech.grossfabrichacks.unsafe.UnsafeUtil", defineClass, applicationClassLoader);

            Class.forName("net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier", true, applicationClassLoader).getDeclaredMethod("unsafifyLoader", ClassLoader.class).invoke(null, GrossFabricHacks.class.getClassLoader());
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        LOGGER.info("no good? no, this man is definitely up to evil.");

        UNSAFE_LOADER = (UnsafeKnotClassLoader) Thread.currentThread().getContextClassLoader();

        SmartEntrypoints.executeOptionalEntrypoint("gfh:prePrePreLaunch", PrePrePreLaunch.class, PrePrePreLaunch::onPrePrePreLaunch);
    }
}
