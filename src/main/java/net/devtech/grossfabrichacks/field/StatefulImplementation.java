package net.devtech.grossfabrichacks.field;

import java.util.function.Consumer;

public class StatefulImplementation implements StatefulInterface {
    @Getter("test")
    public native Consumer<Integer> getTest();

    @Setter("test")
    public native void setTest(Consumer<Integer> test);

    public String getString() {
        return String.format("");
    }

    public void dontGetString() {
        String.format("");
    }
}
