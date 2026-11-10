package auviotre.enigmatic.legacy.client.screen;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.gui.SpellstoneTableMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class SpellstoneTableScreen extends AbstractContainerScreen<SpellstoneTableMenu> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/gui/spellstone_table.png");

    public SpellstoneTableScreen(SpellstoneTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX += 7;
        this.titleLabelY -= 1;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {
        super.render(graphics, x, y, partialTick);
        RenderSystem.disableBlend();
        this.renderTooltip(graphics, x, y);
    }

    @Override
    // 26.1.2-port: renderBg -> extractBackground (public, new argument order). The dimmed world behind the screen is
    // drawn by super now, blit names its pipeline and the texture size, and the RenderSystem state calls are gone.
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        super.extractBackground(graphics, x, y, partialTicks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.hasOutput()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 85, j + 53, 176.0F, 0.0F, 6, 6, 256, 256);
        }
    }
}
