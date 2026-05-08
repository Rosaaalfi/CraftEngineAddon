package me.chyxelmc.ceaddon.listener;

import me.chyxelmc.ceaddon.behavior.BedSessionRegistry;
import me.chyxelmc.ceaddon.behavior.FacingHelper;
import me.chyxelmc.ceaddon.config.BedAddonConfig;
import me.chyxelmc.ceaddon.nms.NmsSleepAdapter;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.api.event.FurnitureBreakEvent;
import net.momirealms.craftengine.bukkit.api.event.FurnitureInteractEvent;
import net.momirealms.craftengine.bukkit.api.event.FurniturePlaceEvent;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles CraftEngine furniture lifecycle and injects real vanilla beds behind the furniture model.
 */
public final class BedInteractListener implements Listener {
    private final Plugin plugin;
    private BedAddonConfig config;

    public BedInteractListener(Plugin plugin, BedAddonConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void clearTracking() {
        // no-op: bed culling/hiding disabled to avoid client/server collision desync
    }

    public void updateConfig(BedAddonConfig newConfig) {
        this.config = newConfig;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurniturePlace(FurniturePlaceEvent event) {
        BedAddonConfig.BedDefinition bed = config.getByFurnitureId(event.furniture().id().asString());
        if (bed == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> injectBed(event.player(), event.location(), bed));
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        BedAddonConfig.BedDefinition bed = config.getByFurnitureId(event.furniture().id().asString());
        if (bed == null) return;

        Player player = event.player();
        World world = event.location().getWorld();
        if (world == null) return;

        BedSessionRegistry.Session session = BedSessionRegistry.get(world.getUID(), new BlockPos(event.location().getBlockX(), event.location().getBlockY(), event.location().getBlockZ()).asLong());
        if (session == null) return;

        event.setCancelled(true);

        if (bed.requireNight && !isNight(world)) {
            return;
        }

        if (bed.monsterRadius > 0.0) {
            Object serverWorld = getHandle(world);
            BlockPos footPos = BlockPos.of(session.footKey);
            if (serverWorld != null && NmsSleepAdapter.hasHostileMobsNearby(serverWorld, footPos.x(), footPos.y(), footPos.z(), bed.monsterRadius)) {
                return; // Hostile mobs nearby, cancel sleep
            }
        }

        Object serverPlayer = getHandle(player);
        if (serverPlayer == null) return;

        BlockPos headPos = BlockPos.of(session.headKey);
        NmsSleepAdapter.tryStartSleeping(serverPlayer, headPos.x(), headPos.y(), headPos.z());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        BedAddonConfig.BedDefinition bed = config.getByFurnitureId(event.furniture().id().asString());
        if (bed == null) return;
        // Cleanup bed ditangani di BlockBreakEvent karena sumber aksi utamanya adalah player memecah block bed.
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Intentionally empty: do not hide/cull bed blocks from clients.
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Intentionally empty: do not hide/cull bed blocks from clients.
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        long posKey = new BlockPos(block.getX(), block.getY(), block.getZ()).asLong();
        BedSessionRegistry.Session session = BedSessionRegistry.get(world.getUID(), posKey);
        if (session == null) return;
        // Only cancel if the broken block is a hidden bed block (foot or head, not origin ground)
        if (posKey == session.footKey || posKey == session.headKey) {
            event.setCancelled(true);
            event.setDropItems(false);
            Bukkit.getScheduler().runTask(plugin, () -> {
                BedSessionRegistry.Session latest = BedSessionRegistry.get(world.getUID(), session.originKey);
                if (latest == null) return;
                // Bed cleanup harus dilakukan dari event block break agar bed server-state benar-benar hilang.
                cleanupBedSession(
                        new Location(world, block.getX(), block.getY(), block.getZ()),
                        latest.config
                );
                removeNearbyFurniture(world, latest, event.getPlayer());
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // no-op: bed culling/hiding disabled
    }

    private void injectBed(Player placer, Location location, BedAddonConfig.BedDefinition bed) {
        World world = location.getWorld();
        if (world == null) return;

        Vec3i footOffset = FacingHelper.rotateOffset(bed.slots.get(0), FacingHelper.yawToDirection(placer.getLocation().getYaw()));
        Vec3i headOffset = FacingHelper.rotateOffset(bed.slots.get(1), FacingHelper.yawToDirection(placer.getLocation().getYaw()));

        BlockPos origin = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockPos footPos = origin.offset(footOffset.x(), footOffset.y(), footOffset.z());
        BlockPos headPos = origin.offset(headOffset.x(), headOffset.y(), headOffset.z());

        BlockState originalFoot = world.getBlockAt(footPos.x(), footPos.y(), footPos.z()).getState();
        BlockState originalHead = world.getBlockAt(headPos.x(), headPos.y(), headPos.z()).getState();

        Object serverWorld = getHandle(world);
        if (serverWorld == null) return;

        String facing = FacingHelper.yawToDirection(placer.getLocation().getYaw()).name();
        if (NmsSleepAdapter.placeVanillaBed(serverWorld, footPos.x(), footPos.y(), footPos.z(), headPos.x(), headPos.y(), headPos.z(), facing)) {
            BedSessionRegistry.register(world.getUID(), origin.asLong(), footPos.asLong(), headPos.asLong(), bed, facing);
            BedSessionRegistry.Session session = BedSessionRegistry.get(world.getUID(), origin.asLong());
            if (session != null) {
                session.originalFoot = originalFoot;
                session.originalHead = originalHead;
            }
        }
    }

    private void cleanupBedSession(Location location, BedAddonConfig.BedDefinition bedDefinition) {
        World world = location.getWorld();
        if (world == null) return;
        long originKey = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()).asLong();

        BedSessionRegistry.Session session = BedSessionRegistry.get(world.getUID(), originKey);
        if (session == null) {
            session = findSessionAround(world, location, bedDefinition, 3);
        }
        if (session == null) return;

        // Restore original blocks to remove the vanilla bed blocks
        try {
            BlockPos footPos = BlockPos.of(session.footKey);
            BlockPos headPos = BlockPos.of(session.headKey);

            if (session.originalFoot != null) {
                session.originalFoot.update(true, false);
            } else {
                world.getBlockAt(footPos.x(), footPos.y(), footPos.z()).setType(Material.AIR, false);
            }
            if (session.originalHead != null) {
                session.originalHead.update(true, false);
            } else {
                world.getBlockAt(headPos.x(), headPos.y(), headPos.z()).setType(Material.AIR, false);
            }
        } catch (Throwable ignored) {}

        BedSessionRegistry.remove(world.getUID(), session.originKey);
    }

    private BedSessionRegistry.Session findSessionAround(World world, Location location, BedAddonConfig.BedDefinition bedDefinition, int range) {
        int bx = location.getBlockX();
        int by = location.getBlockY();
        int bz = location.getBlockZ();
        BedSessionRegistry.Session nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    long key = new BlockPos(bx + dx, by + dy, bz + dz).asLong();
                    BedSessionRegistry.Session session = BedSessionRegistry.get(world.getUID(), key);
                    if (session == null) continue;
                    if (!session.config.furnitureId.equals(bedDefinition.furnitureId)) continue;
                    BlockPos origin = BlockPos.of(session.originKey);
                    double distSq = location.distanceSquared(new Location(world, origin.x() + 0.5, origin.y() + 0.5, origin.z() + 0.5));
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = session;
                    }
                }
            }
        }
        return nearest;
    }

