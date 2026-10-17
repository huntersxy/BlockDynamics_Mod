package com.huntersxy.blockd.event;

import com.huntersxy.blockd.Config;
import com.huntersxy.blockd.blockd;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

import static com.huntersxy.blockd.blockd.LOGGER;

/**
 * 生物繁殖产生子代时触发：检查父代周围 17×17×17（±8 格）范围内的 Mob 数量，
 * 超过配置上限则阻止繁殖（幼体不生成，亲代进入 5 分钟冷却）。
 *
 * <p>注意：此事件只覆盖 Animal 路径（鸡牛羊马等）；村民繁殖走自己的 Brain 逻辑，
 * 不触发 BabyEntitySpawnEvent，因此不受本限制影响。
 */
@EventBusSubscriber(modid = blockd.MODID)
public class BreedingLimiter {
    private static final int MOB_CHECK_RANGE = 8;

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        // 获取父代实体
        Mob parentA = event.getParentA();
        if (parentA == null) {
            return;
        }

        BlockPos parentAPos = parentA.blockPosition();

        // 与冻结器保持一致，统计以父代所在方块为中心、半径 8 的完整方块范围（17×17×17）。
        BlockPos startPos = parentAPos.offset(-MOB_CHECK_RANGE, -MOB_CHECK_RANGE, -MOB_CHECK_RANGE);
        BlockPos endPos = parentAPos.offset(MOB_CHECK_RANGE, MOB_CHECK_RANGE, MOB_CHECK_RANGE);
        long mobCount = parentA.level()
                .getEntitiesOfClass(Mob.class, AABB.encapsulatingFullBlocks(startPos, endPos))
                .size();

        // 超过配置上限则取消繁殖
        if (mobCount > Config.maxMobsNearby) {
            event.setCanceled(true);
            // 大型养殖场会频繁触发，用 debug 级别避免刷屏
            LOGGER.debug("阻止繁殖：{} 附近 17x17x17 范围内 Mob 数量 {} 超过上限 {}",
                    parentAPos.toShortString(), mobCount, Config.maxMobsNearby);
        }
    }
}
