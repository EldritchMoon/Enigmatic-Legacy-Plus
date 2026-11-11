package auviotre.enigmatic.legacy.contents.item.generic;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.attachement.EnigmaticData;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.packets.server.UpdateElytraBoostPacket;
import auviotre.enigmatic.legacy.registries.EnigmaticAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

// 26.1.2-port: the Equipable interface, ElytraItem and Caelus are gone. Chest-slot gliding comes from the vanilla
// GLIDER + EQUIPPABLE components (vanilla also handles flight durability and glide events for the chest slot).
// Gliding from a Curios slot, which Caelus used to provide, is the canGlide()/updateFallFlying() pair in MixinLivingEntity.
public abstract class BaseElytraItem extends BaseCurioItem {
    protected static final Identifier LOCATION = EnigmaticLegacy.location("elytra");
    protected static final AttributeModifier ELYTRA_MODIFIER = new AttributeModifier(LOCATION, 1.0, AttributeModifier.Operation.ADD_VALUE);
    @OnlyIn(Dist.CLIENT)
    private boolean isBoosting;

    public BaseElytraItem(Properties properties) {
        super(properties.component(DataComponents.GLIDER, Unit.INSTANCE)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setDamageOnHurt(false).build()));
    }

    /** 26.1.2-port: replaces ElytraItem.isFlyEnabled. */
    public static boolean isFlyEnabled(ItemStack stack) {
        return !stack.nextDamageWillBreak();
    }

    /** 26.1.2-port: new. True when a BaseElytraItem that can still fly sits in a Curios slot; used by MixinLivingEntity. */
    public static boolean hasCurioGlider(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof BaseElytraItem && isFlyEnabled(stack)))
                .isPresent();
    }

    public static ItemStack getElytra(LivingEntity entity) {
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(entity);
        if (curios.isPresent()) {
            Optional<SlotResult> firstCurio = curios.get().findFirstCurio(stack -> stack.getItem() instanceof BaseElytraItem);
            if (firstCurio.isPresent()) return firstCurio.get().stack();
        }
        ItemStack itemBySlot = entity.getItemBySlot(EquipmentSlot.CHEST);
        return itemBySlot.getItem() instanceof BaseElytraItem ? itemBySlot : ItemStack.EMPTY;
    }

    protected abstract boolean flyingBoost(Player player);

    @OnlyIn(Dist.CLIENT)
    protected abstract void addParticle(Player player);

    @OnlyIn(Dist.CLIENT)
    public void handleBoosting(Player player) {
        if (EnigmaticLegacy.PROXY.getClientPlayer() != player) return;
        if (Minecraft.getInstance().options.keyJump.isDown() && flyingBoost(player)) {
            if (!isBoosting) ClientPacketDistributor.sendToServer(new UpdateElytraBoostPacket(isBoosting = true));
        } else if (isBoosting) ClientPacketDistributor.sendToServer(new UpdateElytraBoostPacket(isBoosting = false));
    }

    // 26.1.2-port: no longer a NeoForge hook; only called from curioTick for the Curios back slot.
    public boolean elytraFlightTick(ItemStack stack, @NotNull LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide()) {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0) {
                if (nextFlightTick % 20 == 0)
                    stack.hurtAndBreak(1, entity, EquipmentSlot.CHEST);
                // 26.1.2-port: the ELYTRA_GLIDE game event that was sent here now comes from vanilla's
                // LivingEntity.updateFallFlying() for every gliding entity (and from MixinLivingEntity when the only
                // glider is in a Curios slot), so sending it here as well would double it.
            }
        } else if (entity instanceof Player player) handleBoosting(player);
        return true;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 1F, 1F);
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        // 26.1.2-port: was "... && getElytra(context.entity()).isEmpty()". The worn elytra may be this very stack,
        // see BaseCurioItem.canEquip.
        ItemStack worn = getElytra(context.entity());
        return super.canEquip(context, stack) && (worn.isEmpty() || worn == stack);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return super.canEquip(stack, slot, entity) && EnigmaticHandler.canUse(entity, stack) && getElytra(entity).isEmpty();
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        @SubscribeEvent
        private static void onTick(PlayerTickEvent.@NotNull Pre event) {
            Player player = event.getEntity();
            EnigmaticData data = player.getData(EnigmaticAttachments.ENIGMATIC_DATA);
            // 26.1.2-port: the Caelus fall-flying attribute is gone; only the boost logic remains here.
            ItemStack stack = getElytra(player);
            if (stack.isEmpty() || !isFlyEnabled(stack)) return;
            BaseElytraItem elytra = (BaseElytraItem) stack.getItem();
            if (player instanceof ServerPlayer && data.isElytraBoosting()) {
                elytra.flyingBoost(player);
                int flightTicks = player.getFallFlyingTicks();
                int nextFlightTick = flightTicks + 1;
                if (nextFlightTick % 5 == 0) stack.hurtAndBreak(1, player, EquipmentSlot.CHEST);
            }
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        private static void onClientTick(PlayerTickEvent.@NotNull Pre event) {
            Player player = event.getEntity();
            EnigmaticData data = player.getData(EnigmaticAttachments.ENIGMATIC_DATA);
            ItemStack stack = getElytra(player);
            // 26.1.2-port: elytraFlightTick no longer runs on the client for the chest slot, so check the jump key here.
            if (player.level().isClientSide() && player.isFallFlying() && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BaseElytraItem chest)
                chest.handleBoosting(player);
            if (player.level().isClientSide() && data.isElytraBoosting()) {
                if (!player.isFallFlying()) {
                    if (player == EnigmaticLegacy.PROXY.getClientPlayer())
                        if (stack.getItem() instanceof BaseElytraItem elytra)
                            elytra.handleBoosting(player);
                        else data.setElytraBoosting(false);
                    return;
                }
                if (stack.getItem() instanceof BaseElytraItem elytra) elytra.addParticle(player);
            }
        }
    }
}
