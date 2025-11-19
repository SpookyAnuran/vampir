package com.vampir;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class VampirInfection {
    private static final String TAG_VAMPIRE = "vampir:vampire";

    // configuration
    private static final long BITE_COOLDOWN_MS = TimeUnit.SECONDS.toMillis(3);
    private static final double BITE_INFECTION_CHANCE = 0.5; // 50% chance on successful special attack
    private static final int EFFECT_DURATION = Integer.MAX_VALUE; // effectively permanent until cleared by milk or removed by code

    // runtime state
    private static final Map<UUID, Long> lastBite = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> wasSleeping = new ConcurrentHashMap<>();
    private static StatusEffect VAMPIR_DISEASE;

    private VampirInfection() {}

    public static void register() {
        registerEffect();

        // special lunge bite: sprint + empty hands
        AttackEntityCallback.EVENT.register((attackerEntity, world, hand, target, hitResult) -> {
            if (!(attackerEntity instanceof ServerPlayerEntity attacker)) return ActionResult.PASS;
            if (!(world instanceof ServerWorld)) return ActionResult.PASS;

            Entity t = target;
            if (!(t instanceof ServerPlayerEntity victim)) return ActionResult.PASS;

            if (attacker.getUuid().equals(victim.getUuid())) return ActionResult.PASS;
            if (!attacker.getCommandTags().contains(TAG_VAMPIRE)) return ActionResult.PASS;
            if (victim.getCommandTags().contains(TAG_VAMPIRE)) return ActionResult.PASS;

            // Intentional lunge condition: attacker must be sprinting and empty-handed
            boolean attackerSprinting = attacker.isSprinting();
            boolean emptyHands = attacker.getMainHandStack().isEmpty() && attacker.getOffHandStack().isEmpty();
            if (!attackerSprinting || !emptyHands) return ActionResult.PASS;

            long now = System.currentTimeMillis();
            Long last = lastBite.get(attacker.getUuid());
            if (last != null && now - last < BITE_COOLDOWN_MS) return ActionResult.PASS;
            lastBite.put(attacker.getUuid(), now);

            // Chance to infect
            double roll = ThreadLocalRandom.current().nextDouble();
            if (roll < BITE_INFECTION_CHANCE) {
                // apply disease effect
                victim.addStatusEffect(new StatusEffectInstance((RegistryEntry<StatusEffect>) VAMPIR_DISEASE, EFFECT_DURATION, 0, false, true, true));

                // subtle feedback at victim location (particles/sound) but no chat
                BlockPos pos = victim.getBlockPos();
                ServerWorld sw = (ServerWorld) victim.getWorld();
                sw.spawnParticles(ParticleTypes.DRIPPING_LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.01);
                sw.playSound(null, pos, SoundEvents.ENTITY_GENERIC_HURT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.9f);
            }

            return ActionResult.PASS;
        });

        // server tick: check death and sleep->wake; apply vampir tag quietly if diseased
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                for (ServerPlayerEntity p : world.getPlayers(player -> true)) {
                    UUID id = p.getUuid();

                    // Death conversion: if player is dead/health <= 0 and currently diseased, tag them
                    boolean isDead = p.isDead() || p.getHealth() <= 0.0F;
                    if (isDead && p.hasStatusEffect((RegistryEntry<StatusEffect>) VAMPIR_DISEASE) && !p.getCommandTags().contains(TAG_VAMPIRE)) {
                        p.removeStatusEffect((RegistryEntry<StatusEffect>) VAMPIR_DISEASE);
                        p.getCommandTags().add(TAG_VAMPIRE);
                    }

                    // Sleep->wake detection
                    boolean previouslySleeping = wasSleeping.getOrDefault(id, false);
                    boolean currentlySleeping = p.isSleeping();
                    if (previouslySleeping && !currentlySleeping) {
                        // woke up
                        if (p.hasStatusEffect((RegistryEntry<StatusEffect>) VAMPIR_DISEASE) && !p.getCommandTags().contains(TAG_VAMPIRE)) {
                            p.removeStatusEffect((RegistryEntry<StatusEffect>) VAMPIR_DISEASE);
                            p.getCommandTags().add(TAG_VAMPIRE);
                        }
                    }
                    wasSleeping.put(id, currentlySleeping);
                }
            }
        });
    }

    // register a simple marker-style status effect (no periodic ticking)
    private static void registerEffect() {
        VAMPIR_DISEASE = Registry.register(Registries.STATUS_EFFECT, Identifier.of("vampir", "vampir_disease"),
                new StatusEffect(StatusEffectCategory.HARMFUL, 0x330000) {
                    @Override
                    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) { /* no periodic behavior */
                        return false;
                    }

                    @Override
                    public boolean canApplyUpdateEffect(int duration, int amplifier) { return false; }
                });
    }
}
