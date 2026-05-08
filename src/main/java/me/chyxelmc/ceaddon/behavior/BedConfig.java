package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.world.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration holder for bed behavior parsed from CraftEngine behavior map.
 */
public class BedConfig {
    public final List<Vec3i> slots;
    public final boolean requireNight;
    public final double monsterRadius;
    public final double sleepPercentage;

    public BedConfig(List<Vec3i> slots, boolean requireNight, double monsterRadius, double sleepPercentage) {
        this.slots = slots;
        this.requireNight = requireNight;
        this.monsterRadius = monsterRadius;
        this.sleepPercentage = sleepPercentage;
    }

    @SuppressWarnings("unchecked")
    public static BedConfig fromMap(Map<String, Object> map) {
        if (map == null) map = Map.of();

        List<Vec3i> slots = new ArrayList<>();
        Object slotsObj = map.get("slots");
        if (slotsObj instanceof Iterable<?>) {
            for (Object o : (Iterable<Object>) slotsObj) {
                if (o instanceof String) {
                    String[] parts = ((String) o).trim().split(",");
                    if (parts.length == 3) {
                        try {
                            int x = Integer.parseInt(parts[0].trim());
                            int y = Integer.parseInt(parts[1].trim());
                            int z = Integer.parseInt(parts[2].trim());
                            slots.add(new Vec3i(x, y, z));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        boolean requireNight = false;
        Object rn = map.get("require-night");
        if (rn instanceof Boolean) requireNight = (Boolean) rn;

        double monsterRadius = 8.0;
        Object mr = map.get("monster-radius");
        if (mr instanceof Number) monsterRadius = ((Number) mr).doubleValue();

        double sleepPercentage = 0.5;
        Object sp = map.get("sleep-percentage");
        if (sp instanceof Number) sleepPercentage = ((Number) sp).doubleValue();

        return new BedConfig(slots, requireNight, monsterRadius, sleepPercentage);
    }
}

