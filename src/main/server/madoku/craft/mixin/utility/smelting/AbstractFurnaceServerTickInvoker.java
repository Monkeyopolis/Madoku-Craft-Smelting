package madoku.craft.mixin.utility.smelting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceServerTickInvoker {
	@Invoker("serverTick")
	static void madokuCraft$invokeServerTick(
		ServerLevel level,
		BlockPos blockPos,
		BlockState blockState,
		AbstractFurnaceBlockEntity furnace
	) {
		throw new AssertionError("Invoker not transformed");
	}
}


