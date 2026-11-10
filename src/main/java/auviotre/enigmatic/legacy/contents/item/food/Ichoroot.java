package auviotre.enigmatic.legacy.contents.item.food;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseItem;
import auviotre.enigmatic.legacy.registries.EnigmaticEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;

import java.util.List;

public class Ichoroot extends BaseItem {
    public static final FoodProperties PROPERTIES = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.9F).alwaysEdible().build();
    // 26.1.2-port: FoodProperties no longer carries effects; they are consume effects on the Consumable component
    public static final Consumable CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(EnigmaticEffects.PURE_RESISTANCE, 360), 0.25F))
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.ABSORPTION, 360), 0.5F))
            .build();

    public Ichoroot() {
        super(IItemHelper.properties().food(PROPERTIES, CONSUMABLE));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        List<MobEffectInstance> list = entity.getActiveEffects().stream().filter(effect -> effect.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL)).toList();
        if (!list.isEmpty()) entity.removeEffect(list.get(entity.getRandom().nextInt(list.size())).getEffect());
        return super.finishUsingItem(stack, level, entity);
    }
}