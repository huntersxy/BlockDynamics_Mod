package com.huntersxy.blockd.block;

import com.huntersxy.blockd.Config;
import com.huntersxy.blockd.Imixin.ILivingEntity;
import com.huntersxy.blockd.method.freeze_ai;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import javax.annotation.Nonnull;

public class Givetagblock extends Block {
    private boolean isPowered = false;

    public Givetagblock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        if (!level.isClientSide() && level instanceof ServerLevel) {
            boolean currentlyPowered = level.hasNeighborSignal(pos);

            if (isPowered && !currentlyPowered) {
                executeCleantag(level, pos);
            } else if (!isPowered && currentlyPowered) {
                executeGivetag(level, pos);
            }

            isPowered = currentlyPowered;
        }
    }

    private void executeGivetag(Level level, BlockPos pos) {
        executeOperation(level, pos, this::givetag);
    }

    private void executeCleantag(Level level, BlockPos pos) {
        executeOperation(level, pos, this::cleantag);
    }

    private void executeOperation(Level level, BlockPos pos, java.util.function.Consumer<Entity> operation) {
        int range = Config.givetagBlockRange;
        BlockPos startPos = pos.offset(-range, -range, -range);
        BlockPos endPos = pos.offset(range, range, range);

        AABB boundingBox = AABB.encapsulatingFullBlocks(startPos, endPos);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);

        for (Entity entity : entities) {
            if (!(entity instanceof Player && ((Player) entity).isCreative())) {
                operation.accept(entity);
            }
        }
    }

    private void givetag(Entity entity) {
        if (entity instanceof Mob mob) {
            entity.setDeltaMovement(0, 0, 0);
            mob.setTarget(null);
            ((ILivingEntity)mob).blockd$set_freeze_ai(true);
            freeze_ai.addFrozenMob(mob);
        }
    }

    private void cleantag(Entity entity) {
        if (entity instanceof Mob mob) {
            entity.setDeltaMovement(0, 0, 0);
            freeze_ai.removeFrozenMob(mob);
            ((ILivingEntity)mob).blockd$set_freeze_ai(false);
        }
    }

    @Override
    public @Nonnull List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);

        drops.clear();
        drops.add(new ItemStack(this));

        return drops;
    }
}