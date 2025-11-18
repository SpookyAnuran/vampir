package com.vampir;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import com.vampir.command.VampirCommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vampir implements ModInitializer {
    public static final String MOD_ID = "vampir";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Vampir loaded.");

        // Register custom commands
        VampirCommands.register();

        // Apply 'human' tag when player joins or respawns
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                applyHumanTag(player);
            }
        });
    }

    private void applyHumanTag(ServerPlayerEntity player) {
        if (!player.getCommandTags().contains("vampir:human") &&
                !player.getCommandTags().contains("vampir:vampire")) {
            player.addCommandTag("vampir:human");
            LOGGER.info("Applied 'human' tag to player: {}", player.getName().getString());
        }
    }
}
