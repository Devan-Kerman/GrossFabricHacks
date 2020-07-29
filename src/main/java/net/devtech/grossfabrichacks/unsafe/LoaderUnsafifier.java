package net.devtech.grossfabrichacks.unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.util.ASMUtil;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class LoaderUnsafifier implements PrePreLaunch, Opcodes {
    private static final Logger LOGGER = GrossFabricHacks.getLogger("LoaderUnsafifier");
    public static final ClassLoader UNSAFE_LOADER = getUnsafeLoader(Thread.currentThread().getContextClassLoader());

    public static ClassLoader getUnsafeLoader(final ClassLoader parent) {
        return UnsafeUtil.unsafeCast(parent, UnsafeUtil.getKlassFromClass(getUnsafeLoaderClass(parent)));
    }

    public static <T> Class<T> getUnsafeLoaderClass(final ClassLoader superLoader) {
        return getUnsafeLoaderClass(superLoader.getClass());
    }

    public static <T> Class<T> getUnsafeLoaderClass(final Class<?> superclass) {
        final ClassNode klass = new ClassNode();
        final String superName = ASMUtil.getInternalName(superclass);
        final String binaryName = superclass.getPackage().getName() + ".GFHUnsafeLoader";
        final String internalName = ASMUtil.toInternalName(binaryName);
        final String UnsafeUtilName = ASMUtil.getInternalName(UnsafeUtil.class);
        final String FieldName = ASMUtil.getInternalName(Field.class);
        final String KnotClassDelegateName = "net/fabricmc/loader/launch/knot/KnotClassDelegate";
        final String ObjectName = ASMUtil.getInternalName(Object.class);
        final String classMap = "UNSAFELY_DEFINED_CLASSES";
        final String mapClass = ASMUtil.getInternalName(ConcurrentHashMap.class);
        final String mapDescriptor = ASMUtil.toDescriptor(mapClass);
        final String getDescriptor = "(Ljava/lang/Object;)Ljava/lang/Object;";
        final String logger = "LOGGER";
        final String loggerClass = ASMUtil.getInternalName(Logger.class);
        final String loggerDescriptor = ASMUtil.toDescriptor(loggerClass);

        LOGGER.info(String.format("%s, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('/') + 1).replace('$', '.')));

        klass.visit(V1_8, Opcodes.ACC_PUBLIC, internalName, null, superName, null);
        klass.visitField(
                ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                classMap,
                mapDescriptor,
                "Ljava/util/concurrent/ConcurrentHashMap<Ljava/lang/ClassLoader;Ljava/util/concurrent/ConcurrentHashMap<Ljava/lang/String;Ljava/lang/Class<*>;>;>;",
                null
        );
        klass.visitField(
                ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                logger,
                loggerDescriptor,
                null,
                null
        );

        final MethodNode clinit = (MethodNode) klass.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);

        clinit.visitLdcInsn("GFHUnsafeLoader");
        clinit.visitMethodInsn(INVOKESTATIC, "net/devtech/grossfabrichacks/GrossFabricHacks", "getLogger", "(Ljava/lang/String;)Lorg/apache/logging/log4j/Logger;", false);
        clinit.visitFieldInsn(PUTSTATIC, internalName, logger, loggerDescriptor);
        clinit.visitTypeInsn(NEW, mapClass);
        clinit.visitInsn(DUP);
        clinit.visitMethodInsn(INVOKESPECIAL, mapClass, "<init>", "()V", false);
        clinit.visitFieldInsn(PUTSTATIC, internalName, classMap, mapDescriptor);
        clinit.visitInsn(RETURN);

        final MethodNode loadClass = (MethodNode) klass.visitMethod(
                ACC_PUBLIC,
                "loadClass",
                "(Ljava/lang/String;Z)Ljava/lang/Class;",
                "(Ljava/lang/String;Z)Ljava/lang/Class<*>;",
                new String[]{"java/lang/ClassNotFoundException"}
        );

        final Label start = new Label();
        final Label end = new Label();
        final Label define = new Label();
        final Label afterResolution = new Label();
        final Label resolve = new Label();
        final Label getClass = new Label();

        loadClass.visitTryCatchBlock(start, end, define, "java/lang/ClassFormatError");
        loadClass.visitFieldInsn(GETSTATIC, internalName, classMap, mapDescriptor);
        loadClass.visitVarInsn(ALOAD, 0);
        loadClass.visitMethodInsn(INVOKEVIRTUAL, mapClass, "get", getDescriptor, false);
        loadClass.visitInsn(DUP);
        loadClass.visitJumpInsn(IFNONNULL, getClass);
        loadClass.visitInsn(POP);
        loadClass.visitTypeInsn(NEW, mapClass);
        loadClass.visitInsn(DUP);
        loadClass.visitMethodInsn(INVOKESPECIAL, mapClass, "<init>", "()V", false);
        loadClass.visitLabel(getClass);
        loadClass.visitTypeInsn(CHECKCAST, mapClass);
        loadClass.visitVarInsn(ASTORE, 3);
        loadClass.visitVarInsn(ALOAD, 0);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitMethodInsn(INVOKESPECIAL, superName, "findLoadedClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        loadClass.visitInsn(DUP);
        loadClass.visitVarInsn(ASTORE, 4);
        loadClass.visitJumpInsn(IFNONNULL, resolve);
        loadClass.visitVarInsn(ALOAD, 3);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitMethodInsn(INVOKEVIRTUAL, mapClass, "get", getDescriptor, false);
        loadClass.visitJumpInsn(IFNULL, start);
        loadClass.visitLabel(resolve);
        loadClass.visitVarInsn(ILOAD, 2);
        loadClass.visitJumpInsn(IFEQ, afterResolution);
        loadClass.visitVarInsn(ALOAD, 0);
        loadClass.visitVarInsn(ALOAD, 4);
        loadClass.visitMethodInsn(INVOKESPECIAL, superName, "resolveClass", "(Ljava/lang/Class;)V", false);
        loadClass.visitLabel(afterResolution);
        loadClass.visitVarInsn(ALOAD, 4);
        loadClass.visitInsn(ARETURN);
        loadClass.visitLabel(start);
        loadClass.visitVarInsn(ALOAD, 0);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitVarInsn(ILOAD, 2);
        loadClass.visitMethodInsn(INVOKESPECIAL, superName, "loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;", false);
        loadClass.visitLabel(end);
        loadClass.visitInsn(ARETURN);
        loadClass.visitLabel(define);
        loadClass.visitInsn(POP);
        loadClass.visitFieldInsn(GETSTATIC, internalName, logger, loggerDescriptor);
        loadClass.visitLdcInsn("Class %s has an illegal format; unsafely defining it.");
        loadClass.visitInsn(ICONST_1);
        loadClass.visitTypeInsn(ANEWARRAY, ObjectName);
        loadClass.visitInsn(DUP);
        loadClass.visitInsn(ICONST_0);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitInsn(AASTORE);
        loadClass.visitMethodInsn(INVOKESTATIC, ASMUtil.getInternalName(String.class), "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
        loadClass.visitMethodInsn(INVOKEINTERFACE, loggerClass, "info", "(Ljava/lang/String;)V", true);
        loadClass.visitVarInsn(ALOAD, 3);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitLdcInsn(Type.getType(ASMUtil.toDescriptor(superName)));
        loadClass.visitLdcInsn("delegate");
        loadClass.visitMethodInsn(
                INVOKEVIRTUAL,
                ASMUtil.getInternalName(Class.class),
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                false
        );
        loadClass.visitInsn(DUP);
        loadClass.visitInsn(ICONST_1);
        loadClass.visitMethodInsn(
                INVOKEVIRTUAL,
                FieldName,
                "setAccessible",
                "(Z)V",
                false
        );
        loadClass.visitVarInsn(ALOAD, 0);
        loadClass.visitMethodInsn(
                INVOKEVIRTUAL,
                FieldName,
                "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                false
        );
        loadClass.visitTypeInsn(CHECKCAST, KnotClassDelegateName);
        loadClass.visitVarInsn(ALOAD, 1);
        loadClass.visitMethodInsn(
                INVOKEVIRTUAL,
                KnotClassDelegateName,
                "getPostMixinClassByteArray",
                "(Ljava/lang/String;)[B",
                false
        );
        loadClass.visitMethodInsn(
                INVOKESTATIC,
                UnsafeUtilName,
                "defineClass",
                "(Ljava/lang/String;[B)Ljava/lang/Class;",
                false
        );
        loadClass.visitInsn(DUP_X2);
        loadClass.visitMethodInsn(
                INVOKEVIRTUAL,
                mapClass,
                "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                false
        );
        loadClass.visitInsn(POP);
        loadClass.visitInsn(ARETURN);

        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        klass.accept(writer);

        return UnsafeUtil.defineClass(binaryName, writer.toByteArray(), superclass.getClassLoader());
    }

    @Override
    public void onPrePreLaunch() {}
}
