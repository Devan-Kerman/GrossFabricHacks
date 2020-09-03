package net.devtech.grossfabrichacks;

import java.lang.invoke.MethodHandle;
import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.devtech.grossfabrichacks.reflection.AccessAllower;
import net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier;
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

    public static final String INTERNAL_NAME = "net/devtech/grossfabrichacks/GrossFabricHacks";

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
        } catch (final Throwable exception) {
            throw new RuntimeException(exception);
        }
    }

    static {
        LOGGER.error("no good? no, this man is definitely up to evil.");

        AccessAllower.init();

        UNSAFE_LOADER = LoaderUnsafifier.unsafifyLoader(Thread.currentThread().getContextClassLoader());

        SmartEntrypoints.executeOptionalEntrypoint("gfh:prePrePreLaunch", PrePrePreLaunch.class, PrePrePreLaunch::onPrePrePreLaunch);
    }
}
