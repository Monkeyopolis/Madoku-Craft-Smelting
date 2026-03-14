package madoku.craft.smelting.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public final class MadokuSmeltingConfig {
	public boolean enableFeature = true;

	public void resetToDefaults() {
		enableFeature = true;
	}

	public boolean updateSmelting(JsonObject root) {
		boolean changed = false;
		enableFeature = readBoolean(root, "enableFeature", enableFeature);
		changed |= setBoolean(root, "enableFeature", enableFeature);
		return changed;
	}

	public static JsonObject buildSmeltingDefaults() {
		JsonObject defaults = new JsonObject();
		defaults.addProperty("enableFeature", true);
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

	private static boolean readBoolean(JsonObject root, String key, boolean fallback) {
		JsonElement element = root.get(key);
		if (element instanceof JsonPrimitive primitive && primitive.isBoolean()) {
			return primitive.getAsBoolean();
		}
		return fallback;
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
}
