package com.vampir;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class VampirDiet {
    private static final Logger LOGGER = LoggerFactory.getLogger("Vampir");

    // Simple allowlist: items vampires are allowed to eat
    private static final Set<Identifier> VAMPIRE_FOOD_IDS = Set.of(
            Identifier.of("minecraft", "beef"),
            Identifier.of("minecraft", "porkchop"),
            Identifier.of("minecraft", "mutton"),
            Identifier.of("minecraft", "chicken"),
            Identifier.of("minecraft", "rabbit"),
            Identifier.of("minecraft", "cod"),
            Identifier.of("minecraft", "salmon"),
            Identifier.of("minecraft", "golden_apple"),
            Identifier.of("minecraft", "enchanted_golden_apple"),
            Identifier.of("minecraft", "mushroom_stew"),
            Identifier.of("vampir", "blood_bottle"),
            Identifier.of("minecraft", "suspicious_stew")
    );

    public static void register() {
        // ------------------------
        // Attack -> blood feeding
        // ------------------------
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && isVampire(serverPlayer)) {
                if (entity instanceof LivingEntity living) {
                    int hungerGain = getBloodValue(living);
                    if (hungerGain > 0) {
                        serverPlayer.getHungerManager().add(hungerGain, 0.5f);
                        if (!world.isClient) {
                            serverPlayer.sendMessage(Text.literal("You feed on blood..."), true);
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });

        // ------------------------
        // UseItem -> restrict allowed foods for vampires
        // ------------------------
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (!(player instanceof ServerPlayerEntity serverPlayer) || !isVampire(serverPlayer)) {
                return TypedActionResult.pass(stack); // non-vampires unaffected
            }

            Identifier id = Registries.ITEM.getId(stack.getItem());

            // Fast path: always allow blood bottle
            if (id.equals(Identifier.of("vampir", "blood_bottle"))) {
                return TypedActionResult.pass(stack);
            }

            // Only block if the item is edible AND not in the allowlist
            if (isEdible(stack) && !VAMPIRE_FOOD_IDS.contains(id)) {
                if (!world.isClient) {
                    serverPlayer.sendMessage(Text.literal("This tastes disgusting..."), true);
                }
                return TypedActionResult.fail(stack);
            }

            // Otherwise, let vanilla handle it
            return TypedActionResult.pass(stack);
        });



    }

    // ------------------------
    // Helpers
    // ------------------------
    private static boolean isVampire(ServerPlayerEntity player) {
        return player.getCommandTags().contains("vampir:vampire");
    }

    private static boolean isEdible(ItemStack stack) {
        try {
            return stack.getItem().getClass().getMethod("getFoodComponent") != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static int getBloodValue(LivingEntity entity) {
        EntityType<?> type = entity.getType();

        // Humanoids → 6 hunger
        if (type.toString().contains("villager") || type.toString().contains("witch")
                || type.toString().contains("pillager") || type.toString().contains("vindicator")
                || type.toString().contains("evoker") || type.toString().contains("illusioner")) {
            return 6;
        }

        // Skip undead/monsters
        if (entity instanceof Monster) return 0;

        // Livestock → 4 hunger
        if (type.toString().contains("cow") || type.toString().contains("pig") || type.toString().contains("sheep")
                || type.toString().contains("horse") || type.toString().contains("donkey")
                || type.toString().contains("mule") || type.toString().contains("goat")) {
            return 4;
        }

        // General feedable animals → 2 hunger
        if (type.toString().contains("chicken") || type.toString().contains("rabbit") || type.toString().contains("wolf")
                || type.toString().contains("parrot") || type.toString().contains("fox") || type.toString().contains("llama")
                || type.toString().contains("polar_bear") || type.toString().contains("panda")
                || type.toString().contains("dolphin")) {
            return 2;
        }

        return 0;
    }
}
