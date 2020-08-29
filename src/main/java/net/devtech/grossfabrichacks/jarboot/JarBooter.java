package net.devtech.grossfabrichacks.jarboot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;

public class JarBooter {
	private static final URLClassLoader DYNAMIC_CLASS_LOADER;
	private static final Method ADD_URL;

	static {
		try {
			Field urlLoaderField = UnsafeKnotClassLoader.SUPERCLASS.getDeclaredField("urlLoader");
			urlLoaderField.setAccessible(true);
			ADD_URL = urlLoaderField.getType().getDeclaredMethod("addURL", URL.class);
			ADD_URL.setAccessible(true);
			DYNAMIC_CLASS_LOADER = (URLClassLoader) urlLoaderField.get(GrossFabricHacks.UNSAFE_LOADER);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Add a URL to the KnotClassLoader, the KnotClassLoader first checks it's URLs before asking parent classloaders for classes,
	 * this allows you to replace library classes, and mix into them (that'll take some creativity on your part until a better api is made)
	 */
	public static void addUrl(URL url) {
		try {
			ADD_URL.invoke(DYNAMIC_CLASS_LOADER, url);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
