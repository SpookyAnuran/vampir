package com.vampir;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

public final class VampirRitual {
    private static final String TAG_VAMPIRE = "vampir:vampire";
    private static final int SCAN_RADIUS = 6;

    private VampirRitual() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.pass(player.getStackInHand(hand));
            if (!(serverPlayer.getServerWorld() instanceof ServerWorld sw)) return TypedActionResult.pass(player.getStackInHand(hand));

            ItemStack main = serverPlayer.getMainHandStack();
            if (main.getItem() != Items.DIAMOND) return TypedActionResult.pass(player.getStackInHand(hand));

            BlockPos playerPos = serverPlayer.getBlockPos();
            BlockPos found = findNearest2x2(sw, playerPos, SCAN_RADIUS);
            if (found == null) return TypedActionResult.fail(player.getStackInHand(hand));

            long time = sw.getTimeOfDay() % 24000L;
            if (time < 13000L || time > 23000L) return TypedActionResult.fail(player.getStackInHand(hand));

            if (serverPlayer.getCommandTags().contains(TAG_VAMPIRE)) return TypedActionResult.fail(player.getStackInHand(hand));

            if (main.getCount() > 1) {
                main.decrement(1);
            } else {
                serverPlayer.setStackInHand(hand, ItemStack.EMPTY);
            }

            serverPlayer.getCommandTags().add(TAG_VAMPIRE);

            BlockPos top = found.up();
            sw.spawnParticles(ParticleTypes.SOUL, top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 0.5, 40, 0.6, 0.6, 0.6, 0.02);
            sw.playSound(null, top, SoundEvents.ENTITY_WITHER_SPAWN, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.95f);

            Text ominous = Text.literal("A chill runs down your spine. Something very wrong as happened in the world.");
            for (ServerPlayerEntity p : sw.getPlayers(p -> true)) {
                p.sendMessage(ominous, false);
            }

            return TypedActionResult.success(player.getStackInHand(hand));
        });
    }

    private static BlockPos findNearest2x2(ServerWorld world, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDistSq = Double.POSITIVE_INFINITY;

        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        int yMin = Math.max(world.getBottomY(), oy - 1);
        int yMax = Math.min(world.getTopY(), oy + 1);

        for (int y = yMin; y <= yMax; y++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos nw = new BlockPos(ox + dx, y, oz + dz);
                    if (matches2x2BlackGlazed(world, nw)) {
                        double distSq = nw.getSquaredDistance(ox, oy, oz);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            best = nw;
                        }
                    }
                }
            }
        }
        return best;
    }

    private static boolean matches2x2BlackGlazed(ServerWorld world, BlockPos nw) {
        BlockPos p0 = nw;
        BlockPos p1 = nw.add(1, 0, 0);
        BlockPos p2 = nw.add(0, 0, 1);
        BlockPos p3 = nw.add(1, 0, 1);

        return isBlackGlazed(world, p0)
                && isBlackGlazed(world, p1)
                && isBlackGlazed(world, p2)
                && isBlackGlazed(world, p3);
    }

    private static boolean isBlackGlazed(ServerWorld world, BlockPos pos) {
        var b = world.getBlockState(pos).getBlock();
        return b == Blocks.BLACK_GLAZED_TERRACOTTA;
    }
}
