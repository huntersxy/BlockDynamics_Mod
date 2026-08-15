package com.huntersxy.blockd.gametest;

//? if <1.21.11 {

import com.huntersxy.blockd.Config;
import com.huntersxy.blockd.item.Moditems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;

/**
 * Block Dynamics 行为测试（gradlew gameTestServer 运行）。
 */
@GameTestHolder("blockd")
@PrefixGameTestTemplate(false)
public class BlockDynamicsGameTests {

    /** 通电→冻结（NoAI + 完全静止）；断电→解冻（恢复重力下落）。 */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void freezeOnPowerAndUnfreeze(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos power = freezer.offset(1, 0, 0);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 12, 6));
        final double[] yBeforeFreeze = new double[1];

        helper.startSequence()
            .thenExecuteAfter(10, () -> yBeforeFreeze[0] = cow.getY())
            .thenExecute(() -> helper.setBlock(power, Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(20, () -> {
                helper.assertTrue(cow.isNoAi(), "通电后生物应被冻结 (NoAI)");
                helper.assertTrue(Math.abs(cow.getY() - yBeforeFreeze[0]) < 0.5, "通电后生物不应移动");
            })
            .thenExecute(() -> helper.setBlock(power, Blocks.AIR))
            .thenExecuteAfter(30, () -> {
                helper.assertTrue(!cow.isNoAi(), "断电后生物应解除冻结");
                helper.assertTrue(cow.getY() < yBeforeFreeze[0] - 1.0, "断电后生物应受重力下落");
            })
            .thenSucceed();
    }

    /** 两个冻结器状态互不干扰（旧实现用全局实例字段，此测试必挂）。 */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void twoFreezersStayIndependent(GameTestHelper helper) {
        BlockPos freezerA = new BlockPos(4, 12, 4);
        BlockPos freezerB = new BlockPos(21, 12, 4);
        BlockPos powerA = freezerA.offset(1, 0, 0);
        BlockPos powerB = freezerB.offset(1, 0, 0);
        helper.setBlock(freezerA, Moditems.GIVETAG_BLOCK.get());
        helper.setBlock(freezerB, Moditems.GIVETAG_BLOCK.get());
        Cow cowA = helper.spawn(EntityType.COW, new BlockPos(4, 12, 6));
        Cow cowB = helper.spawn(EntityType.COW, new BlockPos(21, 12, 6));

        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(powerA, Blocks.REDSTONE_BLOCK);
                helper.setBlock(powerB, Blocks.REDSTONE_BLOCK);
            })
            .thenExecuteAfter(10, () -> {
                helper.assertTrue(cowA.isNoAi(), "A 应被冻结");
                helper.assertTrue(cowB.isNoAi(), "B 应被冻结");
            })
            // 关键场景：给 B 断电（A 仍通电）——旧代码会把全局 isPowered 置 false
            .thenExecute(() -> helper.setBlock(powerB, Blocks.AIR))
            .thenExecuteAfter(10, () -> {
                helper.assertTrue(cowA.isNoAi(), "A 不应受 B 断电影响");
                helper.assertTrue(!cowB.isNoAi(), "B 应解除冻结");
            })
            // 现在给 A 断电——旧代码因 isPowered 已被置 false 而不触发，A 会永久冻结
            .thenExecute(() -> helper.setBlock(powerA, Blocks.AIR))
            .thenExecuteAfter(10, () -> helper.assertTrue(!cowA.isNoAi(), "A 断电后应解除冻结"))
            .thenSucceed();
    }

    /** 直接挖掉冻结器也应解冻范围内生物（旧实现无 onRemove 处理，必挂）。 */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void breakingFreezerUnfreezesMobs(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos power = freezer.offset(1, 0, 0);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 12, 6));

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(power, Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(10, () -> helper.assertTrue(cow.isNoAi(), "前置：生物应先被冻结"))
            .thenExecute(() -> {
                helper.setBlock(freezer, Blocks.AIR);
                helper.setBlock(power, Blocks.AIR);
            })
            .thenExecuteAfter(10, () -> helper.assertTrue(!cow.isNoAi(), "拆掉冻结器后生物应解除冻结"))
            .thenSucceed();
    }

    /** 弱信号（红石粉）也能激活冻结器。 */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void weakRedstoneSignalPowersFreezer(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos dust = freezer.offset(1, 0, 0);
        BlockPos stone = freezer.offset(1, 0, 1);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 12, 6));

        helper.startSequence()
            // 石头 + 通电拉杆（FACE=FLOOR 附着在石头上，强充能石头）
            .thenExecute(() -> helper.setBlock(stone, Blocks.STONE))
            .thenExecute(() -> helper.setBlock(stone.above(), Blocks.LEVER.defaultBlockState()
                    .setValue(LeverBlock.POWERED, true)
                    .setValue(LeverBlock.FACE, AttachFace.FLOOR)))
            // 粉线下方必须有支撑方块，否则 updateShape 会把它替换成空气
            .thenExecute(() -> helper.setBlock(dust.below(), Blocks.STONE))
            // 注：helper.setBlock 直接放置默认状态，会绕过 getStateForPlacement 的连接计算
            // （玩家正常放置时 east/west 连接会自动算好）。这里显式构造连接状态，
            // 等价于玩家放置的效果：西侧连上冻结器（full 方块），东侧连上石头。
            .thenExecute(() -> {
                net.minecraft.world.level.block.state.BlockState wireState = Blocks.REDSTONE_WIRE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(net.minecraft.core.Direction.WEST),
                                net.minecraft.world.level.block.state.properties.RedstoneSide.UP)
                        .setValue(net.minecraft.world.level.block.RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(net.minecraft.core.Direction.EAST),
                                net.minecraft.world.level.block.state.properties.RedstoneSide.UP);
                helper.setBlock(dust, wireState);
            })
            // 粉线只对冻结器输出弱信号（强供电为 0），getBestNeighborSignal 应能识别
            .thenExecuteAfter(20, () -> helper.assertTrue(cow.isNoAi(), "弱信号也应激活冻结器"))
            .thenSucceed();
    }

    /** 超过繁殖上限时阻止产仔；低于上限时正常产仔。 */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void breedingLimitBlocksOverLimit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int oldMax = Config.maxMobsInChunk;
        Config.maxMobsInChunk = 1; // 测试内直接覆盖静态配置
        try {
            Chicken a = helper.spawn(EntityType.CHICKEN, new BlockPos(5, 12, 5));
            Chicken b = helper.spawn(EntityType.CHICKEN, new BlockPos(6, 12, 5));
            helper.spawn(EntityType.COW, new BlockPos(5, 12, 6)); // 第 3 只
            helper.spawn(EntityType.COW, new BlockPos(6, 12, 6)); // 第 4 只

            helper.startSequence()
                .thenExecute(() -> {
                    Config.maxMobsInChunk = 1; // 必须在阶段内设置：方法体的赋值会被 finally 立即还原
                    AABB box = new AABB(a.blockPosition()).inflate(64);
                    long before = level.getEntitiesOfClass(Chicken.class, box).size();
                    a.spawnChildFromBreeding(level, b);
                    long after = level.getEntitiesOfClass(Chicken.class, box).size();
                    helper.assertTrue(before >= 2, "前置：父代应存在");
                    helper.assertTrue(after == before, "超过上限时繁殖应被阻止");
                })
                .thenExecute(() -> {
                    Config.maxMobsInChunk = 100;
                    AABB box = new AABB(a.blockPosition()).inflate(64);
                    long before = level.getEntitiesOfClass(Chicken.class, box).size();
                    a.spawnChildFromBreeding(level, b);
                    long after = level.getEntitiesOfClass(Chicken.class, box).size();
                    helper.assertTrue(after == before + 1, "未超上限时繁殖应成功");
                })
                .thenSucceed();
        } finally {
            Config.maxMobsInChunk = oldMax;
        }
    }
}
//?}