package auviotre.enigmatic.legacy.contents.item.charms;

import auviotre.enigmatic.legacy.client.ClientHelper;
import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.SubscribeConfig;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.CursedCurioItem;
import auviotre.enigmatic.legacy.contents.item.rings.RedemptionRing;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import net.minecraft.tags.ItemTags;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class ForgerCrystal extends CursedCurioItem {
    public static ModConfigSpec.BooleanValue unbreakableRestriction;

    public ForgerCrystal() {
        super(IItemHelper.singleProperties().fireResistant().rarity(Rarity.RARE), true);
    }

    @SubscribeConfig
    public static void onConfig(ModConfigSpec.Builder builder, ModConfig.Type type) {
        builder.translation("item.enigmaticlegacyplus.forger_crystal").push("blessItems.forgerCrystal");
        unbreakableRestriction = builder.define("unbreakableRestriction", true);
        builder.pop(2);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        TooltipHandler.line(list);
        if (ClientHelper.hasShiftDown()) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.enchantersPearl1");
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.forgerCrystal1");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.forgerCrystal2");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.forgerCrystal3");
            Player player = EnigmaticLegacy.PROXY.getClientPlayer();
            if (RedemptionRing.Helper.canUseRelic(player) && player != null) {
                TooltipHandler.line(list);
                TooltipHandler.line(list, "tooltip.enigmaticlegacy.forgerCrystal4");
            }
        } else TooltipHandler.holdShift(list);
        TooltipHandler.line(list);
        TooltipHandler.cursedOnly(list, stack);
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return super.canEquip(context, stack) && !EnigmaticHandler.hasCurio(context.entity(), EnigmaticItems.FORGER_GEM);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext context, Identifier id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
        // 26.1.2-port: was "if (context.entity() instanceof Player)". Curios 15 asks without a wearer (see
        // BaseCurioItem.getDefaultCurioAttributeModifiers), so an unknown wearer has to count as a player here or
        // the extra slot is never given. The mod's Curios data gives curio slots to players only.
        if (context.entity() == null || context.entity() instanceof Player) {
            CuriosApi.addSlotModifier(attributes, "charm", IItemHelper.getLocation(this), 1.0, AttributeModifier.Operation.ADD_VALUE);
        }
        return attributes;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        // 26.1.2-port: the AnvilRepairEvent handler that was here (the anvil never breaks) is now
        // MixinAnvilMenu.modifyBreakChance(); NeoForge removed the event.

        private static boolean isToolOrArmor(ItemStack stack) {
            return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)
                    || stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR) || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR);
        }

        @SubscribeEvent
        private static void onAnvilUse(@NotNull AnvilUpdateEvent event) {
            Player player = event.getPlayer();
            if (EnigmaticHandler.hasCurio(player, EnigmaticItems.FORGER_CRYSTAL)) {
                ItemStack left = event.getLeft();
                ItemStack right = event.getRight();
                if (!left.isEmpty() && !right.isEmpty()) {
                    boolean check = left.isDamageableItem() && !left.has(DataComponents.UNBREAKABLE) && !right.has(DataComponents.UNBREAKABLE) && left.isCombineRepairable(); // 26.1.2-port: isRepairable -> isCombineRepairable
                    if (check && unbreakableRestriction.get())
                        // 26.1.2-port: TieredItem/ArmorItem were removed; tools, swords and armor are identified by tag.
                        check = isToolOrArmor(left);
                    if (player.getAbilities().instabuild && left.isDamageableItem()) check = true;
                    if (left.is(right.getItem()) && check && left.getDamageValue() == 0 && right.getDamageValue() == 0 && !left.isEnchanted() && !right.isEnchanted()) {
                        ItemStack copy = left.copy();
                        copy.set(DataComponents.UNBREAKABLE, Unit.INSTANCE); // 26.1.2-port: the unbreakable component is a plain Unit now
                        copy.set(DataComponents.REPAIR_COST, copy.getOrDefault(DataComponents.REPAIR_COST, 0) + 8);
                        if (event.getName() != null && !event.getName().isEmpty())
                            copy.set(DataComponents.CUSTOM_NAME, Component.literal(event.getName()));
                        event.setOutput(copy);
                        event.setXpCost(50);
                    }
                }
            }
        }
    }
}
