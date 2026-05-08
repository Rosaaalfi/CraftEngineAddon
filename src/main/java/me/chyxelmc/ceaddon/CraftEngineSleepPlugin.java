package me.chyxelmc.ceaddon;

import me.chyxelmc.ceaddon.config.BedAddonConfig;
import me.chyxelmc.ceaddon.behavior.BedSessionRegistry;
import me.chyxelmc.ceaddon.listener.BedInteractListener;
import me.chyxelmc.ceaddon.listener.PlayerSleepListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class CraftEngineSleepPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {
    private BedAddonConfig bedAddonConfig;
    private BedInteractListener bedInteractListener;

    @Override
    public void onEnable() {
        saveDefaultBedAddonConfig();
        this.bedAddonConfig = BedAddonConfig.load(new File(getDataFolder(), "bed-addon.yml"));
        this.bedInteractListener = new BedInteractListener(this, bedAddonConfig);
        getServer().getPluginManager().registerEvents(bedInteractListener, this);
        getServer().getPluginManager().registerEvents(new PlayerSleepListener(), this);

        // Register this class as executor for different command paths
        // CraftEngine commands might be handled differently - this handles addon-specific reloads
        registerCommandExecutor("ce", this);
        registerCommandExecutor("craftengine", this);

        getLogger().info("Registered CraftEngine furniture bed addon listener");
    }

    @Override
    public void onDisable() {
        BedSessionRegistry.clear();
        if (bedInteractListener != null) {
            bedInteractListener.clearTracking();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (isReloadCommand(command.getName(), args)) {
            reloadConfig(sender);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (isReloadCommand(command.getName(), args) || (args.length == 1 && "reload".equalsIgnoreCase(args[0]))) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("config");
            completions.add("all");
            return completions;
        }
        return new ArrayList<>();
    }

    private void reloadConfig(CommandSender sender) {
        try {
            this.bedAddonConfig = BedAddonConfig.load(new File(getDataFolder(), "bed-addon.yml"));
            if (bedInteractListener != null) {
                bedInteractListener.updateConfig(bedAddonConfig);
            }
            sender.sendMessage("§a[CraftEngineSleep] Reloaded bed-addon.yml configuration");
            getLogger().info("Reloaded bed-addon.yml configuration");
        } catch (Exception ex) {
            sender.sendMessage("§c[CraftEngineSleep] Failed to reload configuration: " + ex.getMessage());
            getLogger().warning("Failed to reload configuration: " + ex.getMessage());
        }
    }

    private boolean isReloadCommand(String cmdName, String[] args) {
        // Check if this is a reload-type command
        if (args.length == 0) {
            return "reload".equalsIgnoreCase(cmdName) || "ce".equalsIgnoreCase(cmdName);
        }
        if (args.length >= 1) {
            String first = args[0];
            if ("reload".equalsIgnoreCase(first) ||
                "config".equalsIgnoreCase(first) ||
                "all".equalsIgnoreCase(first)) {

                if (args.length == 1) return true;
                if (args.length == 2 && "reload".equalsIgnoreCase(first) &&
                    ("all".equalsIgnoreCase(args[1]) || "config".equalsIgnoreCase(args[1]))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void registerCommandExecutor(String cmd, CommandExecutor executor) {
        if (getCommand(cmd) != null) {
            getCommand(cmd).setExecutor(executor);
        }
    }

    private void saveDefaultBedAddonConfig() {
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "bed-addon.yml");
        if (!file.exists()) {
            saveResource("bed-addon.yml", false);
        }
    }


    public BedAddonConfig getBedAddonConfig() {
        return bedAddonConfig;
    }
}