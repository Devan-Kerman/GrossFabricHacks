package net.devtech.grossfabrichacks.loader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.field.FieldSynthesizer;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.devtech.grossfabrichacks.util.ASMUtil;
import net.devtech.grossfabrichacks.util.DelegatingInsnList;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class LoaderUnsafifier {
    public static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/UnsafeLoader");

    private static final Map<String, Class<?>> UNSAFELY_DEFINED_CLASSES = new ConcurrentHashMap<>();
    private static final AsmClassTransformer equals = (name, klass) -> {
        MethodNode method = ASMUtil.getFirstMethod(klass, "equals");

        if (method == null) {
            klass.methods.add(method = new MethodNode());
        } else {
            method.instructions.clear();
        }

        method.instructions.add(new InsnNode(Opcodes.ICONST_1));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
    };

    public static void init() {
        LOGGER.info("Unsafifying KnotClassLoader.");

//        InstrumentationApi.retransform("net.fabricmc.loader.launch.knot.KnotClassLoader", (final String name, final ClassNode klass) -> {
//            unsafelyDefineClass(klass);
//            findUnsafelyDefinedClass(klass);
//        });
        InstrumentationApi.retransform(GrossFabricHacks.class, equals);

        LOGGER.info(new GrossFabricHacks().equals(UnsafeUtil.allocateInstance(MinecraftClient.class)));

        LOGGER.info("Unsafified KnotClassLoader. concernedtater");
    }

    private static void unsafelyDefineClass(final ClassNode klass) {
        final DelegatingInsnList instructions = new DelegatingInsnList();
        final String unsafeClassName = ASMUtil.toInternalName(UnsafeUtil.className);

        instructions.addFieldInsn(Opcodes.GETSTATIC, ASMUtil.getInternalName(LoaderUnsafifier.class), "UNSAFELY_DEFINED_CLASSES", Type.getDescriptor(Map.class));
        instructions.addVarInsn(Opcodes.ALOAD, 1);
        instructions.addFieldInsn(Opcodes.GETSTATIC, ASMUtil.getInternalName(UnsafeUtil.class), "UNSAFE", Type.getDescriptor(UnsafeUtil.unsafeClass));
        instructions.addVarInsn(Opcodes.ALOAD, 1);
        instructions.addVarInsn(Opcodes.ALOAD, 5);
        instructions.addInsn(Opcodes.ICONST_0);
        instructions.addVarInsn(Opcodes.ALOAD, 5);
        instructions.addInsn(Opcodes.ARRAYLENGTH);
        instructions.addInsn(Opcodes.ACONST_NULL);
        instructions.addInsn(Opcodes.ACONST_NULL);
        instructions.addMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                unsafeClassName,
                "defineClass",
                "(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;",
                false
        );
        instructions.addInsn(Opcodes.DUP_X1);
        instructions.addMethodInsn(
                Opcodes.INVOKEINTERFACE,
                ASMUtil.getInternalName(Map.class),
                "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                true
        );

        ASMUtil.replaceInstructions(ASMUtil.getFirstMethod(klass, "loadClass").instructions, instructions,
                FrameNode.class::isInstance,
                (final AbstractInsnNode end) -> end instanceof MethodInsnNode && "defineClass".equals(((MethodInsnNode) end).name) && !unsafeClassName.equals(((MethodInsnNode) end).owner)
        );
    }

    private static void findUnsafelyDefinedClass(final ClassNode klass) {
        final DelegatingInsnList instructions = new DelegatingInsnList();
        final Label afterCheck = new Label();

//        info(instructions, "test");
        instructions.addJumpInsn(Opcodes.IFNONNULL, afterCheck);
        instructions.addFieldInsn(Opcodes.GETSTATIC, ASMUtil.getInternalName(LoaderUnsafifier.class), "UNSAFELY_DEFINED_CLASSES", Type.getDescriptor(Map.class));
        instructions.addVarInsn(Opcodes.ALOAD, 1);
        instructions.addMethodInsn(
                Opcodes.INVOKEINTERFACE,
                ASMUtil.getInternalName(Map.class),
                "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                true
        );
        instructions.addLabel(afterCheck);

        for (MethodNode method : klass.methods) {
            ASMUtil.insert(ASMUtil.getFirstMethod(klass, method.name).instructions, instructions,
                    (final AbstractInsnNode after) -> after instanceof MethodInsnNode && "findLoadedClass".equals(((MethodInsnNode) after).name)
            );
        }
    }

    private static void info(final DelegatingInsnList instructions, final String message) {
        instructions.addFieldInsn(Opcodes.GETSTATIC, ASMUtil.getInternalName(FieldSynthesizer.class), "LOGGER", Type.getDescriptor(Logger.class));
        instructions.addLdcInsn(message);
        instructions.addMethodInsn(Opcodes.INVOKEINTERFACE, ASMUtil.getInternalName(Logger.class), "info", "info(Ljava/lang/String;)V", true);
    }
}
