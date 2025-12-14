package com.vampir;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VampirBloodCollection {
    // Track hit counts per player
    private static final Map<UUID, Integer> HIT_COUNTER = new HashMap<>();

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity living)) return ActionResult.PASS;

            // Skip undead/monsters
            if (living instanceof net.minecraft.entity.mob.Monster) {
                return ActionResult.PASS;
            }

            ItemStack offhand = player.getOffHandStack();
            if (offhand.isOf(Items.GLASS_BOTTLE)) {
                UUID id = player.getUuid();
                int hits = HIT_COUNTER.getOrDefault(id, 0) + 1;
                HIT_COUNTER.put(id, hits);

                if (hits >= 3) {
                    offhand.decrement(1);
                    player.getInventory().insertStack(new ItemStack(Vampir.BLOOD_BOTTLE));
                    HIT_COUNTER.put(id, 0);

                    if (!world.isClient) {
                        serverPlayer.sendMessage(Text.literal("You filled a blood bottle..."), true);
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
