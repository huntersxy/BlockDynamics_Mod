package com.huntersxy.blockd.item;

import com.huntersxy.blockd.block.Givetagblock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Moditems {
    static final String MOD_ID = "blockd";
    // 注册物品和方块
    public static final DeferredRegister<Item>
            ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Block>
            BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "blockd");

    // 创建物品
    public static final RegistryObject<Item> test =
    ITEMS.register("test", () -> new ExplosiveSwordItem(Tiers.IRON, 3, -2.4F, new Item.Properties()));

    // 创建物品
    public static final RegistryObject<Item> test2 =
    ITEMS.register("test2", () -> new GiveTag(new Item.Properties()));


    // 注册方块
    // 注册方块 - 修改为泥土硬度，空手可破坏
    public static final RegistryObject<Givetagblock> GIVETAG_BLOCK = BLOCKS.register("givetag_block",
        () -> new Givetagblock(Block.Properties.of()
                .strength(0.5f)  // 泥土硬度
                // 移除 requiresCorrectToolForDrops() 使空手可掉落
        ));


    // 注册方块物品（以便能在物品栏中显示）
    public static final RegistryObject<Item> GIVETAG_BLOCK_ITEM = ITEMS.register("givetag_block",
            () -> new BlockItem(GIVETAG_BLOCK.get(), new Item.Properties()));

    //创造模式物品栏





    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
    }



}
