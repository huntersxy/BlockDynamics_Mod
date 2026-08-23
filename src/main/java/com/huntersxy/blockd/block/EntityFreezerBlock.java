package com.huntersxy.blockd.block;

import com.huntersxy.blockd.Config;
import com.huntersxy.blockd.duck.ILivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
//? if >=1.21.11 {
/*import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.redstone.Orientation;
 *///?}
import java.util.List;
import java.util.function.Consumer;

/**
 * 实体冻结器：红石通电时冻结范围内生物，断电时解除。
 *
 * <p>实现要点：
 * <ul>
 *   <li>充能状态存入 BlockState 的 POWERED 属性（按方块位置持久化到区块），
 *       不用 Block 实例字段（方块是全局单例，实例字段会被所有同名方块共享）；</li>
 *   <li>冻结用引用计数（见 MixinLivingEntity）：范围重叠的多台冻结器各持一个引用，
 *       全部断电才解冻，不会互相误伤；</li>
 *   <li>用 getBestNeighborSignal 检测弱信号（红石粉也能激活）；</li>
 *   <li>onPlace 处理"贴着已通电位置放置"的边沿；</li>
 *   <li>onRemove 在方块被破坏/替换时解冻范围内生物，避免永久冻结。</li>
 * </ul>
 */
public class EntityFreezerBlock extends Block {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public EntityFreezerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // 放置方块本身不会触发 neighborChanged，这里补一次对拍（贴着已通电位置放置也能生效）
        //? if <1.21.11 {
        if (!level.isClientSide && !oldState.is(this)) {
        //?} else {
        /*if (!level.isClientSide() && !oldState.is(this)) {
         *///?}
            reconcilePower(level, pos, state);
        }
    }

    //? if <1.21.11 {
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            reconcilePower(level, pos, state);
        }
    }
    //?}
    //? if >=1.21.11 {
    /*@Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide()) {
            reconcilePower(level, pos, state);
        }
    }
    *///?}

    //? if <1.21.11 {
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 方块被破坏/替换时解冻范围内的实体，避免"拆除冻结器后生物永久冻结"
        if (!level.isClientSide && state.getValue(POWERED) && !newState.is(this)) {
            unfreezeAll(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    //?}
    //? if >=1.21.11 {
    /*@Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        // 1.21.11+ 用 affectNeighborsAfterRemoval 取代 onRemove（仅在方块被其他方块替换时触发）
        if (state.getValue(POWERED)) {
            unfreezeAll(level, pos);
        }
    }
    *///?}

    /**
     * 将实际红石信号与方块状态对拍，仅在边沿变化时执行冻结/解冻。
     * POWERED 状态随区块保存，区块重载后状态一致。
     */
    private void reconcilePower(Level level, BlockPos pos, BlockState state) {
        boolean currentlyPowered = level.getBestNeighborSignal(pos) > 0;
        boolean wasPowered = state.getValue(POWERED);
        if (currentlyPowered && !wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, true), 3);
            freezeAll(level, pos);
        } else if (!currentlyPowered && wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            unfreezeAll(level, pos);
        }
    }

    private void freezeAll(Level level, BlockPos pos) {
        forEachMobInRange(level, pos, this::freeze);
    }

    private void unfreezeAll(Level level, BlockPos pos) {
        forEachMobInRange(level, pos, this::unfreeze);
    }

    /**
     * 在 (2*range+1)^3 范围内对所有 Mob 执行指定操作。
     */
    private void forEachMobInRange(Level level, BlockPos pos, Consumer<Mob> operation) {
        int range = Config.givetagBlockRange;
        BlockPos startPos = pos.offset(-range, -range, -range);
        BlockPos endPos = pos.offset(range, range, range);
        AABB boundingBox = AABB.encapsulatingFullBlocks(startPos, endPos);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, boundingBox)) {
            operation.accept(mob);
        }
    }

    private void freeze(Mob mob) {
        ((ILivingEntity) mob).blockd$acquireFreeze();
    }

    private void unfreeze(Mob mob) {
        ((ILivingEntity) mob).blockd$releaseFreeze();
    }

    @Override
    public @Nonnull List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
        // 本方块无 loot table，固定掉落自身（不再调用 super 取回会被丢弃的空结果）
        //? if <26.1 {
        return List.of(new ItemStack(this));
        //?} else {
        /*return List.of(new ItemStack(this.asItem()));
         *///?}
    }
}
