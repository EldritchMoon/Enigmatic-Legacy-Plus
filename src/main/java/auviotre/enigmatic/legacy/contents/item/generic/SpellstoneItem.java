package auviotre.enigmatic.legacy.contents.item.generic;

import auviotre.enigmatic.legacy.api.item.ISpellstone;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.function.Consumer;

public abstract class SpellstoneItem extends BaseCurioItem implements ISpellstone {
    private final int tunerColor;

    public SpellstoneItem(Properties properties, int color) {
        super(properties.fireResistant());
        this.tunerColor = color;
    }

    public int getColor() {
        return this.tunerColor;
    }

    @OnlyIn(Dist.CLIENT)
    public void addKeyText(Consumer<Component> list) {
        try {
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.spellstoneAbility").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract public void addTuneTooltip(Consumer<Component> list);

    // 26.1.2-port: removed isEnchantable() -> false; items without an enchantable component can't be enchanted.

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return super.canEquip(context, stack) && ISpellstone.get(context.entity()).isEmpty();
    }
}
