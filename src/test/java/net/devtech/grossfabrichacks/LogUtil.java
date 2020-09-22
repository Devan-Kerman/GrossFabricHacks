package net.devtech.grossfabrichacks;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

public class LogUtil {
    public static void logMeanTime(final TestInfo... tests) {
        final DoubleArrayList times = new DoubleArrayList();

        for (final TestInfo test : tests) {
            times.add(meanTime(test));
        }

        for (int i = 0, length = tests.length; i < length; i++) {
            final TestInfo test = tests[i];

            if (test.format == null) {
                printfln(times.getDouble(i));
            } else {
                printfln(test.format, times.getDouble(i));
            }
        }
    }

    public static void logTime(final TestInfo... tests) {
        final DoubleArrayList times = new DoubleArrayList();

        for (final TestInfo test : tests) {
            times.add(time(test));
        }

        for (int i = 0, length = tests.length; i < length; i++) {
            final TestInfo test = tests[i];

            if (test.format == null) {
                printfln(times.getDouble(i));
            } else {
                printfln(test.format, times.getDouble(i));
            }
        }
    }

    public static void logTime(final int iterations, final ThrowingIntConsumer test) {
        printfln(time(iterations, test));
    }

    public static void logMeanTime(final int iterations, final ThrowingIntConsumer test) {
        printfln(meanTime(iterations, test));
    }

    public static void logTime(final String format, final int iterations, final ThrowingIntConsumer test) {
        printfln(format, time(iterations, test));
    }

    public static void logMeanTime(final String format, final int iterations, final ThrowingIntConsumer test) {
        printfln(format, meanTime(iterations, test));
    }

    public static double meanTime(final TestInfo test) {
        return meanTime(test.iterations, test.test);
    }

    public static double time(final TestInfo test) {
        return time(test.iterations, test.test);
    }

    public static double meanTime(final int iterations, final ThrowingIntConsumer test) {
        return time(iterations, test) / iterations;
    }

    public static double time(final int iterations, final ThrowingIntConsumer test) {
        final long start = System.nanoTime();

        try {
            for (int i = 0; i < iterations; i++) {
                test.accept(i);
            }
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        return (System.nanoTime() - start) / 1000000000D;
    }

    public static void printfln(final Object format, final Object... arguments) {
        System.out.printf(format + "%n", arguments);
    }
}
