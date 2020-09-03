package net.devtech.grossfabrichacks.unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import net.devtech.grossfabrichacks.reflection.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * works across all normal JVMs I think
 */
public class UnsafeUtil {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/UnsafeUtil");

    public static final Class<?> CLASS = getUnsafeClass();
    public static final String CLASS_NAME = CLASS.getName();
    public static final Object theUnsafe = getTheUnsafe();

    public static final MethodHandles.Lookup IMPL_LOOKUP = ReflectionUtil.getDeclaredFieldValue(MethodHandles.Lookup.class, "IMPL_LOOKUP");

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

    private static final MethodHandle getInt = getHandle("getInt", int.class, Object.class, long.class);
    private static final MethodHandle getLong = getHandle("getLong", long.class, Object.class, long.class);
    private static final MethodHandle getObject = getHandle("getObject", Object.class, Object.class, long.class);
    private static final MethodHandle getAndSetInt = getHandle("getAndSetInt", int.class, Object.class, long.class, int.class);
    private static final MethodHandle getAndAddInt = getHandle("getAndAddInt", int.class, Object.class, long.class, int.class);
    private static final MethodHandle getAndSetLong = getHandle("getAndSetLong", long.class, Object.class, long.class, long.class);
    private static final MethodHandle putInt = getHandle("putInt", void.class, Object.class, long.class, int.class);
    private static final MethodHandle putLong = getHandle("putLong", void.class, Object.class, long.class, long.class);
    private static final MethodHandle putObject = getHandle("putObject", void.class, Object.class, long.class, Object.class);
    private static final MethodHandle putObjectVolatile = getHandle("putObjectVolatile", void.class, Object.class, long.class, Object.class);
    private static final MethodHandle objectFieldOffset = getHandle("objectFieldOffset", long.class, Field.class);
    private static final MethodHandle staticFieldOffset = getHandle("staticFieldOffset", long.class, Field.class);
    private static final MethodHandle arrayBaseOffset = getHandle("arrayBaseOffset", int.class, Class.class);
    private static final MethodHandle arrayIndexScale = getHandle("arrayIndexScale", int.class, Class.class);
    private static final MethodHandle allocateMemory = getHandle("allocateMemory", long.class, long.class);
    private static final MethodHandle copyMemory0 = getHandle("copyMemory", void.class, Object.class, long.class, Object.class, long.class, long.class);
    private static final MethodHandle copyMemory1 = getHandle("copyMemory", void.class, long.class, long.class, long.class);
    private static final MethodHandle allocateInstance = getHandle("allocateInstance", Object.class, Class.class);
    private static final MethodHandle defineClassHandle = getDefineClassHandle();

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
            throw new RuntimeException(exception);
        }
    }

    public static void putInt(final Class<?> klass, final Object object, final String field, final int value) {
        try {
            putInt(object, objectFieldOffset(klass.getDeclaredField(field)), value);
        } catch (final NoSuchFieldException exception) {
            throw new RuntimeException(exception);
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

    /**
     * @param name           get method {@code name} from the Unsafe class returned by {@link #getUnsafeClass}
     * @param parameterTypes the parameter types of {@code name}
     * @return the Unsafe method with the specified name and parameter types.
     */
    public static MethodHandle getHandle(final String name, final Class<?> returnType, final Class<?>... parameterTypes) {
        try {
            return IMPL_LOOKUP.findVirtual(CLASS, name, MethodType.methodType(returnType, parameterTypes));
        } catch (final NoSuchMethodException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Unsafe#getInt
     */
    public static int getInt(final Object object, final long offset) {
        try {
            return (int) getInt.invoke(theUnsafe, object, offset);
        } catch (final Throwable exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Unsafe#getLong
     */
    public static long getLong(final Object object, final long offset) {
        try {
            return (long) getLong.invoke(theUnsafe, object, offset);
        } catch (final Throwable exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Unsafe#getObject
     */
    public static Object getObject(final Object object, final long offset) {
        return invoke(getObject, theUnsafe, object, offset);
    }

    public static int getAndSetInt(final Object object, final long offset, final int value) {
        return invoke(getAndSetInt, theUnsafe, object, offset, value);
    }

    public static int getAndAddInt(final Object object, final long offset, final int value) {
        return invoke(getAndAddInt, theUnsafe, object, offset, value);
    }

    public static long getAndSetLong(final Object object, final long offset, final long value) {
        return invoke(getAndSetLong, theUnsafe, object, offset, value);
    }

    public static void putInt(final Object object, final long offset, final int value) {
        invoke(putInt, theUnsafe, object, offset, value);
    }

    public static void putLong(final Object object, final long offset, final long value) {
        invoke(putLong, theUnsafe, object, offset, value);
    }

    public static void putObject(final Object owner, final long offset, final Object value) {
        invoke(putObject, theUnsafe, owner, offset, value);
    }

    public static void putObjectVolatile(final Object owner, final long offset, final Object value) {
        invoke(putObjectVolatile, theUnsafe, owner, offset, value);
    }

    public static long objectFieldOffset(final Field field) {
        return invoke(objectFieldOffset, theUnsafe, field);
    }

    public static long staticFieldOffset(final Field field) {
        return invoke(staticFieldOffset, theUnsafe, field);
    }

    public static <T> T allocateInstance(final Class<?> klass) {
        return invoke(allocateInstance, theUnsafe, klass);
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass) {
        return defineClass(binaryName, klass, null, null);
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass, final ClassLoader loader) {
        return defineClass(binaryName, klass, loader, null);
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass,
                                           final ClassLoader loader, final ProtectionDomain protectionDomain) {
        return invoke(defineClassHandle, theUnsafe, binaryName, klass, 0, klass.length, loader, protectionDomain);
    }

    public static int arrayBaseOffset(final Class<?> arrayClass) {
        return invoke(arrayBaseOffset, theUnsafe, arrayClass);
    }

    public static int arrayIndexScale(final Class<?> arrayClass) {
        return invoke(arrayIndexScale, theUnsafe, arrayClass);
    }

    public static long allocateMemory(final long bytes) {
        return invoke(allocateMemory, theUnsafe, bytes);
    }

    public static void copyMemory(final long srcAddress, final long destAddress, final long bytes) {
        invoke(copyMemory1, theUnsafe, srcAddress, destAddress, bytes);
    }

    public static void copyMemory(final Object src, final long srcAddress, final Object dest, final long destAddress, final long bytes) {
        invoke(copyMemory0, theUnsafe, src, srcAddress, dest, destAddress, bytes);
    }

    public static <T> T invoke(final MethodHandle method, final Object... arguments) {
        try {
            return (T) method.invokeWithArguments(arguments);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static Object getTheUnsafe() {
        try {
            final Constructor<?> theConstructor = CLASS.getDeclaredConstructor();

            theConstructor.setAccessible(true);

            return theConstructor.newInstance();
        } catch (final IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Class<?> getUnsafeClass() {
        try {
            return Class.forName("jdk.internal.misc.Unsafe");
        } catch (final ClassNotFoundException bad) {
            try {
                return Class.forName("sun.misc.Unsafe");
            } catch (final ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private static MethodHandle getDefineClassHandle() {
        try {
            return IMPL_LOOKUP.findVirtual(CLASS, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class));
        } catch (final NoSuchMethodException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> Class<T> getClass(final String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> Class<T> getClass(final String name, final boolean initialize, final ClassLoader loader) {
        try {
            return (Class<T>) Class.forName(name, initialize, loader);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static class FirstInt {
        public int val;
    }

    static {
        LOGGER.info("UnsafeUtil init!");

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
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
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