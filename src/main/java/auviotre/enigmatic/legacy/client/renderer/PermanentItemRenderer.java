package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.api.item.IPermanentCrystal;
import auviotre.enigmatic.legacy.api.item.ISharableItem;
import auviotre.enigmatic.legacy.contents.entity.misc.PermanentItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

// 26.1.2-port: render-state renderer, laid out like vanilla's ItemEntityRenderer. Everything the old render() read from
// the entity and the local player is decided in extractRenderState and stored in PermanentItemRenderState.
@OnlyIn(Dist.CLIENT)
public class PermanentItemRenderer extends EntityRenderer<PermanentItemEntity, PermanentItemRenderer.PermanentItemRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    public PermanentItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver(); // 26.1.2-port: ItemRenderer/BakedModel -> ItemModelResolver
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public PermanentItemRenderState createRenderState() {
        return new PermanentItemRenderState();
    }

    @Override
    public void extractRenderState(PermanentItemEntity entity, PermanentItemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ItemStack stack = entity.getItem();
        LocalPlayer player = Minecraft.getInstance().player;
        // Same visibility rules as the old render(): hidden next to a dead player, and sharable items only show to acceptors.
        state.visible = !(player == null || !player.isAlive() && Math.sqrt(entity.distanceToSqr(player.getX(), player.getEyeY(), player.getZ())) <= 1.0);
        if (state.visible && stack.getItem() instanceof ISharableItem) {
            state.visible = entity.acceptors != null && entity.acceptors.contains(player.getUUID());
        }
        state.crystal = stack.getItem() instanceof IPermanentCrystal;
        state.shouldBob = IClientItemExtensions.of(stack).shouldBobAsEntity(stack);
        state.bob = Mth.sin((entity.getAge() + partialTicks) / 10.0F + entity.hoverStart) * 0.1F + 0.1F;
        state.spin = entity.getItemHover(partialTicks);
        state.extractItemGroupRenderState(entity, stack, this.itemModelResolver);
    }

    @Override
    public void submit(PermanentItemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) return;

        poseStack.pushPose();

        if (state.crystal) {
            poseStack.scale(1.25f, 1.25f, 1.25f);
            poseStack.translate(0, -0.1125d, 0);
        }

        // 26.1.2-port: the model's GROUND transform can no longer be read, so the lift that keeps a bobbing item off the
        // ground (was 0.25 * the transform's y scale) is taken from the model's bounding box, as vanilla does now.
        float lift = state.shouldBob ? -((float) state.item.getModelBoundingBox().minY) + 0.0625F : 0;
        poseStack.translate(0.0D, state.bob + lift, 0.0D);
        poseStack.mulPose(Axis.YP.rotation(state.spin));

        ItemEntityRenderer.renderMultipleFromCount(poseStack, collector, state.lightCoords, state, this.random);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /** Vanilla's item cluster state (resolved model, count, seed) plus what this renderer decides per frame. */
    public static class PermanentItemRenderState extends ItemClusterRenderState {
        public boolean visible;
        public boolean crystal;
        public boolean shouldBob;
        public float bob;
        public float spin;
    }
}
