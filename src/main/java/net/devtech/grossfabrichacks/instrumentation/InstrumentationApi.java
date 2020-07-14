package net.devtech.grossfabrichacks.instrumentation;

import static net.devtech.grossfabrichacks.transformer.TransformerBootstrap.transformClass;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.devtech.grossfabrichacks.GrossFabricHacks;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class InstrumentationApi {
	private static final Set<String> TRANSFORMABLE = new HashSet<>();
	/**
	 * adds a transformer that pipes a class through TransformerBootstrap,
	 * this allows you to mix into any all classes (including JDK!) classes, with a few caveats.
	 *
	 * If the class was loaded *before* you get to it, do not call this method!
	 *
	 * don't pipe classes that may already be transformable by mixin, or they may be called twice.
	 *
	 * @param className null if you want to transform JDK classes, the name must come in dot notation, so java.lang.Object
	 */
	public static void pipeClassThroughTransformerBootstrap(String className) {
		TRANSFORMABLE.add(className);
		DontLoad.init();
	}

	public static void retransform(Class<?> cls, AsmClassTransformer transformer) {
		retransform(cls, transformer.asRaw());
	}

	/**
	 * retransform a class, className may be null if it's a JDK class
	 */
	public static void retransform(Class<?> cls, RawClassTransformer transformer) {
		Instrumentation instrumentation = getInstrumentation();
		try {
			ClassFileTransformer fileTransformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
				if(cls.equals(classBeingRedefined)) {
					return transformer.transform(className, classfileBuffer);
				}
				return classfileBuffer;
			};

			instrumentation.addTransformer(fileTransformer, true);
			instrumentation.retransformClasses(cls);
			instrumentation.removeTransformer(fileTransformer);
		} catch (UnmodifiableClassException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * get an instance of Instrumentation, capable of redefinition and redefining of classes.
	 */
	public static Instrumentation getInstrumentation() {
		try {
			return (Instrumentation) Class.forName("gross.agent.InstrumentationAgent")
			                              .getDeclaredField("instrumentation")
			                              .get(null);
		} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Logger LOGGER = Logger.getLogger("InstrumentationApi");

	static {
		File grossHackFolder = new File("gross_hacks");
		//noinspection ResultOfMethodCallIgnored
		grossHackFolder.mkdirs();
		try {
			File grossJar = new File(grossHackFolder, "gross_agent.jar");
			if (!grossJar.exists()) {
				LOGGER.info("no gross_agent.jar found, cloning new one");
				unpack("/jars/gross_agent.jar", grossJar);
			} else {
				LOGGER.info("gross_agent.jar located!");
			}

			String name = ManagementFactory.getRuntimeMXBean()
			                               .getName();
			LOGGER.info("VM name: " + name);
			String pid = name.substring(0, name.indexOf('@'));
			LOGGER.info("VM PID: " + pid);
			LOGGER.info("Attaching to VM");
			try {
				ByteBuddyAgent.attach(grossJar, pid);
			} catch (Throwable t) {
				if(Desktop.isDesktopSupported() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
					JOptionPane.showConfirmDialog(null, "Attention Filthy Linux User, please append -XX:+StartAttachListener to your JVM args!");
				} else {
					for (int i = 0; i < 32; i++) {
						LOGGER.severe("FILTHY LINUX DETECTED: MISSING JVM ARGUMENT '-XX:+StartAttachListener'!!!");
					}

					throw t;
				}
			}
		} catch (Throwable e) {
			LOGGER.severe("error in attaching agent to JVM");
			throw new RuntimeException(e);
		}
	}

	private static void unpack(String path, File file) throws IOException {
		try (InputStream stream = GrossFabricHacks.class.getResourceAsStream(path)) {
			try (FileOutputStream out = new FileOutputStream(file)) {
				byte[] arr = new byte[2048];
				int len;
				while ((len = stream.read(arr)) != -1) {
					out.write(arr, 0, len);
				}
			}
		}
	}


	// to seperate out the static block
	private static class DontLoad {
		static {
			// pipe transformer to
			TransformerApi.manualLoad();
			Instrumentation instrumentation = InstrumentationApi.getInstrumentation();
			instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
				if(TRANSFORMABLE.remove(className)) {
					return transformClass(className,
					               classfileBuffer);
				}
				return classfileBuffer;
			});
			LOGGER.info("Instrumentation<->TransformerBootstrap pipe successfully established!");
		}

		private static void init() {}
	}
}
