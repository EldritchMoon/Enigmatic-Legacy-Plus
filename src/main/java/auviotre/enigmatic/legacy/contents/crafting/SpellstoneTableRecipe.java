package auviotre.enigmatic.legacy.contents.crafting;

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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import java.util.List;

public class SpellstoneTableRecipe implements Recipe<SpellstoneTableRecipe.Input> {
    // 26.1.2-port: result is an ItemStackTemplate and ingredients a plain List (Ingredient.EMPTY is gone);
    // RecipeSerializer is a record now, so the codecs live here. JSON fields are unchanged.
    public static final MapCodec<SpellstoneTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.INT.optionalFieldOf("debris_count", 0).forGetter(recipe -> recipe.debrisCount),
            Ingredient.CODEC.listOf(1, 7).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
            Codec.BOOL.optionalFieldOf("all_different", false).forGetter(recipe -> recipe.allDifferent)
    ).apply(instance, SpellstoneTableRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellstoneTableRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            ByteBufCodecs.VAR_INT, recipe -> recipe.debrisCount,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.ingredients,
            ByteBufCodecs.BOOL, recipe -> recipe.allDifferent,
            SpellstoneTableRecipe::new
    );
    public static final RecipeSerializer<SpellstoneTableRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    final ItemStackTemplate result;
    final int debrisCount;
    final List<Ingredient> ingredients;
    final boolean allDifferent;

    public SpellstoneTableRecipe(ItemStackTemplate result, int count, List<Ingredient> ingredients, boolean flag) {
        this.result = result;
        this.debrisCount = count;
        this.ingredients = ingredients;
        this.allDifferent = flag;
    }

    @Override
    public boolean matches(@NotNull Input input, Level level) {
        ItemStack core = input.core();
        if (core.isEmpty() || !core.is(EnigmaticItems.SPELLCORE)) return false;
        if (!input.noSpellstone) return false;
        if (input.debris().getCount() < this.debrisCount) return false;
        int size = input.ingredients().size();
        List<ItemStack> items = new ArrayList<>(size);
        for (ItemStack ingredient : input.ingredients())
            if (!ingredient.isEmpty()) items.add(ingredient);
        if (allDifferent) {
            Map<Item, Integer> map = new HashMap<>();
            for (ItemStack stack : items) {
                if (map.containsKey(stack.getItem())) {
                    map.put(stack.getItem(), map.get(stack.getItem()) + 1);
                } else map.put(stack.getItem(), 1);
            }
            for (Integer value : map.values()) {
                if (value > 1) return false;
            }
        }
        return RecipeMatcher.findMatches(items, this.ingredients) != null;
    }

    // 26.1.2-port: no longer a Recipe method (only crafting recipes have it); SpellstoneTableMenu calls it directly.
    public NonNullList<ItemStack> getRemainingItems(Input input) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        input.debris().shrink(this.debrisCount);
        nonnulllist.set(0, input.debris().copy());
        for (int i = 2; i < nonnulllist.size(); ++i) {
            ItemStack item = input.getItem(i);
            ItemStack remainder = IItemHelper.getCraftingRemainder(item); // 26.1.2-port: hasCraftingRemainingItem removed
            if (!remainder.isEmpty()) nonnulllist.set(i, remainder);
        }

        return nonnulllist;
    }

    @Override
    public ItemStack assemble(Input input) {
        return this.result.create();
    }

    public int getCount() {
        return this.debrisCount;
    }

    public boolean isAllDifferent() {
        return this.allDifferent;
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public ItemStack getResultItem() {
        return this.result.create();
    }

    // 26.1.2-port: methods every Recipe needs now. The Spellstone Table has no recipe book, so the recipe is not
    // placeable; the category only satisfies the interface. getToastSymbol/canCraftInDimensions were removed.
    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<SpellstoneTableRecipe> getSerializer() {
        return EnigmaticRecipes.SPELLSTONE_TABLE.get();
    }

    @Override
    public RecipeType<SpellstoneTableRecipe> getType() {
        return EnigmaticRecipes.SPELLSTONE_CRAFTING.get();
    }

    public record Input(ItemStack core, ItemStack debris, List<ItemStack> ingredients,
                        boolean noSpellstone) implements RecipeInput {
        @Override
        public ItemStack getItem(int id) {
            if (id == 0) return core;
            else if (id == 1) return debris;
            else if (id >= size()) throw new IllegalArgumentException("Recipe does not contain slot " + id);
            return ingredients.get(id - 2);
        }

        @Override
        public int size() {
            return ingredients.size() + 2;
        }
    }

    // 26.1.2-port: the datagen-only Builder moved to data/recipes/SpellstoneTableRecipeBuilder (parked until phase 9).
}
