package net.devtech.grossfabrichacks.util;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public interface ASMUtil extends Opcodes {
    int VISIBILITY = ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE;
    int ABSTRACT_ALL = ACC_NATIVE | ACC_ABSTRACT;

    static String getInternalName(final Class<?> klass) {
        return toInternalName(klass.getName());
    }

    static String toInternalName(final String binaryName) {
        return binaryName.replace('.', '/');
    }

    static String toBinaryName(final String internalName) {
        return internalName.replace('/', '.');
    }

    static String toDescriptor(final String name) {
        return "L" + toInternalName(name) + ";";
    }

    static List<AbstractInsnNode> getInstructions(final ClassNode klass, final String method) {
        return getInstructions(getFirstMethod(klass, method));
    }

    static List<AbstractInsnNode> getInstructions(final MethodNode method) {
        return Arrays.asList(method.instructions.toArray());
    }

    static MethodNode getFirstMethod(final ClassNode klass, final String name) {
        MethodNode first = null;

        for (final MethodNode method : klass.methods) {
            if (name.equals(method.name)) {
                first = method;
            }
        }

        return first;
    }

    static List<MethodNode> getMethods(final ClassNode klass, final String name) {
        final List<MethodNode> methods = new ReferenceArrayList<>();

        for (final MethodNode method : klass.methods) {
            if (name.equals(method.name)) {
                methods.add(method);
            }
        }

        return methods;
    }

    /**
     * replace all instructions after {@code start}, before and including {@code end} in {@code method} by {@code replacement}
     *
     * @param instructions the list in which to replace instructions
     * @param replacement  the list of instructions that should be inserted
     * @param start        the instruction after which {@code replacement} should be inserted
     * @param end          the last instruction that should be removed
     */
    static void replaceInstructions(final InsnList instructions, final InsnList replacement, final Predicate<AbstractInsnNode> start, final Predicate<AbstractInsnNode> end) {
        final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        AbstractInsnNode instruction;

        while (iterator.hasNext()) {
            instruction = iterator.next();

            if (end.test(instruction)) {
                while (!start.test(instruction)) {
                    iterator.remove();
                    instruction = iterator.previous();
                }

                instructions.insert(instruction, replacement);
            }
        }
    }

    static void insert(final InsnList instructions, final InsnList insertion, final Predicate<AbstractInsnNode> after) {
        final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        AbstractInsnNode instruction;

        while (iterator.hasNext()) {
            if (after.test(instruction = iterator.next())) {
                instructions.insert(instruction, insertion);
            }
        }
    }

    static List<String> getExplicitParameters(final MethodNode method) {
        final List<String> parameters = new ArrayList<>();
        final int end = method.desc.indexOf(')');
        final String primitives = "VZCBSIJFD";
        final StringBuilder parameter = new StringBuilder();
        char character;

        for (int i = method.desc.indexOf('(') + 1; i < end; ++i) {
            character = method.desc.charAt(i);

            parameter.append(character);

            if (character == ';' || primitives.indexOf(character) >= 0 && (parameter.length() == 1 || parameter.length() == 2 && parameter.charAt(0) == '[')) {
                parameters.add(parameter.toString());

                parameter.delete(0, parameter.length());
            }
        }

        return parameters;
    }

    static <T> T getAnnotationValue(final AnnotationNode annotation, final String name, final T alternative) {
        final List<Object> values = annotation.values;
        final int size = values.size();

        for (int i = 0; i < size; i += 2) {
            if (name.equals(values.get(i))) {
                //noinspection unchecked
                return (T) values.get(i + 1);
            }
        }

        return alternative;
    }

    static <T> T getAnnotationValue(final AnnotationNode annotation, final String name) {
        final List<Object> values = annotation.values;
        final int size = values.size();

        for (int i = 0; i < size; i += 2) {
            if (name.equals(values.get(i))) {
                //noinspection unchecked
                return (T) values.get(i + 1);
            }
        }

        throw new RuntimeException(String.format("cannot find the value of %s in %s", name, annotation));
    }

    static Object getNullEquivalent(final String descriptor) {
        switch (descriptor) {
            case "Z":
                return false;
            case "C":
            case "B":
                return (byte) 0;
            case "S":
                return (short) 0;
            case "I":
                return 0;
            case "J":
                return 0L;
            case "F":
                return 0F;
            case "D":
                return 0D;
            default:
                return null;
        }
    }

    static int getLoadOpcode(final String descriptor) {
        switch (descriptor) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                return ILOAD;
            case "J":
                return LLOAD;
            case "F":
                return FLOAD;
            case "D":
                return DLOAD;
            default:
                return ALOAD;
        }
    }

    static int getReturnOpcode(final String descriptor) {
        switch (descriptor) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                return IRETURN;
            case "J":
                return LRETURN;
            case "F":
                return FRETURN;
            case "D":
                return DRETURN;
            case "V":
                return RETURN;
            default:
                return ARETURN;
        }
    }

    static int getReturnOpcode(final MethodNode method) {
        return getReturnOpcode(Type.getReturnType(method.desc).getDescriptor());
    }

    static boolean isReturnOpcode(final int opcode) {
        switch (opcode) {
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
                return true;
            default:
                return false;
        }
    }

    static boolean isLoadOpcode(final int opcode) {
        switch (opcode) {
            case LDC:
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                return true;
            default:
                return false;
        }
    }

    static boolean isVisibilityHigher(int higher, int lower) {
        higher = higher & VISIBILITY;
        lower = lower & VISIBILITY;

        if (lower == ACC_PUBLIC) {
            return false;
        }

        if (higher == ACC_PUBLIC) {
            return true;
        }

        if (lower == ACC_PROTECTED) {
            return false;
        }

        if (higher == ACC_PROTECTED) {
            return true;
        }

        if (lower == 0) {
            return false;
        }

        return higher == 0;
    }
}
