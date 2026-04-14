package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.MadokuSmeltingManager;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
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
public abstract class FurnaceMenuMixin {
	@Shadow
	@Final
	private RecipeType<? extends AbstractCookingRecipe> recipeType;

	@Shadow
	@Final
	private ContainerData data;

	@Inject(method = "canSmelt", at = @At("HEAD"), cancellable = true)
	private void madokuCraftSmelting$canSmelt(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
		if (!MadokuSmeltingManager.isEnabled() || item == null || item.isEmpty()) {
			return;
		}

		if (MadokuSmeltingManager.isAdditionalInput(this.recipeType, item)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isLit", at = @At("RETURN"), cancellable = true)
	private void madokuCraftSmelting$fixLitStateForLargeFuelValues(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			return;
		}

		int litTime = this.data.get(0);
		if (litTime < 0) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getLitProgress", at = @At("RETURN"), cancellable = true)
	private void madokuCraftSmelting$fixLitProgressForLargeFuelValues(CallbackInfoReturnable<Float> cir) {
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
}
