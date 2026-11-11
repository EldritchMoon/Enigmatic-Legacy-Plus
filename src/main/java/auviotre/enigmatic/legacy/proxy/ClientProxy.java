package auviotre.enigmatic.legacy.proxy;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.screen.PermanentDeathScreen;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClientProxy extends CommonProxy {
    @Override
    public void clientInit() {
        // 26.1.2-port: the 26 ItemProperties.register(...) calls that were here are gone with ItemProperties. What each
        // one computed is now a registered item model property in ELItemModelProperties (or a vanilla one; the list is
        // in that file), registered from ClientSetupHandler.
        try {
            EnigmaticItems.SOUL_COMPASS.get().initNeedle();
        } catch (Exception exception) {
            EnigmaticLegacy.LOGGER.warn("Could not load item models.");
        }
    }

    @Override
    public boolean isOperatorItemsTabEnabled() {
        return Minecraft.getInstance().options.operatorItemsTab().get();
    }

    @Override
    public void displayPermanentDeathScreen() {
        if (Minecraft.getInstance().level != null) {
            boolean local = Minecraft.getInstance().isLocalServer();
            Minecraft.getInstance().level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE); // 26.1.2-port: disconnect takes the message

            if (local) {
                Minecraft.getInstance().clearClientLevel(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
            } else {
                Minecraft.getInstance().clearClientLevel(new ProgressScreen(true));
            }
        }

        PermanentDeathScreen screen = new PermanentDeathScreen(new SelectWorldScreen(new TitleScreen()), Component.translatable("gui.enigmaticlegacy.permanent_death_screen_title"), Component.translatable("message.enigmaticlegacy.permanent_death_screen"));
        PermanentDeathScreen.active = screen;
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public String getClientUsername() {
        return Minecraft.getInstance().getUser().getName();
    }

    @Override
    public @Nullable Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public @Nullable Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
