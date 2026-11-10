package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.renderer.model.ExplorerMarkerModel;
import auviotre.enigmatic.legacy.contents.entity.misc.ExplorerMarker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// 26.1.2-port: render-state renderer. The marker needs nothing from the entity, so it uses the base EntityRenderState.
@OnlyIn(Dist.CLIENT)
public class ExplorerMarkerRender extends EntityRenderer<ExplorerMarker, EntityRenderState> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/models/misc/explorer_marker.png");
    private final ExplorerMarkerModel model;

    public ExplorerMarkerRender(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ExplorerMarkerModel(context.bakeLayer(ExplorerMarkerModel.LAYER));
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    // 26.1.2-port: render() -> submit(). As before, the cube is drawn with the outline render type and super is not
    // called (the marker shows no name tag). getTextureLocation() no longer exists on entity renderers.
    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        collector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.outline(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
    }
}
