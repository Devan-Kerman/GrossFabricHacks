package net.devtech.grossfabrichacks;

import java.util.logging.Logger;

import net.devtech.grossfabrichacks.transformer.TransformerBootstrap;

public enum GrossFabricHacks {;
	private static final Logger LOGGER = Logger.getLogger("Fabric-Transformer");
	static {
		LOGGER.severe("no good? no, this man is definitely up to evil.");
		TransformerBootstrap.init();

	}

	public static void onPreLaunch() {
		LOGGER.info("Fabric, took you long enough!");
	}
}
