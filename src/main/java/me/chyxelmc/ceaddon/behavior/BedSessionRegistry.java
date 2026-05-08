package me.chyxelmc.ceaddon.behavior;

import me.chyxelmc.ceaddon.config.BedAddonConfig;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.block.BlockState;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks active injected bed placements per world+block position.
 * Stores original Bukkit block states so restore is independent from CraftEngine internals.
 */
public final class BedSessionRegistry {
    public static final class Session {
        public final UUID worldId;
        public final long originKey;
        public final long footKey;
        public final long headKey;
        public final BedAddonConfig.BedDefinition config;
        public final String facing;
        public BlockState originalFoot;
        public BlockState originalHead;

        public Session(UUID worldId, long originKey, long footKey, long headKey, BedAddonConfig.BedDefinition config, String facing) {
            this.worldId = worldId;
            this.originKey = originKey;
            this.footKey = footKey;
            this.headKey = headKey;
            this.config = config;
            this.facing = facing;
        }
    }

    private static final Map<String, Session> BY_BLOCK = new ConcurrentHashMap<>();
    private static final Map<String, List<Session>> BY_CHUNK = new ConcurrentHashMap<>();

    private BedSessionRegistry() {}

    private static String key(UUID worldId, long posKey) {
        return worldId + ":" + posKey;
    }

    public static void register(UUID worldId, long originKey, long footKey, long headKey, BedAddonConfig.BedDefinition config, String facing) {
        Session session = new Session(worldId, originKey, footKey, headKey, config, facing);
        BY_BLOCK.put(key(worldId, originKey), session);
        BY_BLOCK.put(key(worldId, footKey), session);
        BY_BLOCK.put(key(worldId, headKey), session);
        BlockPos footPos = BlockPos.of(footKey);
        String chunkKey = worldId + ":" + (footPos.x() >> 4) + ":" + (footPos.z() >> 4);
        BY_CHUNK.compute(chunkKey, (k, existing) -> {
            List<Session> list = existing == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(existing);
            list.add(session);
            return list;
        });
    }

    public static Session get(UUID worldId, long posKey) {
        return BY_BLOCK.get(key(worldId, posKey));
    }

    public static Collection<Session> sessions() {
        return new LinkedHashSet<>(BY_BLOCK.values());
    }

    public static Collection<Session> sessionsNearChunk(UUID worldId, int chunkX, int chunkZ, int radius) {
        return BY_CHUNK.entrySet().stream()
                .filter(e -> e.getKey().startsWith(worldId + ":"))
                .flatMap(e -> e.getValue().stream())
                .filter(s -> {
                    BlockPos foot = BlockPos.of(s.footKey);
                    int sx = foot.x() >> 4;
                    int sz = foot.z() >> 4;
                    return Math.abs(sx - chunkX) <= radius && Math.abs(sz - chunkZ) <= radius;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void remove(UUID worldId, long originKey) {
        Session session = BY_BLOCK.remove(key(worldId, originKey));
        if (session == null) return;
        BlockPos footPos = BlockPos.of(session.footKey);
        String chunkKey = worldId + ":" + (footPos.x() >> 4) + ":" + (footPos.z() >> 4);
        BY_CHUNK.computeIfPresent(chunkKey, (k, list) -> {
            List<Session> copy = new java.util.ArrayList<>(list);
            copy.remove(session);
            return copy.isEmpty() ? null : copy;
        });
        BY_BLOCK.remove(key(worldId, session.footKey));
        BY_BLOCK.remove(key(worldId, session.headKey));
        BY_BLOCK.remove(key(worldId, session.originKey));
    }

    public static void clear() {
        BY_BLOCK.clear();
        BY_CHUNK.clear();
    }
}


