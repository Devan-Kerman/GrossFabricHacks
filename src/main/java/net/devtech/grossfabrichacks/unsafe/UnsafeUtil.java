package net.devtech.grossfabrichacks.unsafe;

import net.devtech.grossfabrichacks.Rethrower;
import net.gudenau.lib.unsafe.Unsafe;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.ProtectionDomain;

/**
 * works across all normal JVMs I think
 */
public class UnsafeUtil extends Unsafe {
    // constants
    public static final boolean x64;
    public static final int addressFactor;
    public static final long FIELD_OFFSET;
    public static final long BYTE_ARR_KLASS;
    public static final long SHORT_ARR_KLASS;
    public static final long CHAR_ARR_KLASS;
    public static final long INT_ARR_KLASS;
    public static final long LONG_ARR_KLASS;
    public static final long FLOAT_ARR_KLASS;
    public static final long DOUBLE_ARR_KLASS;
    public static final long KLASS_OFFSET;
    public static final boolean EIGHT_BYTE_KLASS;
    public static final long CLASS_KLASS_OFFSET;

    private static final long FIRST_INT_KLASS;

    /**
     * set the first 4 bytes of an object to something, this can be used to mutate the size of an array
     */
    public static void setFirstInt(Object object, int val) {
        long orig = getKlass(object);
        FirstInt firstInt = unsafeCast(object, FIRST_INT_KLASS);
        firstInt.val = val;
        unsafeCast(object, orig);
    }

    /**
     * Convert an array of primitives of a smaller type into one of a larger type, for example
     * to go from a byte array to an int array you would use this. careful, this directly modifies the klass value
     * in the array, it does not copy it
     *
     * <b>Reflection.upcastArray(byte_array, Reflection.INT_ARR_KLASS, 4)</b>
     *
     * @param array      the original array
     * @param newType    the target type
     * @param conversion the conversion factor, for example an int has 2 shorts so to go from a short array to an int array it would be 2
     * @param <T>        the returned array type
     * @return a non-copied casted array
     */
    public static <T> T upcastArray(Object array, int newType, int conversion) {
        FirstInt wrapper = unsafeCast(array, FIRST_INT_KLASS);
        wrapper.val /= conversion;
        return unsafeCast(array, newType);
    }

    /**
     * Convert an array of primitives of a larger type into one of a smaller type, for example
     * to go from an int array to an byte array you would do, careful, this directly modifies the klass value
     * in the array, it does not copy it.
     * <b>Reflection.downcastArray(int_array, Reflection.BYTE_ARR_KLASS, 4)</b>
     *
     * @param array      the original array
     * @param newType    the target type
     * @param conversion the conversion factor, for example an short has 1/2 ints so to go from an int array to a short array it would be 2
     * @param <T>        the returned array type
     * @return a non-copied casted array
     */
    public static <T> T downcastArray(Object array, int newType, int conversion) {
        FirstInt wrapper = unsafeCast(array, FIRST_INT_KLASS);
        wrapper.val *= conversion;
        return unsafeCast(array, newType);
    }

    /**
     * casts the array to a different type of array without copying it,
     * all the classes inside the array should be an instance of the B class
     * you should recast it to it's original type after you have used it!
     *
     * @param obj    the original array
     * @param bClass the class that each of the elements are expected to be
     * @param <B>    the desired type of the array
     */
    public static <B> B[] arrayCast(Object[] obj, Class<B> bClass) {
        return arrayCast(obj, getKlass(Array.newInstance(bClass, 0)));
    }

    /**
     * casts the array with the class' klass value without copying it, obtained from Reflection#getKlass(Class)
     * you should recast it to it's original type after you have used it!
     *
     * @param obj        the array to be casted
     * @param classKlass the integer klass value
     * @param <B>        the desired type
     * @see UnsafeUtil#getKlass(Object)
     */
    public static <B> B[] arrayCast(Object[] obj, long classKlass) {
        if (EIGHT_BYTE_KLASS) {
            getAndSetLong(obj, KLASS_OFFSET, classKlass);
        } else {
            getAndAddInt(obj, KLASS_OFFSET, (int) classKlass);
        }

        return (B[]) obj;
    }

    public static <B> B defineAndInitializeAndUnsafeCast(final Object object, final String klass, final ClassLoader loader) {
        return unsafeCast(object, getKlassFromClass(findAndDefineAndInitializeClass(klass, loader)));
    }

    public static <B> B unsafeCast(final Object object, final String klass) {
        return unsafeCast(object, loadClass(klass));
    }

    public static <B> B unsafeCast(final Object object, final Class<?> klass) {
        return unsafeCast(object, getKlassFromClass(klass));
    }

    /**
     * casts the object with the class' klass value without copying it, obtained from Reflection#getKlass(Class)
     * recast to original type or stack corruption may occur!
     *
     * @param object     the object to be casted
     * @param klassValue the integer klass value
     * @param <B>        the desired type
     * @see UnsafeUtil#getKlass(Object)
     */
    public static <B> B unsafeCast(Object object, long klassValue) {
        if (EIGHT_BYTE_KLASS) {
            getAndSetLong(object, KLASS_OFFSET, klassValue);
        } else {
            getAndSetInt(object, KLASS_OFFSET, (int) (klassValue));
        }

        return (B) object;
    }

    /**
     * gets the klass value from an object
     *
     * @param cls an instance of the class to obtain the klass value from
     */
    public static long getKlass(Object cls) {
        if (EIGHT_BYTE_KLASS) {
            return getLong(cls, KLASS_OFFSET);
        }

        return getInt(cls, KLASS_OFFSET);
    }

    /**
     * get the klass pointer of a class, only works on instantiatable classes
     */
    public static long getKlassFromClass(Class<?> type) {
        return getKlass(allocateInstance(type));
    }

