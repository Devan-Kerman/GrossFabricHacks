package net.devtech.grossfabrichacks.jarboot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;

public class JarBooter {
	private static final Method ADD_URL;

	/**
	 * Add a URL to the KnotClassLoader, the KnotClassLoader first checks it's URLs before asking parent classloaders for classes,
	 * this allows you to replace library classes, and mix into them (that'll take some creativity on your part until a better api is made)
	 */
	public static void addUrl(final URL url) {
		try {
			ADD_URL.invoke(UnsafeKnotClassLoader.parent, url);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		try {
			ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}
}
