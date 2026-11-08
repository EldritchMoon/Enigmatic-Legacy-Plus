package auviotre.enigmatic.legacy.contents.effect;

import auviotre.enigmatic.legacy.registries.EnigmaticEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;
import net.minecraft.server.level.ServerLevel;

public class MoltenHeart extends MobEffect {
    public MoltenHeart() {
        super(MobEffectCategory.BENEFICIAL, 0xE66410);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, @NotNull LivingEntity entity, int amplifier) { // 26.1.2-port: server-only, gets the level
        if (entity.isOnFire()) entity.clearFire();
        return super.applyEffectTick(level, entity, amplifier);
    }

    @SubscribeEvent
    public void onEntityHurt(@NotNull LivingIncomingDamageEvent event) {
        if (event.getAmount() >= Float.MAX_VALUE) return;
        if (event.getEntity().hasEffect(EnigmaticEffects.MOLTEN_HEART)) {
            if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                event.setCanceled(true);
            }
        }
    }
}
