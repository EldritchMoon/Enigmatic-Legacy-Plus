package auviotre.enigmatic.legacy.compat.jei.category;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.compat.jei.EnigmaticRecipeTypes;
import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class SpellstoneCraftingCategory implements IRecipeCategory<RecipeHolder<SpellstoneTableRecipe>> {
    private static final Identifier SPRITE = EnigmaticLegacy.location("jei/spellstone_table_gui");
    private static final Identifier ICON = EnigmaticLegacy.location("jei/spellstone_table_icon");
    private static final List<Pair<Integer, Integer>> OFFSETS = List.of(
            new Pair<>(38, 52),
            new Pair<>(31, 32),
            new Pair<>(38, 12),
            new Pair<>(58, 5),
            new Pair<>(78, 12),
            new Pair<>(85, 32),
            new Pair<>(78, 52)
    );
    private final IDrawable icon;

    public SpellstoneCraftingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(EnigmaticItems.SPELLCORE);
    }

    @Override
    public IRecipeType<RecipeHolder<SpellstoneTableRecipe>> getRecipeType() {
        return EnigmaticRecipeTypes.SPELLSTONE_CRAFTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.enigmaticlegacy.jei.spellstone_crafting");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 132;
    }

    @Override
    public int getHeight() {
        return 80;
    }

    @Override
    public void draw(RecipeHolder<SpellstoneTableRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, 0, 0, getWidth(), getHeight()); // 26.1.2-port: blitSprite takes the render pipeline
        if (recipe.value().isAllDifferent()) guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, 124, 0, 8, 8);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<SpellstoneTableRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (recipe.value().isAllDifferent() && mouseX >= 124 && mouseY >= 0 && mouseX <= 132 && mouseY <= 8) {
            tooltip.add(Component.translatable("gui.enigmaticlegacy.jei.spellstone_crafting.all_different"));
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RecipeHolder<SpellstoneTableRecipe> holder, IFocusGroup focuses) {
        SpellstoneTableRecipe recipe = holder.value();
        IRecipeSlotBuilder slot = builder.addInputSlot(5, 32);
        IRecipeSlotBuilder spell = builder.addInputSlot(111, 32);
        IRecipeSlotBuilder core = builder.addInputSlot(58, 32);
        List<Ingredient> ingredients = recipe.getIngredients(); // 26.1.2-port: a plain List now
        for (int i = 0; i < 7; i++) {
            Pair<Integer, Integer> pair = OFFSETS.get(i);
            if (i < ingredients.size()) {
                Ingredient ingredient = ingredients.get(i);
                if (recipe.isAllDifferent()) {
                    List<ItemStack> items = List.of(ingredient.getItems());
                    List<ItemStack> list = new ArrayList<>(items);
                    for (int j = 0; j < items.size(); j++) {
                        list.set(j, items.get((j + i) % items.size()));
                    }
                    builder.addInputSlot(pair.getA(), pair.getB()).addItemStacks(list);
                } else builder.addInputSlot(pair.getA(), pair.getB()).add(ingredient); // 26.1.2-port: JEI renamed addIngredients/addItemStack to add
            } else builder.addInputSlot(pair.getA(), pair.getB());
        }
        if (holder.id().equals(EnigmaticLegacy.location("spellstone_fragmentation"))) {
            spell.addIngredients(Ingredient.of(EnigmaticTags.Items.SPELLSTONES));
            builder.addOutputSlot(58, 59).add(EnigmaticItems.SPELLSTONE_DEBRIS.toStack(4));
            return;
        }

        if (recipe.getCount() > 0) slot.add(EnigmaticItems.SPELLSTONE_DEBRIS.toStack(recipe.getCount()));
        core.add(EnigmaticItems.SPELLCORE.toStack());
        builder.addOutputSlot(58, 59).add(recipe.getResultItem()); // 26.1.2-port: getResultItem() no longer takes registries
    }
}
