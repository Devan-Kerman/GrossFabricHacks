package net.devtech.grossfabrichacks;

@SuppressWarnings("unchecked")
public class Rethrower {
    public static  <T extends Throwable> T rethrow(Throwable t) throws T {
        throw (T) t;
    }
}
