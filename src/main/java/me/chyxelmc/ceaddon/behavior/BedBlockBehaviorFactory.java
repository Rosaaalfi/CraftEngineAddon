package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;

import java.util.Map;

public class BedBlockBehaviorFactory implements BlockBehaviorFactory<BedBlockBehavior> {
    @Override
    public BedBlockBehavior create(CustomBlock customBlock, Map<String, Object> map) {
        BedConfig config = BedConfig.fromMap(map);
        return new BedBlockBehavior(customBlock, config);
    }
}

