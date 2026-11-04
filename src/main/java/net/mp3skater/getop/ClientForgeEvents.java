package net.mp3skater.getop;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mp3skater.getop.item.custom.HerobrineSwordItem;
import net.mp3skater.getop.networking.ModMessages;
import net.mp3skater.getop.networking.packet.TeleportC2SPacket;

@Mod.EventBusSubscriber(modid = GetOP.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        // Prüft, ob 'C' gedrückt wurde
        if (ClientModEvents.TELEPORT_KEY.consumeClick()) {
            // Sendet das Paket an den Server
            ModMessages.sendToServer(new TeleportC2SPacket());
        }
    }
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        // Removes the Herobrine Sword from affected entities in an explosion
        event.getAffectedEntities().removeIf(entity ->
                entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() instanceof HerobrineSwordItem
        );
    }
}