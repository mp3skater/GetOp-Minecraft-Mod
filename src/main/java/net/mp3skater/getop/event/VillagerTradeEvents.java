package net.mp3skater.getop.event;



import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mp3skater.getop.item.ModItems;

@Mod.EventBusSubscriber(modid = "getop", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerTradeEvents {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {

            // -------------------------------------------------------------
            // Ice Cave Trade
            // -------------------------------------------------------------
            event.getTrades().get(3).add((trader, random) -> {
                if (!(trader.level instanceof ServerLevel level)) return null;

                TagKey<ConfiguredStructureFeature<?, ?>> iceCaveTag = TagKey.create(
                        Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY,
                        new ResourceLocation("getop", "ice_cave")
                );

                BlockPos pos = level.findNearestMapFeature(iceCaveTag, trader.blockPosition(), 500, true);
                if (pos == null) return null;

                ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);
                MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecoration.Type.RED_X);
                map.setHoverName(new TranslatableComponent("Ice cave Explorer Map"));

                return new MerchantOffer(
                        new ItemStack(Items.EMERALD, 38),
                        new ItemStack(ModItems.PAINITE.get(), 1),
                        map, 12, 10, 0.05f
                );
            });

            // -------------------------------------------------------------
            // Lava Pool Trade
            // -------------------------------------------------------------
            event.getTrades().get(3).add((trader, random) -> {
                if (!(trader.level instanceof ServerLevel level)) return null;

                TagKey<ConfiguredStructureFeature<?, ?>> lavaPoolTag = TagKey.create(
                        Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY,
                        new ResourceLocation("getop", "lavapool")
                );

                BlockPos pos = level.findNearestMapFeature(lavaPoolTag, trader.blockPosition(), 500, true);
                if (pos == null) return null;

                ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);
                MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecoration.Type.RED_X);
                map.setHoverName(new TranslatableComponent("Lavapool Explorer Map"));

                return new MerchantOffer(
                        new ItemStack(Items.EMERALD, 38),
                        new ItemStack(ModItems.PAINITE.get(), 1),
                        map, 12, 10, 0.05f
                );
            });
        }
    }
}