package com.huntersxy.blockd.gametest;

//? if >=26.1 {
/*import com.huntersxy.blockd.item.Moditems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

// 合成测试辅助（26.1+ 版本）：assemble 无注册表参数，其余与 RecipeCraftTest 相同。
final class RecipeCraftTest {
    private RecipeCraftTest() {}

    static ItemStack craft(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(new ItemStack(Items.EGG));
        }
        CraftingInput input = CraftingInput.of(3, 3, items);
        // getRecipeManager() 在 1.21.11+ 从 Level 移到了 ServerLevel.recipeAccess()，
        // 用 MinecraftServer.getRecipeManager() 全版本通用
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level);
        return recipe.map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
    }
}
 *///?}