package auviotre.enigmatic.legacy.client.renderer.layer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.item.generic.BaseElytraItem;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

// 26.1.2-port: layers are generic over the render state (not the entity) and draw in submit(), where the entity is not
// available. The part of the old render() that looked at the entity is extractElytra(); ClientSetupHandler registers it
// as a NeoForge render state modifier for the player and armour stand renderers, and it leaves the stack to draw in the
// state under the ELYTRA key.
@OnlyIn(Dist.CLIENT)
public class EnigmaticElytraLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    /** The BaseElytraItem stack this layer should draw; absent when there is none or its Curios slot is hidden. */
    public static final ContextKey<ItemStack> ELYTRA = new ContextKey<>(EnigmaticLegacy.location("elytra"));
    private static final Map<BaseElytraItem, Identifier> TEXTURE_MAP = ImmutableMap.<BaseElytraItem, Identifier>builder()
            .put(EnigmaticItems.MAJESTIC_ELYTRA.get(), EnigmaticLegacy.location("textures/models/misc/majestic_elytra.png"))
            .put(EnigmaticItems.CHAOS_ELYTRA.get(), EnigmaticLegacy.location("textures/models/misc/chaos_elytra.png"))
            .build();
    // 26.1.2-port: ElytraModel is no longer generic; it animates from any HumanoidRenderState
    private final ElytraModel elytraModel;

    public EnigmaticElytraLayer(RenderLayerParent<S, M> layerParent, EntityModelSet modelSet) {
        super(layerParent);
        this.elytraModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    /** 26.1.2-port: the entity half of the old render(). Runs each frame while the entity's render state is filled. */
    public static void extractElytra(LivingEntity entity, EntityRenderState state) {
        ItemStack elytra = BaseElytraItem.getElytra(entity);
        AtomicBoolean notRender = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(entity).flatMap(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof BaseElytraItem)).ifPresent(curio -> {
            if (!curio.slotContext().visible()) notRender.set(true);
        });
        boolean render = !notRender.get() && !elytra.isEmpty() && elytra.getItem() instanceof BaseElytraItem;
        // the state outlives this call, so it gets a copy of the stack (vanilla does the same for worn equipment)
        state.setRenderData(ELYTRA, render ? elytra.copy() : null);
    }

    // 26.1.2-port: render() -> submit(). copyPropertiesTo() and the explicit setupAnim() call are gone: submitModel()
    // poses the model from the state when it is drawn.
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S state, float yRot, float xRot) {
        ItemStack elytra = state.getRenderData(ELYTRA);
        if (elytra != null && elytra.getItem() instanceof BaseElytraItem item) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, 0.125D);
            // 26.1.2-port: ItemRenderer.getArmorFoilBuffer() is gone. The enchantment glint is a second submit of the
            // same model with the glint render type, one step later in the draw order (as EquipmentLayerRenderer does).
            collector.submitModel(this.elytraModel, state, poseStack, RenderTypes.armorCutoutNoCull(TEXTURE_MAP.get(item)), packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            if (elytra.hasFoil()) {
                collector.order(1).submitModel(this.elytraModel, state, poseStack, RenderTypes.armorEntityGlint(), packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            }
            poseStack.popPose();
        }
    }
}
