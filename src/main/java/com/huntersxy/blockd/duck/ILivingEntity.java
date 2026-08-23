package com.huntersxy.blockd.duck;

/**
 * 冻结计数的 duck 接口（由 MixinLivingEntity 注入 LivingEntity 实现）。
 * 每台通电的实体冻结器对范围内生物持有一个引用，计数大于 0 即冻结，归零才解冻，
 * 因此范围重叠的多台冻结器不会互相误解冻。
 *
 * <p>注意：必须放在 mixin 配置包（com.huntersxy.blockd.mixin）之外，
 * 否则 Mixin 会拒绝直接加载（IllegalClassLoadError）。
 */
public interface ILivingEntity {
    void blockd$acquireFreeze();

    void blockd$releaseFreeze();

    boolean blockd$isFrozen();
}
