package auviotre.enigmatic.legacy.client;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.contents.item.rings.MinerRing;
import auviotre.enigmatic.legacy.contents.item.spellstones.other.SpellstoneSword;
import auviotre.enigmatic.legacy.registries.EnigmaticAttachments;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 26.1.2-port: new file. It replaces the ItemProperties.register(...) calls that were in ClientProxy.clientInit(),
 * EnigmaticEye.registerVariants() and SoulCompass.registerVariants().
 *
 * ItemProperties and the "overrides" list in item model JSON are gone. An item's model is chosen by its item model
 * definition (assets/enigmaticlegacyplus/items/<item>.json), which tests properties: "minecraft:condition" takes a
 * yes/no property, "minecraft:range_dispatch" a number. Each record below is one such property, doing what the old
 * function of the same purpose did; the definitions that use them are phase 7.
 *
 * Old predicate (items)                                     -> property to use in the item model definition
 *   enigmaticlegacyplus:tainted (twisted/pure heart)        -> condition      enigmaticlegacyplus:tainted
 *   enigmaticlegacyplus:eldritch_open (infinitum, eldritch
 *     amulet, desolation ring, violence scroll, chaos
 *     elytra, annihilating sword)                           -> range_dispatch enigmaticlegacyplus:eldritch_open
 *   enigmaticlegacyplus:eldritch_open (abyssal heart)       -> range_dispatch enigmaticlegacyplus:eldritch_open
 *                                                              with "tainted_without_holder": true
 *   blocking / charging (annihilating sword)                -> condition      enigmaticlegacyplus:using_in_hand
 *                                                              with "hand": "off_hand" / "main_hand"
 *   out (spellstone sword)                                  -> condition      enigmaticlegacyplus:engine_hook_out
 *   charged (spellstone sword)                              -> condition      enigmaticlegacyplus:angel_blessing_charged
 *   pulling (spellstone sword)                              -> range_dispatch enigmaticlegacyplus:angel_blessing_pull
 *   enigmaticlegacyplus:form (spellstone sword)             -> range_dispatch enigmaticlegacyplus:spellstone_sword_form
 *   on (miner ring)                                         -> condition      enigmaticlegacyplus:miner_ring_on
 *   enigmatic_eye_activated (enigmatic eye)                 -> range_dispatch enigmaticlegacyplus:enigmatic_eye_activated
 *   angle (soul compass)                                    -> range_dispatch enigmaticlegacyplus:soul_compass_angle
 *
 * These old predicates need no code, vanilla has the property:
 *   blocking (spellstone sword, etherium sword, infernal shield), using (ichor spear), pulling (dragon breath bow)
 *                                                           -> condition      minecraft:using_item
 *   pull (dragon breath bow), was ticks used / 20           -> range_dispatch minecraft:use_duration, "scale": 0.05
 *   broken (earth promise), was "is on cooldown"            -> range_dispatch minecraft:cooldown, any value above 0
 *   active (ethereal forging charm, etherium core), filled (sacred chalice), all the general_boolean component
 *                                                           -> select         minecraft:component with
 *                                                              "component": "enigmaticlegacyplus:general_boolean"
 *
 * Every old function was clamped to 0..1 by ItemProperties; the numeric ones below clamp the same way.
 */
public final class ELItemModelProperties {
    private ELItemModelProperties() {
    }

    public static void registerConditional(RegisterConditionalItemModelPropertyEvent event) {
        event.register(ITaintable.LOCATION, Tainted.MAP_CODEC);
        event.register(EnigmaticLegacy.location("using_in_hand"), UsingInHand.MAP_CODEC);
        event.register(EnigmaticLegacy.location("engine_hook_out"), EngineHookOut.MAP_CODEC);
        event.register(EnigmaticLegacy.location("angel_blessing_charged"), AngelBlessingCharged.MAP_CODEC);
        event.register(EnigmaticLegacy.location("miner_ring_on"), MinerRingOn.MAP_CODEC);
    }

    public static void registerRangeSelect(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(EnigmaticLegacy.location("eldritch_open"), EldritchOpen.MAP_CODEC);
        event.register(EnigmaticLegacy.location("angel_blessing_pull"), AngelBlessingPull.MAP_CODEC);
        event.register(EnigmaticLegacy.location("spellstone_sword_form"), SpellstoneSwordForm.MAP_CODEC);
        event.register(EnigmaticLegacy.location("enigmatic_eye_activated"), EnigmaticEyeActivated.MAP_CODEC);
        event.register(EnigmaticLegacy.location("soul_compass_angle"), SoulCompassAngle.MAP_CODEC);
    }

    // ---------------------------------------------------------------------------------------------- yes/no properties

    /** Was (stack, level, entity, i) -> ITaintable.isTainted(stack) ? 1 : 0. */
    public record Tainted() implements ConditionalItemModelProperty {
        public static final MapCodec<Tainted> MAP_CODEC = MapCodec.unit(new Tainted());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return ITaintable.isTainted(stack);
        }

