package madoku.craft.java.utility;

import madoku.craft.java.core.module.MadokuStandaloneModule;
import madoku.craft.java.core.module.MadokuStandaloneRuntime;
import madoku.craft.java.utility.smelting.MadokuUtilityManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

/** Fabric entrypoint for the standalone Utility jar. */
public final class MadokuUtilityInitializer implements ModInitializer, MadokuStandaloneModule {
	@Override public void onInitialize() { MadokuStandaloneRuntime.initialize(this); }
	@Override public void initialize() { MadokuUtilityManager.initialize(); }
	@Override public void reset() { MadokuUtilityManager.reset(); }
	@Override public void onServerStarted(MinecraftServer server) { MadokuUtilityManager.onServerStarted(server); }
	@Override public void onServerStopped(MinecraftServer server) { MadokuUtilityManager.onServerStopped(server); }
}
