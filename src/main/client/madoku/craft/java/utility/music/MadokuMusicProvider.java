package madoku.craft.java.utility.music;

import net.minecraft.client.Minecraft;

/** Built-in provider for Madoku Music. */
public final class MadokuMusicProvider implements MusicProvider {
	@Override public boolean tick(Minecraft client) { return MadokuMusicManager.tick(client); }
	@Override public boolean overridesVanillaMusic(Minecraft client) { return MadokuMusicManager.overridesVanillaMusic(client); }
	@Override public String getCurrentMusicId() { return MadokuMusicManager.getCurrentMusicId(); }
}
