package auviotre.enigmatic.legacy.mixin;

import auviotre.enigmatic.legacy.compat.CompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 26.1.2-port: new, a workaround for a bug in Curios 15.0.0+26.1.2 (reported in-game: every curio was back in the
 * inventory after rejoining the world).
 *
 * When a player joins, CurioInventory.loadInventoryConfiguration() builds fresh slot handlers from the slot data and
 * copies the saved stacks into them. A fresh handler starts at the slot type's base size and only grows to its real
 * size in update(), which does nothing until setDataLoaded() has been called - and that happens after the stacks
 * have been copied. So while copying, a slot type has only its base size: the spellstone and scroll slots (base 0,
 * opened by a permanent modifier) do not exist yet, nor does the ring slot from the Enchanted Celestial Fruit or the
 * charm slots from other curios, and whatever was in them is treated as invalid and given back to the player.
 * Curios adds a "size_shift" modifier at that point to keep the saved size, but it cannot take effect either.
 *
 * copyModifiers() is called once per slot type right before the stacks are copied, and only from there. Marking the
 * handler as loaded at its start lets the size update run, so the slots exist when the stacks arrive. The handler is
 * one tick oversized by its permanent modifiers (base + modifiers + size_shift); Curios removes size_shift at the
 * end of every tick, which brings it to the right size.
 *
 * The target class is not in the Curios API jar this mod compiles against, hence the name as a string and the
 * reflective call in CompatHandler. It only acts for the Curios version known to have the bug (see
 * CompatHandler.curiosLoginSlotsBroken). Remove it once Curios is fixed.
 */
@Mixin(targets = "top.theillusivec4.curios.common.inventory.CurioStacksHandler", remap = false)
public class MixinCurioStacksHandler {
    @Inject(method = "copyModifiers", at = @At("HEAD"))
    private void enigmaticlegacy$sizeSlotsBeforeStacksAreCopied(ICurioStacksHandler other, CallbackInfo info) {
        if (CompatHandler.curiosLoginSlotsBroken()) CompatHandler.markCurioSlotsLoaded(this);
    }
}
