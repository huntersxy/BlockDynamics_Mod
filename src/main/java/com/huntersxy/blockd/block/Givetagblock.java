package com.huntersxy.blockd.block;

import com.huntersxy.blockd.Config;
import com.huntersxy.blockd.Imixin.ILivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class Givetagblock extends Block {
    // 记录方块是否处于充能状态
    private boolean isPowered = false;

    public Givetagblock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        // 处理红石信号变化
        if (!level.isClientSide && level instanceof ServerLevel) {
            boolean currentlyPowered = level.hasNeighborSignal(pos);

            // 从充能到未充能时执行cleantag
            if (isPowered && !currentlyPowered) {
                executeCleantag(level, pos);
            }
            // 从未充能到充能时执行givetag
            else if (!isPowered && currentlyPowered) {
                executeGivetag(level, pos);
            }

            // 更新充能状态
            isPowered = currentlyPowered;
        }
    }

    /**
     * 在7x7范围内对所有实体执行givetag方法
     * @param level 当前世界
     * @param pos 方块位置
     */
    private void executeGivetag(Level level, BlockPos pos) {
        executeOperation(level, pos, this::givetag);
    }

    private void executeCleantag(Level level, BlockPos pos) {
        executeOperation(level, pos, this::cleantag);
    }

    /**
     * 在7x7范围内对所有实体执行指定操作
     * @param level 当前世界
     * @param pos 方块位置
     * @param operation 要执行的操作
     */
    private void executeOperation(Level level, BlockPos pos, java.util.function.Consumer<Entity> operation) {
        // 定义7x7x7的范围（以方块为中心）
        int range = Config.givetagBlockRange;
        BlockPos startPos = pos.offset(-range, -range, -range);
        BlockPos endPos = pos.offset(range, range, range);

        // 获取范围内的所有实体
        AABB boundingBox = new AABB(startPos, endPos);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);

        // 对每个实体执行指定操作
        for (Entity entity : entities) {
            if (!(entity instanceof Player && ((Player) entity).isCreative())) {
                operation.accept(entity);
            }
        }
    }

    /**
     * 给实体添加标签的方法
     * @param entity 目标实体
     */
    private void givetag(Entity entity) {
        // 检查是否为非玩家实体
        if (entity instanceof Mob mob) {
            //调用blockd$set_freeze_ai
            ((ILivingEntity)mob).blockd$set_freeze_ai(true);
        }
    }

    /**
     * 清除实体标签的方法
     * @param entity 目标实体
     */
    private void cleantag(Entity entity) {
        // 检查是否为非玩家实体
        if (entity instanceof Mob mob) {
            //重置实体运动
            entity.setDeltaMovement(0, 0, 0);
            ((ILivingEntity)mob).blockd$set_freeze_ai(false);
        }
    }
}
