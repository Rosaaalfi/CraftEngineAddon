package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory to create BedBehavior instances for CraftEngine furniture.
 * Parses configuration map (slots, require-night, monster-radius, sleep-percentage).
 */
public class BedBehaviorFactory implements FurnitureBehaviorFactory<BedBehavior> {

    @Override
    public BedBehavior create(CustomFurniture furniture, Map<String, Object> map) {
        BedConfig config = BedConfig.fromMap(map);
        return new BedBehavior(furniture, config);
    }
}



