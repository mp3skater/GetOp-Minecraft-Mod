package net.mp3skater.getop.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.mp3skater.getop.block.entity.ModBlockEntities;
import net.mp3skater.getop.recipe.AnvilOfSageRecipe;
import net.mp3skater.getop.screen.AnvilofSage_Menu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AnvilOfSageBlock_Entity extends BlockEntity implements MenuProvider {

    // Einziger, korrekter ItemHandler mit automatischer Rezept-Vorschau in Slot 2
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(level != null && !level.isClientSide()) {
                if(slot == 0 || slot == 1) {
                    updateOutputSlot();
                }
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;

    public AnvilOfSageBlock_Entity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.ANVIL_OF_SAGE_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return 0;
                    case 1: return 0;
                    case 2: return 0;
                    default: return 0;
                }
            }
            public void set(int index, int value) {}
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new AnvilofSage_Menu(pContainerId, pInventory, this, this.data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // Aktualisiert Slot 2 als Vorschau, wenn Items in Slot 0 und 1 liegen
    public void updateOutputSlot() {
        // Wenn einer der Eingangsslots leer ist, direkt abbrechen und Slot 2 leeren!
        if (this.itemHandler.getStackInSlot(0).isEmpty() || this.itemHandler.getStackInSlot(1).isEmpty()) {
            this.itemHandler.setStackInSlot(2, ItemStack.EMPTY);
            return;
        }

        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        Optional<AnvilOfSageRecipe> match = level.getRecipeManager()
                .getRecipeFor(AnvilOfSageRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent()) {
            ItemStack result = match.get().getResultItem();
            // Setze das Ergebnis in Slot 2 als Vorschau
            this.itemHandler.setStackInSlot(2, result.copy());
        } else {
            // Kein passendes Rezept -> Slot 2 leeren
            this.itemHandler.setStackInSlot(2, ItemStack.EMPTY);
        }
    }

    // Verbraucht die Zutaten in Slot 0 und 1, wenn das Item aus Slot 2 herausgenommen wird
    public void craftAndConsumeItems() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        Optional<AnvilOfSageRecipe> match = level.getRecipeManager()
                .getRecipeFor(AnvilOfSageRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent()) {
            this.itemHandler.extractItem(0, 1, false);
            this.itemHandler.extractItem(1, 1, false);
            setChanged();
            updateOutputSlot();
        }
    }

}