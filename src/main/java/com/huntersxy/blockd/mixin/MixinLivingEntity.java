package com.huntersxy.blockd.mixin;

import com.huntersxy.blockd.Imixin.ILivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements ILivingEntity {
    @Unique
    private boolean freeze_ai;
    @Unique
    private static final String NBT_FREEZE_AI = "blockd_freeze_ai";

    @Override
    public boolean blockd$is_freeze_ai() {
        return freeze_ai;
    }

    @Override
    public void blockd$set_freeze_ai(boolean freeze_ai) {
        this.freeze_ai = freeze_ai;
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
