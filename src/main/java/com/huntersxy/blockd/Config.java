package com.huntersxy.blockd;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = blockd.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MAX_MOBS_IN_CHUNK = BUILDER
            .comment("每个区块内最大生物数量，超过此数量将阻止繁殖")
            .defineInRange("maxMobsInChunk", 10, 1, 1000);

    private static final ForgeConfigSpec.IntValue GIVE_TAG_BLOCK_RANGE = BUILDER
            .comment("方块冻结器的作用范围")
            .defineInRange("givetagBlockRange", 8, 1, 16);

    public static int maxMobsInChunk;
    public static int givetagBlockRange;

    static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        maxMobsInChunk = MAX_MOBS_IN_CHUNK.get();
        givetagBlockRange = GIVE_TAG_BLOCK_RANGE.get();
    }
}