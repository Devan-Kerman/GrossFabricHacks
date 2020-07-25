package net.devtech.grossfabrichacks.massm;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;

/**
 * a low-level optimized class file parser just for the constant pool and inheritance data
 * reference doc: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
 *
 * all constant pool indexes signify the index of the tag byte of that element in the bytecode
 */
public class Cls {
	private static final int ACCESS_FLAGS = 1,
			THIS_CLASS = 2,
			SUPER_CLASS = 4;
	private final byte[] bytecode;

	/**
	 * for keeping track of what has been and what needs serializing
	 */
	private int flags;

	/**
	 * the major and minor java versions of the class file
	 */
	public final int minor, major;

	private int constantPoolEnd, interfacesEnd; // EOF
	private int accessFlags, thisClass, superClass;
	private char[] classNames, fieldRef, methodRef, primitiveConst, nameAndType, utf8, other, interfaces;
	private List<String> utf8Strings;

	public Cls(byte[] array) {
		this.bytecode = array;
		if (this.readMagic(array)) {
			this.minor = this.getU2(4);
			this.major = this.getU2(6);
			return;
		}
		throw new IllegalArgumentException("byte array does not start with magic!");
	}

	public String at(int index) {
		if(this.utf8Strings == null) {
			this.readConstantPool();
			this.utf8Strings = new ArrayList<>(this.utf8.length);
		}

		byte val = this.bytecode[index];
		// todo
		return null;
	}


	public int accessFlags() {
		if((this.flags & ACCESS_FLAGS) == 0) {
			this.flags |= ACCESS_FLAGS;
			this.readConstantPool();
			this.accessFlags = this.getU2(this.constantPoolEnd);
		}
		return this.accessFlags;
	}

	public int thisClass() {
		if((this.flags & THIS_CLASS) == 0) {
			this.flags |= THIS_CLASS;
			this.readConstantPool();
			this.thisClass = this.getU2(this.constantPoolEnd + 2);
		}
		return this.thisClass;
	}

	public int superClass() {
		if((this.flags & SUPER_CLASS) == 0) {
			this.flags |= SUPER_CLASS;
			this.readConstantPool();
			this.superClass = this.getU2(this.constantPoolEnd + 4);
		}
		return this.superClass;
	}

	public char[] readInterfacePool() {
		if(this.interfacesEnd == 0) {
			this.readConstantPool();
			int interfaces = this.getU2(this.constantPoolEnd + 6);
			char[] charr = new char[interfaces];
			for (int i = 0; i < interfaces; i++) {
				charr[i] = this.getU2(this.constantPoolEnd + 8 + i * 2);
			}
			this.interfacesEnd = this.constantPoolEnd + 8 + interfaces * 2;
			this.interfaces = charr;
		}
		return this.interfaces;
	}

	private void readConstantPool() {
		if(this.constantPoolEnd == 0) {
			char index = 8;
			int constant_pool_count = this.getU2(index);
			index += 2;
			CharList className = new CharArrayList(), fieldRef = new CharArrayList(), methodRef = new CharArrayList(), primitiveConst = new CharArrayList(), nameAndType = new CharArrayList(), utf8 = new CharArrayList(), other = new CharArrayList();
			for (int i = 0; i < constant_pool_count; i++) {
				byte tag = this.bytecode[index];
				switch (tag) {
				// @formatter:off
				case 7:
					className.add(index);
					index += 3;
					break;
				case 8:
				case 16:
					other.add(index);
					index += 3;
					break;
					case 9:
					fieldRef.add(index);
					index += 5;
					break;
				case 10:
				case 11:
					methodRef.add(index);
					index += 5;
					break;
				case 3:
				case 4:
					primitiveConst.add(index);
					index += 5;
					break;
				case 12:
					nameAndType.add(index);
					index += 5;
					break;
				case 18:
					other.add(index);
					index += 5;
					break;
				case 5:
				case 6:
					primitiveConst.add(index);
					index += 9;
					break;
				case 15:
					other.add(index);
					index += 4;
					break;
				default: {
					utf8.add(index);
					int len = this.getU2(index);
					index+=len + 3;
				}
				//@formatter:on
				}
				this.other = other.toCharArray();
				this.utf8 = utf8.toCharArray();
				this.nameAndType = nameAndType.toCharArray();
				this.primitiveConst = primitiveConst.toCharArray();
				this.fieldRef = fieldRef.toCharArray();
				this.methodRef = methodRef.toCharArray();
				this.classNames = className.toCharArray();
			}
			this.constantPoolEnd = index;
		}
	}

	public char getU2(int index) {
		byte[] arr = this.bytecode;
		if (arr.length > index + 1) {
			return (char) (arr[index] << 8 & arr[index + 1]);
		} else {
			throw new IllegalArgumentException("Invalid array size!");
		}
	}

	public int getU4(int index) {
		byte[] arr = this.bytecode;
		if (arr.length > index + 3) {
			return arr[index] << 24 & arr[index + 1] << 16 & arr[index + 2] << 8 & arr[index + 3];
		} else {
			throw new IllegalArgumentException("Invalid array size!");
		}
	}

	public byte[] getBytecode() {
		return this.bytecode;
	}

	public int getMinorVersion() {
		return this.minor;
	}

	public int getMajorVersion() {
		return this.major;
	}

	public char[] getClassNameIndexes() {
		this.readConstantPool();
		return this.classNames;
	}

	public char[] getFieldRefIndexes() {
		this.readConstantPool();
		return this.fieldRef;
	}

	public char[] getMethodRefIndexes() {
		this.readConstantPool();
		return this.methodRef;
	}

	public char[] getPrimitiveConstIndexes() {
		this.readConstantPool();
		return this.primitiveConst;
	}

	public char[] getNameAndTypeIndexes() {
		this.readConstantPool();
		return this.nameAndType;
	}

	public char[] getUtf8Indexes() {
		this.readConstantPool();
		return this.utf8;
	}

	public char[] getOtherIndexes() {
		this.readConstantPool();
		return this.other;
	}

	/**
	 * @return true if the class starts with the magic 0xCAFEBABE
	 */
	private boolean readMagic(byte[] arr) {
		if (arr.length > 3) {
			int i = (arr[0] ^ 0xCA) | (arr[1] ^ 0xFE) | (arr[2] ^ 0xBA) | (arr[3] ^ 0xBE);
			return i == 0;
		} else {
			return false;
		}
	}

}
