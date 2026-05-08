package me.chyxelmc.ceaddon.listener;

import me.chyxelmc.ceaddon.nms.NmsSleepAdapter;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Handles player sleep-related events to ensure proper wakeup and time progression.
 * Fixes issues where players get stuck sleeping.
 */
public final class PlayerSleepListener implements Listener {

    public PlayerSleepListener() {
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        // Do nothing here.
        // Calling stopSleepInBed from PlayerBedLeaveEvent causes recursive leave event loops.
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Ensure player is properly awake after respawn
        Player player = event.getPlayer();

        try {
            Object serverPlayer = getHandle(player);
            if (serverPlayer != null) {
                NmsSleepAdapter.tryWakeupPlayer(serverPlayer);
            }
        } catch (Throwable ignored) {
            // Safe to ignore
        }
    }

    private static Object getHandle(Object bukkitObject) {
        if (bukkitObject instanceof CraftPlayer craftPlayer) return craftPlayer.getHandle();
        return null;
    }
}


