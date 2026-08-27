package com.huntersxy.blockd;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = blockd.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MAX_MOBS_NEARBY = BUILDER
            .comment("父代周围 17×17×17 范围内的最大生物数量，超过此数量将阻止繁殖")
            .defineInRange("maxMobsNearby", 10, 1, 1000);

    private static final ModConfigSpec.IntValue GIVE_TAG_BLOCK_RANGE = BUILDER
            .comment("方块冻结器的作用范围")
            .defineInRange("givetagBlockRange", 8, 1, 16);

    public static int maxMobsNearby;
    public static int givetagBlockRange;

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        maxMobsNearby = MAX_MOBS_NEARBY.get();
        givetagBlockRange = GIVE_TAG_BLOCK_RANGE.get();
    }
}