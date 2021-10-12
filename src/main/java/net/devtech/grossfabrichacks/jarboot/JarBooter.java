package net.devtech.grossfabrichacks.jarboot;

import net.devtech.grossfabrichacks.Rethrower;
import net.fabricmc.loader.impl.launch.knot.UnsafeKnotClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JarBooter {
	private static final Method ADD_URL;

	/**
	 * Add a URL to the KnotClassLoader, the KnotClassLoader first checks its URLs before asking pub_parent classloaders for classes,
	 * this allows you to replace library classes, and mix into them (that'll take some creativity on your part until a better api is made)
	 */
	public static void addUrl(final URL url) {
		try {
			ADD_URL.invoke(UnsafeKnotClassLoader.parent, url);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw Rethrower.rethrow(e);
		}
	}

	static {
		try {
			ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		} catch (Throwable throwable) {
			throw Rethrower.rethrow(throwable);
		}
	}
}
