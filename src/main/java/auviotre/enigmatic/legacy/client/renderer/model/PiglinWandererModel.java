package auviotre.enigmatic.legacy.client.renderer.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.piglin.AdultPiglinModel;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// 26.1.2-port: PiglinModel is abstract and no longer generic; vanilla split it into AdultPiglinModel and BabyPiglinModel
// (the method they implement is package-private, so a mod can only extend one of those two). The wanderer is never a
// baby and used the piglin brute layer, which is the adult mesh.
@OnlyIn(Dist.CLIENT)
public class PiglinWandererModel extends AdultPiglinModel {
    public PiglinWandererModel(ModelPart part) {
        super(part);
    }

    // 26.1.2-port: setupAnim() gets the render state instead of the entity.
    @Override
    public void setupAnim(PiglinRenderState state) {
        super.setupAnim(state);
        // 26.1.2-port: was "pose == CROSSBOW_HOLD && entity.isCharging()". PiglinWanderer.getArmPose() only returns
        // CROSSBOW_HOLD while isCharging() is true, so the pose alone says the same thing and no extra state is needed.
        if (state.armPose == PiglinArmPose.CROSSBOW_HOLD) {
            // 26.1.2-port: entity.isLeftHanded() -> state.mainArm (Mob.getMainArm() is LEFT exactly when left-handed)
            if (state.mainArm == HumanoidArm.LEFT) {
                this.leftArm.xRot = this.leftArm.xRot * 0.16F - (float) Math.PI;
                this.leftArm.yRot = 0.0F;
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 10);
                this.rightArm.yRot = 0.0F;
            } else {
                this.rightArm.xRot = this.rightArm.xRot * 0.16F - (float) Math.PI;
                this.rightArm.yRot = 0.0F;
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 10);
                this.leftArm.yRot = 0.0F;
            }
            // 26.1.2-port: the sleeve.copyFrom(arm) calls are gone. The sleeves are child parts of the arms in the
            // player mesh now, so they follow the arms on their own (and piglin models have no sleeve fields).
        }
    }
}
