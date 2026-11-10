package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.entity.projectile.UltimateWitherSkull;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// 26.1.2-port: render-state renderer, laid out like vanilla's WitherSkullRenderer and reusing its WitherSkullRenderState
// (dangerous flag plus the skull model's rotation). The shield swirl uses the base state's ageInTicks.
@OnlyIn(Dist.CLIENT)
public class UltimateWitherSkullRenderer extends EntityRenderer<UltimateWitherSkull, WitherSkullRenderState> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/models/misc/ultimate_wither_armor.png");
    private static final Identifier WITHER_INVULNERABLE_LOCATION = Identifier.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier WITHER_LOCATION = Identifier.withDefaultNamespace("textures/entity/wither/wither.png");
    private final SkullModel model;

    public UltimateWitherSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKULL));
    }

    @Override
    protected int getBlockLightLevel(UltimateWitherSkull entity, BlockPos pos) {
        return 15;
    }

    @Override
    public WitherSkullRenderState createRenderState() {
        return new WitherSkullRenderState();
    }

    @Override
    public void extractRenderState(UltimateWitherSkull entity, WitherSkullRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isDangerous = entity.isDangerous();
        state.modelState.animationPos = 0.0F;
        state.modelState.yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.modelState.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(WitherSkullRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        float inflate = state.isDangerous ? 1.4F : 1.0F;
        poseStack.scale(-inflate, -inflate, inflate);
        collector.submitModel(this.model, state.modelState, poseStack, this.getTextureLocation(state), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        if (state.isDangerous) {
            this.submitShield(state, poseStack, collector, -state.ageInTicks, inflate + 0.1F);
            this.submitShield(state, poseStack, collector, state.ageInTicks, inflate + 0.2F);
        }
        super.submit(state, poseStack, collector, camera);
    }

    private void submitShield(WitherSkullRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float fullTicks, float scale) {
        poseStack.pushPose();
        poseStack.scale(-scale, -scale, scale);
        collector.submitModel(this.model, state.modelState, poseStack, RenderTypes.energySwirl(TEXTURE, Mth.cos(fullTicks * 0.02F) * 2.0F, fullTicks * 0.02F), state.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);
        poseStack.popPose();
    }

    private Identifier getTextureLocation(WitherSkullRenderState state) {
        return state.isDangerous ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }
}
