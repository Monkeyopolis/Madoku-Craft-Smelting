package madoku.craft.smelting.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceCookTimeAccessor {
	@Accessor("cookingTotalTime")
	int madokuCraftSmelting$getCookingTotalTime();

	@Accessor("cookingTotalTime")
	void madokuCraftSmelting$setCookingTotalTime(int value);
}
