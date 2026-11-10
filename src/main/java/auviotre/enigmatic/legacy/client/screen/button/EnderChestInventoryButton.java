package auviotre.enigmatic.legacy.client.screen.button;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.item.rings.CursedRing;
import auviotre.enigmatic.legacy.contents.item.rings.EnderRing;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.packets.server.EnderRingKeyPacket;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.client.ICuriosScreen;

import javax.annotation.Nonnull;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@OnlyIn(Dist.CLIENT)
public class EnderChestInventoryButton extends ImageButton {
    public static final WidgetSprites SPRITES = new WidgetSprites(
            EnigmaticLegacy.location("button/ender_chest_button"),
            EnigmaticLegacy.location("button/ender_chest_button_highlighted")
    );
    protected final AbstractContainerScreen<?> parentGui;
    // 26.1.2-port: getRecipeBookComponent() is gone. The button only watched the recipe book to follow the GUI when it
    // shifts sideways, so it now watches the GUI's left edge directly.
    protected int lastGuiLeft = Integer.MIN_VALUE;

    public EnderChestInventoryButton(AbstractContainerScreen<?> container, int x, int y, int width, int height, Button.OnPress onPress) {
        super(x, y, width, height, SPRITES, onPress);
        this.parentGui = container;
    }

    public @NotNull
    static EnderChestInventoryButton getInstance(AbstractContainerScreen<?> gui, boolean isCreative) {
        EnderChestInventoryButton enderButton = new EnderChestInventoryButton(gui, 0, 0, 20, 18, (input) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack stack = player.containerMenu.getCarried();
                player.containerMenu.setCarried(ItemStack.EMPTY);
                if (gui instanceof InventoryScreen inventory) {
                    // 26.1.2-port: getRecipeBookComponent() is gone; the recipe book is still one of the screen's widgets
                    for (GuiEventListener child : inventory.children()) {
                        if (child instanceof RecipeBookComponent<?> recipeBookGui && recipeBookGui.isVisible()) recipeBookGui.toggleVisibility();
                    }
                }
                ClientPacketDistributor.sendToServer(new EnderRingKeyPacket(stack));
            }
        });

        Tuple<Integer, Integer> enderOffsets = enderButton.getOffsets(isCreative);
        int x = enderOffsets.getA();
        int y = enderOffsets.getB();
        enderButton.setX(gui.getGuiLeft() + x);
        enderButton.setY(gui.getGuiTop() + y);
        return enderButton;
    }

    @Override
    public void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) { // 26.1.2-port: renderWidget -> extractContents
        this.active = true;
        if (this.parentGui instanceof InventoryScreen || this.parentGui instanceof ICuriosScreen) {
            int guiLeft = this.parentGui.getGuiLeft();
            if (guiLeft != this.lastGuiLeft) {
                this.lastGuiLeft = guiLeft;
                Tuple<Integer, Integer> offsets = this.getOffsets(false);
                this.setPosition(this.parentGui.getGuiLeft() + offsets.getA(), this.parentGui.getGuiTop() + offsets.getB());
            }
        } else if (this.parentGui instanceof CreativeModeInventoryScreen creative) {
            if (!creative.isInventoryOpen()) {
                this.active = false;
                return;
            }
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (EnigmaticHandler.isTheCursedOne(player) && CursedRing.enableEnderRing.get() || EnigmaticHandler.hasCurio(player, EnigmaticItems.ENDER_RING)) {
            super.extractContents(graphics, mouseX, mouseY, partialTicks);
        } else this.active = false;
    }

    public Tuple<Integer, Integer> getOffsets(boolean creative) {
        int x = creative ? 170 + EnderRing.buttonOffsetXCreative.get() : 150 + EnderRing.buttonOffsetX.get();
        int y = creative ? 5 + EnderRing.buttonOffsetYCreative.get() : 61 + EnderRing.buttonOffsetY.get();
        return new Tuple<>(x, y);
    }
}