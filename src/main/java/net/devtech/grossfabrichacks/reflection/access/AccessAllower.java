package net.devtech.grossfabrichacks.reflection.access;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import net.devtech.grossfabrichacks.instrumentation.CompatibleClassFileTransformer;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import net.devtech.grossfabrichacks.reflection.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessAllower {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/AccessAllower (Java9🅱️ix v2)");

    public static void init() {
        LOGGER.info("access is gone");

        try {
            final Class<?> reflectionClass = Class.forName(ReflectionUtil.JAVA_9 ? "jdk.internal.reflect.Reflection" : "sun.reflect.Reflection");
            final CompatibleClassFileTransformer transformer = (final ClassLoader loader,
                                                                final String className,
                                                                final Class<?> classBeingRedefined,
                                                                final ProtectionDomain protectionDomain,
                                                                final byte[] classfileBuffer) -> {
                final ClassNode node = new ClassNode();
                final ClassReader reader = new ClassReader(classfileBuffer);
                reader.accept(node, 0);

                if (classBeingRedefined == AccessibleObject.class) {
                    makeAccessibleObjectAccessible(node);
                } else if (classBeingRedefined == Field.class) {
                    transformField(node);
                } else if (classBeingRedefined == MethodHandles.Lookup.class) {
                    transformMethodHandlesLookup(node);
                } else if (classBeingRedefined == reflectionClass) {
                    transformReflection(node);
                } else {
                    return classfileBuffer;
                }

                final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                node.accept(writer);

                return writer.toByteArray();
            };

            InstrumentationApi.INSTRUMENTATION.addTransformer(transformer, true);
            InstrumentationApi.INSTRUMENTATION.retransformClasses(AccessibleObject.class, Field.class, MethodHandles.Lookup.class, reflectionClass);
            InstrumentationApi.INSTRUMENTATION.removeTransformer(transformer);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static void makeAccessibleObjectAccessible(final ClassNode klass) {
        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);
        MethodNode method;

        for (int i = 0, size = methods.length; i < size; i++) {
            if (methods[i].name.equals("checkAccess") || methods[i].name.equals("checkCanSetAccessible")) {
                method = methods[i];

                method.instructions.clear();
                method.visitInsn(Opcodes.ICONST_1);
                method.visitInsn(Opcodes.IRETURN);
            }
        }
    }

    public static void transformField(final ClassNode klass) {
        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);

        for (int i = 0, size = methods.length; i < size; i++) {
            if (methods[i].name.equals("checkAccess")) {
                methods[i].instructions.clear();
                methods[i].visitInsn(Opcodes.RETURN);
            }
        }
    }

    public static void transformMethodHandlesLookup(final ClassNode klass) {
        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);
        MethodNode method;

        for (int i = 0, size = methods.length; i < size; i++) {
            if (methods[i].name.equals("isClassAccessible")) {
                method = methods[i];

                method.instructions.clear();
                method.visitInsn(Opcodes.ICONST_1);
                method.visitInsn(Opcodes.IRETURN);
            } else if (methods[i].name.equals("checkAccess")) {
                methods[i].instructions.clear();
                methods[i].visitInsn(Opcodes.RETURN);
            }
        }
    }

    public static void transformReflection(final ClassNode klass) {
        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);
        MethodNode method;

        for (int i = 0, size = methods.length; i < size; i++) {
            if (methods[i].name.equals("filterFields") || methods[i].name.equals("filterMethods")) {
                method = methods[i];

                method.instructions.clear();
                method.visitVarInsn(Opcodes.ALOAD, 1);
                method.visitInsn(Opcodes.ARETURN);
            } else if (methods[i].name.equals("filter")) {
                method = methods[i];

                method.instructions.clear();
                method.visitVarInsn(Opcodes.ALOAD, 0);
                method.visitInsn(Opcodes.ARETURN);
            }
        }
    }
}
