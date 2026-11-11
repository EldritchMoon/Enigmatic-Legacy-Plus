package auviotre.enigmatic.legacy.contents.item.generic;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class BaseCurioItem extends BaseItem implements ICurioItem {

    public BaseCurioItem() {
        super(IItemHelper.singleProperties());
    }

    public BaseCurioItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return !EnigmaticHandler.hasCurio(context.entity(), this) && ICurioItem.super.canEquip(context, stack);
    }

    // 26.1.2-port: Curios 15 takes a curio's attribute modifiers from this method and no longer reaches
    // getAttributeModifiers(SlotContext, Identifier, ItemStack) for an item: the interface's default body for this
    // method asks Curios' own blank curio, not the item. The 30 curios that override the old method keep it, and this
    // hands its result over.
    // Curios works the modifiers out for the item alone now, so there is no wearer, slot or index to pass: entity()
    // is null, the identifier is empty and the index is -1. The entries are not tied to a slot type; Curios applies
    // them in whichever slot the curio sits in and appends "/<slot>/<index>" to each modifier id.
    @Override
    public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack) {
        CurioAttributeModifiers.Builder builder = CurioAttributeModifiers.builder();
        SlotContext context = new SlotContext("", null, -1, false, true);
        this.getAttributeModifiers(context, IItemHelper.getLocation(this), stack)
                .forEach((attribute, modifier) -> builder.addModifier(attribute, modifier));
        return builder.build();
    }
}
