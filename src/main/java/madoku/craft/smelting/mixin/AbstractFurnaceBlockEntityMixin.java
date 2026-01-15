package madoku.craft.smelting.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import madoku.craft.smelting.system.CustomSmeltingManager;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
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
	private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void madokuSmelting$wrapMatchGetter(
		BlockEntityType<?> blockEntityType,
		BlockPos pos,
		BlockState state,
		RecipeType<? extends AbstractCookingRecipe> recipeType,
		CallbackInfo ci
	) {
		if (!shouldWrap(recipeType)) {
			return;
		}

		ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> original = this.matchGetter;
		this.matchGetter = new FurnaceFallbackMatchGetter(original, recipeType);
	}

	private boolean shouldWrap(RecipeType<? extends AbstractCookingRecipe> recipeType) {
		return recipeType == RecipeType.SMOKING || recipeType == RecipeType.BLASTING;
	}

	private static final class FurnaceFallbackMatchGetter implements ServerRecipeManager.MatchGetter<SingleStackRecipeInput, AbstractCookingRecipe> {
		private final ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> delegate;
		private final RecipeType<? extends AbstractCookingRecipe> recipeType;

		private FurnaceFallbackMatchGetter(
			ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> delegate,
			RecipeType<? extends AbstractCookingRecipe> recipeType
		) {
			this.delegate = delegate;
			this.recipeType = recipeType;
		}

		@Override
		public Optional<RecipeEntry<AbstractCookingRecipe>> getFirstMatch(SingleStackRecipeInput input, ServerWorld world) {
			Optional<? extends RecipeEntry<? extends AbstractCookingRecipe>> original = this.delegate.getFirstMatch(input, world);
			if (original.isPresent()) {
				return Optional.of(cast(original.get()));
			}

			ItemStack stack = input.item();
			if (shouldFallback(stack)) {
				return world.getRecipeManager()
					.getFirstMatch(RecipeType.SMELTING, input, world)
					.map(FurnaceFallbackMatchGetter::cast);
			}

			return Optional.empty();
		}

		private boolean shouldFallback(ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}

			if (!CustomSmeltingManager.isEnabled()) {
				return false;
			}

			if (this.recipeType == RecipeType.SMOKING) {
				return isSmokerCandidate(stack);
			}

			if (this.recipeType == RecipeType.BLASTING) {
				return isBlastCandidate(stack);
			}

			return false;
		}

		private static boolean isSmokerCandidate(ItemStack stack) {
			return CustomSmeltingManager.isSmokerAdditionalInput(stack);
		}

		private static boolean isBlastCandidate(ItemStack stack) {
			return CustomSmeltingManager.isBlastAdditionalInput(stack);
		}

		@SuppressWarnings("unchecked")
		private static RecipeEntry<AbstractCookingRecipe> cast(RecipeEntry<?> entry) {
			return (RecipeEntry<AbstractCookingRecipe>) entry;
		}
	}
}
