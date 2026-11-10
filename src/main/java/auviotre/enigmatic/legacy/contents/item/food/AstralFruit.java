package auviotre.enigmatic.legacy.contents.item.food;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class AstralFruit extends BaseItem {
    public static final FoodProperties DEFAULT_PROPERTIES = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(1.3F).alwaysEdible().build();
    public static final FoodProperties ENCHANTED_PROPERTIES = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(1.3F).alwaysEdible().build();
    // 26.1.2-port: FoodProperties no longer carries effects; they are consume effects on the Consumable component
    public static final Consumable DEFAULT_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffects.REGENERATION, 400, 2),
                    new MobEffectInstance(MobEffects.STRENGTH, 3200, 2),
                    new MobEffectInstance(MobEffects.RESISTANCE, 3200, 1),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800)
            ))).build();
    public static final Consumable ENCHANTED_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffects.REGENERATION, 1200, 2),
                    new MobEffectInstance(MobEffects.STRENGTH, 3200, 3),
                    new MobEffectInstance(MobEffects.RESISTANCE, 3200, 2),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800)
            ))).build();
    private final boolean enchanted;

    public AstralFruit(boolean enchanted) {
        super(IItemHelper.properties().rarity(enchanted ? Rarity.EPIC : Rarity.RARE).fireResistant().component(EnigmaticComponents.CURSED, enchanted)
                .food(enchanted ? ENCHANTED_PROPERTIES : DEFAULT_PROPERTIES, enchanted ? ENCHANTED_CONSUMABLE : DEFAULT_CONSUMABLE));
        this.enchanted = enchanted;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        if (this.enchanted) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.astralFruit");
            TooltipHandler.line(list);
            TooltipHandler.cursedOnly(list, stack);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && this.enchanted) {
            EnigmaticHandler.unlockSpecialSlot("ring", player, IItemHelper.getLocation(this));
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.enchanted;
    }
}
