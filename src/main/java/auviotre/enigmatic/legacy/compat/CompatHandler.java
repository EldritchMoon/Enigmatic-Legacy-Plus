package auviotre.enigmatic.legacy.compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CompatHandler {
    private static final CompatHandler INSTANCE = new CompatHandler();

    public static CompatHandler getInstance() {
        return INSTANCE;
    }

    public static boolean isLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    public void register(IEventBus modEventBus) {
        // TODO(port): the AppleSkin and Thirst handlers are excluded from the build until those mods are on 26.1.2
        // (phase 9, see build.gradle); restore these registrations and the two imports with them.
        // if (isLoaded("appleskin") && FMLEnvironment.getDist().isClient()) {
        //     EnigmaticLegacy.LOGGER.info("Appleskin Compat.");
        //     NeoForge.EVENT_BUS.register(new AppleSkinCompatHandler());
        // }
        // if (isLoaded("thirst")) {
        //     EnigmaticLegacy.LOGGER.info("Thirst Compat.");
        //     NeoForge.EVENT_BUS.register(new ThirstCompatHandler());
        // }
    }
}
