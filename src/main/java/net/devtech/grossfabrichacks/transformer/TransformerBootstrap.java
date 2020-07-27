package net.devtech.grossfabrichacks.transformer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.transformers.TreeTransformer;

// todo caching in api?
public class TransformerBootstrap implements Opcodes {
	private TransformerBootstrap() {}

	static boolean shouldWrite;
	static RawClassTransformer preMixinRawClassTransformer;
	static RawClassTransformer postMixinRawClassTransformer;
	static AsmClassTransformer preMixinAsmClassTransformer;
	static AsmClassTransformer postMixinAsmClassTransformer;

	private static final Logger LOGGER = Logger.getLogger("Evil");
	private static final Method READ_CLASS;
	private static final Method WRITE_CLASS;
	private static final Field PROCESSOR;
	private static final Method APPLY_MIXINS;

	public static byte[] transformClass(String name, byte[] bytecode) {
		MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
		try {
			return transformClass(environment.getActiveTransformer(), environment, name, bytecode);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] transformClass(ClassNode node) {
		MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
		try {
			return transform(environment.getActiveTransformer(), environment, node, null);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] transformClass(Object mixinTransformer, MixinEnvironment environment, String name, byte[] classBytes) throws InvocationTargetException, IllegalAccessException {
		// patched from, and reflectionified from MixinTransformer#transformClass
		TreeTransformer transformer = (TreeTransformer) mixinTransformer;
		// raw class patching
		if (preMixinRawClassTransformer != null) {
			classBytes = preMixinRawClassTransformer.transform(name, classBytes);
		}
		// ASM patching
		ClassNode classNode = (ClassNode) READ_CLASS.invoke(transformer, (Object) classBytes);

		return transform(transformer, environment, classNode, classBytes);
	}

	public static byte[] transform(Object transformer, MixinEnvironment environment, ClassNode classNode, byte[] original) throws IllegalAccessException, InvocationTargetException {
		String n = classNode.name;
		if (preMixinAsmClassTransformer != null) {
			preMixinAsmClassTransformer.transform(n, classNode);
		}


		if (shouldWrite || (Boolean) APPLY_MIXINS.invoke(PROCESSOR.get(transformer), environment, n.replace('/', '.'), classNode)) {
			if(postMixinAsmClassTransformer != null) {
				postMixinAsmClassTransformer.transform(n, classNode);
			}

			// post mixin raw patching
			byte[] post = (byte[]) WRITE_CLASS.invoke(transformer, classNode);
			if(postMixinRawClassTransformer != null) {
				post = postMixinRawClassTransformer.transform(n, post);
			}
			return post;
		}
		return original;
	}

	static {
		try {
			READ_CLASS = TreeTransformer.class.getDeclaredMethod("readClass", byte[].class);
			READ_CLASS.setAccessible(true);
			WRITE_CLASS = TreeTransformer.class.getDeclaredMethod("writeClass", ClassNode.class);
			WRITE_CLASS.setAccessible(true);
			APPLY_MIXINS = MixinProcessor.class.getDeclaredMethod("applyMixins", MixinEnvironment.class, String.class, ClassNode.class);
			APPLY_MIXINS.setAccessible(true);

			// generate class in the same package as MixinTransformer
			// we can't just use a package hack and put this in the source
			// because our mod is loaded with a different classloader than MixinTransformer
			ClassNode node = new ClassNode();

			// we use a BiFunction of our class as it's an object that is made under our classloader, but BiFunction is loaded by the system classloader
			// so our HackedMixinTransformer that we have loaded in the other classloader can still see it and call it
			FieldNode field = new FieldNode(ACC_PUBLIC | ACC_STATIC, "TRANSFORMER_CALLBACK", "Ljava/util/function/BiFunction;", null, null);
			node.fields.add(field);

			// override the transformClass function and call our hook, this is package-private and synchronized, hence the need for the dynamic class
			// creation
			node.visit(V1_8,
			           ACC_PUBLIC,
			           "org/spongepowered/asm/mixin/transformer/HackedMixinTransformer",
			           null,
			           "org/spongepowered/asm/mixin/transformer/MixinTransformer",
			           null);
			MethodNode method = new MethodNode(ACC_PUBLIC,
			                                   "transformClass",
			                                   "(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B",
			                                   null,
			                                   null);
			method.visitCode();
			method.visitFieldInsn(GETSTATIC,
			                      "org/spongepowered/asm/mixin/transformer/HackedMixinTransformer",
			                      "TRANSFORMER_CALLBACK",
			                      "Ljava/util/function/BiFunction;");
			method.visitVarInsn(ALOAD, 2);
			method.visitVarInsn(ALOAD, 3);
			method.visitMethodInsn(INVOKEINTERFACE,
			                       "java/util/function/BiFunction",
			                       "apply",
			                       "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
			                       true);
			method.visitTypeInsn(CHECKCAST, "[B");
			method.visitInsn(ARETURN);
			method.visitEnd();
			node.methods.add(method);

			// write the class to the byte array
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			node.accept(writer);
			byte[] arr = writer.toByteArray();

			Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
			// get the processor reflection field for later
			PROCESSOR = mixinTransformerClass.getDeclaredField("processor");
			PROCESSOR.setAccessible(true);

			// define the class in the same classloader as MixinTransformer, so we can override it's package-private method
			ClassLoader loader = mixinTransformerClass.getClassLoader();
			Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass",
			                                                               String.class,
			                                                               byte[].class,
			                                                               int.class,
			                                                               int.class); // hack into defineClass(String,byte[],int,int)
			defineClassMethod.setAccessible(true);
			Class<?> hacked;
			try {
				hacked = (Class<?>) defineClassMethod.invoke(loader, "org.spongepowered.asm.mixin.transformer.HackedMixinTransformer", arr, 0, arr.length);
			} catch (Throwable error) { // sometimes the class will persist across relaunches of the JVM, I'm not sure why but I'm assuming it has
				// something to do with optimization, this is not an issue, we can just grab it from the system classloader
				LOGGER.info("HackedMixinTransformer already defined");
				hacked = Class.forName("org.spongepowered.asm.mixin.transformer.HackedMixinTransformer", false, loader);
			}

			// this is the bridge we setup earlier, now we just need to inject into it
			Field transformer_callbackField = hacked.getDeclaredField("TRANSFORMER_CALLBACK");
			transformer_callbackField.set(null, (BiFunction<String, byte[], byte[]>) TransformerBootstrap::transformClass);

			Object mixinTransformer = MixinEnvironment.getCurrentEnvironment()
			                                          .getActiveTransformer();
			LOGGER.info("MixinTransformer found! " + mixinTransformer);
			// here, we modify the klass pointer in the object to point towards the HackedMixinTransformer class, effectively turning the existing
			// MixinTransformer instance into
			// an instance of HackedMixinTransformer
			long klass = UnsafeUtil.getKlass(UnsafeUtil.allocateInstance(hacked));
			UnsafeUtil.unsafeCast(mixinTransformer, klass);
			LOGGER.info("Unsafe casted mixin transformer success!");
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}