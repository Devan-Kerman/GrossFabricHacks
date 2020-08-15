package net.devtech.grossfabrichacks.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.objectweb.asm.Opcodes;

public class ReflectionUtil {
    private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();

    private static final Method getDeclaredFields0 = getDeclaredMethod(Class.class, "getDeclaredFields0", boolean.class);

    public static <T> T getDeclaredFieldValue(final String klass, final String name, final Object object) {
        try {
            return (T) getDeclaredField(klass, name).get(object);
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

    public static Field getDeclaredField(final String klass, final String name) {
        try {
            return getDeclaredField(Class.forName(klass, false, LOADER), name);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Field getDeclaredField(final Class<?> klass, final String name) {
        final Field[] fields = getDeclaredFields0(klass);
        final int fieldCount = fields.length;

        for (int i = 0; i < fieldCount; i++) {
            if (fields[i].getName().equals(name)) {
                final Field field = fields[i];

                field.setAccessible(true);

                if ((field.getModifiers() & Opcodes.ACC_STATIC) != 0) {
                    final Field modifiers = getDeclaredField(Field.class, "modifiers");

                    try {
                        modifiers.setInt(field, field.getModifiers() & ~Opcodes.ACC_STATIC);
                    } catch (final IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                return field;
            }
        }

        throw new RuntimeException(String.format("field %s was not found in %s", name, klass.getName()));
    }

    public static Field[] getDeclaredFields0(final String klass) {
        try {
            return getDeclaredFields0(Class.forName(klass, false, LOADER));
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Field[] getDeclaredFields0(final Class<?> klass) {
        try {
            final Field[] fields = (Field[]) getDeclaredFields0.invoke(klass, false);
            final int fieldCount = fields.length;

            for (int i = 0; i < fieldCount; i++) {
                fields[i].setAccessible(true);
            }

            return fields;
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
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
}
