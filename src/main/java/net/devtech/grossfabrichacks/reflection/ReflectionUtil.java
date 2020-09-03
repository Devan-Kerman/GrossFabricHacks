package net.devtech.grossfabrichacks.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.objectweb.asm.Opcodes;

public class ReflectionUtil {
    private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();

    public static final boolean JAVA_9 = isVersion(9);

    public static boolean isVersion(final int version) {
        final String string = System.getProperty("java.version");

        return string.indexOf('.') > 1 ? Integer.parseUnsignedInt(string.substring(0, 2)) >= version : Integer.parseUnsignedInt(string.substring(2, 3)) >= version;
    }

    public static <T> T getDeclaredFieldValue(final String klass, final String name, final Object object) {
        try {
            return (T) getDeclaredField(klass, name).get(object);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T getDeclaredFieldValue(final String klass, final String name) {
        try {
            return (T) getDeclaredField(klass, name).get(null);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T getDeclaredFieldValue(final Class<?> klass, final String name, final Object object) {
        try {
            return (T) getDeclaredField(klass, name).get(object);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T getDeclaredFieldValue(final Class<?> klass, final String name) {
        try {
            return (T) getDeclaredField(klass, name).get(null);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Field getDeclaredField(final String klass, final String name) {
        try {
            return getDeclaredField(Class.forName(klass, false, LOADER), name);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Field getDeclaredField(final Class<?> klass, final String name) {
        try {
            final Field field = klass.getDeclaredField(name);

            if ((field.getModifiers() & Opcodes.ACC_STATIC) != 0) {
                final Field modifiers = getDeclaredField(Field.class, "modifiers");

                modifiers.setInt(field, field.getModifiers() & ~Opcodes.ACC_STATIC);
            }

            return field;
        } catch (final IllegalAccessException | NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Method getDeclaredMethod(final String klass, final String name) {
        return getDeclaredMethod(klass, name, new Class[0]);
    }

    public static Method getDeclaredMethod(final String klass, final String name, final String... parameterTypes) {
        final int parameterCount = parameterTypes.length;
        final Class<?>[] parameterClasses = new Class<?>[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            try {
                parameterClasses[i] = Class.forName(parameterTypes[i], false, LOADER);
            } catch (final ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }
        }

        return getDeclaredMethod(klass, name, parameterClasses);
    }

    public static Method getDeclaredMethod(final String klass, final String name, final Class<?>... parameterTypes) {
        try {
            return getDeclaredMethod(Class.forName(klass, false, LOADER), name, parameterTypes);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Method getDeclaredMethod(final Class<?> klass, final String name, final Class<?>... parameterTypes) {
        try {
            final Method method = klass.getDeclaredMethod(name, parameterTypes);

            method.setAccessible(true);

            return method;
        } catch (final NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T invoke(final Method method, final Object object) {
        return invoke(method, object, new Object[0]);
    }

    public static <T> T invoke(final Method method, final Object object, final Object... arguments) {
        try {
            return (T) method.invoke(object, arguments);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }
}
