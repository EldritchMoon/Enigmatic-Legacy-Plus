package auviotre.enigmatic.legacy.client;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import java.util.Optional;

/**
 * 26.1.2-port (dedicated server): the name tag code of the Insignia, moved here unchanged from the Insignia item
 * class. Both methods take client-only types, and a class that exists on a dedicated server must not have those in
 * its method signatures now that NeoForge no longer strips @OnlyIn members there.
 */
public final class InsigniaNameplate {
    private InsigniaNameplate() {
    }

    /** The small "(real name)" line under an Insignia name tag; only set for viewers who may see it. */
    public static final ContextKey<Component> TRUE_NAME = new ContextKey<>(EnigmaticLegacy.location("insignia_true_name"));

    // 26.1.2-port: this and submitTrueName() replace renderInsigniaNameplate(), which MixinEntityRenderer called in
    // place of EntityRenderer.renderNameTag(). That method is gone: the name tag is a field of the render state, and
    // vanilla draws it. So the decision is made here, while the player's render state is filled (registered in
    // ClientSetupHandler), by changing state.nameTag: the custom name, or the real name for a viewer who may see it
    // when the custom name is blank, or nothing at all. Same rules as before. The below-name scoreboard line is
    // cleared, because the old injector replaced that call too.
    public static void extractNameplate(LivingEntity entity, EntityRenderState state) {
        if (state.nameTag == null || !(entity instanceof Player player)) return;
        ItemStack insignia = EnigmaticHandler.getCurio(player, EnigmaticItems.INSIGNIA);
        if (insignia.isEmpty()) return;
        Optional<Component> customName = EnigmaticItems.INSIGNIA.get().getCustomName(insignia);
        if (customName.isEmpty()) return;
        Player viewer = EnigmaticLegacy.PROXY.getClientPlayer();
        boolean seesTrueName = entity != viewer && EnigmaticItems.INSIGNIA.get().canSeeTrueName(viewer);
        state.scoreText = null;
        if (Minecraft.getInstance().font.width(customName.get()) > 0) {
            state.nameTag = customName.get();
            if (seesTrueName) state.setRenderData(TRUE_NAME, Component.literal("(" + entity.getDisplayName().getString() + ")"));
        } else {
            state.nameTag = seesTrueName ? entity.getDisplayName() : null;
        }
    }

    // 26.1.2-port: the second, smaller line. Called from ClientEventHandler for RenderNameTagEvent.DoRender, i.e. just
    // before vanilla submits the main line. submitNameTag() does the translate/rotate/scale itself, so the old extra
    // scale(0.4) is applied in front of it and the attachment point is divided by 0.4 to stay where it was.
    public static void submitTrueName(RenderNameTagEvent.DoRender event) {
        EntityRenderState state = event.getEntityRenderState();
        Component trueName = state.getRenderData(TRUE_NAME);
        Vec3 attachment = state.nameTagAttachment;
        if (trueName == null || attachment == null || event.getContent() != state.nameTag) return;
        float scale = 0.4F;
        int offset = (int) (-10 * (1.0F / scale));
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        stack.scale(scale, scale, scale);
        Vec3 scaledAttachment = new Vec3(attachment.x / scale, (attachment.y + 0.5) / scale - 0.5, attachment.z / scale);
        event.getSubmitNodeCollector().submitNameTag(stack, scaledAttachment, -offset, trueName, !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, event.getCameraRenderState());
        stack.popPose();
    }
}
