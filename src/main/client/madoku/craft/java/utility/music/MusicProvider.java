package madoku.craft.java.utility.music;

import net.minecraft.client.Minecraft;

/** Public provider contract for Madoku Music runtime behavior. */
public interface MusicProvider {
	default boolean tick(Minecraft client) { return false; }
	default boolean overridesVanillaMusic(Minecraft client) { return false; }
	default String getCurrentMusicId() { return ""; }
}
