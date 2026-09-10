package madoku.craft.java.utility;

import net.fabricmc.api.ClientModInitializer;

/** Fabric client entrypoint for the standalone Utility jar. */
public final class MadokuUtilityClientInitializer implements ClientModInitializer {
	@Override public void onInitializeClient() { MadokuUtilityClient.initialize(); }
}
