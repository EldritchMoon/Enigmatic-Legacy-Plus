package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.renderer.model.SpellstoneModel;
import auviotre.enigmatic.legacy.contents.block.entity.SpellstoneTableEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.Vec3;

// 26.1.2-port: block entity renderers are render-state based too. extractRenderState copies the cube's visibility,
// bob timer and interpolated rotation; submit applies the same transforms and submits the model.
public class SpellstoneTableRender implements BlockEntityRenderer<SpellstoneTableEntity, SpellstoneTableRender.TableRenderState> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/models/misc/spellstone_table_cube.png");
    private final SpellstoneModel model;

    public SpellstoneTableRender(BlockEntityRendererProvider.Context context) {
        this.model = new SpellstoneModel(context.bakeLayer(SpellstoneModel.LAYER));
    }

    @Override
    public TableRenderState createRenderState() {
        return new TableRenderState();
    }

    @Override
    public void extractRenderState(SpellstoneTableEntity entity, TableRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, cameraPosition, breakProgress);
        state.visible = entity.render;
        state.timer = entity.time + partialTick;
        state.rot = Mth.lerp(partialTick, entity.oRot, entity.rot);
    }

    @Override
    public void submit(TableRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.08F + Mth.sin(state.timer * 0.1F) * 0.025F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotation(0.25F + state.rot));
        poseStack.mulPose(Axis.XP.rotation(0.1F - state.rot));
        poseStack.mulPose(Axis.YP.rotation(state.rot));
        // 26.1.2-port: RenderType.entityTranslucentCull was removed. entityTranslucentCullItemTarget is the remaining
        // translucent type that still culls back faces, which the cube's inside-out inner box relies on.
        collector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityTranslucentCullItemTarget(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
    }

    public static class TableRenderState extends BlockEntityRenderState {
        public boolean visible;
        public float timer;
        public float rot;
    }
}
