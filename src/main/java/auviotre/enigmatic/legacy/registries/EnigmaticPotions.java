package auviotre.enigmatic.legacy.registries;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class EnigmaticPotions {
    public static final List<Holder<Potion>> ULTIMATE_POTIONS = new ArrayList<>();

    // 26.1.2-port: Potion needs a name (used for its translation key); the registry path keeps the old keys.
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, EnigmaticLegacy.MODID);
    public static final DeferredHolder<Potion, Potion> HASTE = POTIONS.register("haste", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.HASTE, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_HASTE = POTIONS.register("long_haste", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.HASTE, 9600)));
    public static final DeferredHolder<Potion, Potion> STRONG_HASTE = POTIONS.register("strong_haste", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.HASTE, 1800, 1)));
    public static final DeferredHolder<Potion, Potion> MOLTEN_HEART = POTIONS.register("molten_heart", id -> new Potion(id.getPath(), new MobEffectInstance(EnigmaticEffects.MOLTEN_HEART, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_MOLTEN_HEART = POTIONS.register("long_molten_heart", id -> new Potion(id.getPath(), new MobEffectInstance(EnigmaticEffects.MOLTEN_HEART, 9600)));
    // Ultimate Potions
    public static final DeferredHolder<Potion, Potion> ULTIMATE_NIGHT_VISION = POTIONS.register("ultimate_night_vision", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.NIGHT_VISION, 18000)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_INVISIBILITY = POTIONS.register("ultimate_invisibility", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.INVISIBILITY, 18000)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_LEAPING = POTIONS.register("ultimate_leaping", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.JUMP_BOOST, 3000, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_FIRE_RESISTANCE = POTIONS.register("ultimate_fire_resistance", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 18000)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_SWIFTNESS = POTIONS.register("ultimate_swiftness", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.SPEED, 3000, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_SLOWNESS = POTIONS.register("ultimate_slowness", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.SLOWNESS, 3000, 4)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_TURTLE_MASTER = POTIONS.register("ultimate_turtle_master", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.SLOWNESS, 1000, 5), new MobEffectInstance(MobEffects.RESISTANCE, 1000, 3)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_WATER_BREATHING = POTIONS.register("ultimate_water_breathing", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.WATER_BREATHING, 18000)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_HEALING = POTIONS.register("ultimate_healing", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_HARMING = POTIONS.register("ultimate_harming", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_POISON = POTIONS.register("ultimate_poison", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.POISON, 1200, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_REGENERATION = POTIONS.register("ultimate_regeneration", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.REGENERATION, 1200, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_STRENGTH = POTIONS.register("ultimate_strength", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.STRENGTH, 3000, 2)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_WEAKNESS = POTIONS.register("ultimate_weakness", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.WEAKNESS, 9000)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_LUCK = POTIONS.register("ultimate_luck", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.LUCK, 10800)));
    public static final DeferredHolder<Potion, Potion> ULTIMATE_SLOW_FALLING = POTIONS.register("ultimate_slow_falling", id -> new Potion(id.getPath(), new MobEffectInstance(MobEffects.SLOW_FALLING, 9000)));
}
