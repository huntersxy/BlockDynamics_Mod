package com.huntersxy.blockd;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

import com.huntersxy.blockd.item.ModCreativeTabs;
import com.huntersxy.blockd.item.Moditems;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(blockd.MODID)
public class blockd {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "blockd";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public blockd(IEventBus modEventBus, ModContainer modContainer) {
        // 注册模组物品（含冻结器方块）
        Moditems.register(modEventBus);

        // 注册创造模式物品栏
        ModCreativeTabs.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
