package com.huntersxy.blockd.method;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.huntersxy.blockd.method.freeze_ai.Mod_id;

@Mod.EventBusSubscriber(modid = Mod_id, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class freeze_ai {

    static final String Mod_id = "blockd";

    @SubscribeEvent
    public static void onEntityUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        if (entity instanceof Mob mob && entity.getTags().contains("test")) {
            // 停止所有运动
            mob.goalSelector.tickRunningGoals(false);
            // 停止所有动作
            event.setCanceled(true);
        }
    }
}