        @Override
        public MapCodec<Tainted> type() {
            return MAP_CODEC;
        }
    }

    /**
     * Was the Annihilating Sword's "blocking" (off hand) and "charging" (main hand): the holder is using this stack
     * with the given hand. Vanilla's minecraft:using_item does not look at the hand.
     */
    public record UsingInHand(InteractionHand hand) implements ConditionalItemModelProperty {
        private static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.comapFlatMap(
                name -> switch (name) {
                    case "main_hand" -> DataResult.success(InteractionHand.MAIN_HAND);
                    case "off_hand" -> DataResult.success(InteractionHand.OFF_HAND);
                    default -> DataResult.error(() -> "Unknown hand '" + name + "', expected main_hand or off_hand");
                },
                hand -> hand == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand");
        public static final MapCodec<UsingInHand> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(HAND_CODEC.fieldOf("hand").forGetter(UsingInHand::hand)).apply(i, UsingInHand::new));

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return owner != null && owner.isUsingItem() && owner.getUseItem() == stack && owner.getUsedItemHand() == this.hand;
        }

        @Override
        public MapCodec<UsingInHand> type() {
            return MAP_CODEC;
        }
    }

    /** Was the Spellstone Sword's "out": Lost Engine resonance, held in the main hand, with an engine hook out. */
    public record EngineHookOut() implements ConditionalItemModelProperty {
        public static final MapCodec<EngineHookOut> MAP_CODEC = MapCodec.unit(new EngineHookOut());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return SpellstoneSword.isResonatingWith(stack, EnigmaticItems.LOST_ENGINE) && owner != null && owner.getMainHandItem() == stack
                    && owner.getData(EnigmaticAttachments.ENIGMATIC_DATA).getEngineHook() != null;
        }

        @Override
        public MapCodec<EngineHookOut> type() {
            return MAP_CODEC;
        }
    }

    /** Was the Spellstone Sword's "charged": Angel Blessing resonance with a charge stored in the general_int component. */
    public record AngelBlessingCharged() implements ConditionalItemModelProperty {
        public static final MapCodec<AngelBlessingCharged> MAP_CODEC = MapCodec.unit(new AngelBlessingCharged());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return SpellstoneSword.isResonatingWith(stack, EnigmaticItems.ANGEL_BLESSING) && stack.getOrDefault(EnigmaticComponents.INT, 0) > 0;
        }

        @Override
        public MapCodec<AngelBlessingCharged> type() {
            return MAP_CODEC;
        }
    }

    /** Was the Miner Ring's "on": MinerRing.getPoint(stack) > 0. */
    public record MinerRingOn() implements ConditionalItemModelProperty {
        public static final MapCodec<MinerRingOn> MAP_CODEC = MapCodec.unit(new MinerRingOn());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return MinerRing.getPoint(stack) > 0;
        }

        @Override
        public MapCodec<MinerRingOn> type() {
            return MAP_CODEC;
        }
    }

    // ---------------------------------------------------------------------------------------------- number properties

    /**
     * Was "eldritch_open": the eldritch_timer component, 0..1. With taintedWithoutHolder (the Abyssal Heart) a stack
     * that no living entity holds, such as a dropped one, reads 1 if tainted and 0 if not, as the old function did
     * when its entity was null.
     */
    public record EldritchOpen(boolean taintedWithoutHolder) implements RangeSelectItemModelProperty {
        public static final MapCodec<EldritchOpen> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Codec.BOOL.optionalFieldOf("tainted_without_holder", false).forGetter(EldritchOpen::taintedWithoutHolder)).apply(i, EldritchOpen::new));

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            if (this.taintedWithoutHolder && (owner == null || owner.asLivingEntity() == null)) return ITaintable.isTainted(stack) ? 1.0F : 0.0F;
            return Mth.clamp(stack.getOrDefault(EnigmaticComponents.ELDRITCH_TIMER, 0.0F), 0.0F, 1.0F);
        }

        @Override
        public MapCodec<EldritchOpen> type() {
            return MAP_CODEC;
        }
    }

    /** Was the Spellstone Sword's "pulling": with Angel Blessing resonance, how far through its use time the holder is, 0..1. */
    public record AngelBlessingPull() implements RangeSelectItemModelProperty {
        public static final MapCodec<AngelBlessingPull> MAP_CODEC = MapCodec.unit(new AngelBlessingPull());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            LivingEntity entity = owner == null ? null : owner.asLivingEntity();
            if (entity != null && SpellstoneSword.isResonatingWith(stack, EnigmaticItems.ANGEL_BLESSING) && entity.isUsingItem() && entity.getUseItem() == stack) {
                return Mth.clamp((float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / stack.getUseDuration(entity), 0.0F, 1.0F);
            }
            return 0.0F;
        }

        @Override
        public MapCodec<AngelBlessingPull> type() {
            return MAP_CODEC;
        }
    }

    /** Was "enigmaticlegacyplus:form": SpellstoneSword.Form.getValue(stack), 0 with no resonance and 0.1 to 1.0 per spellstone. */
    public record SpellstoneSwordForm() implements RangeSelectItemModelProperty {
        public static final MapCodec<SpellstoneSwordForm> MAP_CODEC = MapCodec.unit(new SpellstoneSwordForm());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return Mth.clamp(SpellstoneSword.Form.getValue(stack), 0.0F, 1.0F);
        }

        @Override
        public MapCodec<SpellstoneSwordForm> type() {
            return MAP_CODEC;
        }
    }

    /** Was "enigmatic_eye_activated": 1 when awake, 0.4 or 0.8 during the activation animation, else 0. */
    public record EnigmaticEyeActivated() implements RangeSelectItemModelProperty {
        public static final MapCodec<EnigmaticEyeActivated> MAP_CODEC = MapCodec.unit(new EnigmaticEyeActivated());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return EnigmaticItems.ENIGMATIC_EYE.get().getActivation(stack);
        }

        @Override
        public MapCodec<EnigmaticEyeActivated> type() {
            return MAP_CODEC;
        }
    }

    /** Was the Soul Compass's "angle", 0..1; the needle logic and its state stay in SoulCompass. */
    public record SoulCompassAngle() implements RangeSelectItemModelProperty {
        public static final MapCodec<SoulCompassAngle> MAP_CODEC = MapCodec.unit(new SoulCompassAngle());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return EnigmaticItems.SOUL_COMPASS.get().getAngle(stack, level, owner, seed);
        }

        @Override
        public MapCodec<SoulCompassAngle> type() {
            return MAP_CODEC;
        }
    }
}
