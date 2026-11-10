package auviotre.enigmatic.legacy.client.renderer.model;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
// 26.1.2-port: Model takes its root part and is generic over a state type. Model.Simple is the stateless variant, and its
// final renderToBuffer draws the root, which is what the removed render() helper did through the "main" child.
public class SpellstoneModel extends Model.Simple {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(EnigmaticLegacy.location("spellstone"), "main");
    public SpellstoneModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.0F))
                        .texOffs(32, 32).addBox(4.0F, 4.0F, 4.0F, -8.0F, -8.0F, -8.0F, new CubeDeformation(0.54F))
                , PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}