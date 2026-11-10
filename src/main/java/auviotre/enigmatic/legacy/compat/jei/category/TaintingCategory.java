package auviotre.enigmatic.legacy.compat.jei.category;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.compat.jei.EnigmaticRecipeTypes;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TaintingCategory implements IRecipeCategory<TaintingCategory.Recipe> {
    private static final Identifier SPRITE = EnigmaticLegacy.location("jei/tainting_gui");
    private final IDrawable icon;

    public TaintingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(EnigmaticItems.EARTH_HEART);
    }

    @Contract("_, _ -> new")
    public static @NotNull Recipe create(ItemStack input, Ingredient condition) {
        return new Recipe(input, condition);
    }

    @Override
    public IRecipeType<Recipe> getRecipeType() {
        return EnigmaticRecipeTypes.TAINTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.enigmaticlegacy.jei.tainting");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 108;
    }

    @Override
    public int getHeight() {
        return 42;
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, 0, 0, getWidth(), getHeight()); // 26.1.2-port: blitSprite takes the render pipeline
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull TaintingCategory.Recipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(13, 13).add(recipe.input()).setStandardSlotBackground(); // 26.1.2-port: JEI renamed addItemStack/addIngredients to add
        ItemStack output = recipe.input().copy();
        if (output.getItem() instanceof ITaintable) output.set(EnigmaticComponents.TAINTABLE.get(), true);
        builder.addOutputSlot(79, 13).add(output).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 46, 23).add(recipe.condition()); // 26.1.2-port: JEI renamed CATALYST to CRAFTING_STATION
    }

    public record Recipe(ItemStack input, Ingredient condition) {
    }
}
