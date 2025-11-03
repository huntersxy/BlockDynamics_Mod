// 在 ModCreativeTabs.java 文件中
package com.huntersxy.blockd.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Moditems.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CUSTOM_TAB = CREATIVE_TABS.register("custom_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.custom_tab"))
            .icon(() -> new ItemStack(Moditems.test.get()))
            .displayItems((params, output) -> {
                // 添加物品到标签页
                output.accept(Moditems.test.get());
                output.accept(Moditems.test2.get());
                output.accept(Moditems.GIVETAG_BLOCK_ITEM.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
