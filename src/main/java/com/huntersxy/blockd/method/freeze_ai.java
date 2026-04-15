package com.huntersxy.blockd.method;

import com.huntersxy.blockd.Imixin.ILivingEntity;
import com.huntersxy.blockd.blockd;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;

public class freeze_ai {
    private static final Set<Mob> frozenMobs = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (frozenMobs.isEmpty()) return;
        frozenMobs.removeIf(mob -> {
            if (!mob.isAlive() || !((ILivingEntity)mob).blockd$is_freeze_ai()) {
                if (mob.isNoAi()) {
                    mob.setNoAi(false);
                }
                return true;
            } else {
                mob.setDeltaMovement(0, 0, 0);
                mob.setTarget(null);
                mob.setNoAi(true);
                return false;
            }
        });
    }

    public static void addFrozenMob(Mob mob) {
        blockd.LOGGER.info("addFrozenMob called - mob: {}", mob.getName().getString());
        frozenMobs.add(mob);
    }

    public static void removeFrozenMob(Mob mob) {
        blockd.LOGGER.info("removeFrozenMob called - mob: {}", mob.getName().getString());
        frozenMobs.remove(mob);
        if (mob.isNoAi()) {
            mob.setNoAi(false);
        }
    }
}