package net.devtech.grossfabrichacks.reflection;

import java.lang.invoke.MethodHandle;

public class SimpleMethodHandle {
    public final MethodHandle delegate;

    /**
     * @param delegate the backing {@link MethodHandle}
     */
    public SimpleMethodHandle(final MethodHandle delegate) {
        this.delegate = delegate;
    }

    public native <T> T invoke(final Object... arguments);
}
