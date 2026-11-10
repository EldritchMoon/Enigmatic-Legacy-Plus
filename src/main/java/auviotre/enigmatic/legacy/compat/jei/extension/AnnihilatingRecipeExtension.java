package auviotre.enigmatic.legacy.compat.jei.extension;

import auviotre.enigmatic.legacy.contents.crafting.AnnihilatingRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnnihilatingRecipeExtension implements ICraftingCategoryExtension<AnnihilatingRecipe> {
    // 26.1.2-port: new required methods. JEI asks an extension for its inputs as vanilla slot displays. The grid is the
    // one the old setRecipe() filled slot by slot, in the same order: abyssal items in the corners, an Evil Ingot on
    // top, a tainted Abyssal Heart in the middle and a tainted Twisted Heart below it. Ingredient.of(TagKey) is gone;
    // a tag slot display shows every item in the tag.
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<AnnihilatingRecipe> recipeHolder) {
        SlotDisplay abyssal = new SlotDisplay.TagSlotDisplay(EnigmaticTags.Items.ABYSSAL_ITEMS);
        ItemStack abyssalHeart = EnigmaticItems.ABYSSAL_HEART.toStack();
        abyssalHeart.set(EnigmaticComponents.TAINTABLE, true);
        ItemStack twistedHeart = EnigmaticItems.TWISTED_HEART.toStack();
        twistedHeart.set(EnigmaticComponents.TAINTABLE, true);
        return List.of(
                abyssal, new SlotDisplay.ItemSlotDisplay(EnigmaticItems.EVIL_INGOT), abyssal,
                SlotDisplay.Empty.INSTANCE, new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(abyssalHeart)), SlotDisplay.Empty.INSTANCE,
                abyssal, new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(twistedHeart)), abyssal
        );
    }

    @Override
    public int getWidth(RecipeHolder<AnnihilatingRecipe> recipeHolder) {
        return 3;
    }

    @Override
    public int getHeight(RecipeHolder<AnnihilatingRecipe> recipeHolder) {
        return 3;
    }

    // 26.1.2-port: still overridden. JEI's default takes the output from Recipe.display(), which a special recipe like
    // this one leaves empty. Recipe.getResultItem() is gone; assemble() ignores its input and returns the sword.
    @Override
    public void setRecipe(@NotNull RecipeHolder<AnnihilatingRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        craftingGridHelper.createAndSetIngredientsFromDisplays(builder, this.getIngredients(recipeHolder), 3, 3);
        ItemStack result = recipeHolder.value().assemble(CraftingInput.EMPTY);
        craftingGridHelper.createAndSetOutputs(builder, List.of(result));
    }
}
