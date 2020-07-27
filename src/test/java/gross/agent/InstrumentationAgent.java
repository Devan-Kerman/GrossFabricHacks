package gross.agent;

import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {
	public static Instrumentation instrumentation;

	public static void agentmain(String argument,
	                             Instrumentation instrumentation) {
		InstrumentationAgent.instrumentation = instrumentation;
	}
}
