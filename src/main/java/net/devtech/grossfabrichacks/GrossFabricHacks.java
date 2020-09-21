package net.devtech.grossfabrichacks;

import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.devtech.grossfabrichacks.reflection.AccessAllower;
import net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;

public class GrossFabricHacks implements LanguageAdapter {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks");

    public static final UnsafeKnotClassLoader UNSAFE_LOADER;

    @Override
    public native <T> T create(net.fabricmc.loader.api.ModContainer mod, String value, Class<T> type);

    static {
        LOGGER.error("no good? no, this man is definitely up to evil.");
        AccessAllower.init();
        UNSAFE_LOADER = LoaderUnsafifier.unsafifyLoader(Thread.currentThread().getContextClassLoader());

        for (PrePrePreLaunch entrypoint : FabricLoader.getInstance()
                                             .getEntrypoints("gfh:prePrePreLaunch", PrePrePreLaunch.class)) {
            entrypoint.onPrePrePreLaunch();
        }
    }
}
