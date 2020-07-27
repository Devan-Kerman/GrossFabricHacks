package net.devtech.grossfabrichacks.field;

import java.util.function.Consumer;
import net.devtech.grossfabrichacks.field.Fields.Entry;
import org.objectweb.asm.Opcodes;

@Fields({
        @Entry(name = "energy", descriptor = "J"),
        @Entry(name = "width", descriptor = "I"),
        @Entry(name = "classLoader", descriptor = "Ljava/lang/ClassLoader;"),
        @Entry(name = "consumer", descriptor = "Ljava/util/function/Consumer;")
})
public interface StatefulInterface {
    @Getter("energy")
    default long getEnergy() {
        return 734;
    }

    @Setter("energy")
    default void setEnergy(final long energy) {}

    @Getter(value = "width", access = Opcodes.ACC_PRIVATE)
    default int getWidth() {
        return 123;
    }

    @Setter("width")
    default void setWidth(final int width) {}

    @Getter("classLoader")
    default ClassLoader getClassLoader() {
        return null;
    }

    @Setter("classLoader")
    default void setClassLoader(final ClassLoader loader) {}

    @Getter("consumer")
    default Consumer<Integer> getConsumer() {
        return null;
    }

    @Setter("consumer")
    default void setConsumer(final Consumer<Integer> consumer) {}

//    void test();
}
