package com.vampir;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vampir implements ModInitializer {
    public static final String MOD_ID = "vampir";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Register Blood Bottle item (stackable to 16)
    public static final Item BLOOD_BOTTLE = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "blood_bottle"),
            new BloodBottle(new Item.Settings().maxCount(16))
    );

    // Build the creative tab (no args to builder), auto-populate items by namespace
    public static final ItemGroup VAMPIR_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.vampir.vampir_group"))
            .icon(() -> new ItemStack(BLOOD_BOTTLE))
            .entries((context, entries) -> {
                Registries.ITEM.stream()
                        .filter(item -> Registries.ITEM.getId(item).getNamespace().equals(MOD_ID))
                        .forEach(entries::add);
            })
            .build();

    @Override
    public void onInitialize() {
        LOGGER.info("Vampir loaded.");

        // Register systems
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
        VampirBloodCollection.register();

        // Register creative tab with its identifier
        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "vampir_group"), VAMPIR_GROUP);

        // Double wooden sword damage against vampire players
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

        // Tag new players as human unless already vampire
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
