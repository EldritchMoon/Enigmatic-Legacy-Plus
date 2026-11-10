package auviotre.enigmatic.legacy.mixin;

import auviotre.enigmatic.legacy.api.item.ISpellstone;
import auviotre.enigmatic.legacy.contents.item.charms.ScorchedCharm;
import auviotre.enigmatic.legacy.contents.item.generic.BaseElytraItem;
import auviotre.enigmatic.legacy.contents.item.rings.RedemptionRing;
import auviotre.enigmatic.legacy.contents.item.scrolls.CursedScroll;
import auviotre.enigmatic.legacy.contents.item.spellstones.ForgottenIce;
import auviotre.enigmatic.legacy.contents.item.tools.InfernalShield;
import auviotre.enigmatic.legacy.contents.item.tools.TotemOfMalice;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.packets.client.ForgerCrystalPacket;
import auviotre.enigmatic.legacy.packets.client.TotemOfMalicePacket;
import auviotre.enigmatic.legacy.registries.EnigmaticDamageTypes;
import auviotre.enigmatic.legacy.registries.EnigmaticEffects;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ILivingEntityExtension {
    @Shadow
    private boolean effectsDirty;

    public MixinLivingEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract ItemStack getItemBlockingWith();

    @Shadow
    public abstract ItemStack getItemInHand(InteractionHand hand);

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract boolean removeAllEffects(); // 26.1.2-port: NeoForge's EffectCure system was removed

    @Shadow
    public abstract float getMaxHealth();

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickMix(CallbackInfo info) {
        if (this.level().isClientSide() && ForgottenIce.freezingBoost.get() && this.isFullyFrozen() && Math.random() > 0.25D) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getRandomX(0.6D), this.getRandomY() + 0.1D, this.getRandomZ(0.6D), 0.0D, -0.05D, 0.0D);
        }
        if (!this.canFreeze() && this.getTicksFrozen() > 0) this.setTicksFrozen(0);
    }

    @Inject(method = "tickEffects", at = @At("RETURN"))
    public void tickEffectsMix(CallbackInfo ci) {
        if (this.self().getRandom().nextBoolean() && this.self().hasEffect(EnigmaticEffects.STARLIGHT_BLESSING))
            this.effectsDirty = true;
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    public void canFreezeMix(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(info.getReturnValue() && !ISpellstone.get(this.self()).is(EnigmaticItems.FORGOTTEN_ICE));
        // && EnchantmentHelper.getEnchantmentLevel(EnigmaticAddonEnchantments.FROST_PROTECTION, this.self()) < 16
        // !this.self().hasEffect(EnigmaticAddonEffects.FROZEN_HEART_EFFECT) &&
    }

    // 26.1.2-port: the isDamageSourceBlocked() and isBlocking() injectors that were here are gone.
    // LivingEntity.isDamageSourceBlocked() no longer exists: blocking is applyItemBlocking(), driven by the
    // blocks_attacks component. The Infernal Shield's on-block effects moved to InfernalShield.Events.onShieldBlock, and
    // "blocks the moment it is raised" is the component's block delay of 0, which is all the isBlocking() injector did.
    // What is left is the one thing the old injector did that neither of those can: it answered before vanilla's
    // piercing-arrow check, so a player's Infernal Shield also stopped piercing arrows.
    @WrapOperation(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;getPierceLevel()B"))
    private byte infernalShieldStopsPiercingArrows(AbstractArrow arrow, Operation<Byte> original) {
        ItemStack blockingWith = this.getItemBlockingWith();
        if (this.self() instanceof Player && blockingWith != null && blockingWith.getItem() instanceof InfernalShield) return 0;
        return original.call(arrow);
    }

    // 26.1.2-port: new, replaces the Caelus fall-flying attribute. Vanilla's canGlide() only looks at equipment slots
    // (chest-slot mod elytra already work through their GLIDER component). This adds a usable mod elytra in a Curios
    // slot, under the same conditions vanilla applies. Player.canGlide() calls this through super, so creative flight
    // still rules it out.
    @Inject(method = "canGlide", at = @At("RETURN"), cancellable = true)
    private void canGlideWithCurioElytra(CallbackInfoReturnable<Boolean> info) {
        if (!info.getReturnValue() && !this.onGround() && !this.isPassenger() && !this.self().hasEffect(MobEffects.LEVITATION)
                && BaseElytraItem.hasCurioGlider(this.self())) {
            info.setReturnValue(true);
        }
    }

    // 26.1.2-port: new, needed by the injector above. Every second of gliding, vanilla picks a random equipment slot
    // that holds a glider and damages that item. With only a Curios elytra there is no such slot and picking from the
    // empty list would throw. So in that case this sends the game event vanilla was about to send and leaves; the
    // Curios elytra's own durability is handled by BaseElytraItem.elytraFlightTick().
    @Inject(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getRandom(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/lang/Object;"), cancellable = true)
    private void skipEquipmentWearForCurioElytra(CallbackInfo info) {
        if (EquipmentSlot.VALUES.stream().noneMatch(slot -> LivingEntity.canGlideUsing(this.self().getItemBySlot(slot), slot))) {
            this.gameEvent(GameEvent.ELYTRA_GLIDE);
            info.cancel();
        }
    }

    @Inject(method = "canStandOnFluid", at = @At("RETURN"), cancellable = true)
    public void canStandOnFluidMix(FluidState fluidState, @NotNull CallbackInfoReturnable<Boolean> info) {
        if (!info.getReturnValue() && fluidState.is(FluidTags.LAVA)) {
            if (this.self().isCrouching()) return;
            info.setReturnValue(ScorchedCharm.EQUIP_LIST.contains(this.self()));
        }
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
    private void checkMalice(@NotNull DamageSource source, CallbackInfoReturnable<Boolean> info) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || info.getReturnValue()) return;
        if (EnigmaticHandler.isTheCursedOne(this.self())) {
            ItemStack totem = null;
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = getItemInHand(hand);
                if (stack.is(EnigmaticItems.TOTEM_OF_MALICE) && CommonHooks.onLivingUseTotem(this.self(), source, stack, hand)) {
                    totem = stack;
                    break;
                }
            }
            ItemStack curio = EnigmaticHandler.getCurio(this.self(), EnigmaticItems.TOTEM_OF_MALICE);
            if (!curio.isEmpty()) totem = curio;
            if (totem != null && TotemOfMalice.getDurability(totem) > 0) {
                TotemOfMalice.hurtAndBreak(totem, this.self());
                totem = totem.copy();
                if (this.self() instanceof ServerPlayer player) {
                    player.awardStat(Stats.ITEM_USED.get(EnigmaticItems.TOTEM_OF_MALICE.get()), 1);
                    CriteriaTriggers.USED_TOTEM.trigger(player, totem);
                    player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                }

                float damage = this.getMaxHealth() * (0.8F + CursedScroll.getItemCurseLevel(totem) * 0.3F);
                List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8));

                for (LivingEntity entity : entities) {
                    if (entity == this.self()) continue;
                    entity.knockback(0.45F, entity.getX() - this.getX(), entity.getZ() - this.getZ());
                    entity.hurt(EnigmaticDamageTypes.source(level(), EnigmaticDamageTypes.EVIL_CURSE, this), damage);
                    entity.invulnerableTime = 0;
                    if (entity.level() instanceof ServerLevel level) {
                        double hOffset = entity.getBbWidth() / 6;
                        double yOffset = entity.getBbHeight() / 4;
                        level.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY(0.5), entity.getZ(), 12, hOffset, yOffset, hOffset, 0.01);
                    }
                }

                this.setHealth(Math.max(1.0F, this.getMaxHealth() - 1.0F));
                this.removeAllEffects(); // 26.1.2-port: was removeEffectsCuredBy(PROTECTED_BY_TOTEM); the vanilla totem clears all effects
                if (this.isOnFire()) this.clearFire();
                if (this.isFreezing()) this.setTicksFrozen(0);
                if (level() instanceof ServerLevel level)
                    PacketDistributor.sendToPlayersNear(level, null, this.getX(), this.getY(), this.getZ(), 32, new TotemOfMalicePacket(this.position(), totem));

                info.setReturnValue(true);
                return;
            }
        }
        ItemStack curio = EnigmaticHandler.getCurio(this.self(), EnigmaticItems.FORGER_CRYSTAL);
        if (!curio.isEmpty() && RedemptionRing.Helper.canUseRelic(this.self())) {
            ItemStack unbreakable = null;
            // 26.1.2-port: getAllSlots() is gone; this walks the same slots in the same order (hands, armour, body),
            // plus the new saddle slot, which players do not have.
            for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                ItemStack slot = this.self().getItemBySlot(equipmentSlot);
                if (slot.has(DataComponents.UNBREAKABLE)) {
                    unbreakable = slot;
                    break;
                }
            }
            if (unbreakable != null) {
                unbreakable.remove(DataComponents.UNBREAKABLE);

                this.setHealth(Math.max(2.0F, this.getMaxHealth() * 0.1F));
                this.removeAllEffects(); // 26.1.2-port: was removeEffectsCuredBy(PROTECTED_BY_TOTEM); the vanilla totem clears all effects
                this.self().addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                this.self().addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                this.self().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                if (level() instanceof ServerLevel level)
                    PacketDistributor.sendToPlayersNear(level, null, this.getX(), this.getY(), this.getZ(), 32, new ForgerCrystalPacket(this.position(), curio.copy()));

                info.setReturnValue(true);
            }
        }
    }
}
