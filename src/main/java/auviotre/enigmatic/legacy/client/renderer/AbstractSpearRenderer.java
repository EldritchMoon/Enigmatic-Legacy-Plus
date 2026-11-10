package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.contents.entity.projectile.AbstractSpear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
// 26.1.2-port: a renderer no longer draws from the entity. extractRenderState copies what it needs into a render state
// (here the resolved item model and the two interpolated rotations) and submit draws from that state alone.
public abstract class AbstractSpearRenderer<T extends AbstractSpear> extends EntityRenderer<T, AbstractSpearRenderer.SpearRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final Vec3 scale;
    private final float offset;

    public AbstractSpearRenderer(EntityRendererProvider.Context context, float offset, Vec3 scale) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver(); // 26.1.2-port: ItemRenderer/BakedModel -> ItemModelResolver
        this.shadowRadius = 0.175F;
        this.offset = offset;
        this.scale = scale.scale(0.85);
    }

    @Override
    public SpearRenderState createRenderState() {
        return new SpearRenderState();
    }

    @Override
    public void extractRenderState(T spear, SpearRenderState state, float partialTick) {
        super.extractRenderState(spear, state, partialTick);
        state.yRot = Mth.lerp(partialTick, spear.yRotO - 90.0F, spear.getYRot() - 90.0F);
        state.xRot = Mth.lerp(partialTick, spear.xRotO - 45.0F, spear.getXRot() - 45.0F);
        this.itemModelResolver.updateForNonLiving(state.item, spear.getItem(), ItemDisplayContext.NONE, spear);
    }

    @Override
    public void submit(SpearRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        poseStack.translate(-offset, -offset, 0);
        poseStack.translate(0, state.boundingBoxHeight * 0.2, 0);
        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /** The resolved item model (from ThrownItemRenderState) plus the spear's interpolated rotations. */
    public static class SpearRenderState extends ThrownItemRenderState {
        public float xRot;
        public float yRot;
    }
}
