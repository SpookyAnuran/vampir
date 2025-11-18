package com.vampir;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

public class VampirDiet {
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
                    }
                }
            }
            return ActionResult.PASS;
        });

        // ------------------------
        // UseItem -> restrict allowed foods for vampires
        // Allowed: raw meats, golden apple, golden carrot, suspicious stew
        // All other foods are canceled for vampires
        // ------------------------
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();

            if (!(player instanceof ServerPlayerEntity serverPlayer) || !isVampire(serverPlayer)) {
                return TypedActionResult.pass(stack); // non-vampires unaffected
            }

            boolean allowedRawMeat =
                    item == Items.BEEF || item == Items.PORKCHOP || item == Items.MUTTON ||
                            item == Items.CHICKEN || item == Items.RABBIT;

            if (allowedRawMeat || item == Items.GOLDEN_APPLE || item == Items.GOLDEN_CARROT || item == Items.SUSPICIOUS_STEW) {
                return TypedActionResult.pass(stack);
            }

            return TypedActionResult.fail(stack);
        });
    }

    // ------------------------
    // Helpers
    // ------------------------
    private static boolean isVampire(ServerPlayerEntity player) {
        return player.getCommandTags().contains("vampir:vampire");
    }

    private static int getBloodValue(LivingEntity entity) {
        // Illagers explicitly allowed as valuable targets
        if (entity instanceof PillagerEntity || entity instanceof VindicatorEntity ||
                entity instanceof EvokerEntity || entity instanceof IllusionerEntity) return 6; // 3 bars

        if (entity instanceof ChickenEntity || entity instanceof ParrotEntity) return 1; // half bar
        if (entity instanceof SheepEntity || entity instanceof HorseEntity || entity instanceof CowEntity ||
                entity instanceof PolarBearEntity || entity instanceof PandaEntity || entity instanceof PigEntity) return 2; // full bar

        String type = entity.getType().toString();
        if (type.contains("villager") || type.contains("witch")) return 8; // 4 bars

        if (entity instanceof Monster) return 0; // exclude other hostile/undead

        return 0;
    }
}

