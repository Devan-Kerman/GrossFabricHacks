package net.devtech.grossfabrichacks.bootstrap;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * works across all normal JVMs
 */
public class UnsafeUtil {
	// constants
	private static final Field LOOKUP_CLASS_ALLOWED_MODES_FIELD;
	public static final Unsafe UNSAFE;
	private static final long FIRST_INT_KLASS;
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

	static {
		try {
			// unsafe
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			UNSAFE = (Unsafe) f.get(null);

			// some random field or something
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			Field allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
			allowedModes.setAccessible(true);
			int modifiers = allowedModes.getModifiers();
			modifiersField.setInt(allowedModes, modifiers & -17);
			LOOKUP_CLASS_ALLOWED_MODES_FIELD = allowedModes;

			long offset = UNSAFE.objectFieldOffset(FirstInt.class.getField("val"));
			if (offset == 8) { // 32bit jvm
				KLASS_OFFSET = offset - 4;
				EIGHT_BYTE_KLASS = false;
				CLASS_KLASS_OFFSET = 80;
			} else if (offset == 12) { // 64bit jvm with compressed OOPs
				KLASS_OFFSET = offset - 4;
				EIGHT_BYTE_KLASS = false;
				CLASS_KLASS_OFFSET = 84;
			} else if (offset == 16) { // 64bit jvm
				KLASS_OFFSET = offset - 8;
				EIGHT_BYTE_KLASS = true;
				CLASS_KLASS_OFFSET = 160;
			} else {
				throw new UnsupportedOperationException("klass casting not supported!");
			}
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

	public static int getFirstInt(Object object) {
		long orig = getKlass(object);
		FirstInt first = unsafeCast(object, FIRST_INT_KLASS);
		unsafeCast(object, orig);
		return first.val;
	}

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
	 * @param array the original array
	 * @param newType the target type
	 * @param conversion the conversion factor, for example an int has 2 shorts so to go from a short array to an int array it would be 2
	 * @param <T> the returned array type
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
	 * @param array the original array
	 * @param newType the target type
	 * @param conversion the conversion factor, for example an short has 1/2 ints so to go from an int array to a short array it would be 2
	 * @param <T> the returned array type
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
	 * @param obj the original array
	 * @param bClass the class that each of the elements are expected to be
	 * @param <A> the original type of the array
	 * @param <B> the desired type of the array
	 */
	public static <A, B> B[] arrayCast(A[] obj, Class<B> bClass) {
		return (B[]) arrayCast(obj, getKlass(Array.newInstance(bClass, 0)));
	}

	/**
	 * casts the array with the class' klass value without copying it, obtained from Reflection#getKlass(Class)
	 * you should recast it to it's original type after you have used it!
	 *
	 * @param obj the array to be casted
	 * @param classKlass the integer klass value
	 * @param <A> the type of the array
	 * @param <B> the desired type
	 * @see UnsafeUtil#getKlass(Object)
	 */
	public static <A, B> B[] arrayCast(A[] obj, long classKlass) {
		if (EIGHT_BYTE_KLASS) {
			UNSAFE.getAndSetLong(obj, KLASS_OFFSET, classKlass);
		} else {
			UNSAFE.getAndAddInt(obj, KLASS_OFFSET, (int) classKlass);
		}
		return (B[]) obj;
	}

	/**
	 * casts the object with the class' klass value without copying it, obtained from Reflection#getKlass(Class)
	 * recast to original type or stack corruption may occur!
	 *
	 * @param object the object to be casted
	 * @param klassValue the integer klass value
	 * @param <A> the type of the object
	 * @param <B> the desired type
	 * @see UnsafeUtil#getKlass(Object)
	 */
	public static <A, B> B unsafeCast(A object, long klassValue) {
		if (EIGHT_BYTE_KLASS) {
			UNSAFE.getAndSetLong(object, KLASS_OFFSET, klassValue);
		} else {
			UNSAFE.getAndSetInt(object, KLASS_OFFSET, (int) (klassValue));
		}
		return (B) object;
	}

	/**
	 * gets the klass value from an object
	 *
	 * @param cls an instance of the class to obtain the klass value from
	 */
	public static long getKlass(Object cls) {
		if (EIGHT_BYTE_KLASS)
			return UNSAFE.getLong(cls, KLASS_OFFSET);
		else
			return UNSAFE.getInt(cls, KLASS_OFFSET);
	}

	/**
	 * get the klass value from a class
	 */
	public static long getKlassFromClass(Class<?> type) {
		if (EIGHT_BYTE_KLASS)
			return UNSAFE.getLong(type, CLASS_KLASS_OFFSET);
		else
			return UNSAFE.getInt(type, CLASS_KLASS_OFFSET);
	}


	public static class FirstInt {
		public int val;
	}

	public static void setAccessible(MethodHandles.Lookup lookup) throws IllegalAccessException {
		LOOKUP_CLASS_ALLOWED_MODES_FIELD.setInt(lookup, 15);
	}
}