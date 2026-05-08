package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehavior;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.core.item.Item;

/**
 * Bed behavior with basic sleep initiation using NMS reflection adapter.
 * - Checks require-night config (simple time check)
 * - Attempts to call NMS ServerPlayer.startSleeping(BlockPos) via reflection
 */
public class BedBehavior extends FurnitureBehavior {

    private final BedConfig config;

    public BedBehavior(CustomFurniture furniture, BedConfig config) {
        super(furniture);
        this.config = config;
    }

    @Override
    public InteractionResult useOnFurniture(InteractEntityContext context, Furniture furniture) {
        // If player interacts with the furniture directly, try to make them sleep
        // This prevents the bed from being replaced by items
        net.momirealms.craftengine.core.entity.player.Player player = context.getPlayer();

        try {
            // Check if require-night
            if (config.requireNight) {
                long time = context.getLevel().time();
                long dayTime = time % 24000L;
                boolean isNight = dayTime >= 12541L && dayTime <= 23458L;
                if (!isNight) {
                    // Not night, but prevent replacement
                    return InteractionResult.SUCCESS_AND_CANCEL;
                }
            }

            // Try to sleep
            Object serverPlayer = player.serverPlayer();
            if (serverPlayer != null) {
                net.momirealms.craftengine.core.world.BlockPos pos = context.getClickedPos();
                if (pos == null) {
                    net.momirealms.craftengine.core.world.WorldPosition wp = furniture.position();
                    pos = net.momirealms.craftengine.core.world.BlockPos.fromVec3d(wp.toVec3d());
                }

                me.chyxelmc.ceaddon.nms.NmsSleepAdapter.tryStartSleeping(serverPlayer, pos.x(), pos.y(), pos.z());
            }
        } catch (Throwable ignored) {
            // Safe to ignore
        }
        // Return SUCCESS_AND_CANCEL to prevent bed from being replaced
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    public InteractionResult useWithoutItem(InteractEntityContext context, Furniture furniture) {
        // Player clicked bed with empty hand -> try to initiate sleep.
        try {
            net.momirealms.craftengine.core.entity.player.Player player = context.getPlayer();

             // If require-night is set, check simple world time window (night)
             // Validate require-night config (simple time check)
             if (config.requireNight) {
                 long time = context.getLevel().time();
                 long dayTime = time % 24000L;
                 // minecraft night: ~12541-23458 ticks (roughly 8 PM to 6 AM)
                 boolean isNight = dayTime >= 12541L && dayTime <= 23458L;
                 if (!isNight) {
                     return InteractionResult.FAIL;
                 }
             }

            // Check monster-radius if configured
            if (config.monsterRadius > 0.0) {
                try {
                    net.momirealms.craftengine.core.world.BlockPos bedPos = context.getClickedPos();
                    Object serverLevel = context.getLevel().serverWorld();
                    if (serverLevel != null && bedPos != null) {
                        if (me.chyxelmc.ceaddon.nms.NmsSleepAdapter.hasHostileMobsNearby(
                                serverLevel, bedPos.x(), bedPos.y(), bedPos.z(), config.monsterRadius)) {
                            return InteractionResult.FAIL;
                        }
                    }
                } catch (Throwable ignored) {
                    // If entity check fails, allow sleep to proceed
                }
            }

            // Determine block pos to use for sleeping: use clicked pos if available, otherwise furniture position.
            net.momirealms.craftengine.core.world.BlockPos pos = context.getClickedPos();
            if (pos == null) {
                net.momirealms.craftengine.core.world.WorldPosition wp = furniture.position();
                pos = net.momirealms.craftengine.core.world.BlockPos.fromVec3d(wp.toVec3d());
            }

            // Get underlying server player object (NMS) and call startSleeping via adapter
            Object serverPlayer = player.serverPlayer();
            if (serverPlayer == null) {
                return InteractionResult.FAIL;
            }

            boolean started = me.chyxelmc.ceaddon.nms.NmsSleepAdapter.tryStartSleeping(serverPlayer, pos.x(), pos.y(), pos.z());
            return started ? InteractionResult.SUCCESS_AND_CANCEL : InteractionResult.FAIL;
        } catch (Exception ex) {
            // Log via CraftEngine logger? For now, swallow and return FAIL.
            ex.printStackTrace();
            return InteractionResult.FAIL;
        }
    }

    @Override
    public void onRemove(Furniture furniture) {
        // Called when furniture is removed. Cleanup if necessary.
    }

    @Override
    public void onAdd(Furniture furniture) {
        // Called after furniture is added to the world.
    }

    @Override
    public Item<?> itemToPickup(Furniture furniture, net.momirealms.craftengine.core.entity.player.Player player) {
        // Return null for now — CraftEngine will fallback to default drop handling.
        return null;
    }
}





