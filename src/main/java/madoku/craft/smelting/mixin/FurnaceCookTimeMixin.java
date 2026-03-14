package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.MadokuSmeltingManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceCookTimeMixin {
	@Inject(method = "getTotalCookTime", at = @At("RETURN"), cancellable = true)
	private static void madokuSmelting$adjustCookTime(
		ServerLevel world,
		AbstractFurnaceBlockEntity furnace,
		CallbackInfoReturnable<Integer> cir
	) {
		if (!MadokuSmeltingManager.isEnabled()) {
			return;
		}

		int original = cir.getReturnValue();
		int configured = MadokuSmeltingManager.getCookTimeTicks(furnace, original);
		if (configured > 0 && configured != original) {
			cir.setReturnValue(configured);
		}
	}

	@Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
	private void madokuSmelting$adjustFuelDuration(FuelValues fuelValues, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (!MadokuSmeltingManager.isEnabled()) {
			return;
		}

		int original = cir.getReturnValue();
		AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
		int adjusted = MadokuSmeltingManager.getAdjustedFuelTicks(self, stack, original);
		if (adjusted != original) {
			cir.setReturnValue(adjusted);
		}
	}
}
