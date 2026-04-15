package com.huntersxy.blockd.mixin;

import com.huntersxy.blockd.Imixin.ILivingEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ILivingEntity {
    @Unique
    private boolean blockd$freeze_ai;
    @Unique
    private static final String NBT_KEY = "blockd_freeze_ai";

    @Override
    @Unique
    public boolean blockd$is_freeze_ai() {
        return blockd$freeze_ai;
    }

    @Override
    @Unique
    public void blockd$set_freeze_ai(boolean freeze_ai) {
        this.blockd$freeze_ai = freeze_ai;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void blockd$writeFreezeAiToNbt(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(NBT_KEY, this.blockd$freeze_ai);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void blockd$readFreezeAiFromNbt(ValueInput input, CallbackInfo ci) {
        this.blockd$freeze_ai = input.getBooleanOr(NBT_KEY, false);
    }
}