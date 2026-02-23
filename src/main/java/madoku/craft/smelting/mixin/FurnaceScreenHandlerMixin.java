package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.CustomSmeltingManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class FurnaceScreenHandlerMixin {
	@Inject(method = "isSmeltable", at = @At("HEAD"), cancellable = true)
	private void madokuSmelting$isSmeltable(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
		if (!CustomSmeltingManager.isEnabled() || item == null || item.isEmpty()) {
			return;
		}

		if ((Object) this instanceof BlastFurnaceScreenHandler && CustomSmeltingManager.isBlastAdditionalInput(item)) {
			cir.setReturnValue(true);
			return;
		}
		if ((Object) this instanceof SmokerScreenHandler && CustomSmeltingManager.isSmokerAdditionalInput(item)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
	private void madokuSmelting$isFuel(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
		if (!CustomSmeltingManager.isEnabled()) {
			return;
		}
		cir.setReturnValue(CustomSmeltingManager.isFuelItem(item));
	}
}
