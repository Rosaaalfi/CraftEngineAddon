package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import net.momirealms.craftengine.libraries.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persist original block states to NBT for recovery across server restarts.
 * Stores mapping: (world hash + pos) -> blockstate NBT.
 */
public final class OriginalStateStorage {
    private static final String NBT_KEY_PREFIX = "bed_block_orig_";
    private static final Map<String, ImmutableBlockState> IN_MEMORY_CACHE = new ConcurrentHashMap<>();

    private OriginalStateStorage() {}

    /**
     * Store original state in memory cache and optionally to NBT.
     */
    public static void storeOriginal(Object worldObj, BlockPos pos, ImmutableBlockState state, CompoundTag nbtTag) {
        String key = makeKey(worldObj, pos);
        IN_MEMORY_CACHE.put(key, state);

        // Also store in NBT if available
        if (nbtTag != null && state != null) {
            try {
                CompoundTag stateNbt = state.getNbtToSave();
                if (stateNbt != null) {
                    nbtTag.put(NBT_KEY_PREFIX + key, stateNbt);
                }
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Retrieve stored original state by key.
     */
    public static ImmutableBlockState getOriginal(Object worldObj, BlockPos pos) {
        String key = makeKey(worldObj, pos);
        ImmutableBlockState cached = IN_MEMORY_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        // Could load from NBT here if needed
        return null;
    }

    /**
     * Remove and return stored original state.
     */
    public static ImmutableBlockState removeOriginal(Object worldObj, BlockPos pos) {
        String key = makeKey(worldObj, pos);
        return IN_MEMORY_CACHE.remove(key);
    }

    /**
     * Clear all cached originals (e.g., on server shutdown).
     */
    public static void clear() {
        IN_MEMORY_CACHE.clear();
    }

    /**
     * Load original states from NBT tag (on server startup).
     */
    public static void loadFromNBT(CompoundTag nbtTag, Map<String, ImmutableBlockState> target) {
        if (nbtTag == null) return;
        // Iterate through all keys starting with NBT_KEY_PREFIX
        for (String tagKey : nbtTag.keySet()) {
            if (tagKey.startsWith(NBT_KEY_PREFIX)) {
                String stateKey = tagKey.substring(NBT_KEY_PREFIX.length());
                // Note: ImmutableBlockState.fromNBT would need to be called here
                // For now, this is a placeholder for full persistence support
            }
        }
    }

    private static String makeKey(Object worldObj, BlockPos pos) {
        return String.valueOf(System.identityHashCode(worldObj)) + ':' + pos.asLong();
    }
}

