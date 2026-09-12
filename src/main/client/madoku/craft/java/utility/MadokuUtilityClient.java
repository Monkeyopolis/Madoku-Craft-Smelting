package madoku.craft.java.utility;

import madoku.craft.java.utility.music.MadokuMusicManager;

/** Client entrypoint adapter for Utility-owned client systems. */
public final class MadokuUtilityClient {
	private MadokuUtilityClient() {
	}

	public static void initialize() {
		MadokuMusicManager.initialize();
	}
}
