package com.huntersxy.blockd.method;

import com.huntersxy.blockd.Config;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;

import static com.huntersxy.blockd.blockd.LOGGER;

/**
 * Forge事件总线订阅器注解，用于注册BlockD模组的事件监听器
 * 该注解将目标类标记为Forge事件总线的订阅者，使其能够接收和处理游戏事件。
 */
@Mod.EventBusSubscriber(modid = "blockd", bus = Mod.EventBusSubscriber.Bus.FORGE)

public class limit_breeding {
       /**
     * 当生物繁殖产生子代时触发的事件处理函数
     * 该函数会检查父代所在区块内的生物数量，如果超过配置的最大值则阻止繁殖
     *
     * @param event BabyEntitySpawnEvent事件对象，包含父代和子代的信息
     */
    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        // 获取父代实体
        Mob parentA = event.getParentA();
        // 获取父代所在区块
        BlockPos parentAPos = parentA.blockPosition();

        // 检查父代周围8x8x8范围内的生物实体数量
        long parentAEntityCount = parentA.level()
                .getEntitiesOfClass(Mob.class,
                        new AABB(parentAPos.getX() - 8, parentAPos.getY() - 8, parentAPos.getZ() - 8,
                                parentAPos.getX() + 8, parentAPos.getY() + 8, parentAPos.getZ() + 8))
                .size();

        // 如果父代区块内 mob 实体数量大于maxMobsInChunk，则取消繁殖
        if (parentAEntityCount > Config.maxMobsInChunk) {
            event.setCanceled(true);
            LOGGER.info("阻止了一次繁殖");
        }
    }


}
