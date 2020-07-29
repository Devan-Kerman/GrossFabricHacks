package net.devtech.grossfabrichacks.unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class Unsafe {
    public static final Class<?> CLASS = getKlass();
    public static final String CLASS_NAME = CLASS.getName();

    public static final Object theUnsafe = getTheUnsafe();

    private static final Method getInt = getMethod("getInt", Object.class, long.class);
    private static final Method getLong = getMethod("getLong", Object.class, long.class);
    private static final Method getObject = getMethod("getObject", Object.class, long.class);
    private static final Method getAndSetInt = getMethod("getAndSetInt", Object.class, long.class, int.class);
    private static final Method getAndAddInt = getMethod("getAndAddInt", Object.class, long.class, int.class);
    private static final Method getAndSetLong = getMethod("getAndSetLong", Object.class, long.class, long.class);
    private static final Method putInt = getMethod("putInt", Object.class, long.class, int.class);
    private static final Method putLong = getMethod("putLong", Object.class, long.class, long.class);
    private static final Method objectFieldOffset = getMethod("objectFieldOffset", Field.class);
    private static final Method arrayBaseOffset = getMethod("arrayBaseOffset", Class.class);
    private static final Method arrayIndexScale = getMethod("arrayIndexScale", Class.class);
    private static final Method allocateMemory = getMethod("allocateMemory", long.class);
    private static final Method copyMemory0 = getMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);
    private static final Method copyMemory1 = getMethod("copyMemory", long.class, long.class, long.class);
    private static final Method defineClass = getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
    private static final Method allocateInstance = getMethod("allocateInstance", Class.class);

    /**
     * @param name           get method {@code name} from the Unsafe class returned by {@link #getKlass}
     * @param parameterTypes the parameter types of {@code name}
     * @return the Unsafe method with the specified name and parameter types.
     */
    public static Method getMethod(final String name, final Class<?>... parameterTypes) {
        try {
            return CLASS.getDeclaredMethod(name, parameterTypes);
        } catch (final NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Unsafe#getInt
     */
    public static int getInt(final Object object, final long offset) {
        try {
            return (int) getInt.invoke(theUnsafe, object, offset);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Unsafe#getLong
     */
    public static long getLong(final Object object, final long offset) {
        try {
            return (long) getLong.invoke(theUnsafe, object, offset);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Unsafe#getObject
     */
    public static Object getObject(final Object object, final long offset) {
        try {
            return getObject.invoke(theUnsafe, object, offset);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static int getAndSetInt(final Object object, final long offset, final int value) {
        try {
            return (int) getAndSetInt.invoke(theUnsafe, object, offset, value);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static int getAndAddInt(final Object object, final long offset, final int value) {
        try {
            return (int) getAndAddInt.invoke(theUnsafe, object, offset, value);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static long getAndSetLong(final Object object, final long offset, final long value) {
        try {
            return (long) getAndSetLong.invoke(theUnsafe, object, offset, value);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void putInt(final Object object, final long offset, final int value) {
        try {
            putInt.invoke(theUnsafe, object, offset, value);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void putLong(final Object object, final long offset, final long value) {
        try {
            putLong.invoke(theUnsafe, object, offset, value);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static long objectFieldOffset(final Field field) {
        try {
            return (long) objectFieldOffset.invoke(theUnsafe, field);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T allocateInstance(final Class<?> klass) {
        try {
            //noinspection unchecked
            return (T) allocateInstance.invoke(theUnsafe, klass);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> Class<T> defineClass(final String binaryName, final byte[] klass, final int offset, final int length,
                                           final ClassLoader loader, final ProtectionDomain protectionDomain) {
        try {
            //noinspection unchecked
            return (Class<T>) defineClass.invoke(theUnsafe, binaryName, klass, offset, length, loader, protectionDomain);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

//    /**
//     * return a new object with the same field values as those in {@code object}
//     *
//     * @param object the object to clone
//     * @param <T> the type of {@code object}
//     * @return the clone
//     */
//    public static <T> T clone(final T object) {
//        final long size = InstrumentationApi.INSTRUMENTATION.getObjectSize(object);
//        final long cloneAddress = allocateMemory(size);
//
//        copyMemory(addressOf(object), cloneAddress, size);
//
//        //noinspection unchecked
//        return (T) getObject(cloneAddress);
//    }

    public static int arrayBaseOffset(final Class<?> arrayClass) {
        try {
            return (int) arrayBaseOffset.invoke(theUnsafe, arrayClass);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static int arrayIndexScale(final Class<?> arrayClass) {
        try {
            return (int) arrayIndexScale.invoke(theUnsafe, arrayClass);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static long allocateMemory(final long bytes) {
        try {
            return (long) allocateMemory.invoke(theUnsafe, bytes);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyMemory(final long srcAddress, final long destAddress, final long bytes) {
        try {
            copyMemory1.invoke(theUnsafe, srcAddress, destAddress, bytes);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void copyMemory(final Object src, final long srcAddress, final Object dest, final long destAddress, final long bytes) {
        try {
            copyMemory0.invoke(theUnsafe, src, srcAddress, dest, destAddress, bytes);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Object getTheUnsafe() {
        try {
            final Field theField = CLASS.getDeclaredField("theUnsafe");

            theField.setAccessible(true);

            return theField.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Class<?> getKlass() {
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
}
