package auviotre.enigmatic.legacy.client.screen;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IBagContent;
import auviotre.enigmatic.legacy.contents.gui.AntiqueBagContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AntiqueBagScreen extends AbstractContainerScreen<AntiqueBagContainerMenu> {

    public static final Identifier TEXTURE = EnigmaticLegacy.location("textures/gui/antique_bag_gui.png");

    public AntiqueBagScreen(AntiqueBagContainerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    // 26.1.2-port: the render() override is gone. The screen now draws its own background (extractBackground) and the
    // hovered slot's tooltip (AbstractContainerScreen.extractRenderState), which is all the override did.

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> list = super.getTooltipFromContainerItem(stack);
        if (stack.getItem() instanceof IBagContent content) return content.getTooltipInBag(list);
        return list;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) { // 26.1.2-port: renderLabels -> extractLabels, drawString -> text
        guiGraphics.text(this.font, this.title, this.titleLabelX + 1, this.titleLabelY + 1, ARGB.opaque(0x8f7ec4), false); // 26.1.2-port: text() skips colours with alpha 0
        guiGraphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, ARGB.opaque(0xb2cfdb), false);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, ARGB.opaque(4210752), false);
    }

    @Override
    // 26.1.2-port: renderBg -> extractBackground (public, new argument order). The dimmed world behind the screen is
    // drawn by super now, and blit names its pipeline and the texture size.
    public void extractBackground(@Nonnull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }
}
