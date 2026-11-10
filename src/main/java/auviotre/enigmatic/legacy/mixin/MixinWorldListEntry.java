package auviotre.enigmatic.legacy.mixin;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class MixinWorldListEntry {

    @Shadow
    @Final
    LevelSummary summary;

    // 26.1.2-port: the entry no longer draws its text every frame in render(); its constructor builds a text widget
    // from summary.getInfo() once. So the redirect moved to the constructor, and the cache it needed for the per-frame
    // call is gone. The handler is static because it runs while the entry is still being constructed.
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;getInfo()Lnet/minecraft/network/chat/Component;"))
    private static Component getAltInfo(LevelSummary summary) {
        return EnigmaticHandler.getModdedInfo(summary);
    }

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void onJoinWorld(CallbackInfo info) {
        if (EnigmaticHandler.isWorldFractured(EnigmaticHandler.getSaveFolder(this.summary))) {
            EnigmaticLegacy.PROXY.displayPermanentDeathScreen();
            info.cancel();
        }
    }
}
