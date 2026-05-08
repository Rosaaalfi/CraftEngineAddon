package me.chyxelmc.ceaddon.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;

public final class NmsSleepAdapter {
    private NmsSleepAdapter() {}

    public static boolean placeVanillaBed(Object serverLevel, int footX, int footY, int footZ, int headX, int headY, int headZ, String facingName) {
        if (!(serverLevel instanceof ServerLevel level)) return false;
        try {
            var facing = net.minecraft.core.Direction.valueOf(facingName);
            BlockState footState = Blocks.WHITE_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.FACING, facing)
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, BedPart.FOOT)
                    .setValue(net.minecraft.world.level.block.BedBlock.OCCUPIED, false);
            BlockState headState = Blocks.WHITE_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.FACING, facing)
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, BedPart.HEAD)
                    .setValue(net.minecraft.world.level.block.BedBlock.OCCUPIED, false);

            level.setBlock(new BlockPos(footX, footY, footZ), footState, 3);
            level.setBlock(new BlockPos(headX, headY, headZ), headState, 3);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean tryStartSleeping(Object serverPlayer, int x, int y, int z) {
        if (!(serverPlayer instanceof ServerPlayer player)) return false;
        try {
            player.startSleepInBed(new BlockPos(x, y, z), false);
            return player.isSleeping();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean tryWakeupPlayer(Object serverPlayer) {
        if (!(serverPlayer instanceof ServerPlayer player)) return false;
        try {
            player.stopSleepInBed(false, true);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasHostileMobsNearby(Object serverLevel, int x, int y, int z, double radius) {
        if (!(serverLevel instanceof ServerLevel level)) return false;
        try {
            AABB box = new AABB(
                    x - radius, y - radius, z - radius,
                    x + radius, y + radius, z + radius
            );
            return !level.getEntitiesOfClass(Monster.class, box).isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }
}
