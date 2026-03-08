package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.CustomSmeltingManager;
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
	private static void madokuSmelting$trackFurnaceScheduling(
		ServerLevel level,
		BlockPos blockPos,
		BlockState blockState,
		AbstractFurnaceBlockEntity furnace,
		CallbackInfo ci
	) {
		CustomSmeltingManager.onFurnaceServerTick(level, blockPos, blockState, furnace);
	}
}
