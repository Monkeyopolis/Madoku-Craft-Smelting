package madoku.craft.java.utility.smelting;

import com.google.gson.JsonObject;

import madoku.craft.java.core.json.JSONFormatAPIManager;
import madoku.craft.java.core.json.JSONTypeAPIManager;

import java.io.IOException;
import java.nio.file.Path;

/** Static configuration group for furnace, blast-furnace, and smoker behavior. */
public final class SmeltingConfigManager {
	public static final String CONFIG_FILE_NAME = "madoku-smelting.json";
	public static final String FIELD_ENABLED = "enabled";
	public static final String FIELD_BLOCK_ID = "block-id";
	public static final String FIELD_SMELTING_SPEED = "smelting-speed";
	public static final String FIELD_FUEL_EFFICIENCY = "fuel-efficiency";

	private static volatile Settings settings = defaults();

	private SmeltingConfigManager() {
	}

	public static void initialize() {
		Path file = UtilityConfigManager.getRootDirectory().resolve(CONFIG_FILE_NAME);
		try {
			JsonObject normalized = JSONFormatAPIManager.ensureManagedFile(
				file,
				buildDefaultsJson(),
				JSONTypeAPIManager.STATIC_CONFIG,
				null
			);
			settings = fromJson(normalized);
		} catch (IOException | RuntimeException exception) {
			settings = defaults();
			System.getLogger(SmeltingConfigManager.class.getName()).log(
				java.lang.System.Logger.Level.ERROR,
				"Failed to load Madoku Smelting configuration; using defaults.",
				exception
			);
		}
	}

	public static void reset() {
		settings = defaults();
	}

	public static Settings getSettings() {
		return settings;
	}

	public static Settings defaults() {
		return new Settings(
			true,
			new FurnaceSettings(true, "minecraft:furnace", 120.0, 1.0),
			new FurnaceSettings(true, "minecraft:blast-furnace", 80.0, 1.5),
			new FurnaceSettings(true, "minecraft:smoker", 80.0, 1.5)
		);
	}

	public static JsonObject buildDefaultsJson() {
		Settings value = defaults();
		return JSONFormatAPIManager.object()
			.put(FIELD_ENABLED, value.enabled())
			.object("furnace", group -> putGroup(group, value.furnace()))
			.object("blast-furnace", group -> putGroup(group, value.blastFurnace()))
			.object("smoker", group -> putGroup(group, value.smoker()))
			.build();
	}

	public static Settings fromJson(JsonObject source) {
		Settings fallback = defaults();
		return new Settings(
			UtilityConfigManager.readBoolean(source, FIELD_ENABLED, fallback.enabled()),
			readGroup(UtilityConfigManager.object(source, "furnace"), fallback.furnace()),
			readGroup(UtilityConfigManager.object(source, "blast-furnace"), fallback.blastFurnace()),
			readGroup(UtilityConfigManager.object(source, "smoker"), fallback.smoker())
		);
	}

	private static FurnaceSettings readGroup(JsonObject source, FurnaceSettings fallback) {
		return new FurnaceSettings(
			UtilityConfigManager.readBoolean(source, FIELD_ENABLED, fallback.enabled()),
			UtilityConfigManager.normalizeBlockId(
				UtilityConfigManager.readString(source, FIELD_BLOCK_ID, fallback.blockId()),
				fallback.blockId()
			),
			normalizeSpeed(UtilityConfigManager.readDouble(source, FIELD_SMELTING_SPEED, fallback.smeltingSpeed()), fallback.smeltingSpeed()),
			normalizeFuel(UtilityConfigManager.readDouble(source, FIELD_FUEL_EFFICIENCY, fallback.fuelEfficiency()), fallback.fuelEfficiency())
		);
	}

	private static void putGroup(JSONFormatAPIManager.ObjectBuilder group, FurnaceSettings value) {
		group.put(FIELD_ENABLED, value.enabled())
			.put(FIELD_BLOCK_ID, value.blockId())
			.put(FIELD_SMELTING_SPEED, value.smeltingSpeed())
			.put(FIELD_FUEL_EFFICIENCY, value.fuelEfficiency());
	}

	private static double normalizeSpeed(double value, double fallback) {
		return Double.isFinite(value) ? Math.max(20.0, Math.min(1200.0, value)) : fallback;
	}

	private static double normalizeFuel(double value, double fallback) {
		return Double.isFinite(value) ? Math.max(0.1, Math.min(10.0, value)) : fallback;
	}

	public record Settings(boolean enabled, FurnaceSettings furnace, FurnaceSettings blastFurnace, FurnaceSettings smoker) {
		public Settings {
			furnace = furnace == null ? defaults().furnace() : furnace;
			blastFurnace = blastFurnace == null ? defaults().blastFurnace() : blastFurnace;
			smoker = smoker == null ? defaults().smoker() : smoker;
		}

		public boolean hasEnabledGroup() {
			return furnace.enabled() || blastFurnace.enabled() || smoker.enabled();
		}
	}

	public record FurnaceSettings(boolean enabled, String blockId, double smeltingSpeed, double fuelEfficiency) {
		public FurnaceSettings {
			blockId = UtilityConfigManager.normalizeBlockId(blockId, "minecraft:furnace");
			smeltingSpeed = normalizeSpeed(smeltingSpeed, 80.0);
			fuelEfficiency = normalizeFuel(fuelEfficiency, 1.5);
		}
	}
}
