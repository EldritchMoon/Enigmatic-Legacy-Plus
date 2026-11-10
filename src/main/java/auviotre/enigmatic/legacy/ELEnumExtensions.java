package auviotre.enigmatic.legacy;

import auviotre.enigmatic.legacy.contents.item.spellstones.other.SpellstoneSword;
import net.minecraft.client.model.HumanoidModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;


public class ELEnumExtensions {
    @OnlyIn(Dist.CLIENT)
    public static final EnumProxy<HumanoidModel.ArmPose> SPELLSTONE_SWORD = new EnumProxy<>(
            // 26.1.2-port: ArmPose has a second flag, affectsOffhandPose. true = the pose sets both arms itself and the
            // other arm's own pose is not applied afterwards; in 1.21.1 that was simply what happened while using an
            // item, which is the only time this pose is returned. The transformer gets the render state, not the entity.
            HumanoidModel.ArmPose.class, true, true,
            (IArmPoseTransformer) (model, state, arm) -> SpellstoneSword.ClientExtension.animate(model.rightArm, model.leftArm, state, arm)
    );
}
