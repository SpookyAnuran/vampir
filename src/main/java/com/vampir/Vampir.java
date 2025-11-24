package com.vampir;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;


public class Vampir implements ModInitializer {
    public static final String MOD_ID = "vampir";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Vampir loaded.");

        VampirCommands.register();
        ModEffects.register();
        VampirInfection.register();
        VampirDisease.register();
        VampirSunlight.register();
        VampirZombies.register();
        VampirDiet.register();
        VampirRitual.register();
        VampirBoon.register();
        VampirStaticBoon.register();

// double wooden sword damage against players tagged as vampires (simple, no DamageSource)
        AttackEntityCallback.EVENT.register((PlayerEntity attacker, World world, Hand hand, Entity target, EntityHitResult hit) -> {
            if (world.isClient) return ActionResult.PASS;
            if (attacker.getStackInHand(hand).getItem() != Items.WOODEN_SWORD) return ActionResult.PASS;
            if (!(target instanceof ServerPlayerEntity victim)) return ActionResult.PASS;
            if (!victim.getCommandTags().contains("vampir:vampire")) return ActionResult.PASS;

            double attackDamage = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (attackDamage > 0.0) {
                float newHealth = victim.getHealth() - (float) attackDamage;
                victim.setHealth(Math.max(0f, newHealth));
            }
            return ActionResult.PASS;
        });

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
