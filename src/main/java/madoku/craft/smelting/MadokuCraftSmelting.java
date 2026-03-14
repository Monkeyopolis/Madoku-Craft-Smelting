package madoku.craft.smelting;

import madoku.craft.clock.MadokuClock;
import madoku.craft.clock.MadokuTicks;
import madoku.craft.config.StaticJsonSystem;
import madoku.craft.debug.MadokuDebug;
import madoku.craft.scheduler.MadokuScheduler;
import madoku.craft.smelting.system.MadokuSmeltingManager;
import madoku.craft.time.MadokuSleep;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
		StaticJsonSystem.initialize();
		MadokuDebug.initialize();
		MadokuSmeltingManager.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuDebug.resetSession();
			MadokuClock.reset();
			MadokuSleep.reset();
			MadokuScheduler.reset();
			MadokuScheduler.loadPersistedData(server);
			MadokuSmeltingManager.onServerStarted();
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuScheduler.savePersistedData(server);
			MadokuClock.reset();
			MadokuSleep.reset();
			MadokuScheduler.reset();
			MadokuSmeltingManager.onServerStopped();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long tickIncrement = MadokuSleep.getTickIncrement(server);
			MadokuSmeltingManager.onServerTickIncrement(tickIncrement);
			MadokuTicks.advance(server, tickIncrement);
			MadokuScheduler.autosavePersistedData(server);
		});

		debugInfo("Madoku Craft Smelting ready.");
	}
}
