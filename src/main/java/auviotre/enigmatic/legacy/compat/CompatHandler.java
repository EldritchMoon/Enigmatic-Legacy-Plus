package auviotre.enigmatic.legacy.compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import auviotre.enigmatic.legacy.EnigmaticLegacy;

public class CompatHandler {
    private static final CompatHandler INSTANCE = new CompatHandler();

    public static CompatHandler getInstance() {
        return INSTANCE;
    }

    public static boolean isLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    // 26.1.2-port: new, used by MixinApplyBonusCount (which explains the Curios bug). The check is by exact version on
    // purpose: with any other Curios the workaround stays off, and the log line says which way it went.
    private static final String CURIOS_VERSION_WITHOUT_FORTUNE = "15.0.0+26.1.2";
    private static Boolean curiosFortuneBroken;

    public static boolean curiosFortuneBroken() {
        if (curiosFortuneBroken == null) {
            String version = ModList.get().getModContainerById("curios").map(container -> container.getModInfo().getVersion().toString()).orElse("");
            curiosFortuneBroken = CURIOS_VERSION_WITHOUT_FORTUNE.equals(version);
            if (curiosFortuneBroken) {
                EnigmaticLegacy.LOGGER.info("Curios {} does not apply Fortune bonuses from curios; Enigmatic Legacy+ is applying them instead.", version);
            } else {
                EnigmaticLegacy.LOGGER.info("Curios {} is not the version with the known Fortune bug, so the Enigmatic Legacy+ workaround is off. If curios give no Fortune, see MixinApplyBonusCount.", version);
            }
        }
        return curiosFortuneBroken;
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
