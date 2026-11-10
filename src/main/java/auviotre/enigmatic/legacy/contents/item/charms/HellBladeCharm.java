package auviotre.enigmatic.legacy.contents.item.charms;

import auviotre.enigmatic.legacy.client.ClientHelper;
import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.SubscribeConfig;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.compat.CompatHandler;
// TODO(port): Thirst Was Taken compat is excluded from the build until phase 9 (see build.gradle).
// import auviotre.enigmatic.legacy.compat.thirst.ThirstCompatHandler;
import auviotre.enigmatic.legacy.contents.item.generic.BaseCurioItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class HellBladeCharm extends BaseCurioItem {
    public static ModConfigSpec.IntValue attackDamage;
    public static ModConfigSpec.IntValue armorDebuff;

    public HellBladeCharm() {
        super(IItemHelper.singleProperties().rarity(Rarity.UNCOMMON).component(EnigmaticComponents.BOOLEAN, false));
    }

    @SubscribeConfig
    public static void onConfig(ModConfigSpec.Builder builder, ModConfig.Type type) {
        builder.translation("item.enigmaticlegacyplus.hell_blade_charm").push("else.hellBladeCharm");
        attackDamage = builder.defineInRange("attackDamage", 100, 0, 200);
        armorDebuff = builder.defineInRange("armorDebuff1", 100, 0, 100);
        builder.pop(2);
    }

    private static float getModifier(LivingEntity entity) {
        double armor = getAttributeBase(entity, Attributes.ARMOR) - entity.getAttributeValue(Attributes.ARMOR);
        double toughness = getAttributeBase(entity, Attributes.ARMOR_TOUGHNESS) - entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        return (float) (armor * 0.2 + toughness * 0.4);
    }

    public static double getAttributeBase(LivingEntity entity, Holder<Attribute> holder) {
        if (entity == null) return 0;
        AttributeInstance attribute = entity.getAttribute(holder);
        if (attribute == null) return 0;
        double amount = attribute.getBaseValue();
        Set<AttributeModifier> modifiers = attribute.getModifiers();
        for (AttributeModifier modifier : modifiers)
            if (modifier.operation().id() == 0 && modifier.amount() > 0) amount += modifier.amount();
        double multiplier = 0;
        for (AttributeModifier modifier : modifiers)
            if (modifier.operation().id() == 1 && modifier.amount() > 0) multiplier += modifier.amount();
        amount = amount * (1 + multiplier);
        return amount;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        TooltipHandler.line(list);
        ItemStack curio = EnigmaticHandler.getCurio(Minecraft.getInstance().player, EnigmaticItems.HELL_BLADE_CHARM);
        if (curio == stack && !stack.getOrDefault(EnigmaticComponents.BOOLEAN, false)) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.hellBladeCharmUnequip");
            TooltipHandler.line(list);
        }
        if (ClientHelper.hasShiftDown()) {
            int threshold = EnigmaticHandler.isTheCursedOne(Minecraft.getInstance().player) ? 80 : 64;
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.hellBladeCharm1", ChatFormatting.GOLD, threshold + "%");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.hellBladeCharm2");
        } else TooltipHandler.holdShift(list);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getAttributesTooltip(List<Component> list, TooltipContext context, ItemStack stack) {
        TooltipHandler.line(list::add);
        TooltipHandler.line(list::add, "curios.modifiers.charm", ChatFormatting.GOLD);
        float modifier = (int) (getModifier(Minecraft.getInstance().player) * 10) * 0.1F;
        TooltipHandler.line(list::add, "attribute.modifier.plus.0", ChatFormatting.BLUE, modifier, Component.translatable("attribute.name.generic.attack_damage"));
        TooltipHandler.line(list::add, "attribute.modifier.plus.1", ChatFormatting.BLUE, attackDamage.get(), Component.translatable("attribute.name.generic.attack_damage"));
        TooltipHandler.line(list::add, "attribute.modifier.take.1", ChatFormatting.RED, armorDebuff.get(), Component.translatable("attribute.name.generic.armor"));
        TooltipHandler.line(list::add, "attribute.modifier.take.1", ChatFormatting.RED, armorDebuff.get(), Component.translatable("attribute.name.generic.armor_toughness"));
        return list;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof LivingEntity owner && stack.getOrDefault(EnigmaticComponents.BOOLEAN, false)) {
            if (stack != EnigmaticHandler.getCurio(owner, this)) stack.set(EnigmaticComponents.BOOLEAN, false);
        }
    }

    public Multimap<Holder<Attribute>, AttributeModifier> createAttributeMap(LivingEntity entity) {
        Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
        Identifier location = IItemHelper.getLocation(this);
        double armor = -armorDebuff.get() * 0.01;
        attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(location, getModifier(entity), AttributeModifier.Operation.ADD_VALUE));
        attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(EnigmaticLegacy.location("hell_blade_charm.boost"), attackDamage.get() * 0.01, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        attributes.put(Attributes.ARMOR, new AttributeModifier(location, armor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(location, armor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        return attributes;
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        context.entity().getAttributes().addTransientAttributeModifiers(this.createAttributeMap(context.entity()));
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        context.entity().getAttributes().removeAttributeModifiers(this.createAttributeMap(context.entity()));
        super.onUnequip(context, newStack, stack);
    }

    @Override
    public boolean canUnequip(SlotContext context, ItemStack stack) {
        return stack.getOrDefault(EnigmaticComponents.BOOLEAN, false) || context.entity().hasInfiniteMaterials();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Decorator implements IItemDecorator {
        static final Identifier SPRITE = EnigmaticLegacy.location("decorator/hell_blade_charm_slot");

        @Override
        public boolean render(GuiGraphicsExtractor graphics, Font font, @NotNull ItemStack stack, int x, int y) {
            ItemStack curio = EnigmaticHandler.getCurio(Minecraft.getInstance().player, EnigmaticItems.HELL_BLADE_CHARM);
            if (curio == stack && !stack.getOrDefault(EnigmaticComponents.BOOLEAN, false)) {
                graphics.pose().pushMatrix(); // 26.1.2-port: pushPose -> pushMatrix (2D matrix stack)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, x - 1, y - 1, 18, 18); // 26.1.2-port: blitSprite names its pipeline
                graphics.pose().popMatrix();
                return true;
            }
            return false;
        }
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        @SubscribeEvent
        private static void onDamage(LivingDamageEvent.@NotNull Pre event) {
            if (event.getNewDamage() >= Float.MAX_VALUE) return;
            LivingEntity victim = event.getEntity();
            if (EnigmaticHandler.hasCurio(victim, EnigmaticItems.HELL_BLADE_CHARM)) {
                float modifier = Mth.sqrt(getModifier(victim) * 0.01F) + 1;
                event.setNewDamage(event.getNewDamage() * modifier);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        private static void onDamageLowest(LivingDamageEvent.@NotNull Pre event) {
            if (event.getNewDamage() >= Float.MAX_VALUE) return;
            LivingEntity victim = event.getEntity();
            Entity entity = event.getSource().getEntity();
            if (entity instanceof LivingEntity attacker && EnigmaticHandler.hasCurio(attacker, EnigmaticItems.HELL_BLADE_CHARM)) {
                if (attacker.level() instanceof ServerLevel server && EnigmaticHandler.hasCurio(attacker, EnigmaticItems.HELL_BLADE_CHARM) && event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)) {
                    float healthPer = victim.getHealth() * (EnigmaticHandler.isTheCursedOne(attacker) ? 0.8F : 0.64F);
                    if (event.getNewDamage() >= healthPer && !victim.getType().getDescriptionId().contains("dummy")) {
                        attacker.heal((float) (victim.getHealth() * 0.6));
                        if (attacker instanceof Player player) {
                            player.getFoodData().setSaturation(player.getFoodData().getFoodLevel());
                            // TODO(port): re-enable with the Thirst compat (phase 9).
                            // if (CompatHandler.isLoaded("thirst")) ThirstCompatHandler.setHellBladeRecover(player);
                        }
                        event.setNewDamage(Math.min(event.getOriginalDamage() * 5.0F, Float.MAX_VALUE / 10.0F));
                        double hOffset = victim.getBbWidth() / 6;
                        double yOffset = victim.getBbHeight() / 4;
                        server.sendParticles(ParticleTypes.RAID_OMEN, victim.getX(), victim.getY(0.5), victim.getZ(), 32, hOffset, yOffset, hOffset, 0);
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        private static void onDeath(@NotNull LivingDeathEvent event) {
            Entity entity = event.getSource().getEntity();
            if (entity instanceof LivingEntity attacker && EnigmaticHandler.hasCurio(attacker, EnigmaticItems.HELL_BLADE_CHARM)) {
                if (EnigmaticHandler.isTheCursedOne(attacker) && attacker.getRandom().nextFloat() < 0.75F) return;
                ItemStack curio = EnigmaticHandler.getCurio(attacker, EnigmaticItems.HELL_BLADE_CHARM);
                curio.set(EnigmaticComponents.BOOLEAN, true);
            }
        }

        @SubscribeEvent
        private static void onDamaged(LivingDamageEvent.@NotNull Post event) {
            if (event.getHealthDamage() > Float.MAX_VALUE / 10) return;
            if (event.getSource().getEntity() instanceof Player player) {
                CompoundTag data = EnigmaticHandler.getPersistedData(player);
                if (player.level().dimension().equals(Level.NETHER) && !data.getBooleanOr("LootedHellBladeCharm", false)) {
                    boolean hardcore = player.level().getLevelData().isHardcore();
                    float point = data.getFloatOr("HellPoint", 0.0F);
                    float healthFactor = 1 - player.getHealth() / player.getMaxHealth();
                    healthFactor = healthFactor * healthFactor;
                    if (healthFactor < (hardcore ? 0.16F : 0.2F)) return;
                    float damageFactor = Math.min(event.getOriginalDamage(), event.getHealthDamage()) / player.getMaxHealth() * (hardcore ? 9.0F : 8.0F);
                    data.putFloat("HellPoint", point + Math.min(100.0F, healthFactor * damageFactor));
                }
            }
        }
    }
}
