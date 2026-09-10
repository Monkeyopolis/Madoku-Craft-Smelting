package madoku.craft.java.utility.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import madoku.craft.java.core.json.JSONFormatAPIManager;
import madoku.craft.java.core.json.JSONTypeAPIManager;
import madoku.craft.java.utility.smelting.UtilityConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Static client configuration for Madoku Music. */
public final class MusicConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MusicConfigManager.class);
	public static final String CONFIG_FILE_NAME = "madoku-music.json";
	private static final String OVERWORLD = "overworld";
	private static final String CREATIVE = "creative";
	private static final String NETHER = "nether";
	private static final String PALE_GARDEN = "pale-garden";
	private static final String ENABLED = "enabled";
	private static final String FREQUENCY = "frequency";
	private static final String DEFAULT = "default";
	private static final String FREQUENT = "frequent";
	private static final String CONSTANT = "constant";
	private static final String MUSIC = "music";
	private static final String MUSIC_ID = "music-id";
	private static final String VOLUME = "volume";
	private static final String WEIGHT = "weight";
	private static final FrequencySettings DEFAULT_FREQUENCY = new FrequencySettings(16, 24);
	private static final FrequencySettings FREQUENT_FREQUENCY = new FrequencySettings(12, 16);
	private static final FrequencySettings CONSTANT_FREQUENCY = new FrequencySettings(9, 12);
	private static volatile Settings settings = Settings.defaults();

	private MusicConfigManager() { }

	public static void initialize() { loadConfig(); }

	public static void reset() { settings = Settings.defaults(); }

	public static Settings getSettings() { return settings; }

	public static JsonObject buildDefaultsJson() { return Settings.defaults().toConfigJson(); }

	private static void loadConfig() {
		Settings fallback = Settings.defaults();
		try {
			Path file = UtilityConfigManager.getRootDirectory().resolve(CONFIG_FILE_NAME);
			JsonObject defaults = fallback.toConfigJson();
			JsonObject normalized = JSONFormatAPIManager.ensureManagedFile(
				file, defaults, JSONTypeAPIManager.STATIC_CONFIG, null
			);
			Settings loaded = Settings.fromJson(normalized);
			JSONFormatAPIManager.writeManagedFile(
				file, loaded.toConfigJson(), defaults, JSONTypeAPIManager.STATIC_CONFIG, null
			);
			settings = loaded;
		} catch (IOException | RuntimeException exception) {
			settings = fallback;
			LOGGER.error("Failed to load Madoku Music configuration; using defaults.", exception);
		}
	}

	private static JsonObject readObject(JsonObject source, String key) {
		JsonElement element = source == null ? null : source.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
	}

	private static JsonArray readArray(JsonObject source, String key) {
		JsonElement element = source == null ? null : source.get(key);
		return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
	}

	private static boolean readBoolean(JsonObject source, String key, boolean fallback) {
		try {
			return source != null && source.has(key) ? source.get(key).getAsBoolean() : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	private static int readInt(JsonObject source, String key, int fallback) {
		try {
			return source != null && source.has(key) ? source.get(key).getAsInt() : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	private static double readDouble(JsonObject source, String key, double fallback) {
		try {
			return source != null && source.has(key) ? source.get(key).getAsDouble() : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	private static String readString(JsonObject source, String key) {
		try {
			return source != null && source.has(key) ? source.get(key).getAsString() : "";
		} catch (RuntimeException exception) {
			return "";
		}
	}

	public static String normalizeMusicId(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	private static FrequencySettings readFrequency(JsonObject source, String key, FrequencySettings fallback) {
		JsonObject frequency = readObject(source, key);
		int minimum = clampMinutes(readInt(frequency, "minimum-minutes", fallback.minimumMinutes()));
		int maximum = clampMinutes(readInt(frequency, "maximum-minutes", fallback.maximumMinutes()));
		if (maximum < minimum) maximum = minimum;
		return new FrequencySettings(minimum, maximum);
	}

	private static List<TrackSettings> readTracks(JsonObject source, List<TrackSettings> fallback) {
		JsonArray array = readArray(source, MUSIC);
		if (array == null) return fallback;
		List<TrackSettings> tracks = new ArrayList<>();
		for (JsonElement element : array) {
			if (element == null || !element.isJsonObject()) continue;
			JsonObject track = element.getAsJsonObject();
			String id = normalizeMusicId(readString(track, MUSIC_ID));
			if (id.isBlank()) continue;
			double volume = readDouble(track, VOLUME, 1.0D);
			int weight = readInt(track, WEIGHT, 1);
			tracks.add(new TrackSettings(id, clampVolume(volume), Math.max(1, weight)));
		}
		return List.copyOf(tracks);
	}

	private static int clampMinutes(int value) { return Math.max(1, Math.min(1_000_000, value)); }
	private static float clampVolume(double value) {
		if (!Double.isFinite(value)) return 1.0F;
		return (float) Math.max(0.0D, Math.min(1.0D, value));
	}

	public record FrequencySettings(int minimumMinutes, int maximumMinutes) {
		public FrequencySettings {
			minimumMinutes = clampMinutes(minimumMinutes);
			maximumMinutes = clampMinutes(Math.max(minimumMinutes, maximumMinutes));
		}
	}

	public record TrackSettings(String musicId, float volume, int weight) {
		public TrackSettings {
			musicId = normalizeMusicId(musicId);
			volume = clampVolume(volume);
			weight = Math.max(1, weight);
		}
	}

	public record PlaylistSettings(
		boolean enabled,
		FrequencySettings defaultFrequency,
		FrequencySettings frequentFrequency,
		FrequencySettings constantFrequency,
		List<TrackSettings> music
	) {
		public PlaylistSettings {
			defaultFrequency = defaultFrequency == null ? DEFAULT_FREQUENCY : defaultFrequency;
			frequentFrequency = frequentFrequency == null ? FREQUENT_FREQUENCY : frequentFrequency;
			constantFrequency = constantFrequency == null ? CONSTANT_FREQUENCY : constantFrequency;
			music = music == null ? List.of() : List.copyOf(music);
		}

		public FrequencySettings frequency(String option) {
		return switch (option == null ? "default" : option) {
			case "frequent" -> frequentFrequency;
			case "constant" -> constantFrequency;
			default -> defaultFrequency;
		};
		}

		private static PlaylistSettings fromJson(JsonObject source, PlaylistSettings fallback) {
			return playlistFromJson(source, fallback);
		}

		private void write(JSONFormatAPIManager.ObjectBuilder builder) {
			builder.put(ENABLED, enabled)
				.object(FREQUENCY, frequency -> frequency
					.object(DEFAULT, value -> writeFrequency(value, defaultFrequency))
					.object(FREQUENT, value -> writeFrequency(value, frequentFrequency))
					.object(CONSTANT, value -> writeFrequency(value, constantFrequency)))
				.array(MUSIC, value -> writeTracks(value, music));
		}
	}

	public record Settings(
		PlaylistSettings overworld,
		PlaylistSettings creative,
		PlaylistSettings nether,
		PlaylistSettings paleGarden
	) {
		public Settings {
			overworld = overworld == null ? defaults().overworld : overworld;
			creative = creative == null ? defaults().creative : creative;
			nether = nether == null ? defaults().nether : nether;
			paleGarden = paleGarden == null ? defaults().paleGarden : paleGarden;
		}

		private static Settings defaults() {
			return new Settings(
				new PlaylistSettings(true, DEFAULT_FREQUENCY, FREQUENT_FREQUENCY, CONSTANT_FREQUENCY, overworldTracks()),
				new PlaylistSettings(true, DEFAULT_FREQUENCY, FREQUENT_FREQUENCY, CONSTANT_FREQUENCY, overworldTracks()),
				new PlaylistSettings(true, DEFAULT_FREQUENCY, FREQUENT_FREQUENCY, CONSTANT_FREQUENCY, netherTracks()),
				new PlaylistSettings(true, DEFAULT_FREQUENCY, FREQUENT_FREQUENCY, CONSTANT_FREQUENCY, paleGardenTracks())
			);
		}

		private static Settings fromJson(JsonObject source) {
			Settings fallback = defaults();
			return new Settings(
				PlaylistSettings.fromJson(readObject(source, OVERWORLD), fallback.overworld),
				PlaylistSettings.fromJson(readObject(source, CREATIVE), fallback.creative),
				PlaylistSettings.fromJson(readObject(source, NETHER), fallback.nether),
				PlaylistSettings.fromJson(readObject(source, PALE_GARDEN), fallback.paleGarden)
			);
		}

		private JsonObject toConfigJson() {
			return JSONFormatAPIManager.object()
				.object(OVERWORLD, value -> overworld.write(value))
				.object(CREATIVE, value -> creative.write(value))
				.object(NETHER, value -> nether.write(value))
				.object(PALE_GARDEN, value -> paleGarden.write(value))
				.build();
		}
	}

	private static PlaylistSettings playlistFromJson(JsonObject source, PlaylistSettings fallback) {
		return new PlaylistSettings(
			readBoolean(source, ENABLED, fallback.enabled()),
			readFrequency(source, DEFAULT, fallback.defaultFrequency()),
			readFrequency(source, FREQUENT, fallback.frequentFrequency()),
			readFrequency(source, CONSTANT, fallback.constantFrequency()),
			readTracks(source, fallback.music())
		);
	}

	private static TrackSettings track(String id, int weight) { return new TrackSettings(id, 1.0F, weight); }

	private static List<TrackSettings> overworldTracks() {
		return List.of(
			track("a-familiar-room", 5), track("an-ordinary-day", 5), track("ancestry", 1),
			track("below-and-above", 15), track("broken-clocks", 5), track("bromeliad", 5),
			track("clark", 9), track("comforting-memories", 9), track("crescent-dunes", 1),
			track("danny", 9), track("deeper", 5), track("dry-hands", 9), track("ebb", 5),
			track("echo-in-the-wind", 5), track("eld-unknown", 1), track("endless", 9),
			track("featherfall", 5), track("fireflies", 9), track("floating-dreams", 5),
			track("haggstrom", 9), track("home", 5), track("infinite-amethyst", 15),
			track("key", 15), track("komorebi", 1), track("left-to-bloom", 1), track("lilypad", 9),
			track("living-mice", 9), track("memories", 9), track("minecraft", 15), track("nightly", 15),
			track("one-more-day", 1), track("os-piano", 15), track("oxygene", 15), track("pokopoko", 1),
			track("puzzlebox", 5), track("shores", 1), track("stand-tall", 1),
			track("subwoofer-lullaby", 9), track("sweden", 9), track("watcher", 5), track("wending", 1),
			track("wet-hands", 9), track("yakusoku", 1),
			track("aria-math", 5), track("biome-fest", 5), track("blind-spots", 5),
			track("dreiton", 5), track("haunt-muskie", 5), track("taswell", 5),
			track("beginning-2", 5), track("floating-trees", 5), track("moog-city-2", 5), track("mutation", 9),
			track("axolotl", 5), track("dragon-fish", 5), track("shuniji", 9),
			track("aerie", 9), track("firebugs", 5), track("labyrinthine", 9),
			track("the-end-2", 15), track("alpha-2", 15), track("intro", 15)
		);
	}

	private static List<TrackSettings> netherTracks() {
		return List.of(
			track("ballad-of-the-cats", 5), track("concrete-halls", 5), track("chrysopoeia", 1),
			track("dead-voxel", 9), track("rubedo", 1), track("so-below", 9), track("warmth", 9),
			track("the-end", 9), track("the-end-2", 15)
		);
	}

	private static List<TrackSettings> paleGardenTracks() {
		return List.of(
			track("ballad-of-the-cats", 5), track("concrete-halls", 5), track("chrysopoeia", 1),
			track("dead-voxel", 9), track("rubedo", 1), track("so-below", 9), track("warmth", 9),
			track("the-end", 9)
		);
	}

	private static void writeFrequency(JSONFormatAPIManager.ObjectBuilder builder, FrequencySettings value) {
		builder.put("minimum-minutes", value.minimumMinutes()).put("maximum-minutes", value.maximumMinutes());
	}

	private static void writeTracks(JSONFormatAPIManager.ArrayBuilder builder, List<TrackSettings> tracks) {
		for (TrackSettings track : tracks) {
			builder.object(value -> value.put(MUSIC_ID, track.musicId())
				.put(VOLUME, track.volume())
				.put(WEIGHT, track.weight()));
		}
	}
}
