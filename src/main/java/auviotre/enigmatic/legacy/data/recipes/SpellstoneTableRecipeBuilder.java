package auviotre.enigmatic.legacy.data.recipes;

// TODO(port): datagen-only recipe builder, moved out of SpellstoneTableRecipe unchanged. It still uses the 1.21.1 RecipeBuilder
// API and is ported with the rest of the data generators in phase 9 (this package is excluded from the build).

import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticBlocks;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import auviotre.enigmatic.legacy.api.item.IItemHelper;

public class SpellstoneTableRecipeBuilder implements RecipeBuilder {
    private final Map<String, Criterion<?>> criteria;
    private final int debrisCount;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private boolean allDifferent = false;

    private SpellstoneTableRecipeBuilder(ItemLike resultItem, int debrisCount) {
        this.result = resultItem.asItem().getDefaultInstance();
        this.debrisCount = debrisCount;
        this.criteria = new LinkedHashMap<>();
        this.ingredients = NonNullList.create();
    }

    public static SpellstoneTableRecipeBuilder spell(ItemLike result, int debrisCount) {
        return new SpellstoneTableRecipeBuilder(result, debrisCount);
    }

    public SpellstoneTableRecipeBuilder allDifferent() {
        this.allDifferent = true;
        return this;
    }

    public SpellstoneTableRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public SpellstoneTableRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public SpellstoneTableRecipeBuilder requires(ItemLike item, int quantity) {
        for (int i = 0; i < quantity; ++i) this.requires(Ingredient.of(item));
        return this;
    }

    public SpellstoneTableRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public SpellstoneTableRecipeBuilder requires(Ingredient ingredient, int quantity) {
        for (int i = 0; i < quantity; ++i) this.ingredients.add(ingredient);
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String s) {
        return this;
    }

    @Override
    public Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        Identifier location = BuiltInRegistries.ITEM.getKey(this.getResult());
        this.save(recipeOutput, Identifier.fromNamespaceAndPath(location.getNamespace(), "spellstone/" + location.getPath()));
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, Identifier id) {
        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);
        SpellstoneTableRecipe recipe = new SpellstoneTableRecipe(this.result, this.debrisCount, this.ingredients, this.allDifferent);
        recipeOutput.accept(id, recipe, builder.build(id.withPrefix("recipes/spellstone_table/")));
    }
}
