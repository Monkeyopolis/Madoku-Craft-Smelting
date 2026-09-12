package madoku.craft.java.utility.smelting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import madoku.craft.java.core.json.JSONAPIManager;
import madoku.craft.java.core.json.JSONFormatAPIManager;

import java.nio.file.Path;

/** Shared configuration services for the Madoku Utility subsystems. */
public final class UtilityConfigManager {
	public static final String CONFIG_FOLDER_NAME = "madoku-craft-utility";

	private UtilityConfigManager() {
	}

	public static void initialize() {
		SmeltingConfigManager.initialize();
	}

	public static void reset() {
		SmeltingConfigManager.reset();
	}

	public static Path getRootDirectory() {
		return JSONAPIManager.getOrCreateGlobalSystemDirectory(CONFIG_FOLDER_NAME);
	}

	public static Path getSubsystemDirectory(String name) {
		String normalized = normalize(name);
		if (normalized.isBlank() || normalized.contains("/") || normalized.contains("\\")) {
			throw new IllegalArgumentException("Utility subsystem directory must be a simple relative name.");
		}
		Path root = getRootDirectory().toAbsolutePath().normalize();
		Path directory = root.resolve(normalized).normalize();
		if (!directory.startsWith(root)) {
			throw new IllegalArgumentException("Utility subsystem directory must remain inside the utility directory.");
		}
		try {
			java.nio.file.Files.createDirectories(directory);
		} catch (java.io.IOException exception) {
			throw new IllegalStateException("Failed to create utility subsystem directory: " + directory, exception);
		}
		return directory;
	}

	public static JsonObject object(JsonObject source, String key) {
		JsonElement element = source == null ? null : source.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
	}

	public static boolean readBoolean(JsonObject source, String key, boolean fallback) {
		JsonElement element = source == null ? null : source.get(key);
		if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
			return fallback;
		}
		try {
			return element.getAsBoolean();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	public static double readDouble(JsonObject source, String key, double fallback) {
		JsonElement element = source == null ? null : source.get(key);
		if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
			return fallback;
		}
		try {
			return element.getAsDouble();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	public static String readString(JsonObject source, String key, String fallback) {
		JsonElement element = source == null ? null : source.get(key);
		if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
			return normalize(fallback);
		}
		return normalize(element.getAsString());
	}

	public static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
	}

	public static String normalizeBlockId(String value, String fallback) {
		String candidate = JSONAPIManager.normalizeRegistryIdentifierForLookup(value);
		if (candidate.isBlank()) {
			candidate = JSONAPIManager.normalizeRegistryIdentifierForLookup(fallback);
		}
		return JSONAPIManager.normalizeRegistryIdentifierForJson(candidate);
	}

	public static JSONFormatAPIManager.ObjectBuilder objectBuilder() {
		return JSONFormatAPIManager.object();
	}
}
