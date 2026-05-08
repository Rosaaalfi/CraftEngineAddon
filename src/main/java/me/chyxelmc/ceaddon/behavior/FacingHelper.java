package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.Vec3i;

/**
 * Helper to rotate block offsets based on facing direction.
 */
public final class FacingHelper {
    private FacingHelper() {}

    public static Vec3i rotateOffset(Vec3i offset, Direction facing) {
        if (facing == null) return offset;
        int x = offset.x();
        int y = offset.y();
        int z = offset.z();
        return switch (facing) {
            case SOUTH -> offset;                // +Z
            case NORTH -> new Vec3i(-x, y, -z); // -Z
            case EAST -> new Vec3i(z, y, -x);   // +X
            case WEST -> new Vec3i(-z, y, x);   // -X
            default -> offset;
        };
    }

    public static Direction yawToDirection(float yaw) {
        float n = ((yaw % 360f) + 360f) % 360f;
        if (n < 45f || n >= 315f) return Direction.SOUTH;
        if (n < 135f) return Direction.WEST;
        if (n < 225f) return Direction.NORTH;
        return Direction.EAST;
    }

    public static Direction inferPrimaryAxis(java.util.List<Vec3i> slots) {
        if (slots == null || slots.isEmpty()) return Direction.SOUTH;
        int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
        for (Vec3i v : slots) {
            minX = Math.min(minX, v.x());
            maxX = Math.max(maxX, v.x());
            minZ = Math.min(minZ, v.z());
            maxZ = Math.max(maxZ, v.z());
        }
        int xRange = maxX - minX;
        int zRange = maxZ - minZ;
        return xRange >= zRange ? Direction.EAST : Direction.SOUTH;
    }
}
