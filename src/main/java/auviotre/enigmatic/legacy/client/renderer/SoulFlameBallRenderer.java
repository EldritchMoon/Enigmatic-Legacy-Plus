package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.contents.entity.projectile.SoulFlameBall;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulFlameBallRenderer extends EntityRenderer<SoulFlameBall, EntityRenderState> {
    public SoulFlameBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(SoulFlameBall entity, BlockPos pos) {
        return 15;
    }

    // 26.1.2-port: render() and getTextureLocation() no longer exist; a renderer extracts a render state and submits it.
    // Upstream's render() drew nothing (its item drawing, kept below, was already commented out and only a pose
    // push/pop was left), so this renderer needs no state beyond the base one and submits nothing of its own.
//            poseStack.scale(this.scale, this.scale, this.scale);
//            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
//            this.itemRenderer.renderStatic(((ItemSupplier)entity).getItem(), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
