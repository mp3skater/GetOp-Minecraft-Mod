package net.mp3skater.getop.screen.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.mp3skater.getop.block.entity.custom.AnvilOfSageBlock_Entity;

public class ModResultSlot extends SlotItemHandler {
    private final AnvilOfSageBlock_Entity blockEntity;

    public ModResultSlot(IItemHandler itemHandler, int index, int x, int y, AnvilOfSageBlock_Entity blockEntity) {
        super(itemHandler, index, x, y);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false; // Verhindert, dass man Items in den Ausgabeslot legt
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);

        // Löst das Verbrauchen der Zutaten in Slot 0 und 1 aus!
        blockEntity.craftAndConsumeItems();
    }
}