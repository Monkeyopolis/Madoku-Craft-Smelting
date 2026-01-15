package madoku.craft.smelting.system;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CustomSmeltingConfig {
	public boolean enableFeature = true;
	public Map<String, Double> fuelItems = new LinkedHashMap<>(buildDefaultFuelItems());
	public double furnaceSmeltingSpeed = 120.0;
	public double smokerSmeltingSpeed = 80.0;
	public double blastFurnaceSpeed = 80.0;
	public double furnaceFuelEfficiencyMultiplier = 1.0;
	public double smokerFuelEfficiencyMultiplier = 1.5;
	public double blastFurnaceFuelEfficiencyMultiplier = 1.5;
	public List<String> smokerAdditionalInputs = new ArrayList<>(buildDefaultSmokerAdditionalInputs());
	public List<String> blastFurnaceAdditionalInputs = new ArrayList<>(buildDefaultBlastAdditionalInputs());

	private static final double TIME_INCREMENT = 0.125;
	private static final double MIN_TIME = 20.0;
	private static final double MAX_TIME = 1200.0;
	private static final double MIN_MULTIPLIER = 0.5;
	private static final double MAX_MULTIPLIER = 2.0;
	private static final double MIN_FUEL = 0.0;
	private static final double MAX_FUEL = 72000.0;

	public boolean update(JsonObject root) {
		if (root == null) {
			return false;
		}

		boolean changed = false;
		enableFeature = JsonHelper.getBoolean(root, "enableFeature", enableFeature);
		changed |= setBoolean(root, "enableFeature", enableFeature);

		furnaceSmeltingSpeed = normalizeTime(JsonHelper.getDouble(root, "furnaceSmeltingSpeed", furnaceSmeltingSpeed));
		changed |= setDouble(root, "furnaceSmeltingSpeed", furnaceSmeltingSpeed);

		smokerSmeltingSpeed = normalizeTime(JsonHelper.getDouble(root, "smokerSmeltingSpeed", smokerSmeltingSpeed));
		changed |= setDouble(root, "smokerSmeltingSpeed", smokerSmeltingSpeed);

		blastFurnaceSpeed = normalizeTime(JsonHelper.getDouble(root, "blastFurnaceSpeed", blastFurnaceSpeed));
		changed |= setDouble(root, "blastFurnaceSpeed", blastFurnaceSpeed);

		furnaceFuelEfficiencyMultiplier = normalizeMultiplier(JsonHelper.getDouble(root,
			"furnaceFuelEfficiencyMultiplier", furnaceFuelEfficiencyMultiplier));
		changed |= setDouble(root, "furnaceFuelEfficiencyMultiplier", furnaceFuelEfficiencyMultiplier);

		smokerFuelEfficiencyMultiplier = normalizeMultiplier(JsonHelper.getDouble(root,
			"smokerFuelEfficiencyMultiplier", smokerFuelEfficiencyMultiplier));
		changed |= setDouble(root, "smokerFuelEfficiencyMultiplier", smokerFuelEfficiencyMultiplier);

		blastFurnaceFuelEfficiencyMultiplier = normalizeMultiplier(JsonHelper.getDouble(root,
			"blastFurnaceFuelEfficiencyMultiplier", blastFurnaceFuelEfficiencyMultiplier));
		changed |= setDouble(root, "blastFurnaceFuelEfficiencyMultiplier", blastFurnaceFuelEfficiencyMultiplier);

		JsonObject fuelRoot = getFuelRoot(root);
		if (fuelRoot == null) {
			Map<String, Double> defaults = buildDefaultFuelItems();
			JsonObject replacement = new JsonObject();
			for (Map.Entry<String, Double> entry : defaults.entrySet()) {
				replacement.addProperty(entry.getKey(), entry.getValue());
			}
			root.add("fuelItems", replacement);
			fuelItems = new LinkedHashMap<>(defaults);
			changed = true;
		} else {
			Map<String, Double> updated = new LinkedHashMap<>();
			boolean fuelChanged = false;
			for (Map.Entry<String, JsonElement> entry : fuelRoot.entrySet()) {
				String normalizedKey = normalizeIdentifier(entry.getKey());
				if (normalizedKey == null) {
					fuelChanged = true;
					continue;
				}

				double raw = readDouble(entry.getValue(), MIN_FUEL);
				double normalized = normalizeFuelValue(raw);
				if (normalized <= 0.0) {
					fuelChanged = true;
					continue;
				}

				updated.put(normalizedKey, normalized);
				if (!normalizedKey.equals(entry.getKey()) || !isSameNumber(entry.getValue(), normalized)) {
					fuelChanged = true;
				}
			}

			if (fuelChanged) {
				JsonObject replacement = new JsonObject();
				for (Map.Entry<String, Double> entry : updated.entrySet()) {
					replacement.addProperty(entry.getKey(), entry.getValue());
				}
				root.add("fuelItems", replacement);
				changed = true;
			}

			fuelItems = updated;
		}

		JsonElement smokerElement = root.get("smokerAdditionalInputs");
		if (!(smokerElement instanceof JsonArray smokerArray)) {
			List<String> defaults = buildDefaultSmokerAdditionalInputs();
			JsonArray replacement = new JsonArray();
			for (String value : defaults) {
				replacement.add(value);
			}
			root.add("smokerAdditionalInputs", replacement);
			smokerAdditionalInputs = new ArrayList<>(defaults);
			changed = true;
		} else {
			List<String> updated = new ArrayList<>();
			Set<String> seen = new LinkedHashSet<>();
			boolean listChanged = false;
			for (JsonElement element : smokerArray) {
				if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
					listChanged = true;
					continue;
				}
				String normalized = normalizeIdentifier(primitive.getAsString());
				if (normalized == null) {
					listChanged = true;
					continue;
				}
				if (!normalized.equals(primitive.getAsString())) {
					listChanged = true;
				}
				if (!seen.add(normalized)) {
					listChanged = true;
				}
			}

			updated.addAll(seen);
			if (listChanged) {
				JsonArray replacement = new JsonArray();
				for (String value : updated) {
					replacement.add(value);
				}
				root.add("smokerAdditionalInputs", replacement);
				changed = true;
			}
			smokerAdditionalInputs = updated;
		}

		JsonElement blastElement = root.get("blastFurnaceAdditionalInputs");
		if (!(blastElement instanceof JsonArray blastArray)) {
			List<String> defaults = buildDefaultBlastAdditionalInputs();
			JsonArray replacement = new JsonArray();
			for (String value : defaults) {
				replacement.add(value);
			}
			root.add("blastFurnaceAdditionalInputs", replacement);
			blastFurnaceAdditionalInputs = new ArrayList<>(defaults);
			changed = true;
		} else {
			List<String> updated = new ArrayList<>();
			Set<String> seen = new LinkedHashSet<>();
			boolean listChanged = false;
			for (JsonElement element : blastArray) {
				if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
					listChanged = true;
					continue;
				}
				String normalized = normalizeIdentifier(primitive.getAsString());
				if (normalized == null) {
					listChanged = true;
					continue;
				}
				if (!normalized.equals(primitive.getAsString())) {
					listChanged = true;
				}
				if (!seen.add(normalized)) {
					listChanged = true;
				}
			}

			updated.addAll(seen);
			if (listChanged) {
				JsonArray replacement = new JsonArray();
				for (String value : updated) {
					replacement.add(value);
				}
				root.add("blastFurnaceAdditionalInputs", replacement);
				changed = true;
			}
			blastFurnaceAdditionalInputs = updated;
		}

		return changed;
	}

	public static JsonObject buildDefaults() {
		JsonObject defaults = new JsonObject();
		defaults.addProperty("enableFeature", true);
		defaults.addProperty("furnaceSmeltingSpeed", 120.0);
		defaults.addProperty("smokerSmeltingSpeed", 80.0);
		defaults.addProperty("blastFurnaceSpeed", 80.0);
		defaults.addProperty("furnaceFuelEfficiencyMultiplier", 1.0);
		defaults.addProperty("smokerFuelEfficiencyMultiplier", 1.5);
		defaults.addProperty("blastFurnaceFuelEfficiencyMultiplier", 1.5);

		JsonObject fuelRoot = new JsonObject();
		for (Map.Entry<String, Double> entry : buildDefaultFuelItems().entrySet()) {
			fuelRoot.addProperty(entry.getKey(), entry.getValue());
		}
		defaults.add("fuelItems", fuelRoot);

		JsonArray smokerInputs = new JsonArray();
		for (String value : buildDefaultSmokerAdditionalInputs()) {
			smokerInputs.add(value);
		}
		defaults.add("smokerAdditionalInputs", smokerInputs);

		JsonArray blastInputs = new JsonArray();
		for (String value : buildDefaultBlastAdditionalInputs()) {
			blastInputs.add(value);
		}
		defaults.add("blastFurnaceAdditionalInputs", blastInputs);

		return defaults;
	}

	public static Map<String, Double> buildDefaultFuelItems() {
		Map<String, Double> defaults = new LinkedHashMap<>();
		defaults.put("minecraft:lava_bucket", 51200.0);
		defaults.put("minecraft:coal_block", 19200.0);
		defaults.put("minecraft:magma_block", 12800.0);
		defaults.put("minecraft:dried_kelp_block", 4800.0);
		defaults.put("minecraft:blaze_rod", 3600.0);
		defaults.put("minecraft:coal", 2400.0);
		defaults.put("minecraft:charcoal", 1600.0);
		defaults.put("minecraft:mangrove_roots", 800.0);
		defaults.put("minecraft:muddy_mangrove_roots", 800.0);
		defaults.put("minecraft:crimson_stem", 800.0);
		defaults.put("minecraft:warped_stem", 800.0);
		defaults.put("minecraft:stripped_crimson_stem", 800.0);
		defaults.put("minecraft:stripped_warped_stem", 800.0);
		defaults.put("minecraft:oak_log", 600.0);
		defaults.put("minecraft:spruce_log", 600.0);
		defaults.put("minecraft:birch_log", 600.0);
		defaults.put("minecraft:jungle_log", 600.0);
		defaults.put("minecraft:acacia_log", 600.0);
		defaults.put("minecraft:cherry_log", 600.0);
		defaults.put("minecraft:dark_oak_log", 600.0);
		defaults.put("minecraft:mangrove_log", 600.0);
		defaults.put("minecraft:pale_oak_log", 600.0);
		defaults.put("minecraft:stripped_oak_log", 600.0);
		defaults.put("minecraft:stripped_spruce_log", 600.0);
		defaults.put("minecraft:stripped_birch_log", 600.0);
		defaults.put("minecraft:stripped_jungle_log", 600.0);
		defaults.put("minecraft:stripped_acacia_log", 600.0);
		defaults.put("minecraft:stripped_cherry_log", 600.0);
		defaults.put("minecraft:stripped_dark_oak_log", 600.0);
		defaults.put("minecraft:stripped_mangrove_log", 600.0);
		defaults.put("minecraft:stripped_pale_oak_log", 600.0);
		defaults.put("minecraft:oak_wood", 600.0);
		defaults.put("minecraft:spruce_wood", 600.0);
		defaults.put("minecraft:birch_wood", 600.0);
		defaults.put("minecraft:jungle_wood", 600.0);
		defaults.put("minecraft:acacia_wood", 600.0);
		defaults.put("minecraft:cherry_wood", 600.0);
		defaults.put("minecraft:pale_oak_wood", 600.0);
		defaults.put("minecraft:dark_oak_wood", 600.0);
		defaults.put("minecraft:mangrove_wood", 600.0);
		defaults.put("minecraft:stripped_oak_wood", 600.0);
		defaults.put("minecraft:stripped_spruce_wood", 600.0);
		defaults.put("minecraft:stripped_birch_wood", 600.0);
		defaults.put("minecraft:stripped_jungle_wood", 600.0);
		defaults.put("minecraft:stripped_acacia_wood", 600.0);
		defaults.put("minecraft:stripped_cherry_wood", 600.0);
		defaults.put("minecraft:stripped_pale_oak_wood", 600.0);
		defaults.put("minecraft:stripped_dark_oak_wood", 600.0);
		defaults.put("minecraft:stripped_mangrove_wood", 600.0);
		defaults.put("minecraft:stripped_bamboo_block", 450.0);
		defaults.put("minecraft:bamboo_block", 450.0);
		defaults.put("minecraft:crimson_planks", 400.0);
		defaults.put("minecraft:warped_planks", 400.0);
		defaults.put("minecraft:oak_planks", 300.0);
		defaults.put("minecraft:spruce_planks", 300.0);
		defaults.put("minecraft:birch_planks", 300.0);
		defaults.put("minecraft:jungle_planks", 300.0);
		defaults.put("minecraft:acacia_planks", 300.0);
		defaults.put("minecraft:cherry_planks", 300.0);
		defaults.put("minecraft:dark_oak_planks", 300.0);
		defaults.put("minecraft:mangrove_planks", 300.0);
		defaults.put("minecraft:bamboo_planks", 300.0);
		defaults.put("minecraft:pale_oak_planks", 300.0);
		defaults.put("minecraft:leaf_litter", 200.0);
		defaults.put("minecraft:crimson_fungus", 200.0);
		defaults.put("minecraft:warped_fungus", 200.0);
		defaults.put("minecraft:oak_sapling", 150.0);
		defaults.put("minecraft:spruce_sapling", 150.0);
		defaults.put("minecraft:birch_sapling", 150.0);
		defaults.put("minecraft:jungle_sapling", 150.0);
		defaults.put("minecraft:acacia_sapling", 150.0);
		defaults.put("minecraft:dark_oak_sapling", 150.0);
		defaults.put("minecraft:mangrove_propagule", 150.0);
		defaults.put("minecraft:cherry_sapling", 150.0);
		defaults.put("minecraft:pale_oak_sapling", 150.0);
		defaults.put("minecraft:stick", 100.0);
		defaults.put("minecraft:bamboo", 50.0);
		return defaults;
	}

	public static List<String> buildDefaultSmokerAdditionalInputs() {
		List<String> defaults = new ArrayList<>();
		defaults.add("minecraft:cactus");
		defaults.add("minecraft:sea_pickle");
		defaults.add("minecraft:chorus_fruit");
		defaults.add("minecraft:oak_log");
		defaults.add("minecraft:spruce_log");
		defaults.add("minecraft:birch_log");
		defaults.add("minecraft:jungle_log");
		defaults.add("minecraft:acacia_log");
		defaults.add("minecraft:cherry_log");
		defaults.add("minecraft:dark_oak_log");
		defaults.add("minecraft:mangrove_log");
		defaults.add("minecraft:pale_oak_log");
		defaults.add("minecraft:stripped_oak_log");
		defaults.add("minecraft:stripped_spruce_log");
		defaults.add("minecraft:stripped_birch_log");
		defaults.add("minecraft:stripped_jungle_log");
		defaults.add("minecraft:stripped_acacia_log");
		defaults.add("minecraft:stripped_cherry_log");
		defaults.add("minecraft:stripped_dark_oak_log");
		defaults.add("minecraft:stripped_mangrove_log");
		defaults.add("minecraft:stripped_pale_oak_log");
		defaults.add("minecraft:crimson_stem");
		defaults.add("minecraft:warped_stem");
		defaults.add("minecraft:stripped_crimson_stem");
		defaults.add("minecraft:stripped_warped_stem");
		defaults.add("minecraft:oak_wood");
		defaults.add("minecraft:spruce_wood");
		defaults.add("minecraft:birch_wood");
		defaults.add("minecraft:jungle_wood");
		defaults.add("minecraft:acacia_wood");
		defaults.add("minecraft:cherry_wood");
		defaults.add("minecraft:pale_oak_wood");
		defaults.add("minecraft:dark_oak_wood");
		defaults.add("minecraft:mangrove_wood");
		defaults.add("minecraft:stripped_oak_wood");
		defaults.add("minecraft:stripped_spruce_wood");
		defaults.add("minecraft:stripped_birch_wood");
		defaults.add("minecraft:stripped_jungle_wood");
		defaults.add("minecraft:stripped_acacia_wood");
		defaults.add("minecraft:stripped_cherry_wood");
		defaults.add("minecraft:stripped_pale_oak_wood");
		defaults.add("minecraft:stripped_dark_oak_wood");
		defaults.add("minecraft:stripped_mangrove_wood");
		defaults.add("minecraft:crimson_hyphae");
		defaults.add("minecraft:warped_hyphae");
		defaults.add("minecraft:stripped_crimson_hyphae");
		defaults.add("minecraft:stripped_warped_hyphae");
		return defaults;
	}

	public static List<String> buildDefaultBlastAdditionalInputs() {
		List<String> defaults = new ArrayList<>();
		defaults.add("minecraft:netherrack");
		defaults.add("minecraft:clay_ball");
		defaults.add("minecraft:wet_sponge");
		defaults.add("minecraft:sand");
		defaults.add("minecraft:red_sand");
		defaults.add("minecraft:clay");
		defaults.add("minecraft:quartz_block");
		defaults.add("minecraft:basalt");
		defaults.add("minecraft:nether_bricks");
		defaults.add("minecraft:polished_blackstone_bricks");
		defaults.add("minecraft:red_sandstone");
		defaults.add("minecraft:sandstone");
		defaults.add("minecraft:deepslate_tiles");
		defaults.add("minecraft:cobbled_deepslate");
		defaults.add("minecraft:deepslate_bricks");
		defaults.add("minecraft:stone_bricks");
		defaults.add("minecraft:stone");
		defaults.add("minecraft:cobblestone");
		defaults.add("minecraft:terracotta");
		defaults.add("minecraft:white_terracotta");
		defaults.add("minecraft:orange_terracotta");
		defaults.add("minecraft:magenta_terracotta");
		defaults.add("minecraft:light_blue_terracotta");
		defaults.add("minecraft:yellow_terracotta");
		defaults.add("minecraft:lime_terracotta");
		defaults.add("minecraft:pink_terracotta");
		defaults.add("minecraft:gray_terracotta");
		defaults.add("minecraft:light_gray_terracotta");
		defaults.add("minecraft:cyan_terracotta");
		defaults.add("minecraft:purple_terracotta");
		defaults.add("minecraft:blue_terracotta");
		defaults.add("minecraft:brown_terracotta");
		defaults.add("minecraft:green_terracotta");
		defaults.add("minecraft:red_terracotta");
		defaults.add("minecraft:black_terracotta");
		return defaults;
	}

	private static double normalizeFuelValue(double value) {
		if (!Double.isFinite(value)) {
			return MIN_FUEL;
		}
		double clamped = Math.min(MAX_FUEL, Math.max(MIN_FUEL, value));
		double rounded = Math.round(clamped / TIME_INCREMENT) * TIME_INCREMENT;
		return Math.min(MAX_FUEL, Math.max(MIN_FUEL, rounded));
	}

	private static double normalizeTime(double value) {
		if (!Double.isFinite(value)) {
			return MIN_TIME;
		}
		double clamped = Math.min(MAX_TIME, Math.max(MIN_TIME, value));
		double rounded = Math.round(clamped / TIME_INCREMENT) * TIME_INCREMENT;
		return Math.min(MAX_TIME, Math.max(MIN_TIME, rounded));
	}

	private static double normalizeMultiplier(double value) {
		if (!Double.isFinite(value)) {
			return MIN_MULTIPLIER;
		}
		double clamped = Math.min(MAX_MULTIPLIER, Math.max(MIN_MULTIPLIER, value));
		double rounded = Math.round(clamped / TIME_INCREMENT) * TIME_INCREMENT;
		return Math.min(MAX_MULTIPLIER, Math.max(MIN_MULTIPLIER, rounded));
	}

	private static JsonObject getFuelRoot(JsonObject root) {
		JsonElement element = root.get("fuelItems");
		if (element instanceof JsonObject object) {
			return object;
		}
		return null;
	}

	private static String normalizeIdentifier(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
			return null;
		}
		Identifier identifier = Identifier.tryParse(trimmed);
		if (identifier == null) {
			return null;
		}
		return identifier.toString();
	}

	private static double readDouble(JsonElement element, double fallback) {
		if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
			return primitive.getAsDouble();
		}
		return fallback;
	}

	private static boolean isSameNumber(JsonElement element, double value) {
		if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
			return Double.compare(primitive.getAsDouble(), value) == 0;
		}
		return false;
	}

	private static boolean setBoolean(JsonObject root, String key, boolean value) {
		JsonElement element = root.get(key);
		if (element instanceof JsonPrimitive primitive && primitive.isBoolean()) {
			if (primitive.getAsBoolean() == value) {
				return false;
			}
		}
		root.addProperty(key, value);
		return true;
	}

	private static boolean setDouble(JsonObject root, String key, double value) {
		JsonElement element = root.get(key);
		if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
			if (Double.compare(primitive.getAsDouble(), value) == 0) {
				return false;
			}
		}
		root.addProperty(key, value);
		return true;
	}
}
