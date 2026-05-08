package me.chyxelmc.ceaddon.behavior;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.WorldAccessor;

import java.util.concurrent.Callable;

/**
 * Legacy compatibility stub. The addon no longer uses CraftEngine custom block behaviors.
 */
public class BedBlockBehavior extends BlockBehavior {

    public BedBlockBehavior(CustomBlock customBlock, BedConfig config) {
        super(customBlock);
    }

    @Override
    public boolean canPlaceMultiState(WorldAccessor world, BlockPos pos, ImmutableBlockState state) {
        return true;
    }

    @Override
    public void placeMultiState(Object ctxObj, Object[] args, Callable<Object> callable) {
    }

    @Override
    public void onRemove(Object obj, Object[] args, Callable<Object> callable) {
    }
}
