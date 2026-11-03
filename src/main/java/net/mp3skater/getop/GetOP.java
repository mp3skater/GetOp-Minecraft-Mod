package net.mp3skater.getop;


import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mp3skater.getop.block.ModBlocks;
import net.mp3skater.getop.block.entity.ModBlockEntities;
import net.mp3skater.getop.client.renderer.AltarBlockEntityRenderer;
import net.mp3skater.getop.config.GetOPClientConfigs;
import net.mp3skater.getop.config.GetOPCommonConfigs;
import net.mp3skater.getop.effect.GetOPEffects;
import net.mp3skater.getop.enchantment.ModEnchantments;
import net.mp3skater.getop.entity.ModEntityTypes;
import net.mp3skater.getop.item.ModItems;
import net.mp3skater.getop.particle.ModParticles;
import net.mp3skater.getop.recipe.ModRecipes;
import net.mp3skater.getop.screen.ModMenuTypes;
import net.mp3skater.getop.world.dimension.ModDimensions;
import net.mp3skater.getop.world.structure.ModStructures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(GetOP.MOD_ID)
public class GetOP
{
    public static final String MOD_ID = "getop";
    public static final Logger LOGGER = LogManager.getLogger();

    public GetOP() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Hier stand vorher fälschlicherweise "modEventBus":
        eventBus.addListener(this::onClientSetup);

        ModItems.register(eventBus);
        ModBlocks.register(eventBus);

        GetOPEffects.register(eventBus);
        ModEnchantments.register(eventBus);

        ModStructures.register(eventBus);
        ModDimensions.register();

        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);

        ModParticles.register(eventBus);
        ModEntityTypes.register(eventBus);

        GeckoLib.initialize();

        eventBus.addListener(this::setup);

        ModRecipes.register(eventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GetOPClientConfigs.SPEC, "getop-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GetOPCommonConfigs.SPEC, "getop-common.toml");

        // Register for server and game events (doppelte Registrierung entfernt)
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {}

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            // HIER WIRD DER RENDERER REGISTRIERT:
            BlockEntityRenderers.register(
                    ModBlockEntities.ALTAR_BLOCK_ENTITY.get(),
                    AltarBlockEntityRenderer::new
            );

            // RenderLayers für die Altar-Blöcke:
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.DEATHAXE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ICE_SCYTHE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.PICKASHE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.END_SCEPTRE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ABYSS_SHIELD_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.HEROBRINE_ALTAR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
        });
    }
}




