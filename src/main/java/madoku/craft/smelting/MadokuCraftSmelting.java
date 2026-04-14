package madoku.craft.smelting;

import madoku.craft.config.StaticJsonSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import madoku.craft.smelting.system.MadokuSmeltingManager;

public class MadokuCraftSmelting implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-smelting";

	@Override
	public void onInitialize() {
		StaticJsonSystem.initialize();
		MadokuSmeltingManager.initialize();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> MadokuSmeltingManager.onServerStarted());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> MadokuSmeltingManager.onServerStopped());
	}
}
