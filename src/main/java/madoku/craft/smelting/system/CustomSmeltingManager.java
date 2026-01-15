package madoku.craft.smelting.system;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import madoku.craft.API.system.JsonFeatureSystem;
import madoku.craft.smelting.MadokuCraftSmelting;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CustomSmeltingManager {
	private static final String FEATURE_ID = "madoku_craft_smelting";
	private static final int MINIMUM_COOK_TICKS = 20;
	private static final int BASE_FURNACE_COOK_TICKS = 200;
	private static final int BASE_SMOKER_COOK_TICKS = 100;
	private static final int BASE_BLAST_COOK_TICKS = 100;

	private static JsonFeatureSystem.ManagedFeature feature;
	private static final CustomSmeltingConfig configuration = new CustomSmeltingConfig();
	private static Map<Item, Integer> fuelOverrides = Map.of();
	private static Set<Item> smokerAdditionalInputs = Set.of();
	private static Set<Item> blastAdditionalInputs = Set.of();

	private CustomSmeltingManager() {
	}

	public static void initialize() {
		JsonObject defaults = CustomSmeltingConfig.buildDefaults();
		feature = JsonFeatureSystem.loadFeature(FEATURE_ID, defaults);
		boolean changed = configuration.update(feature.getRoot());
		changed |= pruneInvalidEntries(feature.getRoot());
		if (changed) {
			feature.save();
		}
		rebuildRules();
		MadokuCraftSmelting.LOGGER.info("Smelting system is {} (config at {})",
			configuration.enableFeature ? "enabled" : "disabled",
			feature.getPath());
	}

	public static boolean isEnabled() {
		return configuration.enableFeature;
	}

	public static int getCookTimeTicks(AbstractFurnaceBlockEntity furnace, int originalTicks) {
		if (!isEnabled() || furnace == null) {
			return originalTicks;
		}
		int configured = toTicks(getConfiguredTime(furnace));
		return Math.max(MINIMUM_COOK_TICKS, configured);
	}

	public static boolean isFuelItem(ItemStack stack) {
		if (!isEnabled() || stack == null || stack.isEmpty()) {
			return false;
		}
		Integer ticks = fuelOverrides.get(stack.getItem());
		return ticks != null && ticks > 0;
	}

	public static int getFuelTimeTicks(AbstractFurnaceBlockEntity furnace, ItemStack stack) {
		if (!isEnabled() || stack == null || stack.isEmpty()) {
			return 0;
		}
		Integer baseTicks = fuelOverrides.get(stack.getItem());
		if (baseTicks == null || baseTicks <= 0) {
			return 0;
		}
		int baseCookTicks = getBaseCookTicks(furnace);
		double cookTime = getConfiguredTime(furnace);
		if (!Double.isFinite(cookTime) || cookTime <= 0.0) {
			cookTime = baseCookTicks;
		}

		double multiplier = getFuelEfficiencyMultiplier(furnace);
		if (!Double.isFinite(multiplier) || multiplier <= 0.0) {
			multiplier = 1.0;
		}

		double speedFactor = cookTime / (double) baseCookTicks;
		return Math.max(1, (int) Math.round(baseTicks * speedFactor * multiplier));
	}

	public static boolean isSmokerAdditionalInput(ItemStack stack) {
		if (!isEnabled() || stack == null || stack.isEmpty()) {
			return false;
		}
		return smokerAdditionalInputs.contains(stack.getItem());
	}

	public static boolean isBlastAdditionalInput(ItemStack stack) {
		if (!isEnabled() || stack == null || stack.isEmpty()) {
			return false;
		}
		return blastAdditionalInputs.contains(stack.getItem());
	}

	private static void rebuildRules() {
		Map<String, Double> entries = configuration.fuelItems;
		if (entries == null) {
			entries = CustomSmeltingConfig.buildDefaultFuelItems();
			configuration.fuelItems = entries;
		}
		fuelOverrides = buildOverrides(entries);
		smokerAdditionalInputs = buildItemSet(configuration.smokerAdditionalInputs);
		blastAdditionalInputs = buildItemSet(configuration.blastFurnaceAdditionalInputs);
	}

	private static Map<Item, Integer> buildOverrides(Map<String, Double> entries) {
		Map<Item, Integer> overrides = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry : entries.entrySet()) {
			Item item = resolveItem(entry.getKey());
			if (item == null) {
				continue;
			}
			int ticks = toTicks(entry.getValue());
			if (ticks <= 0) {
				continue;
			}
			overrides.put(item, ticks);
		}
		return Map.copyOf(overrides);
	}

	private static Set<Item> buildItemSet(List<String> entries) {
		Set<Item> items = new LinkedHashSet<>();
		if (entries == null) {
			return Set.of();
		}
		for (String entry : entries) {
			Item item = resolveItem(entry);
			if (item != null) {
				items.add(item);
			}
		}
		return Set.copyOf(items);
	}

	private static boolean pruneInvalidEntries(JsonObject root) {
		if (root == null) {
			return false;
		}

		boolean changed = false;
		Map<String, Double> cleanedFuel = new LinkedHashMap<>();
		boolean fuelChanged = false;
		for (Map.Entry<String, Double> entry : configuration.fuelItems.entrySet()) {
			if (resolveItem(entry.getKey()) == null) {
				fuelChanged = true;
				continue;
			}
			cleanedFuel.put(entry.getKey(), entry.getValue());
		}

		if (fuelChanged) {
			JsonObject replacement = new JsonObject();
			for (Map.Entry<String, Double> entry : cleanedFuel.entrySet()) {
				replacement.addProperty(entry.getKey(), entry.getValue());
			}
			root.add("fuelItems", replacement);
			configuration.fuelItems = cleanedFuel;
			changed = true;
		}

		List<String> cleanedSmoker = pruneAdditionalInputs(root, "smokerAdditionalInputs",
			configuration.smokerAdditionalInputs);
		if (!cleanedSmoker.equals(configuration.smokerAdditionalInputs)) {
			configuration.smokerAdditionalInputs = cleanedSmoker;
			changed = true;
		}

		List<String> cleanedBlast = pruneAdditionalInputs(root, "blastFurnaceAdditionalInputs",
			configuration.blastFurnaceAdditionalInputs);
		if (!cleanedBlast.equals(configuration.blastFurnaceAdditionalInputs)) {
			configuration.blastFurnaceAdditionalInputs = cleanedBlast;
			changed = true;
		}

		return changed;
	}

	private static List<String> pruneAdditionalInputs(JsonObject root, String key, List<String> values) {
		List<String> cleaned = new ArrayList<>();
		boolean listChanged = false;
		if (values == null) {
			return cleaned;
		}

		for (String value : values) {
			if (resolveItem(value) == null) {
				listChanged = true;
				continue;
			}
			cleaned.add(value);
		}

		if (listChanged) {
			JsonArray replacement = new JsonArray();
			for (String value : cleaned) {
				replacement.add(value);
			}
			root.add(key, replacement);
		}

		return cleaned;
	}

	private static Item resolveItem(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}

		if (key.charAt(0) == '#') {
			MadokuCraftSmelting.LOGGER.warn("Skipping smelting entry '{}' because tags are not supported; use item IDs instead", key);
			return null;
		}

		Identifier itemId = Identifier.tryParse(key);
		if (itemId == null) {
			MadokuCraftSmelting.LOGGER.warn("Skipping smelting entry '{}' because it is not a valid identifier", key);
			return null;
		}

		if (!Registries.ITEM.containsId(itemId)) {
			MadokuCraftSmelting.LOGGER.warn("Skipping smelting entry '{}' because '{}' is not registered", key, itemId);
			return null;
		}

		return Registries.ITEM.get(itemId);
	}

	private static int toTicks(Double value) {
		if (value == null || !Double.isFinite(value) || value <= 0.0) {
			return 0;
		}
		return Math.max(1, (int) Math.round(value));
	}

	private static int toTicks(double value) {
		if (!Double.isFinite(value) || value <= 0.0) {
			return 0;
		}
		return Math.max(1, (int) Math.round(value));
	}

	private static double getConfiguredTime(AbstractFurnaceBlockEntity furnace) {
		if (furnace instanceof SmokerBlockEntity) {
			return configuration.smokerSmeltingSpeed;
		}
		if (furnace instanceof BlastFurnaceBlockEntity) {
			return configuration.blastFurnaceSpeed;
		}
		return configuration.furnaceSmeltingSpeed;
	}

	private static int getBaseCookTicks(AbstractFurnaceBlockEntity furnace) {
		if (furnace instanceof SmokerBlockEntity) {
			return BASE_SMOKER_COOK_TICKS;
		}
		if (furnace instanceof BlastFurnaceBlockEntity) {
			return BASE_BLAST_COOK_TICKS;
		}
		return BASE_FURNACE_COOK_TICKS;
	}

	private static double getFuelEfficiencyMultiplier(AbstractFurnaceBlockEntity furnace) {
		if (furnace instanceof SmokerBlockEntity) {
			return configuration.smokerFuelEfficiencyMultiplier;
		}
		if (furnace instanceof BlastFurnaceBlockEntity) {
			return configuration.blastFurnaceFuelEfficiencyMultiplier;
		}
		return configuration.furnaceFuelEfficiencyMultiplier;
	}
}
