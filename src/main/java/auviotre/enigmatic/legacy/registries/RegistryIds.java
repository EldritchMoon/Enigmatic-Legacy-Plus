package auviotre.enigmatic.legacy.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * 26.1.2-port: since Minecraft 1.21.2, Item.Properties and BlockBehaviour.Properties must be
 * given their registry ID (setId) before the item or block is constructed, or the game crashes
 * with "Item id not set" / "Block id not set".
 * <p>
 * Enigmatic Legacy+ builds its Properties inside each class's constructor (IItemHelper.properties(),
 * Properties.ofFullCopy(...)), so the registration helpers in EnigmaticItems / EnigmaticBlocks
 * remember which ID is being built, and the property factories read it from here.
 * This keeps the ~160 item and block constructors unchanged.
 */
public final class RegistryIds {
    private static final ThreadLocal<ResourceKey<Item>> ITEM = new ThreadLocal<>();
    private static final ThreadLocal<ResourceKey<Block>> BLOCK = new ThreadLocal<>();

    private RegistryIds() {}

    /** Runs the factory while {@link #currentItem()} returns the key for this ID. */
    static <T> T buildItem(Identifier id, Supplier<T> factory) {
        ResourceKey<Item> previous = ITEM.get();
        ITEM.set(ResourceKey.create(Registries.ITEM, id));
        try {
            return factory.get();
        } finally {
            ITEM.set(previous);
        }
    }

    /** Runs the factory while {@link #currentBlock()} returns the key for this ID. */
    static <T> T buildBlock(Identifier id, Supplier<T> factory) {
        ResourceKey<Block> previous = BLOCK.get();
        BLOCK.set(ResourceKey.create(Registries.BLOCK, id));
        try {
            return factory.get();
        } finally {
            BLOCK.set(previous);
        }
    }

    /** The item being registered right now. Only valid while an item registration helper is running. */
    public static ResourceKey<Item> currentItem() {
        ResourceKey<Item> key = ITEM.get();
        if (key == null)
            throw new IllegalStateException("Item.Properties created outside item registration; use EnigmaticItems' register helpers");
        return key;
    }

    /** The block being registered right now. Only valid while a block registration helper is running. */
    public static ResourceKey<Block> currentBlock() {
        ResourceKey<Block> key = BLOCK.get();
        if (key == null)
            throw new IllegalStateException("Block properties created outside block registration; use EnigmaticBlocks' register helpers");
        return key;
    }
}
