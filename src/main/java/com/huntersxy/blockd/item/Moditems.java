package com.huntersxy.blockd.item;

import com.huntersxy.blockd.block.Givetagblock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Moditems {
    public static final String MOD_ID = "blockd";
    
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredBlock<Givetagblock> GIVETAG_BLOCK = BLOCKS.register("givetag_block",
        () -> new Givetagblock(BlockBehaviour.Properties.of()
                .strength(0.5f)
        ));

    public static final DeferredItem<BlockItem> GIVETAG_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("givetag_block", GIVETAG_BLOCK);

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}