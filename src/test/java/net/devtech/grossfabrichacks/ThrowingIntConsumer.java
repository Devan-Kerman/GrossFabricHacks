package net.devtech.grossfabrichacks;

@FunctionalInterface
public interface ThrowingIntConsumer {
    void accept(int i) throws Throwable;
}
