package net.devtech.grossfabrichacks.adapter;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GFHLanguageAdapter implements LanguageAdapter {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks");

    @Override
    public native <T> T create(ModContainer mod, String value, Class<T> type);

    static {
        LOGGER.error("no good? no, this man is definitely up to evil.");

        final ModContainer[] mods = FabricLoader.getInstance().getAllMods().toArray(new ModContainer[0]);
        final int modCount = mods.length;
        CustomValue prePrePreLaunchValue;

        for (int i = 0; i < modCount; i++) {
            prePrePreLaunchValue = mods[i].getMetadata().getCustomValue("gfh:prePrePreLaunch");

            if (prePrePreLaunchValue != null) {
                if (tryLoadClass(mods[i], prePrePreLaunchValue)) {
                    if (prePrePreLaunchValue.getType() == CustomValue.CvType.ARRAY) {
                        for (final CustomValue element : prePrePreLaunchValue.getAsArray()) {
                            if (tryLoadClass(mods[i], element)) {
                                throw new IllegalArgumentException(String.format("a non-string value was found in the gfh:prePrePreLaunch array in the Fabric configuration file of mod %s", mods[i].getMetadata().getName()));
                            }
                        }
                    } else {
                        throw new IllegalArgumentException(String.format("the value of gfh:prePrePreLaunch in the Fabric configuration file of mod %s is not a binary class name or an array of binary class names", mods[i].getMetadata().getName()));
                    }
                }
            }
        }
    }

    private static boolean tryLoadClass(final ModContainer mod, final CustomValue value) {
        if (value.getType() == CustomValue.CvType.STRING) {
            try {
                Class.forName(value.getAsString(), true, GFHLanguageAdapter.class.getClassLoader());
            } catch (final ClassNotFoundException exception) {
                throw new IllegalArgumentException(String.format("class %s specified in the Fabric configuration file of mod %s does not exist", value.getAsString(), mod.getMetadata().getName()));
            }

            return false;
        }

        return true;
    }
}
