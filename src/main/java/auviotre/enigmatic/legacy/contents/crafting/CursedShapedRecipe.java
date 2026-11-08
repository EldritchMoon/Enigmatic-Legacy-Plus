package auviotre.enigmatic.legacy.contents.crafting;

import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * A shaped recipe whose taintable ingredients must be tainted when the result is a cursed or blessed item.
 * 26.1.2-port: recipes take CommonInfo/CraftingBookInfo and an ItemStackTemplate result; the JSON format is the
 * vanilla 26.1 shaped format. RecipeSerializer is a record now, so the codecs live here.
 */
public class CursedShapedRecipe extends ShapedRecipe {
    public static final MapCodec<CursedShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
    ).apply(instance, CursedShapedRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CursedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            CursedShapedRecipe::new
    );
    public static final RecipeSerializer<CursedShapedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final ItemStackTemplate result;

    public CursedShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack result = this.result.create();
        if (EnigmaticHandler.isCursedItem(result) || EnigmaticHandler.isBlessedItem(result)) {
            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.getItem(i);
                if (stack.getItem() instanceof ITaintable && !ITaintable.isTainted(stack))
                    return false;
            }
        }
        return super.matches(input, level);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        // 26.1.2-port: ShapedRecipe.getSerializer() is typed RecipeSerializer<ShapedRecipe>; our serializer produces
        // CursedShapedRecipe (a ShapedRecipe), so the cast is safe.
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) EnigmaticRecipes.CURSED_SHAPED.get();
    }

    // 26.1.2-port: the datagen-only Builder moved to data/recipes/CursedShapedRecipeBuilder (parked until phase 9).
}
