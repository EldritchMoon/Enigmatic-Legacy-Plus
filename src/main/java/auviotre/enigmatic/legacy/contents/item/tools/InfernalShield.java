package auviotre.enigmatic.legacy.contents.item.tools;

import net.minecraft.world.InteractionResult;
import auviotre.enigmatic.legacy.client.ClientHelper;
import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseCursedItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.world.item.ToolMaterial;

public class InfernalShield extends BaseCursedItem {
    public InfernalShield() {
        // 26.1.2-port: Tiers -> ToolMaterial. Off-hand equippable like the vanilla shield; dispensers equip
        // items with an equippable component automatically, replacing ArmorItem.DISPENSE_ITEM_BEHAVIOR.
        super(IItemHelper.singleProperties().fireResistant().rarity(Rarity.RARE).durability(ToolMaterial.NETHERITE.durability())
                .equippableUnswappable(EquipmentSlot.OFFHAND));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        if (ClientHelper.hasShiftDown()) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield1");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield2");
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield3");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield4");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield5");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield6");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield7");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield8");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.infernalShield9");
        } else TooltipHandler.holdShift(list);
        TooltipHandler.line(list);
        TooltipHandler.cursedOnly(list, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LivingEntity living && living.isHolding(this)) entity.clearFire();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (EnigmaticHandler.canUse(player, stack)) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(itemAbility);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return super.isValidRepairItem(stack, repairCandidate) || repairCandidate.is(Blocks.OBSIDIAN.asItem());
    }

    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32000;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 16;
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        @SubscribeEvent
        private static void onDamage(LivingDamageEvent.@NotNull Pre event) {
            if (event.getNewDamage() >= Float.MAX_VALUE) return;
            LivingEntity victim = event.getEntity();
            if (event.getSource().getEntity() != null) {
                if (victim.getUseItem().getItem() instanceof InfernalShield) {
                    Vec3 sourcePos = event.getSource().getSourcePosition();
                    if (sourcePos != null) {
                        Vec3 viewVec = victim.calculateViewVector(0.0F, victim.getYHeadRot());
                        Vec3 sourceToSelf = sourcePos.vectorTo(victim.position());
                        if (sourceToSelf.dot(viewVec) > 0.0D) event.setNewDamage(event.getNewDamage() * 1.5F);
                    }
                }
            }
        }
    }
}
