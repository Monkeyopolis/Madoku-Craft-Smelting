package madoku.craft.smelting.mixin;

import madoku.craft.smelting.system.MadokuSmeltingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
	@Shadow
	@Final
	@Mutable
	private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void madokuSmelting$wrapQuickCheck(
		BlockEntityType<?> blockEntityType,
		BlockPos pos,
		BlockState state,
		RecipeType<? extends AbstractCookingRecipe> recipeType,
		CallbackInfo ci
	) {
		if (!MadokuSmeltingManager.shouldWrapRecipeType(recipeType)) {
			return;
		}

		RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> original = this.quickCheck;
		this.quickCheck = new FurnaceFallbackCachedCheck(original, blockEntityType, recipeType);
	}

	private static final class FurnaceFallbackCachedCheck implements RecipeManager.CachedCheck<SingleRecipeInput, AbstractCookingRecipe> {
		private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> delegate;
		private final BlockEntityType<?> blockEntityType;
		private final RecipeType<? extends AbstractCookingRecipe> recipeType;

		private FurnaceFallbackCachedCheck(
			RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> delegate,
			BlockEntityType<?> blockEntityType,
			RecipeType<? extends AbstractCookingRecipe> recipeType
		) {
			this.delegate = delegate;
			this.blockEntityType = blockEntityType;
			this.recipeType = recipeType;
		}

		@Override
		public Optional<RecipeHolder<AbstractCookingRecipe>> getRecipeFor(SingleRecipeInput input, Level world) {
			Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> original = this.delegate.getRecipeFor(input, world);
			if (original.isPresent()) {
				return Optional.of(cast(original.get()));
			}

			ItemStack stack = input.item();
			if (stack.isEmpty() || !MadokuSmeltingManager.isEnabled()) {
				return Optional.empty();
			}

			if (!MadokuSmeltingManager.isAdditionalInput(this.blockEntityType, this.recipeType, stack)) {
				return Optional.empty();
			}

			return world.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, input, world)
				.map(FurnaceFallbackCachedCheck::cast);
		}

		@SuppressWarnings("unchecked")
		private static RecipeHolder<AbstractCookingRecipe> cast(RecipeHolder<?> holder) {
			return (RecipeHolder<AbstractCookingRecipe>) holder;
		}
	}
}
