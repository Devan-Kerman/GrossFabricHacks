package net.devtech.grossfabrichacks.util;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
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
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class DelegatingInsnList extends InsnList {
    private static LabelNode getLabelNode(final Label label) {
        return (LabelNode) (label.info == null ? label.info = new LabelNode() : label.info);
    }

    private static Object[] getLabelNodes(final Object[] objects) {
        final Object[] labelNodes = new Object[objects.length];
        Object object;

        for (int i = 0, n = objects.length; i < n; ++i) {
            object = objects[i];

            if (object instanceof Label) {
                object = getLabelNode((Label) object);
            }

            labelNodes[i] = object;
        }

        return labelNodes;
    }

    private static LabelNode[] getLabelNodes(final Label[] labels) {
        final LabelNode[] labelNodes = new LabelNode[labels.length];

        for (int i = 0, n = labels.length; i < n; ++i) {
            labelNodes[i] = getLabelNode(labels[i]);
        }

        return labelNodes;
    }

    public final void addFrame(final int type, final int numLocal, final Object[] local, final int numStack, final Object[] stack) {
        super.add(new FrameNode(
                type,
                numLocal,
                local == null ? null : getLabelNodes(local),
                numStack,
                stack == null ? null : getLabelNodes(stack)
        ));
    }

    public final void addInsn(final int opcode) {
        super.add(new InsnNode(opcode));
    }

    public final void addIntInsn(final int opcode, final int operand) {
        super.add(new IntInsnNode(opcode, operand));
    }

    public final void addVarInsn(final int opcode, final int var) {
        super.add(new VarInsnNode(opcode, var));
    }

    public final void addTypeInsn(final int opcode, final String type) {
        super.add(new TypeInsnNode(opcode, type));
    }

    public final void addFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        super.add(new FieldInsnNode(opcode, owner, name, descriptor));
    }

    public final void addMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        super.add(new MethodInsnNode(opcode, owner, name, descriptor, isInterface));
    }

    public final void addInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
        super.add(new InvokeDynamicInsnNode(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    public final void addJumpInsn(final int opcode, final Label label) {
        super.add(new JumpInsnNode(opcode, getLabelNode(label)));
    }

    public final void addLabel(final Label label) {
        super.add(getLabelNode(label));
    }

    public final void addLdcInsn(final Object value) {
        super.add(new LdcInsnNode(value));
    }

    public final void addIincInsn(final int var, final int increment) {
        super.add(new IincInsnNode(var, increment));
    }

    public final void addTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        super.add(new TableSwitchInsnNode(min, max, getLabelNode(dflt), getLabelNodes(labels)));
    }

    public final void addLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        super.add(new LookupSwitchInsnNode(getLabelNode(dflt), keys, getLabelNodes(labels)));
    }

    public final void addMultiANewArrayInsn(final String descriptor, final int numDimensions) {
        super.add(new MultiANewArrayInsnNode(descriptor, numDimensions));
    }
}
