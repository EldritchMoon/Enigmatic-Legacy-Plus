package auviotre.enigmatic.legacy.client;

import net.minecraft.client.Minecraft;

/**
 * 26.1.2-port: small client-side helpers, so Minecraft API changes only need fixing here.
 * Only call these from client code (tooltips, screens, renderers).
 */
public final class ClientHelper {
    private ClientHelper() {}

    /** Replaces Screen.hasShiftDown(), which no longer exists in 26.1. */
    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
