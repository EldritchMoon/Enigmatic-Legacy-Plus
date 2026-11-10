package auviotre.enigmatic.legacy.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PermanentDeathScreen extends Screen {
    public static PermanentDeathScreen active = null;
    private final MutableComponent reason;
    private final Screen parent;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private int textHeight;

    public PermanentDeathScreen(Screen parent, Component title, MutableComponent reason) {
        super(title);
        this.parent = parent;
        this.reason = reason;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.message = MultiLineLabel.create(this.font, this.reason.withStyle(ChatFormatting.WHITE), this.width - 50);
        this.textHeight = this.message.getLineCount() * 9;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.toWorld"), button -> {
                    active = null;
                    this.minecraft.setScreen(this.parent);
                }).bounds(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30), 200, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) { // 26.1.2-port: render -> extractRenderState
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, ARGB.opaque(11184810)); // 26.1.2-port: text skips colours with alpha 0
        // 26.1.2-port: MultiLineLabel.renderCentered -> visitLines with a text collector (line height 9, as before)
        this.message.visitLines(TextAlignment.CENTER, this.width / 2, this.height / 2 - this.textHeight / 2, 9, guiGraphics.textRenderer());
    }
}
