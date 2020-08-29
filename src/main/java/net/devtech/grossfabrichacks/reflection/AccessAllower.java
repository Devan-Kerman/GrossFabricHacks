package net.devtech.grossfabrichacks.reflection;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessAllower {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/AccessAllower (Java9🅱️ix v2)");

    public static void init() {
        LOGGER.info("this is more concerning than Fabric Zero");

        makeAccessibleObjectAccessible();
        transformField();
        transformReflection();
        transformMethodHandlesLookup();
    }

    public static void makeAccessibleObjectAccessible() {
        InstrumentationApi.retransform(AccessibleObject.class, (final String name, final ClassNode klass) -> {
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
        });
    }

    public static void transformField() {
        InstrumentationApi.retransform(Field.class, (final String name, final ClassNode klass) -> {
            final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);

            for (int i = 0, size = methods.length; i < size; i++) {
                if (methods[i].name.equals("checkAccess")) {
                    methods[i].instructions.clear();
                    methods[i].visitInsn(Opcodes.RETURN);
                }
            }
        });
    }

    public static void transformReflection() {
        InstrumentationApi.retransform(ReflectionUtil.JAVA_9 ? "jdk.internal.reflect.Reflection" : "sun.reflect.Reflection", (final String name, final ClassNode klass) -> {
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
        });
    }

    public static void transformMethodHandlesLookup() {
        InstrumentationApi.retransform(MethodHandles.Lookup.class, (final String name, final ClassNode klass) -> {
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
        });
    }
}
