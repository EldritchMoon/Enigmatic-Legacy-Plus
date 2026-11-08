package auviotre.enigmatic.legacy.contents.attachement;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

public class AntiqueBagInventory implements ValueIOSerializable {
    public static final int SLOTS_COUNT = 12;
    ItemStackHandler handler = new ItemStackHandler(SLOTS_COUNT);

    public ItemStackHandler getStackHandler() {
        return this.handler;
    }

    public void init() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // 26.1.2-port: ValueIOSerializable replaces INBTSerializable (ItemStackHandler implements it too).
    @Override
    public void serialize(ValueOutput output) {
        this.handler.serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.handler.deserialize(input);
    }
}
