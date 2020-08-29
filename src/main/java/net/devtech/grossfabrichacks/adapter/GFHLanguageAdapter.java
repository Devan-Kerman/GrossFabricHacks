package net.devtech.grossfabrichacks.adapter;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GFHLanguageAdapter implements LanguageAdapter {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks");

    @Override
    public native <T> T create(net.fabricmc.loader.api.ModContainer mod, String value, Class<T> type);

    static {
        LOGGER.error("no good? no, this man is definitely up to evil.");

        final ReferenceArrayList<PrePrePreLaunch> entrypoints = ReferenceArrayList.wrap(new PrePrePreLaunch[0], 0);
        final ModContainer[] mods = FabricLoader.getInstance().getAllMods().toArray(new ModContainer[0]);
        final int modCount = mods.length;
        int i;
        int j;
        int entrypointCount;
        EntrypointMetadata[] modEntrypoints;

        for (i = 0; i < modCount; i++) {
            modEntrypoints = mods[i].getInfo().getEntrypoints("gfh:prePrePreLaunch").toArray(new EntrypointMetadata[0]);

            for (j = 0, entrypointCount = modEntrypoints.length; j < entrypointCount; j++) {
                try {
                    entrypoints.add((PrePrePreLaunch) Class.forName(modEntrypoints[j].getValue(), true, GFHLanguageAdapter.class.getClassLoader()).newInstance());
                } catch (final ClassNotFoundException exception) {
                    throw new IllegalArgumentException(String.format("class %s specified in the gfh:prePrePreLaunch entrypoint of mod %s does not exist", modEntrypoints[j].getValue(), mods[i].getMetadata().getName()), exception);
                } catch (final IllegalAccessException | InstantiationException exception) {
                    throw new IllegalStateException(String.format("class %s specified in the gfh:prePrePreLaunch entrypoint of mod %s cannot be instantiated", modEntrypoints[j].getValue(), mods[i].getMetadata().getName()), exception);
                }
            }
        }

        final PrePrePreLaunch[] entrypointArray = entrypoints.elements();
        entrypointCount = entrypoints.size();

        for (i = 0; i < entrypointCount; i++) {
            entrypointArray[i].onPrePrePreLaunch();
        }
    }
}
