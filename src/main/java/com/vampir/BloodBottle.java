package com.vampir;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class BloodBottle extends Item {
    public BloodBottle(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Gate vampires vs. non-vampires
        if (!isVampire(player)) {
            if (!world.isClient) {
                player.sendMessage(Text.literal("This tastes wrong..."), true);
            }
            return TypedActionResult.fail(stack);
        }

        // Apply effects immediately
        if (!world.isClient) {
            player.getHungerManager().add(8, 1.0f); // high hunger & saturation
        }

        // Vanilla signals
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Consume and return glass bottle
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        if (stack.isEmpty()) {
            return TypedActionResult.success(new ItemStack(Items.GLASS_BOTTLE));
        } else {
            // Put bottle into inventory; if full, just succeed without adding
            player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            return TypedActionResult.success(stack);
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        // Keeps the drink animation for cohesion (even though we consume instantly)
        return UseAction.DRINK;
    }

    private static boolean isVampire(PlayerEntity player) {
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            return serverPlayer.getCommandTags().contains("vampir:vampire");
        }
        return false;
    }

    public static Item register() {
        return Registry.register(
                Registries.ITEM,
                Identifier.of("vampir", "blood_bottle"),
                new BloodBottle(new Item.Settings().maxCount(16))
        );
    }
}
