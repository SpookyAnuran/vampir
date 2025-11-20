package com.vampir;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VampirBoon {
    private static final String TAG_VAMPIRE = "vampir:vampire";

    // identifier keys (use tryParse form as in your mappings)
    private static final Identifier DAMAGE_MOD_ID = Identifier.tryParse("vampir", "claw_damage");
    private static final Identifier ATTACK_SPEED_MOD_ID = Identifier.tryParse("vampir", "claw_attack_speed");

    // modifier instances (Identifier, amount, operation) — ADD_VALUE per your mappings
    private static final EntityAttributeModifier DAMAGE_MOD =
            new EntityAttributeModifier(DAMAGE_MOD_ID, 6.0, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final EntityAttributeModifier ATTACK_SPEED_MOD =
            new EntityAttributeModifier(ATTACK_SPEED_MOD_ID, 2.0, EntityAttributeModifier.Operation.ADD_VALUE);

    // claw item ids
    private static final Identifier DIAMOND_CLAW_ID = Identifier.tryParse("vampir", "diamondclaw");
    private static final Identifier IRON_CLAW_ID    = Identifier.tryParse("vampir", "ironclaw");
    private static final Identifier GOLD_CLAW_ID    = Identifier.tryParse("vampir", "goldclaw");
    private static final Identifier NETHERITE_CLAW_ID = Identifier.tryParse("vampir", "netheriteclaw");

    // cached Item references (initialized once inside register)
    private static Item DIAMOND_CLAW;
    private static Item IRON_CLAW;
    private static Item GOLD_CLAW;
    private static Item NETHERITE_CLAW;

    // effect timing tuned to avoid tick spam
    private static final int SHELTER_EFFECT_DURATION = 100; // ticks (5s)
    private static final int SHELTER_EFFECT_REAPPLY_THRESHOLD = 20; // ticks left before reapply

    // state maps to avoid per-tick churn
    private static final Map<UUID, Boolean> wasSheltered = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> hadModifiers = new ConcurrentHashMap<>();

    private VampirBoon() {}

    public static void register() {
        // initialize cached items once
        DIAMOND_CLAW = Registries.ITEM.get(DIAMOND_CLAW_ID);
        IRON_CLAW     = Registries.ITEM.get(IRON_CLAW_ID);
        GOLD_CLAW     = Registries.ITEM.get(GOLD_CLAW_ID);
        NETHERITE_CLAW= Registries.ITEM.get(NETHERITE_CLAW_ID);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                for (PlayerEntity p : world.getPlayers(player -> true)) {
                    UUID id = p.getUuid();

                    // cleanup if player isn't a vampire
                    if (!p.getCommandTags().contains(TAG_VAMPIRE)) {
                        removeModifiersIfPresent(p);
                        wasSheltered.remove(id);
                        hadModifiers.remove(id);
                        continue;
                    }

                    BlockPos pos = p.getBlockPos();
                    boolean currentlySheltered = !isInDirectSunlight(world, pos); // true when sheltered
                    boolean hasEmptyHand = p.getMainHandStack().isEmpty() && p.getOffHandStack().isEmpty();
                    boolean hasClaw = isClawItem(p.getMainHandStack().getItem()) || isClawItem(p.getOffHandStack().getItem());
                    boolean shouldHaveModifiers = hasEmptyHand || hasClaw;

                    // EFFECTS: ensure Speed is present while sheltered (reapply only if expiring)
                    if (currentlySheltered) {
                        StatusEffectInstance speedInst = p.getStatusEffect(StatusEffects.SPEED);

                        boolean needSpeed = (speedInst == null) || (speedInst.getDuration() <= SHELTER_EFFECT_REAPPLY_THRESHOLD);

                        if (needSpeed) {
                            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, SHELTER_EFFECT_DURATION, 0, false, false, true));
                        }
                    } else {
                        // in sunlight: remove immediately
                        p.removeStatusEffect(StatusEffects.SPEED);
                    }
                    wasSheltered.put(id, currentlySheltered);

                    // ATTRIBUTES: add/remove modifiers only on state changes
                    boolean previouslyHadModifiers = hadModifiers.getOrDefault(id, false);
                    if (shouldHaveModifiers && !previouslyHadModifiers) {
                        addModifiersIfMissing(p);
                        hadModifiers.put(id, true);
                    } else if (!shouldHaveModifiers && previouslyHadModifiers) {
                        removeModifiersIfPresent(p);
                        hadModifiers.put(id, false);
                    }
                }
            }
        });
    }

    private static boolean isInDirectSunlight(ServerWorld world, BlockPos pos) {
        if (!world.isDay()) return false;
        BlockPos headPos = pos.up();
        return world.isSkyVisible(headPos) && world.getLightLevel(headPos) > 0;
    }

    private static boolean isClawItem(Item item) {
        return item == DIAMOND_CLAW || item == IRON_CLAW || item == GOLD_CLAW || item == NETHERITE_CLAW;
    }

    private static void addModifiersIfMissing(PlayerEntity p) {
        EntityAttributeInstance dmgInst = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (dmgInst != null) {
            try {
                if (dmgInst.getModifier(DAMAGE_MOD_ID) == null) {
                    dmgInst.addTemporaryModifier(DAMAGE_MOD);
                }
            } catch (Throwable ignored) {
                try {
                    if (dmgInst.getModifier(DAMAGE_MOD.id()) == null) {
                        dmgInst.addTemporaryModifier(DAMAGE_MOD);
                    }
                } catch (Throwable ignored2) {}
            }
        }

        EntityAttributeInstance atkSpdInst = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (atkSpdInst != null) {
            try {
                if (atkSpdInst.getModifier(ATTACK_SPEED_MOD_ID) == null) {
                    atkSpdInst.addTemporaryModifier(ATTACK_SPEED_MOD);
                }
            } catch (Throwable ignored) {
                try {
                    if (atkSpdInst.getModifier(ATTACK_SPEED_MOD.id()) == null) {
                        atkSpdInst.addTemporaryModifier(ATTACK_SPEED_MOD);
                    }
                } catch (Throwable ignored2) {}
            }
        }
    }

    static void removeModifiersIfPresent(PlayerEntity p) {
        EntityAttributeInstance dmgInst = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (dmgInst != null) {
            try { dmgInst.removeModifier(DAMAGE_MOD); } catch (Throwable ignored) {}
            try { dmgInst.removeModifier(DAMAGE_MOD_ID); } catch (Throwable ignored) {}
            try { dmgInst.removeModifier(DAMAGE_MOD.id()); } catch (Throwable ignored) {}
        }

        EntityAttributeInstance atkSpdInst = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (atkSpdInst != null) {
            try { atkSpdInst.removeModifier(ATTACK_SPEED_MOD); } catch (Throwable ignored) {}
            try { atkSpdInst.removeModifier(ATTACK_SPEED_MOD_ID); } catch (Throwable ignored) {}
            try { atkSpdInst.removeModifier(ATTACK_SPEED_MOD.id()); } catch (Throwable ignored) {}
        }
    }
}
