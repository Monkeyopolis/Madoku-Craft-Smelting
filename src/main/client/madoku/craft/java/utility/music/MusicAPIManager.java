package madoku.craft.java.utility.music;

import net.minecraft.client.Minecraft;

/** API boundary for the Madoku Music client subsystem. */
public final class MusicAPIManager {
	private static final MusicProvider UNAVAILABLE_PROVIDER = new MusicProvider() { };
	private static volatile MusicProvider provider = UNAVAILABLE_PROVIDER;

	private MusicAPIManager() { }

	public static void registerProvider(MusicProvider candidate) {
		if (candidate == null) throw new IllegalArgumentException("Music provider must not be null.");
		provider = candidate;
	}

	public static void unregisterProvider() { provider = UNAVAILABLE_PROVIDER; }
	public static boolean tick(Minecraft client) { return provider.tick(client); }
	public static boolean overridesVanillaMusic(Minecraft client) { return provider.overridesVanillaMusic(client); }
	public static String getCurrentMusicId() { return provider.getCurrentMusicId(); }
}
