package auviotre.enigmatic.legacy.contents.item.potions;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.compat.CompatHandler;
import auviotre.enigmatic.legacy.contents.attachement.EnigmaticData;
import auviotre.enigmatic.legacy.contents.item.generic.BaseDrinkableItem;
import auviotre.enigmatic.legacy.registries.EnigmaticAttachments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class ForbiddenJuice extends BaseDrinkableItem {
    // 26.1.2-port: was FoodProperties with effects (applied by the removed Player.eat).
    public static final List<DrinkEffect> EFFECTS = List.of(
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.WITHER, 400, 3), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.NAUSEA, 400, 2), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 480, 3), 1.0F),
            new DrinkEffect(() -> new MobEffectInstance(MobEffects.SLOWNESS, 640, 2), 1.0F)
    );

    public ForbiddenJuice() {
        super(IItemHelper.singleProperties().craftRemainder(Items.GLASS_BOTTLE).rarity(Rarity.RARE).fireResistant());
    }

    public static boolean isForbiddenCursed(LivingEntity entity) {
        return entity != null && entity.getData(EnigmaticAttachments.ENIGMATIC_DATA).isThirstForbiddenCursed();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return CompatHandler.isLoaded("thirst");
    }

    @Override
    public boolean canDrink(Level level, Player player, ItemStack stack) {
        return !isForbiddenCursed(player);
    }

    @Override
    public void onConsumed(Level level, @NotNull Player player, ItemStack stack) {
        applyDrinkEffects(level, player, EFFECTS);
        EnigmaticData data = player.getData(EnigmaticAttachments.ENIGMATIC_DATA);
        data.setThirstForbiddenCursed(true);
    }
}
