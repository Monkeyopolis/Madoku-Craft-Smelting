package madoku.craft.smelting.mixin;

import madoku.craft.debug.MadokuDebug;
import madoku.craft.smelting.system.MadokuSmeltingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		if (MadokuSmeltingManager.isEnabled() && furnace != null) {
			int currentTotal = ((AbstractFurnaceCookTimeAccessor) furnace).madokuCraft$getCookingTotalTime();
			int desiredTotal = MadokuSmeltingManager.getCookTimeTicks(furnace, currentTotal);
			if (currentTotal > 0 && desiredTotal > 0 && currentTotal != desiredTotal) {
				((AbstractFurnaceCookTimeAccessor) furnace).madokuCraft$setCookingTotalTime(desiredTotal);
				furnace.setChanged();
				if (MadokuDebug.shouldEmit(MadokuDebug.Domain.SMELTING, "smelting.cook_time_sync")) {
					MadokuDebug.event("smelting.cook_time_sync", MadokuDebug.Domain.SMELTING)
						.side(MadokuDebug.Side.SERVER)
						.subject("furnace:" + furnace.getClass().getSimpleName())
						.field("current_ticks", currentTotal)
						.field("desired_ticks", desiredTotal)
						.log();
				}
			}
		}
		MadokuSmeltingManager.onFurnaceServerTick(level, blockPos, blockState, furnace);
	}
}
