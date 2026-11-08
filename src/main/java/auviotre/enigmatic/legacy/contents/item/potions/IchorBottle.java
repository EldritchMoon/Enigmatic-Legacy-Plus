package auviotre.enigmatic.legacy.contents.item.potions;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseDrinkableItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticEffects;
import net.minecraft.network.chat.Component;
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

public class IchorBottle extends BaseDrinkableItem {
    // 26.1.2-port: was FoodProperties with effects (applied by the removed Player.eat).
    public static final List<DrinkEffect> EFFECTS = List.of(
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 1200, 4), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 2), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3200), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(EnigmaticEffects.PURE_RESISTANCE, 1200, 2), 1.0F)
    );
    private final boolean enchanted;

    public IchorBottle(boolean enchanted) {
        super(IItemHelper.singleProperties().craftRemainder(Items.GLASS_BOTTLE).rarity(enchanted ? Rarity.EPIC : Rarity.RARE)
                .component(EnigmaticComponents.CURSED, enchanted).component(EnigmaticComponents.BLESSED, enchanted));
        this.enchanted = enchanted;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        if (this.enchanted) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.ichorBottle1");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.ichorBottle2");
            TooltipHandler.line(list);
            TooltipHandler.cursedOnly(list, stack);
        }
    }

    @Override
    public void onConsumed(Level level, Player player, ItemStack stack) {
        if (this.enchanted) {
            EnigmaticHandler.unlockSpecialSlot("charm", player, IItemHelper.getLocation(this));
        }
        applyDrinkEffects(level, player, EFFECTS);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.enchanted;
    }
}
