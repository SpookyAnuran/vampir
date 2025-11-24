package com.vampir;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Grants permanent boons to players tagged as vampires:
 * - Permanent Water Breathing
 * - Immunity to Hunger
 * - Toggleable Night Vision (controlled by VampirClient keybind)
 */
public final class VampirStaticBoon {

    public static final String VAMPIRE_TAG = "vampir:vampire";

    // Flag flipped by VampirClient when the keybind is pressed
    public static boolean nightVisionEnabled = false;

    private VampirStaticBoon() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!isVampire(player)) continue;

                // 1) Permanent Water Breathing
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WATER_BREATHING, 220, 0, true, false, true));

                // 2) Hunger immunity
                if (player.hasStatusEffect(StatusEffects.HUNGER)) {
                    player.removeStatusEffect(StatusEffects.HUNGER);
                }

                // 3) Toggleable Night Vision
                if (nightVisionEnabled) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION, 220, 0, true, false, true));
                } else {
                    player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
            }
        });
    }

    private static boolean isVampire(ServerPlayerEntity player) {
        return player.getCommandTags().contains(VAMPIRE_TAG);
    }
}
