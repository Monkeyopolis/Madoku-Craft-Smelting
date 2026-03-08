package madoku.craft.smelting;

import madoku.craft.debug.MadokuDebug;
import madoku.craft.smelting.system.CustomSmeltingManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MadokuCraftSmelting implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-smelting";
	private static final String DEBUG_METRIC = "smelting.lifecycle";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void debugInfo(String message, Object... args) {
		LOGGER.info(message, args);
		if (!MadokuDebug.shouldEmit(MadokuDebug.Domain.BLOCK_ENTITY, DEBUG_METRIC)) {
			return;
		}
		MadokuDebug.event(DEBUG_METRIC, MadokuDebug.Domain.BLOCK_ENTITY)
			.side(MadokuDebug.Side.SERVER)
			.subject(MOD_ID)
			.details(args == null || args.length == 0 ? message : message + " " + Arrays.toString(args))
			.log();
	}

	@Override
	public void onInitialize() {
		CustomSmeltingManager.initialize();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> CustomSmeltingManager.onServerStarted());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> CustomSmeltingManager.onServerStopped());
		debugInfo("Madoku Craft Smelting ready.");
	}
}
