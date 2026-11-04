package net.mp3skater.getop.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mp3skater.getop.config.GetOPCommonConfigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.item.Rarity.EPIC;

public class HerobrineSwordItem extends SwordItem {
	// Cooldown in Ticks (20 Ticks = 1 Sekunde -> 100 Ticks = 5 Sekunden)
	private static final int AOE_COOLDOWN_TICKS = 400;

	public HerobrineSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		pTooltipComponents.add(new TranslatableComponent("item.getop.herobrine_sword.tooltip"));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) return super.use(level, player, hand);

		if (!level.isClientSide) {
			// SHIFT + Rechtsklick -> AoE Blitze
			if (player.isShiftKeyDown()) {
				// Prüfen, ob der Cooldown noch aktiv ist
				if (!player.getCooldowns().isOnCooldown(this)) {
					aoeLightningPower((ServerLevel) level, player);
					// COOLDOWN HINZUFÜGEN (z.B. 5 Sekunden = 100 Ticks)
					player.getCooldowns().addCooldown(this, AOE_COOLDOWN_TICKS);
				}
			}
			// Normaler Rechtsklick -> Ziel-Blitz mit Teleportation
			else {
				lightningPower(level, player);
			}
		}

		return super.use(level, player, hand);
	}

	// Normaler Rechtsklick: Einzelblitz
	private void lightningPower(Level level, Player player) {
		Double reach = GetOPCommonConfigs.HEROBRINE_SWORD_REACH_DISTANCE.get();

		Vec3 look = player.getLookAngle();
		Vec3 eye = player.getEyePosition();
		Vec3 vec_end = eye.add(look.x * reach, look.y * reach, look.z * reach);

		Entity nearestEntity = null;

		double minX = Math.min(eye.x, vec_end.x);
		double minY = Math.min(eye.y, vec_end.y);
		double minZ = Math.min(eye.z, vec_end.z);
		double maxX = Math.max(eye.x, vec_end.x);
		double maxY = Math.max(eye.y, vec_end.y);
		double maxZ = Math.max(eye.z, vec_end.z);

		AABB boundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		Iterable<Entity> entities = level.getEntities(player, boundingBox);
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
				if (nearestEntity == null || entity.distanceTo(player) < nearestEntity.distanceTo(player)) {
					nearestEntity = entity;
				}
			}
		}

		if (nearestEntity != null && nearestEntity.isAlive()) {
			BlockPos position = nearestEntity.blockPosition();
			Vec3 pos = nearestEntity.position();
			player.hurtMarked = true;
			nearestEntity.hurt(DamageSource.MAGIC, 6);
			ServerLevel world = (ServerLevel) player.level;
			LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
			lightningBolt.moveTo(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
			world.addFreshEntity(lightningBolt);
			player.setDeltaMovement(0, 0, 0);
			nearestEntity.setDeltaMovement(look);
			player.moveTo(pos);
		}
	}

	// Shift + Rechtsklick: AoE-Blitze mit Schutz für Deko/Items
	private void aoeLightningPower(ServerLevel level, Player player) {
		double radius = 10.0D;
		Vec3 pos = player.position();

		AABB aabb = new AABB(
				pos.x - radius, pos.y - radius, pos.z - radius,
				pos.x + radius, pos.y + radius, pos.z + radius
		);

		// Filtert Spieler, Rüstungsständer, Item Frames (HangingEntity) und Boden-Items heraus
		List<Entity> entities = level.getEntities(player, aabb, entity ->
				entity != player &&
						entity.isAlive() &&
						!(entity instanceof ArmorStand) &&
						!(entity instanceof HangingEntity) && // Schützt Item Frames & Bilder
						!(entity instanceof ItemEntity)       // Schützt am Boden liegende Items
		);

		for (Entity entity : entities) {
			BlockPos entityPos = entity.blockPosition();

			LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
			lightningBolt.moveTo(entityPos.getX(), entityPos.getY(), entityPos.getZ(), 0.0F, 0.0F);
			level.addFreshEntity(lightningBolt);

			if (entity instanceof LivingEntity livingEntity) {
				livingEntity.hurt(DamageSource.MAGIC, 6.0F);
			}
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
		if (!pAttacker.getLevel().isClientSide() && pAttacker instanceof Player) {
			Level level = pAttacker.getLevel();
			BlockPos position = pTarget.blockPosition();
			LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
			lightningBolt.moveTo(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
			level.addFreshEntity(lightningBolt);
		}
		if (pTarget.getHealth() <= 0) {
			pTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 1), pAttacker);
		}
		return super.hurtEnemy(pStack, pTarget, pAttacker);
	}

	public @NotNull Rarity getRarity(ItemStack pStack) {
		return EPIC;
	}

	@Override
	public boolean isDamageable(ItemStack stack) {
		return true;
	}

	// Überschreibt die Standard-Reaktion auf Schaden, wenn das Schwert als gedropptes Item auf dem Boden liegt
	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return false;
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		// 1. Nur beim Erstellen des gedroppten Items einmalig setzen
		if (entity.tickCount == 1) {
			entity.setUnlimitedLifetime();
			entity.setInvulnerable(true); // Schützt vor Kakteen, Pfeilen, etc.
		}

		// 2. Void-Schutz: Stoppt das Item und lässt es in der Luft schweben
		if (entity.getY() < -64) {
			// Setze es knapp über die Void-Grenze (z.B. bei Y = -60) oder bei Y = 10
			entity.setPos(entity.getX(), 100, entity.getZ());

			// Jegliche Bewegung stoppen
			entity.setDeltaMovement(0, 0, 0);

			// Schwerkraft ausschalten, damit es an dieser Position eingefroren bleibt und nicht wieder fällt
			entity.setNoGravity(true);
		}

		return super.onEntityItemUpdate(stack, entity);
	}
}