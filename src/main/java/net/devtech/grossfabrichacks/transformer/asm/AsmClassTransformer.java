package net.devtech.grossfabrichacks.transformer.asm;

import org.objectweb.asm.tree.ClassNode;

/**
 * a transformer using the ASM node
 */
public interface AsmClassTransformer {
	void transform(String name, ClassNode node);

	default AsmClassTransformer andThen(AsmClassTransformer fixer) {
		return (s, c) -> {
			this.transform(s, c);
			fixer.transform(s, c);
		};
	}
}
