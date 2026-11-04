package net.mp3skater.getop.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbyssShieldItem extends ShieldItem {

	public AbyssShieldItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		pTooltipComponents.add(new TranslatableComponent("item.getop.abyss_shield.tooltip"));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			// Trigger the ability
			if (!level.isClientSide) {
				boostAttack(player, level);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		return super.use(level, player, hand);
	}

	private void boostAttack(Player player, Level level) {
		// Get entities around the player
		AABB area = new AABB(
			player.getX() - 5, player.getY() - 5, player.getZ() - 5,
			player.getX() + 5, player.getY() + 5, player.getZ() + 5
		);

		// Play sound effect
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.0F);

		// Create particles
		for (int i = 0; i < 100; i++) {
			double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
			double offsetY = level.random.nextDouble() * 2.0;
			double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
			level.addParticle(ParticleTypes.END_ROD, player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ, 0, 0.1, 0);
		}

		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
			if (entity != player) { // Don't affect the player
				// Apply knockback
				double dx = entity.getX() - player.getX();
				double dz = entity.getZ() - player.getZ();
				double distance = Math.sqrt(dx * dx + dz * dz);
				if (distance != 0) {
					dx /= distance;
					dz /= distance;
				}

				double knockbackStrength = 2.0;
				entity.setDeltaMovement(dx * knockbackStrength, 0.5, dz * knockbackStrength);
				entity.hurt(DamageSource.MAGIC, 10.0F);

				// Add particles to the affected entity
				level.addParticle(ParticleTypes.GLOW, entity.getX(), entity.getY() + 1.0, entity.getZ(), 0, 0.2, 0);
			}
		}
	}
}
