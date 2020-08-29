package net.devtech.grossfabrichacks.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.objectweb.asm.Opcodes;

public class ReflectionUtil {
    private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();

    private static final String JAVA_LANG_MODULE = "java.lang.Module";
    private static final String JAVA_LANG_STRING = "java.lang.String";

    private static final Method getDeclaredFields0;

    private static Method addExports;
    private static Method getModule;
    private static Method implAddExportsOrOpens;


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
        final Field[] fields = invoke(getDeclaredFields0, klass, false);
        final int fieldCount = fields.length;

        for (int i = 0; i < fieldCount; i++) {
            fields[i].setAccessible(true);
        }

        return fields;
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

    public static <T> T invoke(final Method method, final Object object, final Object... arguments) {
        try {
            return (T) method.invoke(object, arguments);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> Class<T> forName(final String name, final boolean initialize, final ClassLoader loader) {
        try {
            return (Class<T>) Class.forName(name, initialize, loader);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> Class<T> forName(final String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Object getModule(final Class<?> klass) {
        if (getModule == null) {
            getModule = getDeclaredMethod("java.lang.Class", "getModule");
        }

        return invoke(getModule, klass);
    }

    public static Object addExports(final Object module, final String packageName, final Object other) {
        if (addExports == null) {
            addExports = getDeclaredMethod(JAVA_LANG_MODULE, "addExports", JAVA_LANG_STRING, JAVA_LANG_MODULE);
        }

        return invoke(addExports, module, packageName, other);
    }

    public static void implAddExportsOrOpens(final Object module, final String pn, final Object other, final boolean open, final boolean syncVM) {
        if (implAddExportsOrOpens == null) {
            implAddExportsOrOpens = getDeclaredMethod(JAVA_LANG_MODULE, "implAddExportsOrOpens", forName(JAVA_LANG_STRING), forName(JAVA_LANG_MODULE), boolean.class, boolean.class);
        }
    }

    static {
        getDeclaredFields0 = getDeclaredMethod(Class.class, "getDeclaredFields0", boolean.class);
    }
}
