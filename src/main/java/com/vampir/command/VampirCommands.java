package com.vampir.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class VampirCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerVampirCommand(dispatcher);
        });
    }

    private static void registerVampirCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("vampir")
                .then(literal("addvampire")
                        .then(argument("target", EntityArgumentType.player())
                                .executes(context -> {
                                    if (!context.getSource().hasPermissionLevel(2)) {
                                        context.getSource().sendError(Text.literal("You must be an operator to use this command."));
                                        return 0;
                                    }

                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
                                    player.removeCommandTag("vampir:human");
                                    player.addCommandTag("vampir:vampire");
                                    context.getSource().sendFeedback(() -> Text.literal("Converted " + player.getName().getString() + " to vampire."), false);
                                    return 1;
                                })
                        )
                )
                .then(literal("curevampire")
                        .then(argument("target", EntityArgumentType.player())
                                .executes(context -> {
                                    if (!context.getSource().hasPermissionLevel(2)) {
                                        context.getSource().sendError(Text.literal("You must be an operator to use this command."));
                                        return 0;
                                    }

                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
                                    player.removeCommandTag("vampir:vampire");
                                    player.addCommandTag("vampir:human");
                                    context.getSource().sendFeedback(() -> Text.literal("Cured " + player.getName().getString() + " of vampirism."), false);
                                    return 1;
                                })
                        )
                )
                .then(literal("status")
                        .then(argument("target", EntityArgumentType.player())
                                .executes(context -> {
                                    if (!context.getSource().hasPermissionLevel(2)) {
                                        context.getSource().sendError(Text.literal("You must be an operator to use this command."));
                                        return 0;
                                    }

                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
                                    String status;
                                    if (player.getCommandTags().contains("vampir:vampire")) {
                                        status = "Vampire";
                                    } else if (player.getCommandTags().contains("vampir:human")) {
                                        status = "Human";
                                    } else {
                                        status = "Unknown (no vampirism tags)";
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal(player.getName().getString() + " is currently: " + status), false);
                                    return 1;
                                })
                        )
                )
        );
    }
}
