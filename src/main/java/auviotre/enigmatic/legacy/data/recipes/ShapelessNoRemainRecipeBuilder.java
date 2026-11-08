package auviotre.enigmatic.legacy.data.recipes;

// TODO(port): datagen-only recipe builder, moved out of ShapelessNoRemainRecipe unchanged. It still uses the 1.21.1 RecipeBuilder
// API and is ported with the rest of the data generators in phase 9 (this package is excluded from the build).

import auviotre.enigmatic.legacy.contents.crafting.ShapelessNoRemainRecipe;
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
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ShapelessNoRemainRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final ItemStack resultStack;
    private final NonNullList<Ingredient> ingredients;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;

    public ShapelessNoRemainRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this(category, new ItemStack(result, count));
    }

    public ShapelessNoRemainRecipeBuilder(RecipeCategory category, ItemStack result) {
        this.ingredients = NonNullList.create();
        this.criteria = new LinkedHashMap<>();
        this.category = category;
        this.result = result.getItem();
        this.resultStack = result;
    }

    public static ShapelessNoRemainRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return new ShapelessNoRemainRecipeBuilder(category, result, 1);
    }

    public static ShapelessNoRemainRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new ShapelessNoRemainRecipeBuilder(category, result, count);
    }

    public static ShapelessNoRemainRecipeBuilder shapeless(RecipeCategory category, ItemStack result) {
        return new ShapelessNoRemainRecipeBuilder(category, result);
    }

    public ShapelessNoRemainRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public ShapelessNoRemainRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public ShapelessNoRemainRecipeBuilder requires(ItemLike item, int quantity) {
        for (int i = 0; i < quantity; ++i) this.requires(Ingredient.of(item));
        return this;
    }

    public ShapelessNoRemainRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ShapelessNoRemainRecipeBuilder requires(Ingredient ingredient, int quantity) {
        for (int i = 0; i < quantity; ++i) this.ingredients.add(ingredient);
        return this;
    }

    @Override
    public ShapelessNoRemainRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public ShapelessNoRemainRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, Identifier id) {
        if (this.criteria.isEmpty()) throw new IllegalStateException("No way of obtaining recipe " + id);
        Advancement.Builder advancement$builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(advancement$builder);
        this.criteria.forEach(advancement$builder::addCriterion);
        ShapelessNoRemainRecipe recipe = new ShapelessNoRemainRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), this.resultStack, this.ingredients);
        recipeOutput.accept(id, recipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
