package com.huntersxy.blockd.gametest;

//? if >=1.21.11 {
/*import com.huntersxy.blockd.blockd;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

// 1.21.11+ 的 gametest 注册（新 registry 框架）：
// - 测试函数（Consumer<GameTestHelper>）经 RegisterEvent 注册进 TEST_FUNCTION 内置注册表；
// - 测试实例经 RegisterGameTestsEvent 注册进 TEST_INSTANCE 数据包注册表。
//
// 21.11 与 26.1 的差异仅在于 TestEnvironmentDefinition 是否泛型化（26.1 为
// TestEnvironmentDefinition<?>）。这里用 raw type 桥接，两个版本共用一份代码。
@EventBusSubscriber(modid = blockd.MODID)
public class GameTestRegistrarModern {

    @SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.TEST_FUNCTION)) {
            return;
        }
        event.register(Registries.TEST_FUNCTION, id("freeze_on_power_and_unfreeze"), () -> BlockDynamicsGameTestsModern::freezeOnPowerAndUnfreeze);
        event.register(Registries.TEST_FUNCTION, id("two_freezers_stay_independent"), () -> BlockDynamicsGameTestsModern::twoFreezersStayIndependent);
        event.register(Registries.TEST_FUNCTION, id("breaking_freezer_unfreezes_mobs"), () -> BlockDynamicsGameTestsModern::breakingFreezerUnfreezesMobs);
        event.register(Registries.TEST_FUNCTION, id("weak_redstone_signal_powers_freezer"), () -> BlockDynamicsGameTestsModern::weakRedstoneSignalPowersFreezer);
        event.register(Registries.TEST_FUNCTION, id("breeding_limit_blocks_over_limit"), () -> BlockDynamicsGameTestsModern::breedingLimitBlocksOverLimit);
    }

    @SubscribeEvent
    @SuppressWarnings("rawtypes")
    public static void registerGameTests(RegisterGameTestsEvent event) {
        // 注册一个空环境（等价 vanilla 的 minecraft:default），所有测试共用
        Holder env = event.registerEnvironment(id("default"), new TestEnvironmentDefinition.AllOf());
        registerTest(event, env, "freeze_on_power_and_unfreeze", 300);
        registerTest(event, env, "two_freezers_stay_independent", 400);
        registerTest(event, env, "breaking_freezer_unfreezes_mobs", 300);
        registerTest(event, env, "weak_redstone_signal_powers_freezer", 300);
        registerTest(event, env, "breeding_limit_blocks_over_limit", 300);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("blockd", path);
    }

    // 21.11: TestData<Holder<TestEnvironmentDefinition>>
    // 26.1:  TestData<Holder<TestEnvironmentDefinition<?>>>
    // 用 raw type 让两版本共用同一实现（仅产生 unchecked 警告）。
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerTest(RegisterGameTestsEvent event, Holder env, String name, int maxTicks) {
        ResourceKey<Consumer<GameTestHelper>> functionKey = ResourceKey.create(Registries.TEST_FUNCTION, id(name));
        event.registerTest(
                id(name),
                new FunctionGameTestInstance(
                        functionKey,
                        new TestData(env, id("empty"), maxTicks, 0, true)));
    }
}
 *///?}