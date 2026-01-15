package madoku.craft.smelting;

import madoku.craft.smelting.system.CustomSmeltingManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MadokuCraftSmelting implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-smelting";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CustomSmeltingManager.initialize();
		LOGGER.info("Madoku Craft Smelting ready.");
	}
}
