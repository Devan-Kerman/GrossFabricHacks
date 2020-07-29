package net.devtech.grossfabrichacks.field;

import java.util.List;
import net.devtech.grossfabrichacks.asm.ASMUtil;
import net.devtech.grossfabrichacks.asm.DelegatingInsnList;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class FieldSynthesizer {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/FieldSynthesizer");

    public static void init() {
        LOGGER.info("Initializing field synthesis capability.");

        TransformerApi.registerPreMixinAsmClassTransformer(FieldSynthesizer::transform);

        LOGGER.info("Initialization complete. Multiple inheritance of state is now at your key presses.");
    }

    private static void transform(final String name, final ClassNode klass) {
        if (klass.visibleAnnotations != null) {
            for (final AnnotationNode annotation : klass.visibleAnnotations) {
                if ("Lnet/devtech/grossfabrichacks/field/Fields;".equals(annotation.desc)) {
                    //noinspection unchecked
                    for (final AnnotationNode field : (List<AnnotationNode>) ASMUtil.getAnnotationValue(annotation, "value")) {
                        addField(klass, field);
                    }
                }
            }
        }

        for (final MethodNode method : klass.methods) {
            if (method.visibleAnnotations != null) {
                for (final AnnotationNode annotation : method.visibleAnnotations) {
                    if (Type.getDescriptor(Getter.class).equals(annotation.desc)) {
                        FieldSynthesizer.get(name, method, annotation);
                    } else if (Type.getDescriptor(Setter.class).equals(annotation.desc)) {
                        FieldSynthesizer.set(name, method, annotation);
                    }
                }
            }
        }
    }

    private static void addField(final ClassNode klass, AnnotationNode annotation) {
        final String name = ASMUtil.getAnnotationValue(annotation, "name");
        final String signature = ASMUtil.getAnnotationValue(annotation, "signature", Fields.Entry.NO_SIGNATURE);

        for (final FieldNode field : klass.fields) {
            if (field.name.equals(name)) {
                throw new RuntimeException(String.format("field %s already exists in %s.", name, klass.name));
            }
        }

        //noinspection StringEquality
        klass.fields.add(new FieldNode(
                ASMUtil.getAnnotationValue(annotation, "access", Fields.Entry.DEFAULT_ACCESS),
                name,
                ASMUtil.getAnnotationValue(annotation, "descriptor"),
                signature == Fields.Entry.NO_SIGNATURE ? null : signature,
                null)
        );
    }

    private static void get(final String klass, final MethodNode method, final AnnotationNode annotation) {
        final String fieldDescriptor = Type.getReturnType(method.desc).getDescriptor();
        final String fieldName = ASMUtil.getAnnotationValue(annotation, "value");
        final DelegatingInsnList instructions = new DelegatingInsnList();

        instructions.addVarInsn(Opcodes.ALOAD, 0);
        instructions.addFieldInsn(Opcodes.GETFIELD, klass, fieldName, fieldDescriptor);

        if (FieldSynthesizer.insertAndSetAccess(method, annotation, instructions)) {
            instructions.addInsn(ASMUtil.getReturnOpcode(fieldDescriptor));

            method.instructions.insert(instructions);
        }
    }

    private static void set(final String klass, MethodNode method, final AnnotationNode annotation) {
        final String fieldDescriptor = ASMUtil.getExplicitParameters(method).get(0);
        final String fieldName = ASMUtil.getAnnotationValue(annotation, "value");
        final DelegatingInsnList instructions = new DelegatingInsnList();

        instructions.addVarInsn(Opcodes.ALOAD, 0);
        instructions.addVarInsn(ASMUtil.getLoadOpcode(fieldDescriptor), 1);
        instructions.addFieldInsn(Opcodes.PUTFIELD, klass, fieldName, fieldDescriptor);

        if (FieldSynthesizer.insertAndSetAccess(method, annotation, instructions)) {
            instructions.addInsn(ASMUtil.getReturnOpcode(method));

            method.instructions.insert(instructions);
        }
    }

    private static boolean insertAndSetAccess(final MethodNode method, final AnnotationNode annotation, final InsnList instructions) {
        final int access = ASMUtil.getAnnotationValue(annotation, "access", Setter.DEFAULT_ACCESS);
        final boolean override = ASMUtil.getAnnotationValue(annotation, "overrideAccess", true);

        boolean incomplete = true;

        for (final AbstractInsnNode instruction : method.instructions) {
            if (ASMUtil.isReturnOpcode(instruction.getOpcode())) {
                method.instructions.insertBefore(instruction, instructions);

                incomplete = false;
            }
        }

        if (access == Getter.DEFAULT_ACCESS) {
            if (override && incomplete) {
                method.access &= ~ASMUtil.ABSTRACT_ALL;
            }
        } else {
            if (override) {
                method.access = access;
            } else {
                method.access |= access;
            }
        }

        return incomplete;
    }
}
