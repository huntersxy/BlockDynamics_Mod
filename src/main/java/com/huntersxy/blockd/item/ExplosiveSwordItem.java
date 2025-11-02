package com.huntersxy.blockd.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;

public class ExplosiveSwordItem extends SwordItem {
    public ExplosiveSwordItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 调用父类方法保持原有的攻击效果
        boolean result = super.hurtEnemy(stack, target, attacker);

        // 检查攻击是否会导致目标死亡
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            Level level = target.level();
            BlockPos pos = target.blockPosition();

            // 如果目标生命值降到0或以下，则安排爆炸
            if (target.getHealth() <= 0 || target.isDeadOrDying()) {
                // 在下一游戏刻爆炸
                level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 4.0F, Level.ExplosionInteraction.MOB);
            }
        }

        return result;
    }
}
