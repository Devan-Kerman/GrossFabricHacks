package net.fabricmc.loader.launch.knot;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * a class loader that is inteded to be used for only a single class<br>
 * so that it and its defined class may be garbage collected when desired,<br>
 * allowing more than just method bodies to be redefined.
 */
public class UnloadableClassLoader extends UnsafeKnotClassLoader {
    public static final Object2ReferenceOpenHashMap<String, UnloadableClassLoader> UNLOADABLE_CLASSES = new Object2ReferenceOpenHashMap<>(0, 0.75F);

    public final String name;
    public final byte[] bytecode;
    public final ClassNode classNode;
    public final Class<?> klass;

    public UnloadableClassLoader(final String name) {
        super(DEVELOPMENT, ENVIRONMENT, PROVIDER);

        final byte[] bytecode = this.getDelegate().getPostMixinClassByteArray(name);
        final ClassReader reader = new ClassReader(bytecode);
        final ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getClassLoader0", "()Ljava/lang/ClassLoader;", null, null);
        method.visitFieldInsn(Opcodes.GETSTATIC, GrossFabricHacks.INTERNAL_NAME, "UNSAFE_LOADER", UnsafeKnotClassLoader.INTERNAL_NAME);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(1, 1);

        node.methods.add(method);

        final ClassWriter writer = new ClassWriter(0);
        node.accept(writer);

        this.name = name;
        this.classNode = node;
        UnsafeKnotClassLoader.UNSAFE_CLASSES.put(name, this.klass = UnsafeUtil.defineClass(name, this.bytecode = writer.toByteArray()));
    }

    public UnloadableClassLoader(final String name, final byte[] bytecode) {
        super(DEVELOPMENT, ENVIRONMENT, PROVIDER);

        final ClassReader reader = new ClassReader(bytecode);

        this.name = name;
        reader.accept(this.classNode = new ClassNode(), 0);
        this.klass = UnsafeUtil.defineClass(name, this.bytecode = bytecode);
    }

    public UnloadableClassLoader(final String name, final ClassNode classNode) {
        super(DEVELOPMENT, ENVIRONMENT, PROVIDER);

        final ClassNode node = new ClassNode();
        final ClassWriter writer = new ClassWriter(0);
        node.accept(writer);

        this.name = name;
        this.classNode = node;
        this.klass = UnsafeUtil.defineClass(name, this.bytecode = writer.toByteArray());
    }

    public static Class<?> define(final String name) {
        final UnloadableClassLoader loader = new UnloadableClassLoader(name);

        UNLOADABLE_CLASSES.put(name, loader);

        return loader.klass;
    }

    public static void unload(final Class<?> klass) {
        unload(klass.getName());
    }

    public static void unload(final String name) {
        final UnloadableClassLoader loader = UNLOADABLE_CLASSES.remove(name);

        try {
            KnotClassDelegate.class.getDeclaredField("itf").set(loader.getDelegate(), null);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Class<?> retransform(final String name, final RawClassTransformer transformer) {
        final UnloadableClassLoader loader = new UnloadableClassLoader(name, transformer.transform(name, UNLOADABLE_CLASSES.get(name).bytecode));

        unload(name);

        UNLOADABLE_CLASSES.put(name, loader);

        return loader.klass;
    }

    public static Class<?> retransform(final String name, final AsmClassTransformer transformer) {
        final ClassNode node;
        transformer.transform(name, node = UNLOADABLE_CLASSES.get(name).classNode);
        final UnloadableClassLoader loader = new UnloadableClassLoader(name, node);

        unload(name);

        UNLOADABLE_CLASSES.put(name, loader);

        return loader.klass;
    }

    public static Class<?> retransform(final Class<?> klass, final RawClassTransformer transformer) {
        final String name = klass.getName();
        final UnloadableClassLoader loader = new UnloadableClassLoader(name, transformer.transform(name, UNLOADABLE_CLASSES.get(name).bytecode));

        unload(name);

        UNLOADABLE_CLASSES.put(name, loader);

        return loader.klass;
    }

    public static Class<?> retransform(final Class<?> klass, final AsmClassTransformer transformer) {
        final String name = klass.getName();
        final ClassNode node;
        transformer.transform(name, node = UNLOADABLE_CLASSES.get(name).classNode);
        final UnloadableClassLoader loader = new UnloadableClassLoader(name, node);

        unload(name);

        UNLOADABLE_CLASSES.put(name, loader);

        return loader.klass;
    }
}
