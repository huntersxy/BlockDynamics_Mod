package com.huntersxy.blockd.gametest;

import com.huntersxy.blockd.blockd;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = blockd.MODID, bus = EventBusSubscriber.Bus.MOD)
public class GameTestRegistrar {
    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        event.register(BlockDynamicsGameTests.class);
    }
}