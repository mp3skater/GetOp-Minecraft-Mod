package net.mp3skater.getop.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.mp3skater.getop.item.custom.HerobrineSwordItem;
import net.mp3skater.getop.util.ModUtils;
import net.mp3skater.getop.world.dimension.ModDimensions;

import java.util.function.Supplier;

public class TeleportC2SPacket {
    public TeleportC2SPacket() {}

    public TeleportC2SPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                // Teleportiert nur, wenn das Schwert gehalten wird
                if (mainHand.getItem() instanceof HerobrineSwordItem || offHand.getItem() instanceof HerobrineSwordItem) {
                    ModUtils.teleportEntityToDimension(player, ModDimensions.GTDIM_KEY, new Vec3(-23, 92, 18));
                }
            }
        });
        return true;
    }
}
