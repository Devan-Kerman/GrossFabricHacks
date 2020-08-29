package net.devtech.grossfabrichacks.adapter;

import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GFHLanguageAdapter implements LanguageAdapter {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks");

    @Override
    public native <T> T create(ModContainer mod, String value, Class<T> type);

    static {
        LOGGER.error("no good? no, this man is definitely up to evil.");

        final EntrypointContainer<PrePrePreLaunch>[] entrypoints = FabricLoader.getInstance().getEntrypointContainers("gfh:prePrePreLaunch", PrePrePreLaunch.class).toArray(new EntrypointContainer[0]);
        final int entrypointCount = entrypoints.length;

        for (int i = 0; i < entrypointCount; i++) {
            entrypoints[i].getEntrypoint().onPrePrePreLaunch();
        }
    }
}
