package com.huntersxy.blockd.method;

import com.huntersxy.blockd.Config;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import static com.huntersxy.blockd.blockd.LOGGER;

public class limit_breeding {
    /**
     * 生物繁殖产生子代时触发：检查父代周围 17×17×17（±8 格）范围内的 Mob 数量，
     * 超过配置上限则阻止繁殖（幼体不生成，亲代进入 5 分钟冷却）。
     *
     * <p>注意：此事件只覆盖 Animal 路径（鸡牛羊马等）；村民繁殖走自己的 Brain 逻辑，
     * 不触发 BabyEntitySpawnEvent，因此不受本限制影响。
     */
    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        // 获取父代实体
        Mob parentA = event.getParentA();
        if (parentA == null) {
            return;
        }

        BlockPos parentAPos = parentA.blockPosition();

        // 统计父代周围 17x17x17 范围内的生物实体数量
        long mobCount = parentA.level()
                .getEntitiesOfClass(Mob.class,
                        new AABB(parentAPos.getX() - 8, parentAPos.getY() - 8, parentAPos.getZ() - 8,
                                parentAPos.getX() + 8, parentAPos.getY() + 8, parentAPos.getZ() + 8))
                .size();

        // 超过配置上限则取消繁殖
        if (mobCount > Config.maxMobsInChunk) {
            event.setCanceled(true);
            LOGGER.info("阻止了一次繁殖");
        }
    }
}