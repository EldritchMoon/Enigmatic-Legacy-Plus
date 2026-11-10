package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.renderer.model.IchorSpriteModel;
import auviotre.enigmatic.legacy.client.renderer.state.IchorSpriteRenderState;
import auviotre.enigmatic.legacy.contents.entity.IchorSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

// 26.1.2-port: render-state renderer, following vanilla's VexRenderer (held items) and GuardianRenderer (beam).
// MobRenderer takes the state type as a second type argument.
public class IchorSpriteRenderer extends MobRenderer<IchorSprite, IchorSpriteRenderState, IchorSpriteModel> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/entity/ichor_sprite.png");
    private static final Identifier BEAM_LOCATION = EnigmaticLegacy.location("textures/entity/ichor_sprite_beam.png");
    // 26.1.2-port: RenderType.entityTranslucentCull was removed. entityTranslucentCullItemTarget is the remaining type on
    // the ENTITY_TRANSLUCENT_CULL pipeline, so the beam stays translucent and back-face culled.
    private static final RenderType BEAM_RENDER_TYPE = RenderTypes.entityTranslucentCullItemTarget(BEAM_LOCATION);

    public IchorSpriteRenderer(EntityRendererProvider.Context context) {
        super(context, new IchorSpriteModel(context.bakeLayer(IchorSpriteModel.LAYER)), 0.3F);
        // 26.1.2-port: ItemInHandLayer no longer takes an ItemInHandRenderer; it draws the item states held in
        // ArmedEntityRenderState.
        this.addLayer(new ItemInHandLayer<>(this));
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int alpha, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public IchorSpriteRenderState createRenderState() {
        return new IchorSpriteRenderState();
    }

    @Override
    public void extractRenderState(IchorSprite entity, IchorSpriteRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
        state.attackScale = entity.getAttackAnimationScale(partialTicks);
        LivingEntity target = entity.getActiveAttackTarget();
        if (target != null) {
            Vec3 tarPos = this.getPosition(target, target.getBbHeight() * 0.5, partialTicks);
            Vec3 selfPos = this.getPosition(entity, entity.getEyeHeight(), partialTicks);
            Vec3 delta = tarPos.subtract(selfPos);
            state.beamVector = delta;
            state.beamLength = (float) (delta.length() + 0.25 * target.getBbWidth());
            state.beamCapFrame = entity.tickCount % 2 == 0;
        } else {
            state.beamVector = null;
        }
    }

    // 26.1.2-port: render() -> submit(). The beam is drawn from the state; its numbers are unchanged.
    @Override
    public void submit(IchorSpriteRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        Vec3 delta = state.beamVector;
        if (delta != null) {
            float scale = state.attackScale;
            float timer = scale * 30.0F;
            float f2 = timer * 0.5F % 1.0F;
            poseStack.pushPose();
            poseStack.translate(0.0F, state.eyeHeight, 0.0F);
            float len = state.beamLength;
            delta = delta.normalize();
            float phi = (float) Math.acos(delta.y);
            float theta = (float) Math.atan2(delta.z, delta.x);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.PI / 2 - theta)));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(phi)));

            float f7 = timer * 0.05F * -1.5F;
            float f8 = scale * scale;
            int alpha = Math.clamp((int) (f8 * 256), 0, 255);
            float f11 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float f12 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float f13 = Mth.cos(f7 + (float) (Math.PI / 4)) * 0.282F;
            float f14 = Mth.sin(f7 + (float) (Math.PI / 4)) * 0.282F;
            float f15 = Mth.cos(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float f16 = Mth.sin(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float f17 = Mth.cos(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float f18 = Mth.sin(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float f19 = Mth.cos(f7 + (float) Math.PI) * 0.2F;
            float f20 = Mth.sin(f7 + (float) Math.PI) * 0.2F;
            float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
            float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
            float f23 = Mth.cos(f7 + (float) (Math.PI / 2)) * 0.2F;
            float f24 = Mth.sin(f7 + (float) (Math.PI / 2)) * 0.2F;
            float f25 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float f26 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float f29 = -1.0F + f2;
            float f30 = len * 2.5F + f29;
            float yOffset = state.beamCapFrame ? 0.5F : 0.0F;
            collector.submitCustomGeometry(poseStack, BEAM_RENDER_TYPE, (pose, consumer) -> {
                vertex(consumer, pose, f19, len, f20, alpha, 0.4999F, f30);
                vertex(consumer, pose, f19, 0.0F, f20, alpha, 0.4999F, f29);
                vertex(consumer, pose, f21, 0.0F, f22, alpha, 0.0F, f29);
                vertex(consumer, pose, f21, len, f22, alpha, 0.0F, f30);
                vertex(consumer, pose, f23, len, f24, alpha, 0.4999F, f30);
                vertex(consumer, pose, f23, 0.0F, f24, alpha, 0.4999F, f29);
                vertex(consumer, pose, f25, 0.0F, f26, alpha, 0.0F, f29);
                vertex(consumer, pose, f25, len, f26, alpha, 0.0F, f30);
                vertex(consumer, pose, f11, len, f12, alpha, 0.5F, yOffset + 0.5F);
                vertex(consumer, pose, f13, len, f14, alpha, 1.0F, yOffset + 0.5F);
                vertex(consumer, pose, f17, len, f18, alpha, 1.0F, yOffset);
                vertex(consumer, pose, f15, len, f16, alpha, 0.5F, yOffset);
            });
            poseStack.popPose();
        }
    }

    @Override
    protected int getBlockLightLevel(IchorSprite entity, BlockPos pos) {
        return 15;
    }

    // 26.1.2-port: takes the render state instead of the entity
    @Override
    public Identifier getTextureLocation(IchorSpriteRenderState state) {
        return TEXTURE;
    }

    private Vec3 getPosition(LivingEntity entity, double yOffset, float partialTick) {
        double d0 = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double d1 = Mth.lerp(partialTick, entity.yOld, entity.getY()) + yOffset;
        double d2 = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        return new Vec3(d0, d1, d2);
    }
}
