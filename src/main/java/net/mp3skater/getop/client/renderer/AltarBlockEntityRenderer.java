package net.mp3skater.getop.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.mp3skater.getop.block.entity.custom.AltarBlockEntity;
import net.mp3skater.getop.client.model.AltarBlockModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class AltarBlockEntityRenderer extends GeoBlockRenderer<AltarBlockEntity> {

    public AltarBlockEntityRenderer(BlockEntityRendererProvider.Context rendererContext) {
        super(rendererContext, new AltarBlockModel());
    }

    @Override
    public void render(net.minecraft.world.level.block.entity.BlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLightIn, int packedOverlayIn) {
        // 1. Hier wird GeckoLib mit dem allgemeinen Typen aufgerufen, damit die Animation greift
        super.render(entity, partialTicks, stack, bufferSource, packedLightIn, packedOverlayIn);

        // 2. Hier prüfen und casten wir für dein schwebendes Item
        if (entity instanceof AltarBlockEntity blockEntity) {
            ItemStack itemStack = blockEntity.getStoredItem();
            if (!itemStack.isEmpty() && blockEntity.getLevel() != null) {
                stack.pushPose();

                long time = blockEntity.getLevel().getGameTime();
                double bob = Math.sin((time + partialTicks) * 0.1D) * 0.07D; // 0.1 = Geschwindigkeit, 0.07 = Schwingungshöhe
                stack.translate(0.5D, 1.6D + bob, 0.5D);
                stack.scale(1.0f, 1.0f, 1.0f);

                // 2. Drehbewegung im Kreis (Y-Achse)
                float angle = (time + partialTicks) * 3.0f;
                stack.mulPose(com.mojang.math.Vector3f.YP.rotationDegrees(angle));

                // 3. Die 45-Grad-Neigung (X-Achse)
                stack.mulPose(com.mojang.math.Vector3f.ZP.rotationDegrees(-45.0f));

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                Minecraft.getInstance().getItemRenderer().renderStatic(
                        itemStack,
                        ItemTransforms.TransformType.FIXED,
                        packedLightIn,
                        OverlayTexture.NO_OVERLAY,
                        stack,
                        bufferSource,
                        (int) blockEntity.getBlockPos().asLong()
                );

                RenderSystem.disableBlend();
                stack.popPose();
            }
        }
    }
}