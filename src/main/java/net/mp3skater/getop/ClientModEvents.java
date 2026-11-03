package net.mp3skater.getop;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.mp3skater.getop.block.ModBlocks;
import net.mp3skater.getop.block.entity.ModBlockEntities;
import net.mp3skater.getop.client.renderer.AltarBlockEntityRenderer;
import net.mp3skater.getop.screen.AnvilofSage_Screen;
import net.mp3skater.getop.screen.ModMenuTypes;

@Mod.EventBusSubscriber(modid = GetOP.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.ANVILOFSAGE_MENU.get(), AnvilofSage_Screen::new);

            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                    ModBlockEntities.ALTAR_BLOCK_ENTITY.get(),
                    AltarBlockEntityRenderer::new
            );

            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.DEATHAXE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ICE_SCYTHE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.PICKASHE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.END_SCEPTRE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ABYSS_SHIELD_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.HEROBRINE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
        });
    }
}