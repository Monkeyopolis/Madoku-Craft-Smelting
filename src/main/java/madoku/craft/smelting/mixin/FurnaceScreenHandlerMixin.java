package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.CustomSmeltingManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class FurnaceScreenHandlerMixin {
	@Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
	private void madokuSmelting$isFuel(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
		if (!CustomSmeltingManager.isEnabled()) {
			return;
		}
		cir.setReturnValue(CustomSmeltingManager.isFuelItem(item));
	}
}
