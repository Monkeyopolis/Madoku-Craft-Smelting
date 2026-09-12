package madoku.craft.mixin.utility.smelting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import madoku.craft.java.utility.smelting.SmeltingAPIManager;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceServerTickMixin {
	@Inject(method = "serverTick", at = @At("TAIL"))
	private static void madokuCraft$trackFurnaceScheduling(
		ServerLevel level,
		BlockPos blockPos,
		BlockState blockState,
		AbstractFurnaceBlockEntity furnace,
		CallbackInfo ci
	) {
		if (SmeltingAPIManager.isEnabled() && furnace != null) {
			int currentTotal = ((AbstractFurnaceCookTimeAccessor) furnace).madokuCraft$getCookingTotalTime();
			int desiredTotal = SmeltingAPIManager.getCookTimeTicks(furnace, currentTotal);
			if (currentTotal > 0 && desiredTotal > 0 && currentTotal != desiredTotal) {
				((AbstractFurnaceCookTimeAccessor) furnace).madokuCraft$setCookingTotalTime(desiredTotal);
				furnace.setChanged();
			}
		}
		SmeltingAPIManager.onFurnaceServerTick(level, blockPos, blockState, furnace);
	}
}


