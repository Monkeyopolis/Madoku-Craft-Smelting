package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.CustomSmeltingManager;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceCookTimeMixin {
	@Inject(method = "getCookTime", at = @At("RETURN"), cancellable = true)
	private static void madokuSmelting$adjustCookTime(ServerWorld world, AbstractFurnaceBlockEntity furnace,
			CallbackInfoReturnable<Integer> cir) {
		if (!CustomSmeltingManager.isEnabled()) {
			return;
		}

		int original = cir.getReturnValue();
		int configured = CustomSmeltingManager.getCookTimeTicks(furnace, original);
		if (configured > 0 && configured != original) {
			cir.setReturnValue(configured);
		}
	}

	@Inject(method = "getFuelTime", at = @At("RETURN"), cancellable = true)
	private void madokuSmelting$adjustFuelTime(FuelRegistry fuelRegistry, ItemStack stack,
			CallbackInfoReturnable<Integer> cir) {
		if (!CustomSmeltingManager.isEnabled()) {
			return;
		}

		int original = cir.getReturnValue();
		AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
		int configured = CustomSmeltingManager.getFuelTimeTicks(self, stack);
		if (configured != original) {
			cir.setReturnValue(configured);
		}
	}

	@Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
	private void madokuSmelting$allowCustomFuel(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (slot != 1) {
			return;
		}

		if (!CustomSmeltingManager.isEnabled()) {
			return;
		}

		cir.setReturnValue(CustomSmeltingManager.isFuelItem(stack));
	}
}
