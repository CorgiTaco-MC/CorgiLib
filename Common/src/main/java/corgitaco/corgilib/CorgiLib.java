package corgitaco.corgilib;

import corgitaco.corgilib.core.CorgiLibRegistry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CorgiLib {

	public static final String MOD_ID = "corgilib";
	public static final String MOD_NAME = "CorgiLib";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static void init() {
		CorgiLibRegistry.init();
	}

	public static ResourceLocation createLocation(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}