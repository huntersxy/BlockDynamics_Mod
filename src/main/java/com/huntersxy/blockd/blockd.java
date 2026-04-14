package com.huntersxy.blockd;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import com.huntersxy.blockd.item.Moditems;
import com.huntersxy.blockd.method.freeze_ai;
import com.huntersxy.blockd.method.limit_breeding;

@Mod(blockd.MODID)
public class blockd {
    public static final String MODID = "blockd";
    public static final Logger LOGGER = LogUtils.getLogger();


    public blockd(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTab);

        

          // 注册模组物品
        Moditems.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);


        // 注册事件监听器
        NeoForge.EVENT_BUS.register(freeze_ai.class);
        NeoForge.EVENT_BUS.register(limit_breeding.class);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(Moditems.GIVETAG_BLOCK_ITEM.get());
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
