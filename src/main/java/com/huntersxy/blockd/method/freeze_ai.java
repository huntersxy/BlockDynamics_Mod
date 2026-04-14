package com.huntersxy.blockd.method;

import com.huntersxy.blockd.Imixin.ILivingEntity;
import com.huntersxy.blockd.blockd;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;

public class freeze_ai {
    // 存储被冻结的实体
    private static final Set<Mob> frozenMobs = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (frozenMobs.isEmpty()) return;
        // 遍历被冻结的实体
        frozenMobs.removeIf(mob -> {
            if (!mob.isAlive() || !((ILivingEntity)mob).blockd$is_freeze_ai()) {
                // 实体已死亡或不再被冻结，恢复AI并从集合中移除
                if (mob.isNoAi()) {
                    mob.setNoAi(false);
                }
                return true;
            } else {
                // 停止所有运动
                mob.setDeltaMovement(0, 0, 0);
                // 停止所有AI目标
                mob.setTarget(null);
                // 暂停AI更新
                mob.setNoAi(true);
                return false;
            }
        });
    }

    // 添加实体到冻结集合
    public static void addFrozenMob(Mob mob) {
        blockd.LOGGER.info("addFrozenMob called - mob: {}", mob.getName().getString());
        frozenMobs.add(mob);
    }

    // 从冻结集合中移除实体
    public static void removeFrozenMob(Mob mob) {
        blockd.LOGGER.info("removeFrozenMob called - mob: {}", mob.getName().getString());
        frozenMobs.remove(mob);
        // 立即恢复AI
        if (mob.isNoAi()) {
            mob.setNoAi(false);
        }
    }
}