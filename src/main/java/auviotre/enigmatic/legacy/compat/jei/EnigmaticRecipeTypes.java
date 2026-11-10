package auviotre.enigmatic.legacy.compat.jei;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.compat.jei.category.TaintingCategory;
import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;

public class EnigmaticRecipeTypes {
    // 26.1.2-port: JEI's own RecipeType class is deprecated for removal (its name collides with vanilla's); the
    // replacements are IRecipeType.create and, for a type that wraps a vanilla recipe type, IRecipeHolderType.create.
    public static final IRecipeType<TaintingCategory.Recipe> TAINTING = IRecipeType.create(EnigmaticLegacy.MODID, "tainting", TaintingCategory.Recipe.class);
    public static final IRecipeHolderType<SpellstoneTableRecipe> SPELLSTONE_CRAFTING = IRecipeHolderType.create(EnigmaticRecipes.SPELLSTONE_CRAFTING.get());

}
