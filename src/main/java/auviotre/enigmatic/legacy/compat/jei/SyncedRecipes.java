package auviotre.enigmatic.legacy.compat.jei;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.compat.CompatHandler;
import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.List;

/**
 * 26.1.2-port: new file. Recipes are no longer sent to clients, so the client has no RecipeManager to ask (the JEI
 * plugin used Minecraft.getInstance().level.getRecipeManager()). NeoForge's replacement: a mod on the server asks for
 * the recipe types it wants sent (OnDatapackSyncEvent.sendRecipes), and the client gets them in RecipesReceivedEvent.
 * JEI does this for the vanilla types, which covers the two crafting recipes of this mod; this class does it for
 * Spellstone Table recipes. It has no JEI imports, so it loads whether or not JEI is installed.
 */
public final class SyncedRecipes {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    private SyncedRecipes() {
    }

    /** The Spellstone Table recipes the server sent; empty when it sent none. */
    public static List<RecipeHolder<SpellstoneTableRecipe>> spellstoneCrafting() {
        return List.copyOf(recipes.byType(EnigmaticRecipes.SPELLSTONE_CRAFTING.get()));
    }

    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Server {
        // Only when JEI is on the server too. JEI's client treats any non-empty recipe packet as the complete synced
        // set, so sending just this one type from a server without JEI would leave JEI with no vanilla recipes where
        // it would otherwise fall back to its built-in copy of them.
        @SubscribeEvent
        private static void onDatapackSync(OnDatapackSyncEvent event) {
            if (CompatHandler.isLoaded("jei")) event.sendRecipes(EnigmaticRecipes.SPELLSTONE_CRAFTING.get());
        }
    }

    @EventBusSubscriber(modid = EnigmaticLegacy.MODID, value = Dist.CLIENT)
    public static class Client {
        // Default priority. JEI listens for this event at LOWEST and starts loading plugins from there, so the map is
        // already stored when JEIHandler.registerRecipes() runs.
        @SubscribeEvent
        private static void onRecipesReceived(RecipesReceivedEvent event) {
            recipes = event.getRecipeMap();
        }

        @SubscribeEvent
        private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            recipes = RecipeMap.EMPTY;
        }
    }
}
