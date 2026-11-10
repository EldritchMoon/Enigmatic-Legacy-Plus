package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.entity.projectile.AngelBeam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// 26.1.2-port: render-state renderer. Everything read from the entity moved to extractRenderState(); submit() only uses
// the state. The geometry and its numbers are unchanged.
public class AngelBeamRenderer extends EntityRenderer<AngelBeam, AngelBeamRenderer.AngelBeamRenderState> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/entity/projectiles/angel_beam.png");
    // 26.1.2-port: RenderType.entityTranslucentCull was removed. entityTranslucentCullItemTarget is the remaining type on
    // the ENTITY_TRANSLUCENT_CULL pipeline. The beam draws the front and the back of each side quad itself, so it has
    // to stay on a culled type (unculled, every quad would be blended twice).
    private static final RenderType BEAM_RENDER_TYPE = RenderTypes.entityTranslucentCullItemTarget(TEXTURE);

    public AngelBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int alpha, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(AngelBeam entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(AngelBeam entity, BlockPos pos) {
        return 15;
    }

    @Override
    public boolean shouldRender(AngelBeam livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public AngelBeamRenderState createRenderState() {
        return new AngelBeamRenderState();
    }

    @Override
    public void extractRenderState(AngelBeam beam, AngelBeamRenderState state, float partialTick) {
        super.extractRenderState(beam, state, partialTick);
        state.beamVector = new Vec3(beam.getBeginning()).subtract(beam.getEyePosition(partialTick));
        state.capFrame = beam.tickCount % 4 < 2;
    }

    // 26.1.2-port: render() -> submit(). state.ageInTicks is tickCount + partialTick (set by the base class), which is
    // what the old code added up by hand. As before, super is not called.
    @Override
    public void submit(AngelBeamRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Vec3 delta = state.beamVector;
        float len = (float) delta.length();
        delta = delta.normalize();
        float phi = (float) Math.acos(delta.y);
        float theta = (float) Math.atan2(delta.z, delta.x);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.PI / 2 - theta)));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(phi)));

        float timer = state.ageInTicks * 8.0F;
        float f7 = timer * 0.05F * -1.5F;
        float f8 = (float) Math.pow((48.0F - state.ageInTicks) / 48.0F, 0.6);
        int alpha = Math.clamp((int) (f8 * 256), 0, 255);
        float f11 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
        float f12 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
        float f13 = Mth.cos(f7 + (float) (Math.PI / 4)) * 0.282F;
        float f14 = Mth.sin(f7 + (float) (Math.PI / 4)) * 0.282F;
        float f15 = Mth.cos(f7 + (float) (Math.PI * 5.0F / 4.0F)) * 0.282F;
        float f16 = Mth.sin(f7 + (float) (Math.PI * 5.0F / 4.0F)) * 0.282F;
        float f17 = Mth.cos(f7 + (float) (Math.PI * 7.0F / 4.0F)) * 0.282F;
        float f18 = Mth.sin(f7 + (float) (Math.PI * 7.0F / 4.0F)) * 0.282F;
        float f19 = Mth.cos(f7 + (float) Math.PI) * 0.2F;
        float f20 = Mth.sin(f7 + (float) Math.PI) * 0.2F;
        float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
        float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
        float f23 = Mth.cos(f7 + (float) (Math.PI / 2)) * 0.2F;
        float f24 = Mth.sin(f7 + (float) (Math.PI / 2)) * 0.2F;
        float f25 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
        float f26 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
        float v1 = 0.0F;
        float v2 = 1.0F;
        float yOffset = state.capFrame ? 0.5F : 0.0F;
        // 26.1.2-port: bufferSource.getBuffer(type) + direct vertex calls -> submitCustomGeometry(poseStack, type, callback)
        collector.submitCustomGeometry(poseStack, BEAM_RENDER_TYPE, (pose, consumer) -> {
            vertex(consumer, pose, f19, len, f20, alpha, 0.4999F, v2);
            vertex(consumer, pose, f19, 0.0F, f20, alpha, 0.4999F, v1);
            vertex(consumer, pose, f21, 0.0F, f22, alpha, 0.0F, v1);
            vertex(consumer, pose, f21, len, f22, alpha, 0.0F, v2);
            vertex(consumer, pose, f19, len, f20, alpha, 0.4999F, v2);
            vertex(consumer, pose, f21, len, f22, alpha, 0.0F, v2);
            vertex(consumer, pose, f21, 0.0F, f22, alpha, 0.0F, v1);
            vertex(consumer, pose, f19, 0.0F, f20, alpha, 0.4999F, v1);
            vertex(consumer, pose, f23, len, f24, alpha, 0.4999F, v2);
            vertex(consumer, pose, f23, 0.0F, f24, alpha, 0.4999F, v1);
            vertex(consumer, pose, f25, 0.0F, f26, alpha, 0.0F, v1);
            vertex(consumer, pose, f25, len, f26, alpha, 0.0F, v2);
            vertex(consumer, pose, f23, len, f24, alpha, 0.4999F, v2);
            vertex(consumer, pose, f25, len, f26, alpha, 0.0F, v2);
            vertex(consumer, pose, f25, 0.0F, f26, alpha, 0.0F, v1);
            vertex(consumer, pose, f23, 0.0F, f24, alpha, 0.4999F, v1);

            vertex(consumer, pose, f11, len, f12, alpha, 0.5F, yOffset + 0.5F);
            vertex(consumer, pose, f13, len, f14, alpha, 1.0F, yOffset + 0.5F);
            vertex(consumer, pose, f17, len, f18, alpha, 1.0F, yOffset);
            vertex(consumer, pose, f15, len, f16, alpha, 0.5F, yOffset);
        });
        poseStack.popPose();
    }

    // 26.1.2-port: getTextureLocation() no longer exists on entity renderers; TEXTURE is used directly above.

    public static class AngelBeamRenderState extends EntityRenderState {
        /** From the beam entity's eye position to the point it was fired from. */
        public Vec3 beamVector = Vec3.ZERO;
        /** Which of the two end-cap frames to show (tickCount % 4 < 2). */
        public boolean capFrame;
    }
}
