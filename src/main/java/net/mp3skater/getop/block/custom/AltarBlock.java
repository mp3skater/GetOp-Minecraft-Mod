package net.mp3skater.getop.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mp3skater.getop.block.entity.ModBlockEntities;
import net.mp3skater.getop.block.entity.custom.AltarBlockEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import java.util.Collections;
import java.util.List;


import java.util.function.Supplier;

public class AltarBlock extends BaseEntityBlock {
	private final Supplier<Item> defaultItem;

	public AltarBlock(Properties properties, Supplier<Item> defaultItem) {
		super(properties);
		this.defaultItem = defaultItem;
	}

	public Item getDefaultItem() {
		return defaultItem.get();
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		// Erstellt die BlockEntity für diesen Altar
		return new AltarBlockEntity(pPos, pState);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		// WICHTIG: Sagt Minecraft, dass GeckoLib den Block rendern soll!
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}


	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		// Verbindet die BlockEntity mit der Tick-Methode (für Animationen / Logik)
		return createTickerHelper(pBlockEntityType, ModBlockEntities.ALTAR_BLOCK_ENTITY.get(),
				(level, pos, state, blockEntity) -> AltarBlockEntity.tick(level, pos, state, blockEntity));
	}



	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		if (!pLevel.isClientSide()) {
			BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
			if (blockEntity instanceof AltarBlockEntity altarBE) {
				Item itemToStore = defaultItem.get();
				System.out.println("DEBUG: Altar platziert mit Item: " + itemToStore); // <-- Hier schauen was in der Konsole steht!

				altarBE.setStoredItem(new ItemStack(itemToStore));
				altarBE.setClaimed(false);

				altarBE.setChanged();
				pLevel.sendBlockUpdated(pPos, pState, pState, 3);
			}
		}
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		if (!pLevel.isClientSide()) {
			BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
			if (blockEntity instanceof AltarBlockEntity altarBE) {
				if (altarBE.isClaimed()) {
					return InteractionResult.sidedSuccess(false);
				}

				ItemStack stored = altarBE.getStoredItem();
				if (!stored.isEmpty()) {
					if (!pPlayer.getInventory().add(stored)) {
						pPlayer.drop(stored, false);
					}

					altarBE.setStoredItem(ItemStack.EMPTY);
					altarBE.setClaimed(true);

					altarBE.setChanged(); // Speichert die Änderung ab
					pLevel.sendBlockUpdated(pPos, pState, pState, 3); // Informiert den Client, dass sich was geändert hat (z.B. für den Renderer!)
				}
			}
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide());
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
		// Verhindert, dass der Block als Item droppt (weder normal noch mit Seidenberührung)
		return Collections.emptyList();
	}

	@Override
	public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack) {
		super.spawnAfterBreak(pState, pLevel, pPos, pStack);
		// Droppt beim Abbauen immer die XP (700 bis 1400), genau wie ein Spawner
		int xpAmount = 700 + pLevel.random.nextInt(701);
		popExperience(pLevel, pPos, xpAmount);
	}

}