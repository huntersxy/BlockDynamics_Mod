package com.huntersxy.blockd.gametest;

//? if >=1.21.11 {
/*import com.huntersxy.blockd.Config;
import com.huntersxy.blockd.item.Moditems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.AABB;

// 1.21.11+ 新 gametest 框架下的行为测试（注册见 GameTestRegistrarModern）。
public class BlockDynamicsGameTestsModern {
    // 按注册表 id 取实体类型：EntityType.COW/CHICKEN 常量在 26.2 移到了 EntityTypes 类，
    // 用 BuiltInRegistries 查询对所有 21.11+ 版本通用（避免引入嵌套的版本标记）。
    @SuppressWarnings("unchecked")
    private static <E extends Entity> EntityType<E> entityType(String id) {
        return (EntityType<E>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", id));
    }

    // 通电→冻结（NoAI + 完全静止）；断电→解冻（恢复重力下落）。
    public static void freezeOnPowerAndUnfreeze(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos power = freezer.offset(1, 0, 0);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(entityType("cow"), new BlockPos(4, 12, 6));
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

    // 两个冻结器状态互不干扰（旧实现用全局实例字段，此测试必挂）。
    public static void twoFreezersStayIndependent(GameTestHelper helper) {
        BlockPos freezerA = new BlockPos(4, 12, 4);
        BlockPos freezerB = new BlockPos(21, 12, 4);
        BlockPos powerA = freezerA.offset(1, 0, 0);
        BlockPos powerB = freezerB.offset(1, 0, 0);
        helper.setBlock(freezerA, Moditems.GIVETAG_BLOCK.get());
        helper.setBlock(freezerB, Moditems.GIVETAG_BLOCK.get());
        Cow cowA = helper.spawn(entityType("cow"), new BlockPos(4, 12, 6));
        Cow cowB = helper.spawn(entityType("cow"), new BlockPos(21, 12, 6));

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

    // 两台冻结器范围重叠、冻结同一生物：一台断电不应解冻另一台仍在冻结的生物（引用计数，
    // boolean 实现在此场景会错误解冻）。
    public static void overlappingFreezersRefCountFreeze(GameTestHelper helper) {
        BlockPos freezerA = new BlockPos(4, 12, 4);
        BlockPos freezerB = new BlockPos(8, 12, 8); // 与 A 相距 4 格，±8 范围重叠
        BlockPos powerA = freezerA.offset(1, 0, 0);
        BlockPos powerB = freezerB.offset(1, 0, 0);
        helper.setBlock(freezerA, Moditems.GIVETAG_BLOCK.get());
        helper.setBlock(freezerB, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(entityType("cow"), new BlockPos(6, 12, 6)); // 两台共同覆盖

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(powerA, Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(10, () -> helper.assertTrue(cow.isNoAi(), "A 通电后生物应被冻结"))
            // B 也通电：同一生物被两台冻结器共同冻结（计数 2）
            .thenExecute(() -> helper.setBlock(powerB, Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(10, () -> helper.assertTrue(cow.isNoAi(), "B 通电后生物仍应冻结"))
            // 关键场景：B 断电（A 仍通电）——boolean 实现会错误解冻共同范围内的生物
            .thenExecute(() -> helper.setBlock(powerB, Blocks.AIR))
            .thenExecuteAfter(10, () -> helper.assertTrue(cow.isNoAi(), "B 断电后 A 仍通电，生物不应解冻"))
            // A 也断电：最后一个引用释放，才真正解冻
            .thenExecute(() -> helper.setBlock(powerA, Blocks.AIR))
            .thenExecuteAfter(10, () -> helper.assertTrue(!cow.isNoAi(), "全部断电后生物应解除冻结"))
            .thenSucceed();
    }

    // 直接挖掉冻结器也应解冻范围内生物（旧实现无 onRemove 处理，必挂）。
    public static void breakingFreezerUnfreezesMobs(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos power = freezer.offset(1, 0, 0);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(entityType("cow"), new BlockPos(4, 12, 6));

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

    // 原生 NoAI 生物：解冻应恢复冻结前的 NoAI 状态，而不是无条件唤醒
    // （旧实现无条件 setNoAi(false)，会唤醒地图/刷怪笼的 NoAI 生物）。
    public static void nativeNoAiMobNotAwakenedByUnfreeze(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos power = freezer.offset(1, 0, 0);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(entityType("cow"), new BlockPos(4, 12, 6));

        helper.startSequence()
            // 模拟原生 NoAI 生物（地图/刷怪笼生成的生物）
            .thenExecute(() -> cow.setNoAi(true))
            .thenExecute(() -> helper.setBlock(power, Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(10, () -> helper.assertTrue(cow.isNoAi(), "冻结中应保持 NoAI"))
            .thenExecute(() -> helper.setBlock(power, Blocks.AIR))
            .thenExecuteAfter(10, () -> helper.assertTrue(cow.isNoAi(), "解冻不应唤醒原生 NoAI 生物"))
            .thenSucceed();
    }

    // 弱信号（红石粉）也能激活冻结器。
    public static void weakRedstoneSignalPowersFreezer(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos dust = freezer.offset(1, 0, 0);
        BlockPos stone = freezer.offset(1, 0, 1);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(entityType("cow"), new BlockPos(4, 12, 6));

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
                BlockState wireState = Blocks.REDSTONE_WIRE.defaultBlockState()
                        .setValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(Direction.WEST), RedstoneSide.UP)
                        .setValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(Direction.EAST), RedstoneSide.UP);
                helper.setBlock(dust, wireState);
            })
            // 粉线只对冻结器输出弱信号（强供电为 0），getBestNeighborSignal 应能识别
            .thenExecuteAfter(20, () -> helper.assertTrue(cow.isNoAi(), "弱信号也应激活冻结器"))
            .thenSucceed();
    }

    // 超过繁殖上限时阻止产仔；低于上限时正常产仔。
    public static void breedingLimitBlocksOverLimit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int oldMax = Config.maxMobsNearby;
        Config.maxMobsNearby = 1; // 测试内直接覆盖静态配置
        try {
            Chicken a = helper.spawn(entityType("chicken"), new BlockPos(5, 12, 5));
            Chicken b = helper.spawn(entityType("chicken"), new BlockPos(6, 12, 5));
            helper.spawn(entityType("cow"), new BlockPos(5, 12, 6)); // 第 3 只
            helper.spawn(entityType("cow"), new BlockPos(6, 12, 6)); // 第 4 只

            helper.startSequence()
                .thenExecute(() -> {
                    Config.maxMobsNearby = 1; // 必须在阶段内设置：方法体的赋值会被 finally 立即还原
                    AABB box = new AABB(a.blockPosition()).inflate(64);
                    long before = level.getEntitiesOfClass(Chicken.class, box).size();
                    a.spawnChildFromBreeding(level, b);
                    long after = level.getEntitiesOfClass(Chicken.class, box).size();
                    helper.assertTrue(before >= 2, "前置：父代应存在");
                    helper.assertTrue(after == before, "超过上限时繁殖应被阻止");
                })
                .thenExecute(() -> {
                    Config.maxMobsNearby = 100;
                    AABB box = new AABB(a.blockPosition()).inflate(64);
                    long before = level.getEntitiesOfClass(Chicken.class, box).size();
                    a.spawnChildFromBreeding(level, b);
                    long after = level.getEntitiesOfClass(Chicken.class, box).size();
                    helper.assertTrue(after == before + 1, "未超上限时繁殖应成功");
                })
                .thenSucceed();
        } finally {
            Config.maxMobsNearby = oldMax;
        }
    }

    // 配方合成测试：9 个鸡蛋应能按配方合成冻结器（验证配方 JSON 可解析且可匹配）。
    public static void recipeCraftsFreezer(GameTestHelper helper) {
        ItemStack result = RecipeCraftTest.craft(helper);
        helper.assertTrue(!result.isEmpty(), "冻结器配方应存在且能解析匹配");
        helper.assertTrue(result.is(Moditems.GIVETAG_BLOCK_ITEM.get()), "合成结果应为冻结器方块");
        helper.succeed();
    }

    // 解冻瞬间速度矢量应为 0：冻结期间每 tick 注入水平速度（模拟挤压/击退等外力），
    // 解冻后残留冲量不应被结算，实体只受重力正常下落。
    public static void unfreezeClearsVelocity(GameTestHelper helper) {
        BlockPos freezer = new BlockPos(4, 12, 4);
        BlockPos power = freezer.offset(1, 0, 0);
        helper.setBlock(freezer, Moditems.GIVETAG_BLOCK.get());
        Cow cow = helper.spawn(entityType("cow"), new BlockPos(4, 12, 6));
        final double[] x0 = new double[1];
        final double[] y0 = new double[1];

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(power, Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(10, () -> {
                helper.assertTrue(cow.isNoAi(), "前置：生物应先被冻结");
                x0[0] = cow.getX();
                y0[0] = cow.getY();
            })
            // 冻结期间每 tick 注入水平速度，模拟挤压/击退等外力（travel 应将其清零）
            .thenExecuteFor(20, () -> cow.setDeltaMovement(5, 0, 0))
            // 断电（解冻）：速度清零发生在方块 tick 阶段，早于下一次实体 travel
            .thenExecute(() -> helper.setBlock(power, Blocks.AIR))
            // 解冻后：不应有水平位移（残留冲量未被结算）
            .thenExecuteAfter(1, () -> {
                helper.assertTrue(!cow.isNoAi(), "断电后生物应解除冻结");
                helper.assertTrue(Math.abs(cow.getX() - x0[0]) < 0.01, "解冻瞬间不应有水平位移（速度矢量应保持为 0）");
            })
            .thenExecuteAfter(30, () -> helper.assertTrue(cow.getY() < y0[0] - 1.0, "解冻后生物应受重力下落"))
            .thenSucceed();
    }
}
 *///?}