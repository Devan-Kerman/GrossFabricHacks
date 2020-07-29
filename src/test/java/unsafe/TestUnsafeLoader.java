package unsafe;

import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;

class TestUnsafeLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        final Class<?> klass = this.findLoadedClass(name);

        if (klass != null) {
            if (resolve) {
                this.resolveClass(klass);
            }

            return klass;
        }

        try {
            return super.loadClass(name);
        } catch (final ClassFormatError error) {
            return UnsafeUtil.getObject(ClassLoader.class, "delegate");
        }
    }
}