    /**
     * get the klass value from a class
     *
     * @deprecated doesn't work, idk why todo fix
     */
    @Deprecated
    public static long getKlassFromClass0(Class<?> type) {
        if (EIGHT_BYTE_KLASS) {
            return getLong(type, CLASS_KLASS_OFFSET);
        }

        return getInt(type, CLASS_KLASS_OFFSET);
    }

    public static void putInt(final Object object, final String field, final int value) {
        try {
            putInt(object, objectFieldOffset(object.getClass().getDeclaredField(field)), value);
        } catch (final NoSuchFieldException exception) {
            throw Rethrower.rethrow(exception);
        }
    }

    public static void putInt(final Class<?> klass, final Object object, final String field, final int value) {
        try {
            putInt(object, objectFieldOffset(klass.getDeclaredField(field)), value);
        } catch (final NoSuchFieldException exception) {
            throw Rethrower.rethrow(exception);
        }
    }

    public static <T> T getObject(final long address) {
        final Object[] box = new Object[1];
        final long baseOffset = arrayBaseOffset(Object[].class);

        putLong(box, baseOffset, address);

        return (T) box[0];
    }

    public static long addressOf(final Object object) {
        return addressOf(0, object);
    }

    public static long addressOf(final int index, final Object... objects) {
        final long offset = arrayBaseOffset(objects.getClass());
        final long scale = arrayIndexScale(objects.getClass());

        return (getInt(objects, offset + index * scale) & 0xFFFFFFFL) * addressFactor;
    }

    public static <T> Class<T> defineAndInitialize(final String binaryName, final byte[] klass) {
        return defineAndInitialize(binaryName, klass, null, null);
    }

    public static <T> Class<T> defineAndInitialize(final String binaryName, final byte[] klass, final ClassLoader loader) {
        return defineAndInitialize(binaryName, klass, loader, null);
    }

    public static <T> Class<T> defineAndInitialize(final String binaryName, final byte[] bytecode,
                                                   final ClassLoader loader, final ProtectionDomain protectionDomain) {
        final Class<?> klass;

        ensureClassInitialized(klass = defineClass(binaryName, bytecode, 0, bytecode.length, loader, protectionDomain));

        return (Class<T>) klass;
    }

    public static <T> Class<T> initialiizeClass(final Class<?> klass) {
        ensureClassInitialized(klass);

        return (Class<T>) klass;
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass) {
        return defineClass(binaryName, klass, 0, klass.length, null, null);
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass, final ClassLoader loader) {
        return defineClass(binaryName, klass, 0, klass.length, loader, null);
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass,
                                           final ClassLoader loader, final ProtectionDomain protectionDomain) {
        return defineClass(binaryName, klass, 0, klass.length, loader, protectionDomain);
    }

    public static <T> Class<T> findAndDefineClass(final String binaryName, final ClassLoader loader) {
        return defineClass(binaryName, findClass(binaryName), loader);
    }

    public static byte[] findClass(final String binaryName) {
        try {
            final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(binaryName.replace('.', '/') + ".class");
            final byte[] bytecode = new byte[stream.available()];

            while (stream.read(bytecode) != -1) {}

            return bytecode;
        } catch (final Throwable throwable) {
            throw Rethrower.rethrow(throwable);
        }
    }

    public static <T> Class<T> findAndDefineAndInitializeClass(final String binaryName, final ClassLoader loader) {
        try {
            return initialiizeClass(findAndDefineClass(binaryName, loader));
        } catch (final Throwable throwable) {
            throw Rethrower.rethrow(throwable);
        }
    }

    public static <T> Class<T> loadClass(final String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (final ClassNotFoundException exception) {
            throw Rethrower.rethrow(exception);
        }
    }

    public static <T> Class<T> loadClass(final String name, final boolean initialize, final ClassLoader loader) {
        try {
            return (Class<T>) Class.forName(name, initialize, loader);
        } catch (final ClassNotFoundException exception) {
            throw Rethrower.rethrow(exception);
        }
    }

    public static class FirstInt {
        public int val;
    }

    static {
        try {
            FIELD_OFFSET = objectFieldOffset(FirstInt.class.getField("val"));

            if (FIELD_OFFSET == 8) { // 32bit jvm
                x64 = false;
                KLASS_OFFSET = FIELD_OFFSET - 4;
                EIGHT_BYTE_KLASS = false;
                CLASS_KLASS_OFFSET = 80;
            } else if (FIELD_OFFSET == 12) { // 64bit jvm with compressed OOPs
                x64 = true;
                KLASS_OFFSET = FIELD_OFFSET - 4;
                EIGHT_BYTE_KLASS = false;
                CLASS_KLASS_OFFSET = 84;
            } else if (FIELD_OFFSET == 16) { // 64bit jvm
                x64 = true;
                KLASS_OFFSET = FIELD_OFFSET - 8;
                EIGHT_BYTE_KLASS = true;
                CLASS_KLASS_OFFSET = 160;
            } else {
                throw new UnsupportedOperationException("klass casting not supported!");
            }

            addressFactor = x64 ? 8 : 1;
        } catch (final Throwable throwable) {
            throw Rethrower.rethrow(throwable);
        }

        FIRST_INT_KLASS = getKlass(new FirstInt());
        BYTE_ARR_KLASS = getKlass(new byte[0]);
        SHORT_ARR_KLASS = getKlass(new short[0]);
        CHAR_ARR_KLASS = getKlass(new char[0]);
        INT_ARR_KLASS = getKlass(new int[0]);
        LONG_ARR_KLASS = getKlass(new long[0]);
        FLOAT_ARR_KLASS = getKlass(new float[0]);
        DOUBLE_ARR_KLASS = getKlass(new double[0]);
    }
}