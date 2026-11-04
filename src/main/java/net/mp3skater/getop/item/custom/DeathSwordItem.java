package net.mp3skater.getop.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mp3skater.getop.config.GetOPCommonConfigs;
import org.jetbrains.annotations.NotNull;
import net.mp3skater.getop.util.ModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DeathSwordItem extends AxeItem implements RareItem {
	public DeathSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	@Override
	public boolean canPerformAction(@NotNull ItemStack stack, net.minecraftforge.common.@NotNull ToolAction toolAction) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		pTooltipComponents.add(new TranslatableComponent("item.getop.deathaxe.tooltip"));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
		}

		Vec3 look = player.getLookAngle();
		Vec3 start = player.getEyePosition();
		Vec3 end = start.add(look.x * GetOPCommonConfigs.DEATH_SWORD_REACH_DISTANCE.get(),
			look.y * GetOPCommonConfigs.DEATH_SWORD_REACH_DISTANCE.get(),
			look.z * GetOPCommonConfigs.DEATH_SWORD_REACH_DISTANCE.get());
		double marginXZ = 3.0;
		double marginY = 2.0;

		AABB boundingBox = new AABB(
			Math.min(start.x, end.x) - marginXZ, Math.min(start.y, end.y) - marginY, Math.min(start.z, end.z) - marginXZ,
			Math.max(start.x, end.x) + marginXZ, Math.max(start.y, end.y) + marginY, Math.max(start.z, end.z) + marginXZ);

		if (!level.isClientSide) {
			Iterable<Entity> entities = level.getEntities(player, boundingBox);
			int damageDealt = 0;
			int maxDamage = 5;

			for (Entity entity : entities) {
				if (entity instanceof LivingEntity livingEntity) {
					livingEntity.hurt(DamageSource.MAGIC, 12);
					livingEntity.setDeltaMovement(0.5 * look.x, 0, 0.5 * look.z);
					damageDealt++;

					if (damageDealt >= maxDamage) break;
				}
			}

			if (damageDealt > 0) {
				player.hurt(DamageSource.WITHER, damageDealt);
			}

			player.getItemInHand(hand).hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		} else {
			for (int i = 0; i < 20; i++) {
				double offsetX = (level.random.nextDouble() - 0.5) * marginXZ;
				double offsetY = (level.random.nextDouble() - 0.5) * marginY;
				double offsetZ = (level.random.nextDouble() - 0.5) * marginXZ;

				level.addParticle(ParticleTypes.DAMAGE_INDICATOR,
					start.x + offsetX, start.y + offsetY, start.z + offsetZ, 0, 0.1, 0);
			}
		}

		player.getCooldowns().addCooldown(this, 60);
		return super.use(level, player, hand);
	}

	@Override
	public boolean hurtEnemy(@NotNull ItemStack pStack, @NotNull LivingEntity pTarget, @NotNull LivingEntity pAttacker) {
		if (!pAttacker.level.isClientSide()) {
			pTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2), pAttacker);
		}
		return super.hurtEnemy(pStack, pTarget, pAttacker);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack pStack, @NotNull BlockState pState) {
		return 1.0f;
	}
}
