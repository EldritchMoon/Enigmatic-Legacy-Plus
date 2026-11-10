package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.renderer.model.PiglinWandererModel;
import auviotre.enigmatic.legacy.contents.entity.PiglinWanderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.piglin.AdultPiglinModel;
import net.minecraft.client.model.monster.piglin.PiglinModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CrossbowItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// 26.1.2-port: render-state renderer, following vanilla's PiglinRenderer. It uses vanilla's PiglinRenderState as is.
@OnlyIn(Dist.CLIENT)
public class PiglinWandererRender extends HumanoidMobRenderer<PiglinWanderer, PiglinRenderState, PiglinModel> {
    private static final Identifier TEXTURE = EnigmaticLegacy.location("textures/entity/piglin_wanderer.png");

    // 26.1.2-port: the inner/outer armour layers became one ArmorModelSet (a model per armour slot).
    public PiglinWandererRender(EntityRendererProvider.Context context, ModelLayerLocation layer, ArmorModelSet<ModelLayerLocation> armorSet) {
        // 26.1.2-port: the renderer takes an adult and a baby model; the wanderer is never a baby, so both are the same
        // mesh (vanilla does this for the piglin brute). The three head scale numbers (1.0019531, 1, 1.0019531) became
        // a CustomHeadLayer.Transforms; vanilla's piglin constant holds the same 1.0019531 horizontal scale.
        super(context, new PiglinWandererModel(context.bakeLayer(layer)), new PiglinWandererModel(context.bakeLayer(layer)), 0.5F, PiglinRenderer.PIGLIN_CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer<>(
                        this,
                        ArmorModelSet.bake(armorSet, context.getModelSet(), AdultPiglinModel::new),
                        context.getEquipmentRenderer()
                )
        );
    }

    @Override
    public PiglinRenderState createRenderState() {
        return new PiglinRenderState();
    }

    // 26.1.2-port: what the model and isShaking() used to read from the entity. Same fields as vanilla's PiglinRenderer
    // fills, except isBrute, which only picks vanilla's texture.
    @Override
    public void extractRenderState(PiglinWanderer entity, PiglinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armPose = entity.getArmPose();
        state.maxCrossbowChageDuration = CrossbowItem.getChargeDuration(entity.getUseItem(), entity);
        state.isConverting = entity.isConverting();
    }

    @Override
    public Identifier getTextureLocation(PiglinRenderState state) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(PiglinRenderState state) {
        return super.isShaking(state) || state.isConverting;
    }
}
