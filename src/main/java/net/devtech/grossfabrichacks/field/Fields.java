package net.devtech.grossfabrichacks.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * This annotation should contain information about synthetic fields.<br>
 * It is intended to be used with {@link Getter} or {@link Setter}.
 */
@Target(ElementType.TYPE)
public @interface Fields {
    /**
     * @return information about the fields to generate in the annotated type.
     */
    Entry[] value();

    /**
     * This annotation describes a field declaration.<br>
     * It is intended for use in {@link Fields#value}.
     */
    @Target(ElementType.TYPE)
    @Repeatable(Fields.class)
    @interface Entry {
        /**
         * The default access for generated fields is {@code public synthetic}.
         */
        int DEFAULT_ACCESS = Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC;

        String NO_SIGNATURE = "NO_SIGNATURE";

        /**
         * @return the {@linkplain Opcodes modifiers} of this field,<br>
         * which can be used in order make the field {@code final}, {@code private}, {@code abstract} and so on.
         */
        int access() default DEFAULT_ACCESS;

        /**
         * @return the name of the field to generate.
         */
        String name();

        /**
         * @return the descriptor of the field to generate.
         * <br><br>
         * <h3>descriptors</h3>
         * void: V<br>
         * boolean: Z<br>
         * char: C<br>
         * byte: B<br>
         * short: S<br>
         * int: I<br>
         * long: J<br>
         * float: F<br>
         * double: D<br>
         * any non-array non-primitive type: L + {@linkplain Type#getInternalName internal name} + ;<br>
         * array: [ + descriptor of component class
         */
        String descriptor();

        /**
         * @return the signature of the field to generate.<br>
         * If this method returns {@link #NO_SIGNATURE}, then the field is not given a signature.<br>
         * This method may be used only for fields of generic types.<br>
         * It is <b>always optional</b>.
         */
        String signature() default NO_SIGNATURE;
    }
}
