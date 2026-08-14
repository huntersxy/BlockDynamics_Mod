package com.huntersxy.blockd.mixin;

import com.huntersxy.blockd.Imixin.ILivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 冻结状态直接存在实体上（NBT 持久化），不再依赖外部静态集合：
 * 区块重载后从 NBT 恢复 freeze_ai，travel 注入每 tick 强制生效。
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity implements ILivingEntity {
    @Unique
    private boolean freeze_ai;
    @Unique
    private static final String NBT_FREEZE_AI = "blockd_freeze_ai";

    @Unique
    private LivingEntity blockd$self() {
        return (LivingEntity) (Object) this;
    }

    @Override
    public boolean blockd$is_freeze_ai() {
        return freeze_ai;
    }

    @Override
    public void blockd$set_freeze_ai(boolean freeze_ai) {
        this.freeze_ai = freeze_ai;
        if ((Object) this instanceof Mob mob) {
            if (freeze_ai) {
                // 冻结：停 AI（已冻结则不重复设置），清除目标
                if (!mob.isNoAi()) {
                    mob.setNoAi(true);
                }
                mob.setTarget(null);
            } else if (mob.isNoAi()) {
                // 解冻：恢复 AI。注意：若实体本身是原生 NoAI（非本模组设置），
                // 解冻会一并恢复其 AI，属已知边缘行为。
                mob.setNoAi(false);
            }
        }
    }

    /**
     * 冻结时完全阻断移动：cancel travel 使重力、水流、击退等全部失效（实体完全静止），
     * 同时每 tick 重新确保 NoAI（覆盖区块重载/其他模组清标志的情况）。
     */
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void blockd$freezeMovement(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity self = blockd$self();
        if (freeze_ai && !self.level().isClientSide) {
            self.setDeltaMovement(0, 0, 0);
            if ((Object) this instanceof Mob mob && !mob.isNoAi()) {
                mob.setNoAi(true);
            }
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void blockd$writeFreezeAiToNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean(NBT_FREEZE_AI, freeze_ai);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void blockd$readFreezeAiFromNbt(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains(NBT_FREEZE_AI)) {
            freeze_ai = nbt.getBoolean(NBT_FREEZE_AI);
        }
    }
}
