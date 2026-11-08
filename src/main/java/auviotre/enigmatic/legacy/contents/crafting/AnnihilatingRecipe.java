package auviotre.enigmatic.legacy.contents.crafting;

import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public class AnnihilatingRecipe extends CustomRecipe {
    // 26.1.2-port: CustomRecipe has no category any more and SimpleCraftingRecipeSerializer was removed;
    // like vanilla's special recipes, this is a single instance with a unit codec.
    public static final AnnihilatingRecipe INSTANCE = new AnnihilatingRecipe();
    public static final RecipeSerializer<AnnihilatingRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.size() < 9) return false;
        if (!input.getItem(0).is(EnigmaticTags.Items.ABYSSAL_ITEMS)) return false;
        if (!input.getItem(1).is(EnigmaticItems.EVIL_INGOT)) return false;
        if (!input.getItem(2).is(EnigmaticTags.Items.ABYSSAL_ITEMS)) return false;
        if (!input.getItem(3).isEmpty()) return false;
        if (!input.getItem(5).isEmpty()) return false;
        if (!input.getItem(6).is(EnigmaticTags.Items.ABYSSAL_ITEMS)) return false;
        if (!input.getItem(7).is(EnigmaticItems.TWISTED_HEART)) return false;
        if (!input.getItem(8).is(EnigmaticTags.Items.ABYSSAL_ITEMS)) return false;
        ItemStack heart = input.getItem(4);
        return heart.is(EnigmaticItems.ABYSSAL_HEART) && ITaintable.isTainted(heart);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return EnigmaticItems.ANNIHILATING_SWORD.toStack();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        Player player = CommonHooks.getCraftingPlayer();
        if (EnigmaticHandler.isTheWorthyOne(player)) {
            CompoundTag data = EnigmaticHandler.getPersistedData(player);
            int gained = data.getIntOr("AbyssalHeartsGained", 0);
            if (gained > 4) list.set(0, input.getItem(0).copy());
            if (gained > 3) list.set(2, input.getItem(2).copy());
            if (gained > 2) list.set(6, input.getItem(6).copy());
            if (gained > 1) list.set(8, input.getItem(8).copy());
        }
        return list;
    }


    // 26.1.2-port: removed getResultItem and canCraftInDimensions (no longer part of Recipe); matches() already
    // requires a full 3x3 grid.

    @Override
    public RecipeSerializer<AnnihilatingRecipe> getSerializer() {
        return EnigmaticRecipes.ANNIHILATING.get();
    }
}
