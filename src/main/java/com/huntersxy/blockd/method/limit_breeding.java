package com.huntersxy.blockd.method;

import com.huntersxy.blockd.Config;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import static com.huntersxy.blockd.blockd.LOGGER;

public class limit_breeding {
    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        Mob parentA = event.getParentA();
        BlockPos parentAPos = parentA.blockPosition();

        long parentAEntityCount = parentA.level()
                .getEntitiesOfClass(Mob.class,
                        new AABB(parentAPos.getX() - 8, parentAPos.getY() - 8, parentAPos.getZ() - 8,
                                parentAPos.getX() + 8, parentAPos.getY() + 8, parentAPos.getZ() + 8))
                .size();

        if (parentAEntityCount > Config.maxMobsInChunk) {
            event.setCanceled(true);
            LOGGER.info("阻止了一次繁殖");
        }
    }
}