package madoku.craft.mixin.utility.smelting;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceCookTimeAccessor {
	@Accessor("cookingTotalTime")
	int madokuCraft$getCookingTotalTime();

	@Accessor("cookingTotalTime")
	void madokuCraft$setCookingTotalTime(int value);
}


