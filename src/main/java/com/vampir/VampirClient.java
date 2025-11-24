package com.vampir;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client initializer for Vampir.
 * Registers keybindings and listens for key presses.
 */
public class VampirClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the keybinding
        VampirKeys.register();

        // Listen for key presses each client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (VampirKeys.TOGGLE_NIGHT_VISION.wasPressed()) {
                // Flip the toggle flag in VampirStaticBoon
                VampirStaticBoon.nightVisionEnabled = !VampirStaticBoon.nightVisionEnabled;
            }
        });
    }
}
