package net.devtech.grossfabrichacks;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.reflect.InvocationTargetException;
import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.devtech.grossfabrichacks.reflection.AccessAllower;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.devtech.grossfabrichacks.unsafe.LoaderUnsafifier;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.launch.knot.UnloadableClassLoader;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

public class GrossFabricHacks implements LanguageAdapter {
    private static final Logger LOGGER = LogManager.getLogger("GrossFabricHacks");

    public static final String INTERNAL_NAME = "net/devtech/grossfabrichacks/GrossFabricHacks";

    public static final UnsafeKnotClassLoader UNSAFE_LOADER;

    public static boolean mixinLoaded;

    @Override
    public native <T> T create(net.fabricmc.loader.api.ModContainer mod, String value, Class<T> type);

    private static void transform(String name, ClassNode klass) {
        int thing = 1;

        System.out.println(thing);
    }

    static {
        LOGGER.error("no good? no, this man is definitely up to evil.");

        AccessAllower.init();

        UNSAFE_LOADER = LoaderUnsafifier.unsafifyLoader(Thread.currentThread().getContextClassLoader());

        UnloadableClassLoader.UNLOADABLE_CLASSES.put("net/minecraft/client/MinecraftClient", null);

        TransformerApi.registerPostMixinAsmClassTransformer(GrossFabricHacks::transform);

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
                    final Class<?> klass = Class.forName(modEntrypoints[j].getValue());

                    if (PrePrePreLaunch.class.isAssignableFrom(klass)) {
                        entrypoints.add((PrePrePreLaunch) klass.getConstructor().newInstance());
                    }
                } catch (final ClassNotFoundException exception) {
                    throw new IllegalArgumentException(String.format("class %s specified in the gfh:prePrePreLaunch entrypoint of mod %s does not exist", modEntrypoints[j].getValue(), mods[i].getMetadata().getName()), exception);
                } catch (final IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
                    throw new IllegalStateException(String.format("class %s specified in the gfh:prePrePreLaunch entrypoint of mod %s cannot be instantiated", modEntrypoints[j].getValue(), mods[i].getMetadata().getName()), exception);
                } catch (final InvocationTargetException exception) {
                    throw new RuntimeException(String.format("an error was encountered during the execution of the gfh:prePrePreLaunch entrypoint of class %s", modEntrypoints[j].getValue()));
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
