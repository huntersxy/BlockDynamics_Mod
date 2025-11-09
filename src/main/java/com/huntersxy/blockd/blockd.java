package com.huntersxy.blockd;

import com.huntersxy.blockd.item.ModCreativeTabs;
import com.huntersxy.blockd.item.Moditems;
import com.huntersxy.blockd.method.limit_breeding;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(blockd.MOD_ID)
public class blockd
{

    public static final String MOD_ID = "blockd";


    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    public blockd(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        new limit_breeding();

        // 注册通用设置方法用于模组加载
        modEventBus.addListener(this::commonSetup);

        // 注册模组物品
        Moditems.register(modEventBus);

        // 注册创造模式物品栏
        ModCreativeTabs.register(modEventBus);


        // 为我们感兴趣的服务器和其他游戏事件注册自己
        MinecraftForge.EVENT_BUS.register(this);


        // 注册我们模组的ForgeConfigSpec，以便Forge可以为我们创建和加载配置文件
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);


}

/***
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
        //如果父代区块内 mob 实体数量大于maxMobsInChunk，则取消繁殖
        if (parentAEntityCount >  Config.maxMobsInChunk) {
            event.setCanceled(true);
            LOGGER.info("阻止了一次繁殖");
        }
    }

**/


    // 模组通用设置方法
    // 在模组加载时执行
    private void commonSetup(final FMLCommonSetupEvent event)
    {

        LOGGER.info("HELLO FROM COMMON SETUP");

    }

    // 模组创造模式物品栏注册方法


    // 服务器启动时执行
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    // 客户端设置方法,用于注册客户端设置
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
