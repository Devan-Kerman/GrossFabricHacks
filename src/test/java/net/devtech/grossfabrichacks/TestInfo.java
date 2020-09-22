package net.devtech.grossfabrichacks;

public class TestInfo {
    public final String format;
    public final int iterations;
    public final ThrowingIntConsumer test;

    public TestInfo(final int iterations, final ThrowingIntConsumer test) {
        this.format = null;
        this.iterations = iterations;
        this.test = test;
    }

    public TestInfo(final String format, final int iterations, final ThrowingIntConsumer test) {
        this.format = format;
        this.iterations = iterations;
        this.test = test;
    }
}
