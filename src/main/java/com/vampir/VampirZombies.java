package com.vampir;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public class VampirZombies {
    // Constants for easy tuning
    private static final double VAMPIRE_LURE_RANGE = 40.0;
    private static final double HUMAN_ATTACK_RANGE = 10.0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getWorlds().forEach(world -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                boolean isVampire = isVampire(player);
                boolean isHuman = isHuman(player);

                if (!isVampire && !isHuman) continue;

                Box area = new Box(
                        player.getX() - VAMPIRE_LURE_RANGE, player.getY() - 20, player.getZ() - VAMPIRE_LURE_RANGE,
                        player.getX() + VAMPIRE_LURE_RANGE, player.getY() + 20, player.getZ() + VAMPIRE_LURE_RANGE
                );

                for (ZombieEntity zombie : world.getEntitiesByClass(ZombieEntity.class, area, ZombieEntity::isAlive)) {
                    // If a vampire attacked this zombie, allow vanilla retaliation
                    LivingEntity attacker = zombie.getAttacker();
                    if (attacker instanceof ServerPlayerEntity vampAttacker && isVampire(vampAttacker)) {
                        continue;
                    }

                    // Block zombies from targeting vampires
                    LivingEntity currentTarget = zombie.getTarget();
                    if (currentTarget instanceof ServerPlayerEntity targetPlayer && isVampire(targetPlayer)) {
                        zombie.setTarget(null);              // clear target
                        zombie.getNavigation().stop();       // stop moving toward vampire
                        System.out.println("Zombie blocked from targeting vampire: "
                                + targetPlayer.getName().getString()); // debug line
                        continue;                            // skip rest of logic this tick
                    }

                    ServerPlayerEntity nearestHuman = findNearestHuman(zombie);
                    if (nearestHuman != null) {
                        zombie.setTarget(nearestHuman);
                        continue;
                    }

                    ServerPlayerEntity nearestVampire = findNearestVampire(zombie);
                    if (nearestVampire != null) {
                        double dist = zombie.squaredDistanceTo(nearestVampire);
                        if (dist > 36.0) { // only follow if farther than 6 blocks
                            zombie.getNavigation().startMovingTo(nearestVampire, 1.0);
                        } else {
                            zombie.getNavigation().stop(); // too close — hold position
                        }
                    }
                }
            }
        }));
    }

    private static boolean isVampire(ServerPlayerEntity player) {
        return player.getCommandTags().contains("vampir:vampire");
    }

    private static boolean isHuman(ServerPlayerEntity player) {
        return player.getCommandTags().contains("vampir:human");
    }

    private static ServerPlayerEntity findNearestVampire(ZombieEntity zombie) {
        ServerPlayerEntity best = null;
        double bestDist = VAMPIRE_LURE_RANGE * VAMPIRE_LURE_RANGE;
        for (PlayerEntity player : zombie.getWorld().getPlayers()) {
            if (player instanceof ServerPlayerEntity serverPlayer && isVampire(serverPlayer)) {
                double d = serverPlayer.squaredDistanceTo(zombie);
                if (d <= bestDist) {
                    bestDist = d;
                    best = serverPlayer;
                }
            }
        }
        return best;
    }

    private static ServerPlayerEntity findNearestHuman(ZombieEntity zombie) {
        ServerPlayerEntity best = null;
        double bestDist = HUMAN_ATTACK_RANGE * HUMAN_ATTACK_RANGE;
        for (PlayerEntity player : zombie.getWorld().getPlayers()) {
            if (player instanceof ServerPlayerEntity serverPlayer && isHuman(serverPlayer)) {
                double d = serverPlayer.squaredDistanceTo(zombie);
                if (d <= bestDist) {
                    bestDist = d;
                    best = serverPlayer;
                }
            }
        }
        return best;
    }
}
