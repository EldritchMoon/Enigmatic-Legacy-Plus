package auviotre.enigmatic.legacy.contents.capability;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.attachement.AntiqueBagInventory;
import auviotre.enigmatic.legacy.registries.EnigmaticAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import auviotre.enigmatic.legacy.handlers.SaveDataHelper;

public class AntiqueBagCapability implements IAntiqueBagHandler {
    public static final Identifier ID = EnigmaticLegacy.location("antique_bag_inventory");
    AntiqueBagInventory inventory;
    LivingEntity owner;

    public AntiqueBagCapability(final LivingEntity entity) {
        this.owner = entity;
        this.inventory = entity.getData(EnigmaticAttachments.ANTIQUE_BAG_INVENTORY);
    }

    @Override
    public void reset() {
        this.inventory.init();
    }

    @Override
    public int getSlots() {
        return AntiqueBagInventory.SLOTS_COUNT;
    }

    @Override
    public ItemStack getBook(int index) {
        return this.inventory.getStackHandler().getStackInSlot(index);
    }

    @Override
    public void setBook(int index, ItemStack book) {
        this.inventory.getStackHandler().setStackInSlot(index, book);
    }

    @Override
    public ItemStack findBook(Item book) {
        IItemHandlerModifiable handler = this.getInventory();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.is(book)) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public IItemHandlerModifiable getInventory() {
        return this.inventory.getStackHandler();
    }

    @Override
    public Tag writeTag() {
        return SaveDataHelper.toTag(this.owner.registryAccess(), this.inventory::serialize); // 26.1.2-port
    }

    @Override
    public void readTag(Tag nbt) {
        SaveDataHelper.fromTag(this.owner.registryAccess(), (CompoundTag) nbt, this.inventory::deserialize); // 26.1.2-port
    }
}
