package com.vampir;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashMap;
import java.util.UUID;

public class VampirSunlight {
    // Track exposure time and pulse counters per player
    private static final HashMap<UUID, Integer> exposureTicks = new HashMap<>();
    private static final HashMap<UUID, Integer> pulseTicks = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getCommandTags().contains("vampir:vampire")) {
                    boolean isDay = player.getWorld().isDay();
                    boolean canSeeSky = player.getWorld().isSkyVisible(player.getBlockPos());

                    if (isDay && canSeeSky && !player.isSubmergedInWater()) {
                        // Increase exposure counter
                        int exposure = exposureTicks.getOrDefault(player.getUuid(), 0) + 1;
                        exposureTicks.put(player.getUuid(), exposure);

                        // Only start damage after 5 seconds (100 ticks)
                        if (exposure >= 100) {
                            // Increment pulse counter
                            int pulse = pulseTicks.getOrDefault(player.getUuid(), 0) + 1;
                            pulseTicks.put(player.getUuid(), pulse);

                            // Apply damage every 40 ticks
                            if (pulse >= 40) {
                                player.addStatusEffect(new StatusEffectInstance(
                                        StatusEffects.WITHER,
                                        40, // lasts 2 seconds
                                        0,  // level I
                                        true,
                                        false
                                ));
                                pulseTicks.put(player.getUuid(), 0); // reset pulse
                            }
                        }
                    } else {
                        // Reset if safe
                        exposureTicks.remove(player.getUuid());
                        pulseTicks.remove(player.getUuid());
                    }
                } else {
                    // Not a vampire, clear counters
                    exposureTicks.remove(player.getUuid());
                    pulseTicks.remove(player.getUuid());
                }
            }
        });
    }
}
