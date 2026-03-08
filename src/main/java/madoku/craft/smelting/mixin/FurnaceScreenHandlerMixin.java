package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.CustomSmeltingManager;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceMenu.class)
public abstract class FurnaceScreenHandlerMixin {
	@Shadow
	@Final
	private RecipeType<? extends AbstractCookingRecipe> recipeType;

	@Inject(method = "canSmelt", at = @At("HEAD"), cancellable = true)
	private void madokuSmelting$canSmelt(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
		if (!CustomSmeltingManager.isEnabled() || item == null || item.isEmpty()) {
			return;
		}

		if (CustomSmeltingManager.isAdditionalInput(this.recipeType, item)) {
			cir.setReturnValue(true);
		}
	}
}
