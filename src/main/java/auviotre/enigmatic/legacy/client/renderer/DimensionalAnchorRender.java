package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.contents.block.DimensionalAnchor;
import auviotre.enigmatic.legacy.contents.block.entity.DimensionalAnchorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.EnumSet;
import java.util.Set;

// 26.1.2-port: block entity renderers are render-state based too. extractRenderState records whether the anchor is
// charged and which faces to draw; submit hands the same end-portal quads to the collector as custom geometry.
@OnlyIn(Dist.CLIENT)
public class DimensionalAnchorRender implements BlockEntityRenderer<DimensionalAnchorEntity, DimensionalAnchorRender.AnchorRenderState> {
    public DimensionalAnchorRender(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public AnchorRenderState createRenderState() {
        return new AnchorRenderState();
    }

    @Override
    public void extractRenderState(DimensionalAnchorEntity entity, AnchorRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, cameraPosition, breakProgress);
        state.charged = entity.getBlockState().getValue(DimensionalAnchor.CHARGE) > 0;
        state.facesToShow.clear();
        for (Direction direction : Direction.values()) {
            if (entity.shouldRenderFace(direction)) state.facesToShow.add(direction);
        }
    }

    @Override
    public void submit(AnchorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.charged) {
            collector.submitCustomGeometry(poseStack, this.renderType(), (pose, consumer) -> this.renderCube(state.facesToShow, pose, consumer));
        }
    }

    private void renderCube(Set<Direction> faces, PoseStack.Pose pose, VertexConsumer consumer) {
        float f = this.getOffsetDown();
        float f1 = this.getOffsetUp();
        this.renderFace(faces, pose, consumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
        this.renderFace(faces, pose, consumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
        this.renderFace(faces, pose, consumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
        this.renderFace(faces, pose, consumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
        this.renderFace(faces, pose, consumer, 0.0F, 1.0F, f, f, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
        this.renderFace(faces, pose, consumer, 0.1F, 0.9F, f1, f1, 0.9F, 0.9F, 0.1F, 0.1F, Direction.UP);
    }

    private void renderFace(Set<Direction> faces, PoseStack.Pose pPose, VertexConsumer consumer, float pX0, float pX1, float pY0, float pY1, float pZ0, float pZ1, float pZ2, float pZ3, Direction pDirection) {
        if (faces.contains(pDirection)) {
            consumer.addVertex(pPose, pX0, pY0, pZ0);
            consumer.addVertex(pPose, pX1, pY0, pZ1);
            consumer.addVertex(pPose, pX1, pY1, pZ2);
            consumer.addVertex(pPose, pX0, pY1, pZ3);
        }
    }

    protected float getOffsetUp() {
        return 1.0F - (1F / 16F);
    }

    protected float getOffsetDown() {
        return 0.375F;
    }

    protected RenderType renderType() {
        return RenderTypes.endPortal();
    }

    public static class AnchorRenderState extends BlockEntityRenderState {
        public boolean charged;
        public final Set<Direction> facesToShow = EnumSet.noneOf(Direction.class);
    }
}
