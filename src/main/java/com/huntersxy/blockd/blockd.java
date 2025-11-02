package com.huntersxy.blockd;

import com.huntersxy.blockd.item.Moditems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(blockd.MOD_ID)
public class blockd
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "blockd";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public blockd(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 注册通用设置方法用于模组加载
        modEventBus.addListener(this::commonSetup);

        // 注册模组物品
        Moditems.register(modEventBus);


        // 为我们感兴趣的服务器和其他游戏事件注册自己
        MinecraftForge.EVENT_BUS.register(this);

        // 注册物品到创造模式物品栏
        modEventBus.addListener(this::addCreative);

        // 注册我们模组的ForgeConfigSpec，以便Forge可以为我们创建和加载配置文件
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        //监测繁殖事件
        MinecraftForge.EVENT_BUS.addListener(this::onBabySpawn);
}



    @SubscribeEvent
    public void onBabySpawn(BabyEntitySpawnEvent event) {
        // 获取父代实体
        Mob parentA = event.getParentA();
        // 获取父代所在区块
        BlockPos parentAPos = parentA.blockPosition();

        // 获取父代区块内 mob 实体的数量
        long parentAEntityCount = parentA.level()
                .getEntitiesOfClass(Mob.class,
                        new AABB(parentAPos.getX() - 8, parentAPos.getY() - 8, parentAPos.getZ() - 8,
                                parentAPos.getX() + 8, parentAPos.getY() + 8, parentAPos.getZ() + 8))
                .size();


        // 获取即将出生的子代
        Mob child = event.getChild();
        //如果父代区块内 mob 实体数量大于10，则取消繁殖
        if (parentAEntityCount >  Config.maxMobsInChunk) {
            event.setCanceled(true);
            LOGGER.info("阻止了一次繁殖");
        }
    }














    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
