package net.devtech.grossfabrichacks.unsafe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoaderUnsafifier {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks/LoaderUnsafifier");

    public static <T extends ClassLoader> T unsafifyLoader(final ClassLoader victim) {
        final String superName = victim.getClass().getName();

        LOGGER.warn("{}, you fool! Loading me was a grave mistake.", superName.substring(superName.lastIndexOf('.') + 1).replace('$', '.'));

        return UnsafeUtil.defineAndInitializeAndUnsafeCast(victim, "net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader", victim.getClass().getClassLoader());
    }
}
