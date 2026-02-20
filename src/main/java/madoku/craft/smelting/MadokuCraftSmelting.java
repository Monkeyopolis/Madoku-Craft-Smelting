package madoku.craft.smelting;

import madoku.craft.API.system.MadokuInfoDebugSystem;
import madoku.craft.smelting.system.CustomSmeltingManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MadokuCraftSmelting implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-smelting";
	public static final String DEBUG_SOURCE = "Smelting";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void debugInfo(String message, Object... args) {
		MadokuInfoDebugSystem.info(LOGGER, DEBUG_SOURCE, message, args);
	}

	@Override
	public void onInitialize() {
		CustomSmeltingManager.initialize();
		debugInfo("Madoku Craft Smelting ready.");
	}
}
