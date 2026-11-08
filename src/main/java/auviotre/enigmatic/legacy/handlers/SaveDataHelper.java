package auviotre.enigmatic.legacy.handlers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;

/**
 * 26.1.2-port: saved data (entities, attachments) is written through ValueOutput / read through ValueInput now
 * instead of CompoundTag. These two helpers bridge to a CompoundTag where EL+ still needs one
 * (sync packets, persistent data), so each class only has one serialize/deserialize implementation.
 */
public final class SaveDataHelper {
    private SaveDataHelper() {}

    public static CompoundTag toTag(HolderLookup.Provider provider, Consumer<ValueOutput> writer) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        writer.accept(output);
        return output.buildResult();
    }

    public static void fromTag(HolderLookup.Provider provider, CompoundTag tag, Consumer<ValueInput> reader) {
        reader.accept(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }

    /** Replaces ItemStack.save / saveOptional (removed): empty stacks become an empty compound, as before. */
    public static Tag saveItem(HolderLookup.Provider provider, ItemStack stack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    /** Replaces ItemStack.parseOptional (removed): missing or broken data gives an empty stack, as before. */
    public static ItemStack parseItem(HolderLookup.Provider provider, Tag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).resultOrPartial().orElse(ItemStack.EMPTY);
    }
}
