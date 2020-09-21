package net.devtech.grossfabrichacks.instrumentation;

import java.lang.instrument.Instrumentation;

class InstrumentationAgent {
    private static Instrumentation instrumentation;

    public static void agentmain(final String argument, final Instrumentation instrumentation) {
        InstrumentationAgent.instrumentation = instrumentation;
    }
}
