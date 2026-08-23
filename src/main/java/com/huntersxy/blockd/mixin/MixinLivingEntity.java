package com.huntersxy.blockd.mixin;

import com.huntersxy.blockd.duck.ILivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.11 {
/*import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
 *///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 冻结引用计数：每台通电的实体冻结器对范围内 Mob 持有一个引用，
 * 计数大于 0 即冻结，归零才解冻——范围重叠的多台冻结器互不误伤
 * （boolean 标志会在其中一台断电时把其他冻结器仍冻结的生物错误解冻）。
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity implements ILivingEntity {
    @Unique
    private int freezeCount;

    // 冻结开始时的原始 NoAI 状态，解冻时原样恢复：
    // 原生 NoAI 生物（地图/刷怪笼生物）不会被误唤醒，
    // 其他模组在冻结前设置的 NoAI 也不会被清掉
    @Unique
    private boolean frozenWasNoAi;

    @Unique
    private static final String NBT_FREEZE_COUNT = "blockd_freeze_count";

    @Unique
    private static final String NBT_WAS_NOAI = "blockd_freeze_was_noai";

    // 1.1.x 及更早版本用 boolean 字段，读档时兼容（true 视为计数 1）
    @Unique
    private static final String NBT_FREEZE_COUNT_LEGACY = "blockd_freeze_ai";

    @Override
    public boolean blockd$isFrozen() {
        return freezeCount > 0;
    }

    @Override
    public void blockd$acquireFreeze() {
        if (freezeCount++ > 0) {
            return;
        }
        // 第一台冻结器覆盖：真正冻结
        if ((Object) this instanceof Mob mob) {
            frozenWasNoAi = mob.isNoAi();
            if (!frozenWasNoAi) {
                mob.setNoAi(true);
            }
            mob.setTarget(null);
            mob.setDeltaMovement(0, 0, 0);
        }
    }

    @Override
    public void blockd$releaseFreeze() {
        if (freezeCount == 0 || --freezeCount > 0) {
            return;
        }
        // 最后一个引用释放：解冻
        if ((Object) this instanceof Mob mob) {
            mob.setNoAi(frozenWasNoAi);
            // 解冻瞬间速度矢量归零：冻结期间 travel 每 tick 清零 deltaMovement，
            // 但 Level.pushEntities（实体挤压/爆炸冲量等）在实体 tick 之后运行，
            // 上一 tick 末尾写入的速度会在解冻后的第一次 travel 中被结算，
            // 造成"解冻弹飞"。这里清零保证解冻瞬间速度为 0、挤压不结算。
            mob.setDeltaMovement(0, 0, 0);
        }
        frozenWasNoAi = false;
    }

    /**
     * 冻结时完全阻断移动：cancel travel 使重力、水流、击退等全部失效（实体完全静止），
     * 同时每 tick 重新确保 NoAI（覆盖区块重载/其他模组清标志的情况）。
     */
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void blockd$freezeMovement(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        //? if <1.21.11 {
        if (freezeCount > 0 && !self.level().isClientSide) {
        //?} else {
        /*if (freezeCount > 0 && !self.level().isClientSide()) {
         *///?}
            self.setDeltaMovement(0, 0, 0);
            if ((Object) this instanceof Mob mob && !mob.isNoAi()) {
                mob.setNoAi(true);
            }
            ci.cancel();
        }
    }

    // NBT 仅在冻结中（计数 > 0）时写入，不给普通生物的存档添加多余字段。
    //? if <1.21.11 {
    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void blockd$writeFreezeCountToNbt(CompoundTag nbt, CallbackInfo ci) {
        if (freezeCount > 0) {
            nbt.putInt(NBT_FREEZE_COUNT, freezeCount);
            nbt.putBoolean(NBT_WAS_NOAI, frozenWasNoAi);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void blockd$readFreezeCountFromNbt(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains(NBT_FREEZE_COUNT)) {
            freezeCount = nbt.getInt(NBT_FREEZE_COUNT);
            frozenWasNoAi = nbt.getBoolean(NBT_WAS_NOAI);
        } else if (nbt.contains(NBT_FREEZE_COUNT_LEGACY)) {
            freezeCount = nbt.getBoolean(NBT_FREEZE_COUNT_LEGACY) ? 1 : 0;
        }
    }
    //?}
    //? if 1.21.11 {
    /*@Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("TAIL"))
    private void blockd$writeFreezeCountToNbt(ValueOutput output, CallbackInfo ci) {
        if (this.freezeCount > 0) {
            output.putInt(NBT_FREEZE_COUNT, this.freezeCount);
            output.putBoolean(NBT_WAS_NOAI, this.frozenWasNoAi);
        }
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void blockd$readFreezeCountFromNbt(ValueInput input, CallbackInfo ci) {
        this.freezeCount = input.getIntOr(NBT_FREEZE_COUNT, 0);
        this.frozenWasNoAi = input.getBooleanOr(NBT_WAS_NOAI, false);
        if (this.freezeCount == 0 && input.getBooleanOr(NBT_FREEZE_COUNT_LEGACY, false)) {
            this.freezeCount = 1;
        }
    }
    *///?}
    //? if >=26.1 {
    /*@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void blockd$writeFreezeCountToNbt(ValueOutput output, CallbackInfo ci) {
        if (this.freezeCount > 0) {
            output.putInt(NBT_FREEZE_COUNT, this.freezeCount);
            output.putBoolean(NBT_WAS_NOAI, this.frozenWasNoAi);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void blockd$readFreezeCountFromNbt(ValueInput input, CallbackInfo ci) {
        this.freezeCount = input.getIntOr(NBT_FREEZE_COUNT, 0);
        this.frozenWasNoAi = input.getBooleanOr(NBT_WAS_NOAI, false);
        if (this.freezeCount == 0 && input.getBooleanOr(NBT_FREEZE_COUNT_LEGACY, false)) {
            this.freezeCount = 1;
        }
    }
    *///?}
}
