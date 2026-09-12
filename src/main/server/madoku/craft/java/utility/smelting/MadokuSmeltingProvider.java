package madoku.craft.java.utility.smelting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Built-in provider for Madoku Smelting. */
public final class MadokuSmeltingProvider implements SmeltingProvider {
	@Override public boolean isEnabled() { return MadokuSmeltingManager.isEnabled(); }
	@Override public int getCookTimeTicks(AbstractFurnaceBlockEntity furnace, int originalTicks) { return MadokuSmeltingManager.getCookTimeTicks(furnace, originalTicks); }
	@Override public int getCookTimeTicks(RecipeType<?> recipeType, int originalTicks) { return MadokuSmeltingManager.getCookTimeTicks(recipeType, originalTicks); }
	@Override public int getAdjustedFuelTicks(AbstractFurnaceBlockEntity furnace, ItemStack stack, int originalTicks) { return MadokuSmeltingManager.getAdjustedFuelTicks(furnace, stack, originalTicks); }
	@Override public void onFurnaceServerTick(ServerLevel level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity furnace) { MadokuSmeltingManager.onFurnaceServerTick(level, blockPos, blockState, furnace); }
	@Override public String describeRecipeType(RecipeType<?> recipeType) { return MadokuSmeltingManager.describeRecipeType(recipeType); }
}
