package me.chyxelmc.ceaddon.config;

import net.momirealms.craftengine.core.world.Vec3i;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Addon-owned configuration for CraftEngine furniture beds.
 * CraftEngine keeps the model/config container; the addon reads this file for logic.
 */
public final class BedAddonConfig {
    public static final class BedDefinition {
        public final String furnitureId;
        public final List<Vec3i> slots;
        public final boolean requireNight;
        public final double monsterRadius;
        public final boolean hideBedBlocks;

        public BedDefinition(String furnitureId, List<Vec3i> slots, boolean requireNight, double monsterRadius, boolean hideBedBlocks) {
            this.furnitureId = furnitureId;
            this.slots = slots;
            this.requireNight = requireNight;
            this.monsterRadius = monsterRadius;
            this.hideBedBlocks = hideBedBlocks;
        }
    }

    private final Map<String, BedDefinition> beds;

    private BedAddonConfig(Map<String, BedDefinition> beds) {
        this.beds = beds;
    }

    public BedDefinition getByFurnitureId(String furnitureId) {
        return beds.get(furnitureId);
    }

    public Collection<BedDefinition> allBeds() {
        return Collections.unmodifiableCollection(beds.values());
    }

    public static BedAddonConfig load(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, BedDefinition> beds = new LinkedHashMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("beds");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection bed = section.getConfigurationSection(key);
                if (bed == null) continue;
                String furnitureId = bed.getString("furniture-id", key);
                List<Vec3i> slots = parseSlots(bed.getStringList("slots"));
                if (slots.isEmpty()) {
                    slots = List.of(new Vec3i(0, 0, 0), new Vec3i(1, 0, 0));
                }
                boolean requireNight = bed.getBoolean("require-night", true);
                double monsterRadius = bed.getDouble("monster-radius", 8.0);
                boolean hideBedBlocks = bed.getBoolean("hide-bed-blocks", true);
                beds.put(furnitureId, new BedDefinition(furnitureId, slots, requireNight, monsterRadius, hideBedBlocks));
            }
        }
        return new BedAddonConfig(beds);
    }

    private static List<Vec3i> parseSlots(List<String> raw) {
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        List<Vec3i> slots = new ArrayList<>();
        for (String value : raw) {
            String[] parts = value.trim().split(",");
            if (parts.length != 3) continue;
            try {
                slots.add(new Vec3i(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim())));
            } catch (NumberFormatException ignored) {
            }
        }
        return slots;
    }
}

