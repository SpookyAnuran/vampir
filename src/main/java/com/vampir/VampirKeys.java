package com.vampir;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Defines the custom keybindings for the Vampir mod.
 * Only registers the Toggle Night Vision key.
 */
public final class VampirKeys {
    public static KeyBinding TOGGLE_NIGHT_VISION;

    private VampirKeys() {}

    public static void register() {
        TOGGLE_NIGHT_VISION = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.vampir.toggle_night_vision", // translation key for lang file
                        InputUtil.Type.KEYSYM,            // key type
                        GLFW.GLFW_KEY_N,                  // default key: N
                        "category.vampir"                 // category in Controls menu
                )
        );
    }
}