    private void removeNearbyFurniture(World world, BedSessionRegistry.Session session, Player breaker) {
        BlockPos origin = BlockPos.of(session.originKey);
        BlockPos foot = BlockPos.of(session.footKey);
        BlockPos head = BlockPos.of(session.headKey);
        org.bukkit.entity.Entity bestEntity = null;
        double bestScore = Double.MAX_VALUE;

        for (org.bukkit.entity.Entity entity : world.getNearbyEntities(
                new Location(world, origin.x() + 0.5, origin.y() + 0.5, origin.z() + 0.5),
                3.0, 3.0, 3.0
        )) {
            var loaded = CraftEngineFurniture.getLoadedFurnitureByBaseEntity(entity);
            if (loaded == null) continue;
            if (!loaded.id().asString().equals(session.config.furnitureId)) continue;

            Location el = entity.getLocation();
            double toOrigin = el.distanceSquared(new Location(world, origin.x() + 0.5, origin.y() + 0.5, origin.z() + 0.5));
            double toFoot = el.distanceSquared(new Location(world, foot.x() + 0.5, foot.y() + 0.5, foot.z() + 0.5));
            double toHead = el.distanceSquared(new Location(world, head.x() + 0.5, head.y() + 0.5, head.z() + 0.5));
            double score = Math.min(toOrigin, Math.min(toFoot, toHead));

            if (score < bestScore) {
                bestScore = score;
                bestEntity = entity;
            }
        }

        if (bestEntity != null) {
            var loaded = CraftEngineFurniture.getLoadedFurnitureByBaseEntity(bestEntity);
            if (loaded != null && loaded.id().asString().equals(session.config.furnitureId)) {
                CraftEngineFurniture.remove(loaded, breaker, true, true);
            }
        }
    }

    private static boolean isNight(World world) {
        long time = world.getTime() % 24000L;
        return time >= 12541L && time <= 23458L;
    }

    private static Object getHandle(Object bukkitObject) {
        if (bukkitObject instanceof CraftWorld craftWorld) return craftWorld.getHandle();
        if (bukkitObject instanceof CraftPlayer craftPlayer) return craftPlayer.getHandle();
        return null;
    }
}

