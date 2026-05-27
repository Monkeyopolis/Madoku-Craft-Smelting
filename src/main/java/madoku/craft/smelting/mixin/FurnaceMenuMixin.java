package madoku.craft.smelting.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceMenu.class)
public abstract class FurnaceMenuMixin {
	@Shadow
	@Final
	protected Level level;

	@Shadow
	@Final
	private RecipeType<? extends AbstractCookingRecipe> recipeType;

	@Shadow
	@Final
	private ContainerData data;

	@Inject(method = "isLit", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$fixLitStateForLargeFuelValues(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			return;
		}

		int litTime = this.data.get(0);
		if (litTime < 0) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getLitProgress", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$fixLitProgressForLargeFuelValues(CallbackInfoReturnable<Float> cir) {
		int litTime = this.data.get(0);
		int litDuration = this.data.get(1);
		if (litTime >= 0 && litDuration >= 0) {
			return;
		}

		int normalizedLitTime = litTime & 0xFFFF;
		int normalizedDuration = litDuration & 0xFFFF;
		if (normalizedDuration <= 0) {
			normalizedDuration = 200;
		}

		float progress = (float) normalizedLitTime / (float) normalizedDuration;
		cir.setReturnValue(Mth.clamp(progress, 0.0F, 1.0F));
	}

	@Inject(method = "canSmelt", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$allowShiftClickForAddedCookingRecipes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() || stack == null || stack.isEmpty() || this.level == null || this.recipeType == null) {
			return;
		}
		if (!(this.level.recipeAccess() instanceof RecipeManager recipeManager)) {
			return;
		}

		boolean hasRecipe = recipeManager
			.getRecipeFor(this.recipeType, new SingleRecipeInput(stack), this.level)
			.isPresent();
		if (hasRecipe) {
			cir.setReturnValue(true);
		}
	}
}
