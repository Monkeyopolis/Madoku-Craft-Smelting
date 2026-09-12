package madoku.craft.java.utility.smelting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Public provider contract for Madoku Smelting runtime behavior. */
public interface SmeltingProvider {
	default boolean isEnabled() { return false; }
	default int getCookTimeTicks(AbstractFurnaceBlockEntity furnace, int originalTicks) { return originalTicks; }
	default int getCookTimeTicks(RecipeType<?> recipeType, int originalTicks) { return originalTicks; }
	default int getAdjustedFuelTicks(AbstractFurnaceBlockEntity furnace, ItemStack stack, int originalTicks) { return originalTicks; }
	default void onFurnaceServerTick(ServerLevel level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity furnace) { }
	default String describeRecipeType(RecipeType<?> recipeType) { return "unknown"; }
}
