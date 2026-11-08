package auviotre.enigmatic.legacy.contents.crafting;

import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

/**
 * A shapeless recipe that leaves no remainders (bottles, buckets) behind.
 * 26.1.2-port: same structure as the 26.1 ShapelessRecipe (CommonInfo/CraftingBookInfo, ItemStackTemplate result,
 * List<Ingredient>); RecipeSerializer is a record now, so the codecs live here.
 */
public class ShapelessNoRemainRecipe extends ShapelessRecipe {
    public static final MapCodec<ShapelessNoRemainRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9)).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
    ).apply(instance, ShapelessNoRemainRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessNoRemainRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.ingredients,
            ShapelessNoRemainRecipe::new
    );
    public static final RecipeSerializer<ShapelessNoRemainRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    public ShapelessNoRemainRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo, result, ingredients);
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        // 26.1.2-port: ShapelessRecipe.getSerializer() is typed RecipeSerializer<ShapelessRecipe>; the cast is safe.
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) EnigmaticRecipes.SHAPELESS_NO_REMAIN.get();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    // 26.1.2-port: the datagen-only Builder moved to data/recipes/ShapelessNoRemainRecipeBuilder (parked until phase 9).
}
