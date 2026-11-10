package auviotre.enigmatic.legacy.client.renderer.layer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.item.etherium.EtheriumArmor;
import auviotre.enigmatic.legacy.contents.item.etherium.EtheriumProperties;
import auviotre.enigmatic.legacy.registries.EnigmaticAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// 26.1.2-port: layers are generic over the render state (not the entity) and draw in submit(), where the entity is not
// available. The shield check moved to extractShield(), which ClientSetupHandler registers as a NeoForge render state
// modifier for the player renderers; it marks the state with the SHIELD key.
@OnlyIn(Dist.CLIENT)
public class EtheriumShieldLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    /** Present (TRUE) on the render state of a player whose Etherium shield should be drawn. */
    public static final ContextKey<Boolean> SHIELD = new ContextKey<>(EnigmaticLegacy.location("etherium_shield"));
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/models/misc/ultimate_wither_armor.png");
    private final PlayerModel model;

    public EtheriumShieldLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new PlayerModel(modelSet.bakeLayer(ModelLayers.PLAYER), false) {
            // 26.1.2-port: PlayerModel.setupAnim() now hides the hat/jacket/sleeve/trouser overlays the player switched
            // off in Skin Customisation. The old layer called setAllVisible(true) (and showed head + hat for
            // spectators), so the shield always covered the overlays; switching them back on here keeps that. For
            // spectators the body, arms and legs stay hidden by super, which hides their overlays with them.
            @Override
            public void setupAnim(AvatarRenderState state) {
                super.setupAnim(state);
                this.hat.visible = true;
                this.jacket.visible = true;
                this.leftSleeve.visible = true;
                this.rightSleeve.visible = true;
                this.leftPants.visible = true;
                this.rightPants.visible = true;
            }
        };
    }

    /** 26.1.2-port: the entity half of the old render(). Runs each frame while the entity's render state is filled. */
    public static void extractShield(LivingEntity entity, EntityRenderState state) {
        boolean shield = entity instanceof Player player && EtheriumArmor.etheriumShieldRenderLayer.get()
                && (EtheriumProperties.hasShield(player) || player.getData(EnigmaticAttachments.ENIGMATIC_DATA).getEtherealShield() > 0);
        state.setRenderData(SHIELD, shield ? Boolean.TRUE : null);
    }

    // 26.1.2-port: render() -> submit(). The old code posed its own PlayerModel by hand (prepareMobModel, crouching, a
    // copy of the 1.21.1 PlayerRenderer.getArmPose(), copyPropertiesTo, setupAnim). All of that is in AvatarRenderState
    // now and submitModel() poses the model from the state when it is drawn, so the hand-written copy was removed.
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AvatarRenderState state, float yRot, float xRot) {
        if (state.getRenderData(SHIELD) != null) {
            float f = state.ageInTicks; // 26.1.2-port: tickCount + partialTick

            float scale = 1.02F;
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0, (1F - scale) / 2F, 0);

            // 26.1.2-port: order(1) draws the swirl after the player model, as vanilla's EnergySwirlLayer does.
            // 0xAAFFFFFF is the tint that used to be passed to renderToBuffer().
            collector.order(1).submitModel(this.model, state, poseStack, RenderTypes.energySwirl(TEXTURE, xOffset(f), f * 0.01F), packedLight, OverlayTexture.NO_OVERLAY, 0xAAFFFFFF, null, state.outlineColor, null);
            poseStack.popPose();
        }
    }

    protected float xOffset(float tickCount) {
        return Mth.cos(tickCount * 0.02F) * 2.0F;
    }

    protected PlayerModel model() {
        return this.model;
    }
}
