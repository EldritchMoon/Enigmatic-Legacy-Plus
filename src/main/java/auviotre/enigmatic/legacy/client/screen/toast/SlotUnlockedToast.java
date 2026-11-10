package auviotre.enigmatic.legacy.client.screen.toast;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlotUnlockedToast implements Toast {
    private static final Identifier BACKGROUND = EnigmaticLegacy.location("toast/slot_unlock");
    private static final Component DESCRIPTION_TEXT = Component.translatable("toast.enigmaticlegacy.slot_unlock.description");
    private final ItemStack drawnStack;
    private final String identifier;

    public SlotUnlockedToast(ItemStack stack, String id) {
        this.drawnStack = stack;
        this.identifier = id;
    }

    // 26.1.2-port: Toast.render was split in three. update() decides how long the toast stays (same rule as before:
    // hidden at once for an empty stack, otherwise after 5 seconds times the accessibility multiplier),
    // getWantedVisibility() reports it, and extractRenderState() only draws and is handed the font.
    private Visibility wantedVisibility = Visibility.HIDE;

    @Override
    public Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager toast, long timeLastVisible) {
        if (drawnStack.isEmpty()) this.wantedVisibility = Visibility.HIDE;
        else this.wantedVisibility = timeLastVisible >= 5000.0 * toast.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long timeLastVisible) {
        if (drawnStack.isEmpty()) return;
        Component slot_id = Component.translatable("curios.identifier." + this.identifier);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, this.width(), this.height());
        graphics.text(font, Component.translatable("toast.enigmaticlegacy.slot_unlock.title", slot_id), 30, 7, -11534256, false);
        graphics.text(font, DESCRIPTION_TEXT, 30, 18, -16777216, false);
        graphics.fakeItem(this.drawnStack, 8, 8);
    }
}