package net.devtech.grossfabrichacks.bootstrap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.FabricMixinTransformerProxy;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.transformers.TreeTransformer;

public class ClassBootstrap implements Opcodes {
	private ClassBootstrap() {}

	private static final Logger LOGGER = Logger.getLogger("Evil");
	public static final Method READ_CLASS;
	public static final Method WRITE_CLASS;
	public static final Field PROCESSOR;
	public static final Method APPLY_MIXINS;

	@Retention(RetentionPolicy.CLASS)
	@interface Hint {String value();}

	@Hint("only those intelligent enough to mixin into the right place here get to wield such power")
	public static byte[] transformClass(Object mixinTransformer, MixinEnvironment environment, String name, byte[] classBytes) throws InvocationTargetException, IllegalAccessException {
		TreeTransformer transformer = (TreeTransformer) mixinTransformer;
		// todo now turn this into an api, allowing mods to mixin to this for now
		ClassNode classNode = (ClassNode) READ_CLASS.invoke(transformer, (Object) classBytes);
		if ((Boolean) APPLY_MIXINS.invoke(PROCESSOR.get(transformer), environment, name, classNode)) {
			return (byte[]) WRITE_CLASS.invoke(transformer, classNode);
		}
		return classBytes;
	}

	static {
		try {
			READ_CLASS = TreeTransformer.class.getDeclaredMethod("readClass", byte[].class);
			READ_CLASS.setAccessible(true);
			WRITE_CLASS = TreeTransformer.class.getDeclaredMethod("writeClass", ClassNode.class);
			WRITE_CLASS.setAccessible(true);
			APPLY_MIXINS = MixinProcessor.class.getDeclaredMethod("applyMixins", MixinEnvironment.class, String.class, ClassNode.class);
			APPLY_MIXINS.setAccessible(true);


			ClassNode node = new ClassNode();
			node.visit(V1_8, ACC_PUBLIC, "org/spongepowered/asm/mixin/transformer/HackedMixinTransformer", null,"org/spongepowered/asm/mixin/transformer/MixinTransformer", null);
			MethodNode method = new MethodNode(ACC_PUBLIC | ACC_SYNCHRONIZED, "transformClass","(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B", null, null);
			method.visitCode();
			method.visitVarInsn(ALOAD, 0);
			method.visitVarInsn(ALOAD, 1);
			method.visitVarInsn(ALOAD, 2);
			method.visitVarInsn(ALOAD, 3);
			method.visitMethodInsn(INVOKESTATIC, "net/devtech/grossfabrichacks/bootstrap/ClassBootstrap", "transformClass","(Ljava/lang/Object;Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B", false);
			method.visitInsn(ARETURN);
			method.visitEnd();
			node.methods.add(method);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			node.accept(writer);
			byte[] arr = writer.toByteArray();
			Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
			PROCESSOR = mixinTransformerClass.getDeclaredField("processor");
			PROCESSOR.setAccessible(true);
			ClassLoader loader = mixinTransformerClass.getClassLoader();
			Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
			defineClassMethod.setAccessible(true);
			Class<?> hacked;
			try {
				hacked = (Class<?>) defineClassMethod.invoke(loader, "org.spongepowered.asm.mixin.transformer.HackedMixinTransformer", arr, 0, arr.length);
			} catch (Throwable error) {
				LOGGER.info("HackedMixinTransformer already defined");
				hacked = Class.forName("org.spongepowered.asm.mixin.transformer.HackedMixinTransformer", false, loader);
			}

			Object mixinTransformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
			LOGGER.info("MixinTransformer found! " + mixinTransformer);

			long klass = UnsafeUtil.getKlass(UnsafeUtil.UNSAFE.allocateInstance(hacked));
			UnsafeUtil.unsafeCast(mixinTransformer, klass);
			LOGGER.info("Unsafe casted mixin transformer success!");
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static void init() {}
}