package net.mp3skater.getop.block.entity.custom;


import net.minecraft.core.BlockPos;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mp3skater.getop.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;


public class AltarBlockEntity extends BlockEntity implements IAnimatable {

    private ItemStack storedItem = ItemStack.EMPTY;

    private boolean claimed = false;


// GeckoLib Animation Factory
    private final AnimationFactory factory = new AnimationFactory(this);


    public AltarBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ALTAR_BLOCK_ENTITY.get(), pPos, pBlockState);


    }

    public ItemStack getStoredItem() {
        return storedItem;

    }

    public void setStoredItem(ItemStack itemStack) {
        this.storedItem = itemStack;

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        }

    }

    public boolean isClaimed() {
        return claimed;

    }


    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        }

    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        if (!storedItem.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            storedItem.save(itemTag);
            tag.put("storedItem", itemTag);
        }

        tag.putBoolean("claimed", claimed);

    }


    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("storedItem")) {
            storedItem = ItemStack.of(nbt.getCompound("storedItem"));
        } else {
            storedItem = ItemStack.EMPTY;
        }

        claimed = nbt.getBoolean("claimed");
    }


    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AltarBlockEntity pEntity) {

// Hier kannst du später z.B. Rotations-Logik oder Animationen einbauen

    }


    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this, BlockEntity::saveWithFullMetadata);

    }


    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();

    }


    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);

        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;

    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 1, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        // Erzwinge das Abspielen der Animation "altar" im Loop
        event.getController().setAnimation(new AnimationBuilder().addAnimation("altar", true));
        return PlayState.CONTINUE;
    }

} 