package unsafe;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import sun.misc.Unsafe;

@Testable
class GFHTest {
    static final Unsafe theUnsafe = getTheUnsafe();
    static final boolean x64 = System.getProperty("os.arch").contains("64");
    static final long OFFSET = offset(Int.class, "value");

    static void replaceObject(final Object object, final Object replacement) {
        final long objectSize = sizeOf(object);
        final long objectAddress = addressOf(object);
        final long replacementSize = sizeOf(replacement);
        final long replacementAddress = addressOf(replacement);

        if (objectSize != replacementSize) {
            final long newAddress = theUnsafe.allocateMemory(replacementSize);

            theUnsafe.putAddress(objectAddress, newAddress);
            theUnsafe.copyMemory(replacementAddress, newAddress, replacementSize);
        } else {
            theUnsafe.copyMemory(replacementAddress, objectAddress, replacementSize);
        }
    }

    @SafeVarargs
    static <T> long addressOf(final T... singleton) {
        return addressOf(0, singleton);
    }

    @SafeVarargs
    static <T> long addressOf(final int index, final T... objects) {
        final long offset = theUnsafe.arrayBaseOffset(objects.getClass());
        final long scale = theUnsafe.arrayIndexScale(objects.getClass());

        return (theUnsafe.getInt(objects, offset + index * scale) & 0xFFFFFFFFL) * (x64 ? 8 : 1);
    }

    static long sizeOf(final Object object) {
        return sizeOf(object.getClass());
    }

    static long sizeOf(Class<?> klass) {
        long size = 0;

        while (klass != null) {
            final Field[] fields = klass.getDeclaredFields();

            if (fields.length != 0) {
                size = theUnsafe.objectFieldOffset(fields[fields.length - 1]) - OFFSET;
            }

            klass = klass.getSuperclass();
        }

        return size + 8 + OFFSET;
    }

    static Unsafe getTheUnsafe() {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");

            theUnsafe.setAccessible(true);

            return (Unsafe) theUnsafe.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Test
    void test() {
        final Int inter = new Int(123);
        final Int otherInter = new Int(897);
        final long address = addressOf(inter);
        final long otherAddress = addressOf(otherInter);
        replaceObject(inter, otherInter);
        replaceObject(inter, otherInter);

        return;
    }

    static class Int {
        int value;
        int thing;
        int otherthing;
        int one;
        int two;
        int three;

        Int(final int value) {
            this.value = value;
        }
    }

    static class Inter {
        int value;
    }

    static class Interest extends Inter {
        int thing;
    }

    static long offset(final Class<?> klass, final String field) {
        try {
            return theUnsafe.objectFieldOffset(klass.getDeclaredField(field));
        } catch (final NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }
}
