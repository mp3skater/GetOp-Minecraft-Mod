package net.mp3skater.getop.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mp3skater.getop.GetOP;

public class ModRecipes {
  public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
    .create(ForgeRegistries.RECIPE_SERIALIZERS, GetOP.MOD_ID);
  public static final RegistryObject<RecipeSerializer<AnvilOfSageRecipe>> ANVIL_OF_SAGE_SERIALIZER = SERIALIZERS
    .register("anvil_of_sage", () -> AnvilOfSageRecipe.Serializer.INSTANCE);

  public static void register(IEventBus eventBus) {SERIALIZERS.register(eventBus);
  }
}
