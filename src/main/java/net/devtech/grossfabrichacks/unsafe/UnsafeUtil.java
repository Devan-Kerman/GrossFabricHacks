package net.devtech.grossfabrichacks.unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * works across all normal JVMs I think
 */
public class UnsafeUtil {
    private static final Logger LOGGER = Logger.getLogger("UnsafeUtil");
    // constants
    public static final boolean x64;
    public static final int addressFactor;
    private static final Field LOOKUP_CLASS_ALLOWED_MODES_FIELD;
    private static final long FIRST_INT_KLASS;
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
        //noinspection unchecked
        return (B[]) arrayCast(obj, getKlass(Array.newInstance(bClass, 0)));
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
            Unsafe.getAndSetLong(obj, KLASS_OFFSET, classKlass);
        } else {
            Unsafe.getAndAddInt(obj, KLASS_OFFSET, (int) classKlass);
        }

        //noinspection unchecked
        return (B[]) obj;
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
            Unsafe.getAndSetLong(object, KLASS_OFFSET, klassValue);
        } else {
            Unsafe.getAndSetInt(object, KLASS_OFFSET, (int) (klassValue));
        }

        //noinspection unchecked
        return (B) object;
    }

    /**
     * gets the klass value from an object
     *
     * @param cls an instance of the class to obtain the klass value from
     */
    public static long getKlass(Object cls) {
        if (EIGHT_BYTE_KLASS)
            return Unsafe.getLong(cls, KLASS_OFFSET);
        else
            return Unsafe.getInt(cls, KLASS_OFFSET);
    }

    /**
     * get the klass pointer of a class, only works on instantiatable classes
     */
    public static long getKlassFromClass(Class<?> type) {
        return getKlass(Unsafe.allocateInstance(type));
    }

    /**
     * get the klass value from a class
     *
     * @deprecated doesn't work, idk why todo fix
     */
    @Deprecated
    public static long getKlassFromClass0(Class<?> type) {
        if (EIGHT_BYTE_KLASS) {
            return Unsafe.getLong(type, CLASS_KLASS_OFFSET);
        }

        return Unsafe.getInt(type, CLASS_KLASS_OFFSET);
    }

    public static void putInt(final Object object, final String field, final int value) {
        try {
            Unsafe.putInt(object, Unsafe.objectFieldOffset(object.getClass().getDeclaredField(field)), value);
        } catch (final NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void putInt(final Class<?> klass, final Object object, final String field, final int value) {
        try {
            Unsafe.putInt(object, Unsafe.objectFieldOffset(klass.getDeclaredField(field)), value);
        } catch (final NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T getObject(final long address) {
        final Object[] box = new Object[1];
        final long baseOffset = Unsafe.arrayBaseOffset(Object[].class);

        Unsafe.putLong(box, baseOffset, address);

        //noinspection unchecked
        return (T) box[0];
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass) {
        return defineClass(binaryName, klass, null);
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass, final ClassLoader loader) {
        return Unsafe.defineClass(binaryName, klass, 0, klass.length, loader, null);
    }

    public static long addressOf(final Object object) {
        return addressOf(0, object);
    }

    public static long addressOf(final int index, final Object... objects) {
        final long offset = Unsafe.arrayBaseOffset(objects.getClass());
        final long scale = Unsafe.arrayIndexScale(objects.getClass());

        return (Unsafe.getInt(objects, offset + index * scale) & 0xFFFFFFFL) * addressFactor;
    }

    public static void setAccessible(MethodHandles.Lookup lookup) throws IllegalAccessException {
        LOOKUP_CLASS_ALLOWED_MODES_FIELD.setInt(lookup, 15);
    }

    public static class FirstInt {
        public int val;
    }


    static {
        LOGGER.info("UnsafeUtil init!");
        try {
            // todo does not exist in Java 14
            // some random field or something
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            // todo does not exist in Java 14
            Field allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            allowedModes.setAccessible(true);
            int modifiers = allowedModes.getModifiers();
            // todo pleasefix hardcoded value
            modifiersField.setInt(allowedModes, modifiers & -17);
            LOOKUP_CLASS_ALLOWED_MODES_FIELD = allowedModes;

            FIELD_OFFSET = Unsafe.objectFieldOffset(FirstInt.class.getField("val"));

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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
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