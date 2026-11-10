package auviotre.enigmatic.legacy.contents.item.tools;

import auviotre.enigmatic.legacy.client.ClientHelper;
import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.SubscribeConfig;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.common.Tags;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class EnderSlayer extends Item {
    // 26.1.2-port: SwordItem/Tier were removed; sword behaviour (tool rules, durability, repair,
    // enchantability, 1 damage per hit) now comes from Item.Properties.sword(ToolMaterial, ...).
    // Repair: c:obsidians/normal is plain obsidian, same as the old Ingredient.of(Blocks.OBSIDIAN).
    public static final ToolMaterial TIER = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1876, 8.5F, 3.5F, 12, Tags.Items.OBSIDIANS_NORMAL);
    public static ModConfigSpec.IntValue specialDamageBoost;

    public EnderSlayer() {
        super(IItemHelper.singleProperties().fireResistant().rarity(Rarity.RARE).component(EnigmaticComponents.CURSED, true).sword(TIER, 3.5F, -2.6F));
    }

    @SubscribeConfig
    public static void onConfig(ModConfigSpec.Builder builder, ModConfig.Type type) {
        builder.translation("item.enigmaticlegacyplus.ender_slayer").push("cursedItems.enderSlayer");
        specialDamageBoost = builder.defineInRange("specialDamageBoost", 150, 0, 1000);
        builder.pop(2);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        if (ClientHelper.hasShiftDown()) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer3", ChatFormatting.GOLD, specialDamageBoost.get() + "%");
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer5");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer6");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer7");
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer8");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer9");
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer10");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer11");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer12");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer13");
        } else {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer1");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enderSlayer2");
            TooltipHandler.line(list);
            TooltipHandler.holdShift(list);
        }
        TooltipHandler.line(list);
        TooltipHandler.cursedOnly(list, stack);
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        @SubscribeEvent
        private static void onDamage(LivingDamageEvent.@NotNull Pre event) {
            if (event.getNewDamage() >= Float.MAX_VALUE) return;
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)) {
                if (attacker.getWeaponItem().is(EnigmaticItems.ENDER_SLAYER) && EnigmaticHandler.canUse(attacker, attacker.getWeaponItem())) {
                    if (event.getEntity() instanceof ServerPlayer player) {
                        ItemCooldowns cooldowns = player.getCooldowns();
                        cooldowns.addCooldown(Items.ENDER_PEARL.getDefaultInstance(), 400);
                        cooldowns.addCooldown(EnigmaticItems.RECALL_POTION.get().getDefaultInstance(), 400);
                        cooldowns.addCooldown(EnigmaticItems.TWISTED_MIRROR.get().getDefaultInstance(), 400);
                        cooldowns.addCooldown(EnigmaticItems.EYE_OF_NEBULA.get().getDefaultInstance(), 400);
                        cooldowns.addCooldown(EnigmaticItems.THE_CUBE.get().getDefaultInstance(), 400);
                    } else if (event.getEntity() instanceof EnderMan || event.getEntity() instanceof Shulker) {
                        event.getEntity().getPersistentData().putInt("EnderSlayerTeleportForbidden", 400);
                    }

                    if (event.getEntity().is(EnigmaticTags.EntityTypes.END_DWELLERS)) {
                        if (attacker.level().dimension().equals(Level.END)) {
                            if (event.getEntity() instanceof EnderMan man && attacker instanceof Player player) {
                                float scale = player.getAttackStrengthScale(0.5F);
                                if (scale >= 0.99F) {
                                    event.setNewDamage((event.getNewDamage() + 100F) * 10F);
                                    man.getPersistentData().putBoolean("EnderSlayerVictim", true);
                                }
                            }
                            event.setNewDamage(event.getNewDamage() * (1.0F + 0.01F * specialDamageBoost.get()));
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        private static void onTick(EntityTickEvent.@NotNull Pre event) {
            Entity entity = event.getEntity();
            if (entity.getPersistentData().contains("EnderSlayerTeleportForbidden")) {
                int tick = entity.getPersistentData().getIntOr("EnderSlayerTeleportForbidden", 0);
                if (tick > 1) entity.getPersistentData().putInt("EnderSlayerTeleportForbidden", tick - 1);
                else entity.getPersistentData().remove("EnderSlayerTeleportForbidden");
            }
        }

        @SubscribeEvent
        private static void onEnderTeleport(EntityTeleportEvent.@NotNull EnderEntity event) {
            if (event.getEntity().getPersistentData().contains("EnderSlayerTeleportForbidden")) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        private static void onLivingDropsLowest(@NotNull LivingDropsEvent event) {
            LivingEntity killed = event.getEntity();
            if (killed instanceof EnderMan && killed.getPersistentData().getBooleanOr("EnderSlayerVictim", false)) {
                for (ItemEntity entity : event.getDrops()) {
                    if (entity.getItem().is(Items.ENDER_PEARL)) {
                        dropXPOrb(killed.level(), killed.getX(), killed.getY(), killed.getZ(), 10);
                    } else if (entity.getItem().is(Items.ENDER_EYE)) {
                        dropXPOrb(killed.level(), killed.getX(), killed.getY(), killed.getZ(), 20);
                    } else {
                        dropXPOrb(killed.level(), killed.getX(), killed.getY(), killed.getZ(), 8);
                    }
                }
                event.getDrops().clear();
                event.setCanceled(true);
            }
        }

        private static void dropXPOrb(Level level, double x, double y, double z, int xp) {
            ExperienceOrb orb = new ExperienceOrb(level, x, y, z, xp);
            level.addFreshEntity(orb);
        }
    }
}