package com.huntersxy.blockd.item;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class GiveTag extends Item {  // 继承 Item 类

    public GiveTag(Properties properties) {
        super(properties);
    }
@Override
public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

    if (attacker instanceof Player) {
        // 添加标签到目标实体
        target.addTag(TAG_NAME);
        // 可以在这里添加更多标签操作逻辑
        executeTagLogic(target, (Player) attacker);
        //为该生物命名为test
        target.setCustomName(net.minecraft.network.chat.Component.literal(CUSTOM_NAME));

    }
    return super.hurtEnemy(stack, target, attacker);
}

// 建议在类中添加常量定义
private static final String TAG_NAME = "test";
private static final String CUSTOM_NAME = "test";


    private void executeTagLogic(LivingEntity target, Player player) {
        // 在这里实现标签相关的逻辑
        // 例如发送消息给玩家
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("已标记目标实体")
        );

        // 可以在这里安排20秒后移除标签的任务
        scheduleTagRemoval(target);
    }

    private void scheduleTagRemoval(LivingEntity target) {
        // 简单实现：记录目标和时间，后续通过事件处理
        // 更复杂的实现可能需要调度系统
    }
}
