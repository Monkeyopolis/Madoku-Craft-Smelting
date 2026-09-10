package madoku.craft.java.utility.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Runtime subsystem that overrides vanilla music selection for configured contexts. */
public final class MadokuMusicManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MadokuMusicManager.class);
	private static final int TICKS_PER_MINUTE = 1_200;
	private static final int INITIAL_DELAY_TICKS = 100;
	private static final RandomSource RANDOM = RandomSource.create();
	private static volatile boolean initialized;
	private static SoundInstance currentMusic;
	private static String currentMusicId = "";
	private static String currentPlaylistId = "";
	private static int nextSongDelay = INITIAL_DELAY_TICKS;

	private MadokuMusicManager() { }

	public static void initialize() {
		if (initialized) return;
		MusicConfigManager.initialize();
		MusicAPIManager.registerProvider(new MadokuMusicProvider());
		resetRuntimeState(Minecraft.getInstance());
		initialized = true;
	}

	public static void reset() {
		stopCurrentMusic(Minecraft.getInstance());
		initialized = false;
		currentPlaylistId = "";
		MusicAPIManager.unregisterProvider();
		MusicConfigManager.reset();
	}

	public static String getCurrentMusicId() { return currentMusicId; }

	/** Claims the complete configured world music context so vanilla cannot transition by biome. */
	public static boolean overridesVanillaMusic(Minecraft client) {
		return initialized && client != null && !resolvePlaylistId(client).isBlank();
	}

	/** Returns true when this tick replaced vanilla's MusicManager behavior. */
	public static boolean tick(Minecraft client) {
		if (!initialized || client == null) return false;

		String playlistId = resolvePlaylistId(client);
		MusicConfigManager.PlaylistSettings playlist = resolvePlaylist(client, playlistId);
		if (playlist == null || !playlist.enabled() || playlist.music().isEmpty()
			|| client.options.getSoundSourceVolume(SoundSource.MUSIC) <= 0.0F) {
			stopCurrentMusic(client);
			currentPlaylistId = "";
			return overridesVanillaMusic(client);
		}

		if (!playlistId.equals(currentPlaylistId)) {
			stopCurrentMusic(client);
			currentPlaylistId = playlistId;
			nextSongDelay = INITIAL_DELAY_TICKS;
		}

		SoundManager soundManager = client.getSoundManager();
		if (currentMusic != null) {
			if (soundManager.isActive(currentMusic)) return true;
			currentMusic = null;
			currentMusicId = "";
			nextSongDelay = chooseDelay(client, playlist);
		}

		if (nextSongDelay > 0) {
			nextSongDelay--;
			return true;
		}

		MusicConfigManager.TrackSettings track = selectTrack(playlist.music());
		if (track == null) {
			stopCurrentMusic(client);
			return false;
		}

		String musicId = MusicConfigManager.normalizeMusicId(track.musicId());
		Identifier soundLocation = getSoundLocation(musicId);
		if (soundLocation == null || musicId.isBlank() || !isSoundResourceAvailable(soundLocation)) {
			LOGGER.warn("Madoku Music resource is unavailable for configured track {} ({}).", musicId, soundLocation);
			nextSongDelay = INITIAL_DELAY_TICKS;
			return true;
		}

		Identifier eventId = Identifier.fromNamespaceAndPath("madoku-craft", "music/" + musicId);
		MadokuMusicSoundInstance instance = new MadokuMusicSoundInstance(eventId, soundLocation, track.volume());
		SoundEngine.PlayResult result = soundManager.play(instance);
		if (result == SoundEngine.PlayResult.STARTED || result == SoundEngine.PlayResult.STARTED_SILENTLY) {
			currentMusic = instance;
			currentMusicId = musicId;
			nextSongDelay = Integer.MAX_VALUE;
			return true;
		}

		LOGGER.warn("Madoku Music could not start configured track {} ({}).", track.musicId(), soundLocation);
		nextSongDelay = INITIAL_DELAY_TICKS;
		return true;
	}

	private static MusicConfigManager.PlaylistSettings resolvePlaylist(Minecraft client, String playlistId) {
		MusicConfigManager.Settings settings = MusicConfigManager.getSettings();
		return switch (playlistId) {
			case "creative" -> settings.creative();
			case "nether" -> settings.nether();
			case "pale-garden" -> settings.paleGarden();
			case "overworld" -> settings.overworld();
			default -> null;
		};
	}

	private static String resolvePlaylistId(Minecraft client) {
		if (client.level == null || client.player == null) return "";
		if (Level.END.equals(client.level.dimension())) return "";
		if (Level.NETHER.equals(client.level.dimension())) return "nether";
		if (client.level.getBiome(client.player.blockPosition()).is(Biomes.PALE_GARDEN)) return "pale-garden";
		return client.player.isCreative() ? "creative" : "overworld";
	}

	private static int chooseDelay(Minecraft client, MusicConfigManager.PlaylistSettings playlist) {
		Object value = client.options.musicFrequency().get();
		String option = value instanceof MusicManager.MusicFrequency frequency
			? frequency.getSerializedName().toLowerCase(Locale.ROOT) : "default";
		MusicConfigManager.FrequencySettings frequency = playlist.frequency(option);
		long minimum = (long) frequency.minimumMinutes() * TICKS_PER_MINUTE;
		long maximum = (long) frequency.maximumMinutes() * TICKS_PER_MINUTE;
		int safeMinimum = (int) Math.min(Integer.MAX_VALUE - 1L, minimum);
		int safeMaximum = (int) Math.min(Integer.MAX_VALUE - 1L, Math.max(minimum, maximum));
		int delay = safeMinimum >= safeMaximum ? safeMinimum : RANDOM.nextInt(safeMinimum, safeMaximum + 1);
		return delay;
	}

	private static MusicConfigManager.TrackSettings selectTrack(List<MusicConfigManager.TrackSettings> tracks) {
		List<MusicConfigManager.TrackSettings> supported = new ArrayList<>();
		long totalWeight = 0L;
		for (MusicConfigManager.TrackSettings track : tracks) {
			if (track == null) continue;
			Identifier soundLocation = getSoundLocation(track.musicId());
			if (soundLocation == null || !isSoundResourceAvailable(soundLocation)) continue;
			supported.add(track);
			totalWeight += track.weight();
		}
		if (supported.isEmpty() || totalWeight <= 0L) return null;
		long selected = Math.floorMod(RANDOM.nextLong(), totalWeight);
		for (MusicConfigManager.TrackSettings track : supported) {
			selected -= track.weight();
			if (selected < 0L) return track;
		}
		return supported.get(supported.size() - 1);
	}

	private static boolean isSoundResourceAvailable(Identifier soundLocation) {
		Minecraft client = Minecraft.getInstance();
		return client != null
			&& client.getResourceManager()
				.getResource(Sound.SOUND_LISTER.idToFile(soundLocation))
				.isPresent();
	}

	private static Identifier getSoundLocation(String musicId) {
		String id = MusicConfigManager.normalizeMusicId(musicId);
		if (id.isBlank()) return null;
		if (id.equals("alpha-2") || id.equals("intro") || id.equals("the-end-2")) {
			return Identifier.fromNamespaceAndPath("madoku-craft", "music/" + id);
		}
		String path = switch (id) {
			case "a-familiar-room" -> "music/game/a_familiar_room";
			case "an-ordinary-day" -> "music/game/an_ordinary_day";
			case "ancestry" -> "music/game/deep_dark/ancestry";
			case "below-and-above" -> "music/game/below_and_above";
			case "broken-clocks" -> "music/game/broken_clocks";
			case "bromeliad" -> "music/game/bromeliad";
			case "clark" -> "music/game/clark";
			case "comforting-memories" -> "music/game/comforting_memories";
			case "crescent-dunes" -> "music/game/badlands/crescent_dunes";
			case "danny" -> "music/game/danny";
			case "deeper" -> "music/game/deep_dark/deeper";
			case "dry-hands" -> "music/game/dry_hands";
			case "ebb" -> "music/game/ebb";
			case "echo-in-the-wind" -> "music/game/echo_in_the_wind";
			case "eld-unknown" -> "music/game/eld_unknown";
			case "endless" -> "music/game/endless";
			case "featherfall" -> "music/game/featherfall";
			case "fireflies" -> "music/game/swamp/fireflies";
			case "floating-dreams" -> "music/game/floating_dream";
			case "haggstrom" -> "music/game/haggstrom";
			case "home" -> "music/game/home";
			case "infinite-amethyst" -> "music/game/infinite_amethyst";
			case "key" -> "music/game/key";
			case "komorebi" -> "music/game/komorebi";
			case "left-to-bloom" -> "music/game/left_to_bloom";
			case "lilypad" -> "music/game/lilypad";
			case "living-mice" -> "music/game/living_mice";
			case "memories" -> "music/game/memories";
			case "minecraft" -> "music/game/minecraft";
			case "nightly" -> "music/game/nightly";
			case "one-more-day" -> "music/game/one_more_day";
			case "os-piano" -> "music/game/os_piano";
			case "oxygene" -> "music/game/oxygene";
			case "pokopoko" -> "music/game/pokopoko";
			case "puzzlebox" -> "music/game/puzzlebox";
			case "shores" -> "music/game/shores";
			case "stand-tall" -> "music/game/stand_tall";
			case "subwoofer-lullaby" -> "music/game/subwoofer_lullaby";
			case "sweden" -> "music/game/sweden";
			case "watcher" -> "music/game/watcher";
			case "wending" -> "music/game/wending";
			case "wet-hands" -> "music/game/wet_hands";
			case "yakusoku" -> "music/game/yakusoku";
			case "aria-math" -> "music/game/creative/aria_math";
			case "biome-fest" -> "music/game/creative/biome_fest";
			case "blind-spots" -> "music/game/creative/blind_spots";
			case "dreiton" -> "music/game/creative/dreiton";
			case "haunt-muskie" -> "music/game/creative/haunt_muskie";
			case "taswell" -> "music/game/creative/taswell";
			case "beginning-2" -> "music/menu/beginning_2";
			case "floating-trees" -> "music/menu/floating_trees";
			case "moog-city-2" -> "music/menu/moog_city_2";
			case "mutation" -> "music/menu/mutation";
			case "axolotl" -> "music/game/water/axolotl";
			case "dragon-fish" -> "music/game/water/dragon_fish";
			case "shuniji" -> "music/game/water/shuniji";
			case "aerie" -> "music/game/swamp/aerie";
			case "firebugs" -> "music/game/swamp/firebugs";
			case "labyrinthine" -> "music/game/swamp/labyrinthine";
			case "ballad-of-the-cats" -> "music/game/nether/ballad_of_the_cats";
			case "concrete-halls" -> "music/game/nether/concrete_halls";
			case "chrysopoeia" -> "music/game/nether/crimson_forest/chrysopoeia";
			case "dead-voxel" -> "music/game/nether/dead_voxel";
			case "rubedo" -> "music/game/nether/nether_wastes/rubedo";
			case "so-below" -> "music/game/nether/soulsand_valley/so_below";
			case "warmth" -> "music/game/nether/warmth";
			case "the-end" -> "music/game/end/the_end";
			default -> "";
		};
		return path.isBlank() ? null : Identifier.fromNamespaceAndPath("minecraft", path);
	}

	private static void stopCurrentMusic(Minecraft client) {
		if (currentMusic != null && client != null) {
			client.getSoundManager().stop(currentMusic);
		}
		currentMusic = null;
		currentMusicId = "";
		nextSongDelay = INITIAL_DELAY_TICKS;
	}

	private static void resetRuntimeState(Minecraft client) {
		stopCurrentMusic(client);
		currentPlaylistId = "";
	}
}
