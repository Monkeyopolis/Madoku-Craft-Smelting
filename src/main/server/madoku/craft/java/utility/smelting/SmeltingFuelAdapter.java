package madoku.craft.java.utility.smelting;

import net.minecraft.world.item.ItemStack;

/** Optional adapter for module-specific fuel duration adjustments. */
@FunctionalInterface
public interface SmeltingFuelAdapter {
	int adjustFuelTicks(ItemStack stack, int originalTicks);
}
