package auviotre.enigmatic.legacy.contents.item.potions;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseDrinkableItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class IchorCurseBottle extends BaseDrinkableItem {
    // 26.1.2-port: was FoodProperties with effects (applied by the removed Player.eat).
    public static final List<DrinkEffect> EFFECTS = List.of(
            new DrinkEffect(() -> new MobEffectInstance(EnigmaticEffects.ICHOR_CURSE, 9600 * 4), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(EnigmaticEffects.ICHOR_CORROSION, 1200), 0.8F),
            new DrinkEffect(() -> new MobEffectInstance(EnigmaticEffects.ICHOR_CORROSION, 1200), 0.4F),
            new DrinkEffect(() -> new MobEffectInstance(EnigmaticEffects.ICHOR_CORROSION, 1200), 0.2F)
    );

    public IchorCurseBottle() {
        super(IItemHelper.singleProperties().craftRemainder(Items.GLASS_BOTTLE).rarity(Rarity.RARE)
                .component(EnigmaticComponents.CURSED, true)
                .component(EnigmaticComponents.BLESSED, true)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        MutableComponent component = Component.translatable(EnigmaticEffects.ICHOR_CURSE.get().getDescriptionId());
        list.accept(Component.translatable("potion.withDuration", component, Component.literal(StringUtil.formatTickDuration(9600 * 4, context.tickRate()))).withStyle(ChatFormatting.RED));
        if (EnigmaticHandler.isTheBlessedOne(EnigmaticLegacy.PROXY.getClientPlayer())) {
            component = Component.translatable(MobEffects.ABSORPTION.value().getDescriptionId());
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency.4"));
            list.accept(Component.translatable("potion.withDuration", component, Component.literal(StringUtil.formatTickDuration(2400, context.tickRate()))).withStyle(ChatFormatting.BLUE));
        }
        TooltipHandler.line(list);
        TooltipHandler.cursedOnly(list, stack);
    }

    @Override
    public void onConsumed(Level level, Player player, ItemStack stack) {
        applyDrinkEffects(level, player, EFFECTS);
        if (EnigmaticHandler.isTheBlessedOne(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
