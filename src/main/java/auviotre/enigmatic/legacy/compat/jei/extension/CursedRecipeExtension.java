package auviotre.enigmatic.legacy.compat.jei.extension;

import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.contents.crafting.CursedShapedRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CursedRecipeExtension implements ICraftingCategoryExtension<CursedShapedRecipe> {
    // 26.1.2-port: new required methods. JEI asks an extension for its inputs as vanilla slot displays, and for the
    // grid size of a shaped recipe. setRecipe() below still draws the recipe itself, with tainted hearts.
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<CursedShapedRecipe> recipeHolder) {
        return recipeHolder.value().getIngredients().stream().map(Ingredient::optionalIngredientToDisplay).toList();
    }

    @Override
    public int getWidth(RecipeHolder<CursedShapedRecipe> recipeHolder) {
        return recipeHolder.value().getWidth();
    }

    @Override
    public int getHeight(RecipeHolder<CursedShapedRecipe> recipeHolder) {
        return recipeHolder.value().getHeight();
    }

    @Override
    public void setRecipe(@NotNull RecipeHolder<CursedShapedRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        CursedShapedRecipe recipe = recipeHolder.value();
        List<List<ItemStack>> inputs = new ArrayList<>();
        // 26.1.2-port: an empty grid cell is an empty Optional (Ingredient.EMPTY is gone), and getItems() became
        // items(), a stream of item holders; each becomes a stack as before.
        for (Optional<Ingredient> ingredient : recipe.getIngredients()) {
            List<ItemStack> list = new ArrayList<>();
            if (ingredient.isPresent()) {
                ingredient.get().items().forEach(item -> {
                    ItemStack stack = new ItemStack(item);
                    if (stack.getItem() instanceof ITaintable) stack.set(EnigmaticComponents.TAINTABLE, true);
                    list.add(stack);
                });
            }
            inputs.add(list);
        }
        craftingGridHelper.createAndSetInputs(builder, inputs, recipe.getWidth(), recipe.getHeight());
        // 26.1.2-port: getResultItem() no longer takes registries, so the level lookup that was here is gone
        ItemStack result = recipe.getResultItem();
        if (result.getItem() instanceof ITaintable) result.set(EnigmaticComponents.TAINTABLE, true);
        craftingGridHelper.createAndSetOutputs(builder, List.of(result));
    }
}
