package com.vampir;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class VampirInfection {
    private static final String TAG_VAMPIRE = "vampir:vampire";

    private static final long BITE_COOLDOWN_MS = TimeUnit.SECONDS.toMillis(3);
    private static final double BITE_INFECTION_CHANCE = 0.5;
    private static final int EFFECT_DURATION = 20 * 180;

    private static final Map<UUID, Long> lastBite = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> wasSleeping = new ConcurrentHashMap<>();

    private VampirInfection() {}

    public static void register() {
        // Ensure ModEffects.register() was called from your mod initializer before this.

        AttackEntityCallback.EVENT.register((attackerEntity, world, hand, target, hitResult) -> {
            if (!(attackerEntity instanceof ServerPlayerEntity attacker)) return ActionResult.PASS;
            if (!(world instanceof ServerWorld)) return ActionResult.PASS;

            Entity t = target;
            if (!(t instanceof ServerPlayerEntity victim)) return ActionResult.PASS;
            if (attacker.getUuid().equals(victim.getUuid())) return ActionResult.PASS;
            if (!attacker.getCommandTags().contains(TAG_VAMPIRE)) return ActionResult.PASS;
            if (victim.getCommandTags().contains(TAG_VAMPIRE)) return ActionResult.PASS;

            boolean attackerSprinting = attacker.isSprinting();
            boolean emptyHands = attacker.getMainHandStack().isEmpty() && attacker.getOffHandStack().isEmpty();
            if (!attackerSprinting || !emptyHands) return ActionResult.PASS;

            long now = System.currentTimeMillis();
            Long last = lastBite.get(attacker.getUuid());
            if (last != null && now - last < BITE_COOLDOWN_MS) return ActionResult.PASS;
            lastBite.put(attacker.getUuid(), now);

            // immediate launch feedback (always play/spawn to mask who is infected)
            BlockPos hitPos = t.getBlockPos();
            ServerWorld sw = (ServerWorld) t.getWorld();
            sw.playSound(null, hitPos, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
            sw.playSound(null, hitPos, SoundEvents.ITEM_AXE_STRIP, net.minecraft.sound.SoundCategory.PLAYERS, 0.9f, 1.0f);
            sw.spawnParticles(ParticleTypes.SOUL, hitPos.getX() + 0.5, hitPos.getY() + 1.0, hitPos.getZ() + 0.5, 12, 0.25, 0.25, 0.25, 0.02);

            double roll = ThreadLocalRandom.current().nextDouble();
            if (roll < BITE_INFECTION_CHANCE) {
                RegistryEntry.Reference<StatusEffect> diseaseEntry = ModEffects.VAMPIR_DISEASE_ENTRY;
                if (diseaseEntry != null) {
                    // construct StatusEffectInstance using the registry entry as your mappings expect
                    victim.addStatusEffect(new StatusEffectInstance(diseaseEntry, 20 * 60 * 5, 0, false, true, true));

                    BlockPos pos = victim.getBlockPos();
                    sw = (ServerWorld) victim.getWorld();
                    sw.spawnParticles(ParticleTypes.DRIPPING_LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.01);
                    sw.playSound(null, pos, SoundEvents.ENTITY_GENERIC_HURT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.9f);
                }
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                for (ServerPlayerEntity p : world.getPlayers(player -> true)) {
                    UUID id = p.getUuid();

                    boolean isDead = p.isDead() || p.getHealth() <= 0.0F;
                    RegistryEntry.Reference<StatusEffect> diseaseEntry = ModEffects.VAMPIR_DISEASE_ENTRY;

                    if (isDead && diseaseEntry != null && p.hasStatusEffect(diseaseEntry) && !p.getCommandTags().contains(TAG_VAMPIRE)) {
                        p.removeStatusEffect(diseaseEntry);
                        p.getCommandTags().add(TAG_VAMPIRE);
                    }

                    boolean previouslySleeping = wasSleeping.getOrDefault(id, false);
                    boolean currentlySleeping = p.isSleeping();
                    if (previouslySleeping && !currentlySleeping) {
                        if (diseaseEntry != null && p.hasStatusEffect(diseaseEntry) && !p.getCommandTags().contains(TAG_VAMPIRE)) {
                            p.removeStatusEffect(diseaseEntry);
                            p.getCommandTags().add(TAG_VAMPIRE);
                        }
                    }
                    wasSleeping.put(id, currentlySleeping);
                }
            }
        });
    }
}