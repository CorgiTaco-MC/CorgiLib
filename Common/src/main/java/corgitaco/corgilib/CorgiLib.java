package corgitaco.corgilib;

import corgitaco.corgilib.core.CorgiLibRegistry;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CorgiLib {

	public static final String MOD_ID = "corgilib";
	public static final String MOD_NAME = "CorgiLib";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static void init() {
		CorgiLibRegistry.init();
	}

	public static Identifier createLocation(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}