package auviotre.enigmatic.legacy.contents.effect;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.registries.EnigmaticAttributes;
import auviotre.enigmatic.legacy.registries.EnigmaticEffects;
import auviotre.enigmatic.legacy.registries.EnigmaticParticles;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

public class StarlightBlessing extends MobEffect {
    public StarlightBlessing() {
        super(MobEffectCategory.BENEFICIAL, 0xCEE3EC);
        Identifier location = EnigmaticLegacy.location("effect.starlight_blessing");
        this.addAttributeModifier(EnigmaticAttributes.ETHERIUM_SHIELD, location, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEffectExpired(MobEffectEvent.@NotNull Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.is(EnigmaticEffects.STARLIGHT_BLESSING)) {
            event.getEntity().heal(event.getEntity().getMaxHealth());
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.RESISTANCE));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEffectRemove(MobEffectEvent.@NotNull Remove event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.is(EnigmaticEffects.STARLIGHT_BLESSING)) {
            event.getEntity().heal(event.getEntity().getMaxHealth() * 0.4F);
        }
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent.@NotNull Pre event) {
        if (event.getNewDamage() >= Float.MAX_VALUE) return;
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            MobEffectInstance effect = attacker.getEffect(EnigmaticEffects.STARLIGHT_BLESSING);
            if (effect != null) {
                double value = attacker.getAttributeValue(EnigmaticAttributes.ETHERIUM_SHIELD);
                event.setNewDamage(event.getNewDamage() * (1 + (float) value * 0.8F));
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void setEffectParticle(@NotNull EffectParticleModificationEvent event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffect();
        if (effect.is(EnigmaticEffects.STARLIGHT_BLESSING)) {
            int color = Mth.hsvToRgb(entity.getRandom().nextFloat(), 1.0F, 1.0F);
            int i = effect.isAmbient() ? 38 : 255;
            event.setParticleOptions(ColorParticleOption.create(EnigmaticParticles.SPELL.get(), ARGB.color(i, color)));
        }
    }
}
