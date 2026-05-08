package me.chyxelmc.ceaddon.persistence;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BedDatabase {
    public static final class Entry {
        public final String furnitureId;
        public final String worldName;
        public final int x;
        public final int y;
        public final int z;
        public final float yaw;
        public final float pitch;
        public final String facing;
        public final int footX;
        public final int footY;
        public final int footZ;
        public final int headX;
        public final int headY;
        public final int headZ;

        public Entry(String furnitureId, String worldName, int x, int y, int z, float yaw, float pitch,
                     String facing, int footX, int footY, int footZ, int headX, int headY, int headZ) {
            this.furnitureId = furnitureId;
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.facing = facing;
            this.footX = footX;
            this.footY = footY;
            this.footZ = footZ;
            this.headX = headX;
            this.headY = headY;
            this.headZ = headZ;
        }

        public boolean sameBlock(Location location) {
            World world = location.getWorld();
            if (world == null) return false;
            return world.getName().equals(worldName)
                    && location.getBlockX() == x
                    && location.getBlockY() == y
                    && location.getBlockZ() == z;
        }
    }

    private final File file;
    private final Map<String, List<Entry>> entriesByFurniture = new LinkedHashMap<>();

    public BedDatabase(File file) {
        this.file = file;
    }

    public void load() {
        entriesByFurniture.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String furnitureId : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(furnitureId);
            if (section == null) continue;
            List<Map<?, ?>> locations = section.getMapList("locations");
            List<Entry> list = new ArrayList<>();
            for (Map<?, ?> raw : locations) {
                Object worldRaw = raw.containsKey("world") ? raw.get("world") : "";
                String world = String.valueOf(worldRaw);
                int x = toInt(raw.get("x"));
                int y = toInt(raw.get("y"));
                int z = toInt(raw.get("z"));
                float yaw = toFloat(raw.get("yaw"));
                float pitch = toFloat(raw.get("pitch"));
                String facing = String.valueOf(raw.containsKey("facing") ? raw.get("facing") : "");
                int footX = toInt(raw.containsKey("footX") ? raw.get("footX") : x);
                int footY = toInt(raw.containsKey("footY") ? raw.get("footY") : y);
                int footZ = toInt(raw.containsKey("footZ") ? raw.get("footZ") : z);
                int headX = toInt(raw.containsKey("headX") ? raw.get("headX") : x);
                int headY = toInt(raw.containsKey("headY") ? raw.get("headY") : y);
                int headZ = toInt(raw.containsKey("headZ") ? raw.get("headZ") : z);
                if (!world.isEmpty()) {
                    list.add(new Entry(furnitureId, world, x, y, z, yaw, pitch, facing, footX, footY, footZ, headX, headY, headZ));
                }
            }
            if (!list.isEmpty()) {
                entriesByFurniture.put(furnitureId, list);
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, List<Entry>> e : entriesByFurniture.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Entry entry : e.getValue()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("x", entry.x);
                m.put("y", entry.y);
                m.put("z", entry.z);
                m.put("world", entry.worldName);
                m.put("yaw", entry.yaw);
                m.put("pitch", entry.pitch);
                m.put("facing", entry.facing);
                m.put("footX", entry.footX);
                m.put("footY", entry.footY);
                m.put("footZ", entry.footZ);
                m.put("headX", entry.headX);
                m.put("headY", entry.headY);
                m.put("headZ", entry.headZ);
                list.add(m);
            }
            yaml.set(e.getKey() + ".locations", list);
        }
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            yaml.save(file);
        } catch (IOException ignored) {
        }
    }

    public Collection<Entry> all() {
        List<Entry> all = new ArrayList<>();
        for (List<Entry> entries : entriesByFurniture.values()) {
            all.addAll(entries);
        }
        return Collections.unmodifiableList(all);
    }

    public List<Entry> byFurnitureId(String furnitureId) {
        return entriesByFurniture.getOrDefault(furnitureId, Collections.emptyList());
    }

    public void upsert(String furnitureId, Location location) {
        upsert(furnitureId, location, "", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public void upsert(String furnitureId, Location location, String facing,
                       int footX, int footY, int footZ,
                       int headX, int headY, int headZ) {
        World world = location.getWorld();
        if (world == null) return;
        List<Entry> list = new ArrayList<>(entriesByFurniture.getOrDefault(furnitureId, new ArrayList<>()));
        list.removeIf(entry -> entry.sameBlock(location));
        list.add(new Entry(
                furnitureId,
                world.getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getYaw(),
                location.getPitch(),
                facing,
                footX, footY, footZ,
                headX, headY, headZ
        ));
        entriesByFurniture.put(furnitureId, list);
    }

    public void removeAt(String furnitureId, Location location) {
        List<Entry> list = new ArrayList<>(entriesByFurniture.getOrDefault(furnitureId, new ArrayList<>()));
        list.removeIf(entry -> entry.sameBlock(location));
        if (list.isEmpty()) {
            entriesByFurniture.remove(furnitureId);
        } else {
            entriesByFurniture.put(furnitureId, list);
        }
    }

    public List<Entry> nearChunk(String worldName, int chunkX, int chunkZ, int radius) {
        List<Entry> out = new ArrayList<>();
        for (List<Entry> entries : entriesByFurniture.values()) {
            for (Entry entry : entries) {
                if (!entry.worldName.equals(worldName)) continue;
                int ex = entry.x >> 4;
                int ez = entry.z >> 4;
                if (Math.abs(ex - chunkX) <= radius && Math.abs(ez - chunkZ) <= radius) {
                    out.add(entry);
                }
            }
        }
        return out;
    }

    private static int toInt(Object value) {
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static float toFloat(Object value) {
        if (value instanceof Number n) return n.floatValue();
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception ignored) {
            return 0.0f;
        }
    }
}
