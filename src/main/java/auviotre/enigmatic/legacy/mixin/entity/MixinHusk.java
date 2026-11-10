package auviotre.enigmatic.legacy.mixin.entity;

import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Husk.class)
public abstract class MixinHusk extends Zombie {
    protected MixinHusk(EntityType<? extends Zombie> type, Level world) {
        super(type, world);
    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    // 26.1.2-port: doHurtTarget takes the ServerLevel first
    public void doHurtTargetMix(ServerLevel level, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && EnigmaticHandler.isCurseBoosted(this) && this.getWeaponItem().isEmpty() && entity instanceof LivingEntity living) {
            float effectiveDifficulty = level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100 * (int) effectiveDifficulty), this);
        }
    }
}
