package net.devtech.grossfabrichacks.transformer.asm;

/**
 * an interface for transforming class bytecode given the raw bytecode
 *
 * raw bytecode access is powerful, even more than raw ASM for some usecases,
 * for example if you want to redirect every method in a class, this can be done
 * by replacing the reference to that method in the constant pool, meaning you don't need
 * to go through every method call in ASM.
 */
public interface RawClassTransformer {
	/**
	 * transform the byte data however you want
	 * @param name the internal name of the class
	 * @param data the bytecode of the class
	 * @return the same, or a newly created byte array of the class.
	 */
	byte[] transform(String name, byte[] data);

	default RawClassTransformer andThen(RawClassTransformer transfomer) {
		return (s, d) -> {
			d = this.transform(s, d);
			return transfomer.transform(s, d);
		};
	}
}
