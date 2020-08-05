package net.devtech.grossfabrichacks.asm;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public interface ASMUtil extends Opcodes {
    int ABSTRACT_ALL = ACC_NATIVE | ACC_ABSTRACT;
    int NA = 0;

    int[] STACK_SIZE_DELTA = {
            0, // nop = 0 (0x0)
            1, // aconst_null = 1 (0x1)
            1, // iconst_m1 = 2 (0x2)
            1, // iconst_0 = 3 (0x3)
            1, // iconst_1 = 4 (0x4)
            1, // iconst_2 = 5 (0x5)
            1, // iconst_3 = 6 (0x6)
            1, // iconst_4 = 7 (0x7)
            1, // iconst_5 = 8 (0x8)
            2, // lconst_0 = 9 (0x9)
            2, // lconst_1 = 10 (0xa)
            1, // fconst_0 = 11 (0xb)
            1, // fconst_1 = 12 (0xc)
            1, // fconst_2 = 13 (0xd)
            2, // dconst_0 = 14 (0xe)
            2, // dconst_1 = 15 (0xf)
            1, // bipush = 16 (0x10)
            1, // sipush = 17 (0x11)
            1, // ldc = 18 (0x12)
            NA, // ldc_w = 19 (0x13)
            NA, // ldc2_w = 20 (0x14)
            1, // iload = 21 (0x15)
            2, // lload = 22 (0x16)
            1, // fload = 23 (0x17)
            2, // dload = 24 (0x18)
            1, // aload = 25 (0x19)
            NA, // iload_0 = 26 (0x1a)
            NA, // iload_1 = 27 (0x1b)
            NA, // iload_2 = 28 (0x1c)
            NA, // iload_3 = 29 (0x1d)
            NA, // lload_0 = 30 (0x1e)
            NA, // lload_1 = 31 (0x1f)
            NA, // lload_2 = 32 (0x20)
            NA, // lload_3 = 33 (0x21)
            NA, // fload_0 = 34 (0x22)
            NA, // fload_1 = 35 (0x23)
            NA, // fload_2 = 36 (0x24)
            NA, // fload_3 = 37 (0x25)
            NA, // dload_0 = 38 (0x26)
            NA, // dload_1 = 39 (0x27)
            NA, // dload_2 = 40 (0x28)
            NA, // dload_3 = 41 (0x29)
            NA, // aload_0 = 42 (0x2a)
            NA, // aload_1 = 43 (0x2b)
            NA, // aload_2 = 44 (0x2c)
            NA, // aload_3 = 45 (0x2d)
            -1, // iaload = 46 (0x2e)
            0, // laload = 47 (0x2f)
            -1, // faload = 48 (0x30)
            0, // daload = 49 (0x31)
            -1, // aaload = 50 (0x32)
            -1, // baload = 51 (0x33)
            -1, // caload = 52 (0x34)
            -1, // saload = 53 (0x35)
            -1, // istore = 54 (0x36)
            -2, // lstore = 55 (0x37)
            -1, // fstore = 56 (0x38)
            -2, // dstore = 57 (0x39)
            -1, // astore = 58 (0x3a)
            NA, // istore_0 = 59 (0x3b)
            NA, // istore_1 = 60 (0x3c)
            NA, // istore_2 = 61 (0x3d)
            NA, // istore_3 = 62 (0x3e)
            NA, // lstore_0 = 63 (0x3f)
            NA, // lstore_1 = 64 (0x40)
            NA, // lstore_2 = 65 (0x41)
            NA, // lstore_3 = 66 (0x42)
            NA, // fstore_0 = 67 (0x43)
            NA, // fstore_1 = 68 (0x44)
            NA, // fstore_2 = 69 (0x45)
            NA, // fstore_3 = 70 (0x46)
            NA, // dstore_0 = 71 (0x47)
            NA, // dstore_1 = 72 (0x48)
            NA, // dstore_2 = 73 (0x49)
            NA, // dstore_3 = 74 (0x4a)
            NA, // astore_0 = 75 (0x4b)
            NA, // astore_1 = 76 (0x4c)
            NA, // astore_2 = 77 (0x4d)
            NA, // astore_3 = 78 (0x4e)
            -3, // iastore = 79 (0x4f)
            -4, // lastore = 80 (0x50)
            -3, // fastore = 81 (0x51)
            -4, // dastore = 82 (0x52)
            -3, // aastore = 83 (0x53)
            -3, // bastore = 84 (0x54)
            -3, // castore = 85 (0x55)
            -3, // sastore = 86 (0x56)
            -1, // pop = 87 (0x57)
            -2, // pop2 = 88 (0x58)
            1, // dup = 89 (0x59)
            1, // dup_x1 = 90 (0x5a)
            1, // dup_x2 = 91 (0x5b)
            2, // dup2 = 92 (0x5c)
            2, // dup2_x1 = 93 (0x5d)
            2, // dup2_x2 = 94 (0x5e)
            0, // swap = 95 (0x5f)
            -1, // iadd = 96 (0x60)
            -2, // ladd = 97 (0x61)
            -1, // fadd = 98 (0x62)
            -2, // dadd = 99 (0x63)
            -1, // isub = 100 (0x64)
            -2, // lsub = 101 (0x65)
            -1, // fsub = 102 (0x66)
            -2, // dsub = 103 (0x67)
            -1, // imul = 104 (0x68)
            -2, // lmul = 105 (0x69)
            -1, // fmul = 106 (0x6a)
            -2, // dmul = 107 (0x6b)
            -1, // idiv = 108 (0x6c)
            -2, // ldiv = 109 (0x6d)
            -1, // fdiv = 110 (0x6e)
            -2, // ddiv = 111 (0x6f)
            -1, // irem = 112 (0x70)
            -2, // lrem = 113 (0x71)
            -1, // frem = 114 (0x72)
            -2, // drem = 115 (0x73)
            0, // ineg = 116 (0x74)
            0, // lneg = 117 (0x75)
            0, // fneg = 118 (0x76)
            0, // dneg = 119 (0x77)
            -1, // ishl = 120 (0x78)
            -1, // lshl = 121 (0x79)
            -1, // ishr = 122 (0x7a)
            -1, // lshr = 123 (0x7b)
            -1, // iushr = 124 (0x7c)
            -1, // lushr = 125 (0x7d)
            -1, // iand = 126 (0x7e)
            -2, // land = 127 (0x7f)
            -1, // ior = 128 (0x80)
            -2, // lor = 129 (0x81)
            -1, // ixor = 130 (0x82)
            -2, // lxor = 131 (0x83)
            0, // iinc = 132 (0x84)
            1, // i2l = 133 (0x85)
            0, // i2f = 134 (0x86)
            1, // i2d = 135 (0x87)
            -1, // l2i = 136 (0x88)
            -1, // l2f = 137 (0x89)
            0, // l2d = 138 (0x8a)
            0, // f2i = 139 (0x8b)
            1, // f2l = 140 (0x8c)
            1, // f2d = 141 (0x8d)
            -1, // d2i = 142 (0x8e)
            0, // d2l = 143 (0x8f)
            -1, // d2f = 144 (0x90)
            0, // i2b = 145 (0x91)
            0, // i2c = 146 (0x92)
            0, // i2s = 147 (0x93)
            -3, // lcmp = 148 (0x94)
            -1, // fcmpl = 149 (0x95)
            -1, // fcmpg = 150 (0x96)
            -3, // dcmpl = 151 (0x97)
            -3, // dcmpg = 152 (0x98)
            -1, // ifeq = 153 (0x99)
            -1, // ifne = 154 (0x9a)
            -1, // iflt = 155 (0x9b)
            -1, // ifge = 156 (0x9c)
            -1, // ifgt = 157 (0x9d)
            -1, // ifle = 158 (0x9e)
            -2, // if_icmpeq = 159 (0x9f)
            -2, // if_icmpne = 160 (0xa0)
            -2, // if_icmplt = 161 (0xa1)
            -2, // if_icmpge = 162 (0xa2)
            -2, // if_icmpgt = 163 (0xa3)
            -2, // if_icmple = 164 (0xa4)
            -2, // if_acmpeq = 165 (0xa5)
            -2, // if_acmpne = 166 (0xa6)
            0, // goto = 167 (0xa7)
            1, // jsr = 168 (0xa8)
            0, // ret = 169 (0xa9)
            -1, // tableswitch = 170 (0xaa)
            -1, // lookupswitch = 171 (0xab)
            -1, // ireturn = 172 (0xac)
            -2, // lreturn = 173 (0xad)
            -1, // freturn = 174 (0xae)
            -2, // dreturn = 175 (0xaf)
            -1, // areturn = 176 (0xb0)
            0, // return = 177 (0xb1)
            NA, // getstatic = 178 (0xb2)
            NA, // putstatic = 179 (0xb3)
            NA, // getfield = 180 (0xb4)
            NA, // putfield = 181 (0xb5)
            NA, // invokevirtual = 182 (0xb6)
            NA, // invokespecial = 183 (0xb7)
            NA, // invokestatic = 184 (0xb8)
            NA, // invokeinterface = 185 (0xb9)
            NA, // invokedynamic = 186 (0xba)
            1, // new = 187 (0xbb)
            0, // newarray = 188 (0xbc)
            0, // anewarray = 189 (0xbd)
            0, // arraylength = 190 (0xbe)
            NA, // athrow = 191 (0xbf)
            0, // checkcast = 192 (0xc0)
            0, // instanceof = 193 (0xc1)
            -1, // monitorenter = 194 (0xc2)
            -1, // monitorexit = 195 (0xc3)
            NA, // wide = 196 (0xc4)
            NA, // multianewarray = 197 (0xc5)
            -1, // ifnull = 198 (0xc6)
            -1, // ifnonnull = 199 (0xc7)
            NA, // goto_w = 200 (0xc8)
            NA // jsr_w = 201 (0xc9)
    };

    static String getInternalName(final Class<?> klass) {
        return toInternalName(klass.getName());
    }

    static String toInternalName(final String binaryName) {
        return binaryName.replace('.', '/');
    }

    static String getBinaryName(final ClassNode klass) {
        return toBinaryName(klass.name);
    }

    static String toBinaryName(final String internalName) {
        return internalName.replace('/', '.');
    }

    static String toDescriptor(final String name) {
        return "L" + toInternalName(name) + ";";
    }

    static MethodNode copyMethod(final ClassNode klass, final MethodNode method) {
        method.accept(klass);

        return ASMUtil.getFirstMethod(klass, method.name);
    }

    static InsnList copyInstructions(final InsnList instructions) {
        return copyInstructions(instructions, new InsnList());
    }

    static <T extends InsnList> T copyInstructions(final InsnList instructions, final T copy) {
        AbstractInsnNode instruction = instructions.getFirst();

        while (instruction != null) {
            copy.add(copyInstruction(instruction));

            instruction = instruction.getNext();
        }

        return copy;
    }

    @SuppressWarnings("unchecked")
    static <T extends AbstractInsnNode> T copyInstruction(final T instruction) {
        if (instruction instanceof InsnNode) {
            return (T) new InsnNode(instruction.getOpcode());
        } else if (instruction instanceof VarInsnNode) {
            return (T) new VarInsnNode(instruction.getOpcode(), ((VarInsnNode) instruction).var);
        } else if (instruction instanceof FieldInsnNode) {
            final FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;

            return (T) new FieldInsnNode(instruction.getOpcode(), fieldInstruction.owner, fieldInstruction.name, fieldInstruction.desc);
        } else if (instruction instanceof MethodInsnNode) {
            final MethodInsnNode methodInstruction = (MethodInsnNode) instruction;

            return (T) new MethodInsnNode(instruction.getOpcode(), methodInstruction.owner, methodInstruction.name, methodInstruction.desc, methodInstruction.itf);
        } else if (instruction instanceof InvokeDynamicInsnNode) {
            final InvokeDynamicInsnNode lambdaInstruction = (InvokeDynamicInsnNode) instruction;

            return (T) new InvokeDynamicInsnNode(lambdaInstruction.name, lambdaInstruction.desc, lambdaInstruction.bsm, lambdaInstruction.bsmArgs);
        } else if (instruction instanceof TypeInsnNode) {
            return (T) new TypeInsnNode(instruction.getOpcode(), ((TypeInsnNode) instruction).desc);
        } else if (instruction instanceof MultiANewArrayInsnNode) {
            final MultiANewArrayInsnNode arrayInstruction = (MultiANewArrayInsnNode) instruction;

            return (T) new MultiANewArrayInsnNode(arrayInstruction.desc, arrayInstruction.dims);
        } else if (instruction instanceof LabelNode) {
            return (T) new LabelNode(((LabelNode) instruction).getLabel());
        } else if (instruction instanceof IntInsnNode) {
            return (T) new IntInsnNode(instruction.getOpcode(), ((IntInsnNode) instruction).operand);
        } else if (instruction instanceof LdcInsnNode) {
            return (T) new LdcInsnNode(((LdcInsnNode) instruction).cst);
        } else if (instruction instanceof FrameNode) {
            final FrameNode frameNode = (FrameNode) instruction;

            return (T) new FrameNode(frameNode.getOpcode(), frameNode.local.size(), frameNode.local.toArray(), frameNode.stack.size(), frameNode.stack.toArray());
        } else if (instruction instanceof JumpInsnNode) {
            return (T) new JumpInsnNode(instruction.getOpcode(), ((JumpInsnNode) instruction).label);
        } else if (instruction instanceof IincInsnNode) {
            final IincInsnNode incrementation = (IincInsnNode) instruction;

            return (T) new IincInsnNode(incrementation.var, incrementation.incr);
        } else if (instruction instanceof LineNumberNode) {
            final LineNumberNode lineNode = (LineNumberNode) instruction;

            return (T) new LineNumberNode(lineNode.line, lineNode.start);
        } else if (instruction instanceof LookupSwitchInsnNode) {
            final LookupSwitchInsnNode lookupSwitchNode = (LookupSwitchInsnNode) instruction;
            final Object[] keyObjects = lookupSwitchNode.keys.toArray();
            final int[] keys = new int[keyObjects.length];

            for (int i = 0; i < keyObjects.length; i++) {
                keys[i] = (int) keyObjects[i];
            }

            return (T) new LookupSwitchInsnNode(lookupSwitchNode.dflt, keys, lookupSwitchNode.labels.toArray(new LabelNode[0]));
        } else if (instruction instanceof TableSwitchInsnNode) {
            final TableSwitchInsnNode tableSwitchNode = (TableSwitchInsnNode) instruction;

            return (T) new TableSwitchInsnNode(tableSwitchNode.min, tableSwitchNode.max, tableSwitchNode.dflt, tableSwitchNode.labels.toArray(new LabelNode[0]));
        }

        throw new IllegalArgumentException(String.valueOf(instruction));
    }

    static ClassNode getClassNode(final Class<?> klass) {
        try {
            final ClassNode node = new ClassNode();
            final ClassReader reader = new ClassReader(klass.getName());

            reader.accept(node, 0);

            return node;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    static ClassNode getClassNode(final String className) {
        try {
            final ClassNode klass = new ClassNode();
            final ClassReader reader = new ClassReader(className);

            reader.accept(klass, 0);

            return klass;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    static LocalVariableNode getLocalVariable(final MethodNode method, final int index) {
        LocalVariableNode result = null;

        for (final LocalVariableNode local : method.localVariables) {
            if (index == local.index) {
                result = local;

                break;
            }
        }

        return result;
    }

    static LocalVariableNode getLocalVariable(final MethodNode method, final String name) {
        LocalVariableNode result = null;

        for (final LocalVariableNode local : method.localVariables) {
            if (name.equals(local.name)) {
                result = local;

                break;
            }
        }

        return result;
    }

    static List<AbstractInsnNode> getInstructions(final ClassNode klass, final String method) {
        return getInstructions(getFirstMethod(klass, method));
    }

    static List<AbstractInsnNode> getInstructions(final MethodNode method) {
        return Arrays.asList(method.instructions.toArray());
    }

    static MethodNode getFirstInheritedMethod(ClassNode klass, final String name) {
        MethodNode first = null;

        outer:
        while (true) {
            for (final MethodNode method : klass.methods) {
                if (name.equals(method.name)) {
                    first = method;
                    break outer;
                }
            }

            if (klass.superName != null) {
                try {
                    final ClassReader reader = new ClassReader(klass.superName);

                    klass = new ClassNode();

                    reader.accept(klass, 0);
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                break;
            }
        }

        return first;
    }

    static MethodNode getFirstMethod(final ClassNode klass, final String name) {
        MethodNode first = null;

        for (final MethodNode method : klass.methods) {
            if (name.equals(method.name)) {
                first = method;
                break;
            }
        }

        return first;
    }

    static List<MethodNode> getAllMethods(ClassNode klass) {
        final List<MethodNode> methods = new ReferenceArrayList<>();

        while (true) {
            methods.addAll(klass.methods);

            if (klass.superName != null) {
                try {
                    final ClassReader reader = new ClassReader(klass.superName);

                    klass = new ClassNode();

                    reader.accept(klass, 0);
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                break;
            }
        }

        return methods;
    }

    static List<MethodNode> getMethods(final String internalClassName, final String name) {
        return getMethods(getClassNode(internalClassName), name);
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

    static List<AbstractInsnNode> getInstructions(final InsnList instructions, final Predicate<AbstractInsnNode> condition) {
        final List<AbstractInsnNode> matchingInstructions = new ReferenceArrayList<>();

        for (final AbstractInsnNode instruction : instructions) {
            if (condition.test(instruction)) {
                matchingInstructions.add(instruction);
            }
        }

        return matchingInstructions;
    }

    static List<String> getExplicitParameters(final InvokeDynamicInsnNode instruction) {
        return getExplicitParameters(instruction.desc);
    }

    static List<String> getExplicitParameters(final MethodInsnNode instruction) {
        return getExplicitParameters(instruction.desc);
    }

    static List<String> getExplicitParameters(final MethodNode method) {
        return getExplicitParameters(method.desc);
    }

    static List<String> getExplicitParameters(final String descriptor) {
        final List<String> parameters = new ArrayList<>();
        final int end = descriptor.indexOf(')');
        final String primitives = "VZCBSIJFD";
        final StringBuilder parameter = new StringBuilder();
        char character;

        for (int i = descriptor.indexOf('(') + 1; i < end; ++i) {
            character = descriptor.charAt(i);

            parameter.append(character);

            if (character == ';' || primitives.indexOf(character) >= 0 && (parameter.length() == 1 || parameter.length() == 2 && parameter.charAt(0) == '[')) {
                parameters.add(parameter.toString());

                parameter.delete(0, parameter.length());
            }
        }

        return parameters;
    }

    static String getReturnType(final InvokeDynamicInsnNode instruction) {
        return getReturnType(instruction.desc);
    }

    static String getReturnType(final MethodInsnNode instruction) {
        return getReturnType(instruction.desc);
    }

    static String getReturnType(final MethodNode method) {
        return getReturnType(method.desc);
    }

    static String getReturnType(final String descriptor) {
        return descriptor.substring(descriptor.indexOf(')') + 1);
    }

    static List<String> parseDescriptor(final InvokeDynamicInsnNode instruction) {
        return parseDescriptor(instruction.desc);
    }

    static List<String> parseDescriptor(final MethodInsnNode instruction) {
        return parseDescriptor(instruction.desc);
    }

    static List<String> parseDescriptor(final MethodNode method) {
        return parseDescriptor(method.desc);
    }

    static List<String> parseDescriptor(final String descriptor) {
        final List<String> types = new ArrayList<>();
        final int end = descriptor.indexOf(')');
        final String primitives = "VZCBSIJFD";
        final StringBuilder parameter = new StringBuilder();
        char character;

        for (int i = descriptor.indexOf('(') + 1; i < end; ++i) {
            character = descriptor.charAt(i);

            parameter.append(character);

            if (character == ';' || primitives.indexOf(character) >= 0 && (parameter.length() == 1 || parameter.length() == 2 && parameter.charAt(0) == '[')) {
                types.add(parameter.toString());

                parameter.delete(0, parameter.length());
            }
        }

        types.add(descriptor.substring(descriptor.indexOf(')') + 1));

        return types;
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

    static Object getDefaultValue(final String descriptor) {
        switch (descriptor) {
            case "Z":
                return false;
            case "C":
                return (char) 0;
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

    static boolean isReturnInstruction(final AbstractInsnNode instruction) {
        return isReturnOpcode(instruction.getOpcode());
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
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
                return true;
            default:
                return false;
        }
    }

    static boolean isStoreOpcode(final int opcode) {
        switch (opcode) {
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
                return true;
            default:
                return false;
        }
    }
}
