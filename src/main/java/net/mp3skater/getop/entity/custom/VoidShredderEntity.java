package net.mp3skater.getop.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mp3skater.getop.GetOP;
import net.mp3skater.getop.item.ModItems;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

public class VoidShredderEntity extends FlyingMob implements IAnimatable, Enemy {
    private AnimationFactory factory = new AnimationFactory(this);
    public VoidShredderEntity(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.xpReward = 20;
        this.moveControl = new VoidShredderMoveControl(this);
        this.lookControl = new VoidShredderLookControl(this);
        setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SENTINEL_BLADE.get()));
    }
    private boolean charging = false;

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 85.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0f)
                .add(Attributes.ATTACK_SPEED, 2.5f)
                .add(Attributes.MOVEMENT_SPEED, 10f)
                .add(Attributes.FLYING_SPEED, 1f).build();
    }

    // Update the entity's position
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
    }

    //Goals of Entity
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new ChargeTargetGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomFloatAroundGoal());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    public float getBrightness() {
        return -1.0F;
    }

    // Sets the animation in different States
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        Random random = new Random();
        int average = 400; // Average time (ticks I think) after which he spins his weapon

        Vec3 v = getDeltaMovement();
        // Entity is moving
        if(event.isMoving()) {
            // up
            if(v.y > 0) event.getController().setAnimation(new AnimationBuilder()
              .addAnimation("up", true));
            // down
//            else if(v.y < 0) event.getController().setAnimation(new AnimationBuilder()
//              .addAnimation("down", true));
            // idle
            else event.getController().setAnimation(new AnimationBuilder()
                  .addAnimation("idle", true));
        }
        // idle
        else event.getController().setAnimation(new AnimationBuilder()
          .addAnimation("idle", true));

        // Hitting
        if(charging) {
            event.getController().setAnimation(new AnimationBuilder()
              .addAnimation("hit", false));
            charging = false;
        }
        // Make a spin
        else if(random.nextInt()%average==0) {
            event.getController().setAnimation(new AnimationBuilder()
              .addAnimation("idle2", false));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                1, this::predicate));
    }
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    class ChargeTargetGoal extends Goal {
        public ChargeTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (VoidShredderEntity.this.getTarget() != null && !VoidShredderEntity.this.getMoveControl().hasWanted() && VoidShredderEntity.this.random.nextInt(reducedTickDelay(3)) == 0) {
                return VoidShredderEntity.this.distanceToSqr(VoidShredderEntity.this.getTarget()) > 4.0D;
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return VoidShredderEntity.this.getMoveControl().hasWanted() && VoidShredderEntity.this.getTarget() != null && VoidShredderEntity.this.getTarget().isAlive();
        }
        public void start() {
            LivingEntity livingentity = VoidShredderEntity.this.getTarget();
            if (livingentity != null) {
                Vec3 vec3 = livingentity.getEyePosition();
                VoidShredderEntity.this.moveControl.setWantedPosition(vec3.x, vec3.y - 1, vec3.z, 8.0D);
            }
            charging = true;

            VoidShredderEntity.this.playSound(SoundEvents.VEX_CHARGE, 2.0F, 1.0F);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = VoidShredderEntity.this.getTarget();
            if (livingentity != null) {
                int intMinX = (int) Math.floor(VoidShredderEntity.this.getBoundingBox().minX - 1);
                int intMinY = (int) Math.floor(VoidShredderEntity.this.getBoundingBox().minY - 1);
                int intMinZ = (int) Math.floor(VoidShredderEntity.this.getBoundingBox().minZ - 1);
                int intMaxX = (int) Math.ceil(VoidShredderEntity.this.getBoundingBox().maxX + 1);
                int intMaxY = (int) Math.ceil(VoidShredderEntity.this.getBoundingBox().maxY + 1);
                int intMaxZ = (int) Math.ceil(VoidShredderEntity.this.getBoundingBox().maxZ + 1);
                BoundingBox box = new BoundingBox(intMinX, intMinY, intMinZ, intMaxX, intMaxY, intMaxZ);
                int intMinX2 = (int) Math.floor(livingentity.getBoundingBox().minX);
                int intMinY2 = (int) Math.floor(livingentity.getBoundingBox().minY);
                int intMinZ2 = (int) Math.floor(livingentity.getBoundingBox().minZ);
                int intMaxX2 = (int) Math.ceil(livingentity.getBoundingBox().maxX);
                int intMaxY2 = (int) Math.ceil(livingentity.getBoundingBox().maxY);
                int intMaxZ2 = (int) Math.ceil(livingentity.getBoundingBox().maxZ);
                BoundingBox box2 = new BoundingBox(intMinX2, intMinY2, intMinZ2, intMaxX2, intMaxY2, intMaxZ2);
                if (box.intersects(box2)) {
                    VoidShredderEntity.this.doHurtTarget(livingentity);
                } else {
                    double d0 = VoidShredderEntity.this.distanceToSqr(livingentity);
                    if (d0 < 9.0D) {
                        Vec3 vec3 = livingentity.getEyePosition();
                        VoidShredderEntity.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
                    }
                }

            }
        }
    }

    // Called when the entity is attacked
    public boolean hurt(DamageSource pSource, float pAmount) {if (pSource.getDirectEntity() instanceof LightningBolt && pSource.getEntity() instanceof Player) {
        super.hurt(pSource, 1000.0F);
        return true;
    } else {
        return super.hurt(pSource, pAmount);
    }
    }
    public boolean isPersistenceRequired() {
        return true;
    }

    static class VoidShredderLookControl extends LookControl {
        public VoidShredderLookControl(Mob pMob) {
            super(pMob);
        }
        // Updates look every tick
        public void tick() {
        }
    }

    class VoidShredderMoveControl extends MoveControl {
        public VoidShredderMoveControl(VoidShredderEntity p_34062_) {
            super(p_34062_);
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - VoidShredderEntity.this.getX(), this.wantedY - VoidShredderEntity.this.getY(), this.wantedZ - VoidShredderEntity.this.getZ());
                double d0 = vec3.length();
                if (d0 < VoidShredderEntity.this.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    VoidShredderEntity.this.setDeltaMovement(VoidShredderEntity.this.getDeltaMovement().scale(0.5D));
                } else {
                    VoidShredderEntity.this.setDeltaMovement(VoidShredderEntity.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05D / d0)));
                    if (VoidShredderEntity.this.getTarget() == null) {
                        Vec3 vec31 = VoidShredderEntity.this.getDeltaMovement();
                        VoidShredderEntity.this.setYRot(-((float)Mth.atan2(vec31.x, vec31.z)) * (180F / (float)Math.PI));
                        VoidShredderEntity.this.yBodyRot = VoidShredderEntity.this.getYRot();
                    } else {
                        double d2 = VoidShredderEntity.this.getTarget().getX() - VoidShredderEntity.this.getX();
                        double d1 = VoidShredderEntity.this.getTarget().getZ() - VoidShredderEntity.this.getZ();
                        VoidShredderEntity.this.setYRot(-((float)Mth.atan2(d2, d1)) * (180F / (float)Math.PI));
                        VoidShredderEntity.this.yBodyRot = VoidShredderEntity.this.getYRot();
                    }
                }

            }
        }
    }


    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    public SoundSource getSoundSource() { return SoundSource.HOSTILE; }
    protected SoundEvent getAmbientSound() { return SoundEvents.GUARDIAN_AMBIENT; }
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) { return SoundEvents.WITHER_HURT; }
    protected SoundEvent getDeathSound() { return SoundEvents.VEX_DEATH; }
    protected float getSoundVolume() { return 5f; }

    class RandomFloatAroundGoal extends Goal {
        public RandomFloatAroundGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return !VoidShredderEntity.this.getMoveControl().hasWanted() && VoidShredderEntity.this.random.nextInt(reducedTickDelay(3)) == 0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void tick() {
            BlockPos blockpos = VoidShredderEntity.this.blockPosition();

            for(int i = 0; i < 3; ++i) {
                BlockPos blockpos1 = blockpos.offset(VoidShredderEntity.this.random.nextInt(15) - 7, VoidShredderEntity.this.random.nextInt(11) - 5, VoidShredderEntity.this.random.nextInt(15) - 7);
                if (VoidShredderEntity.this.level.isEmptyBlock(blockpos1)) {
                    VoidShredderEntity.this.moveControl.setWantedPosition((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 0.25D);
                    if (VoidShredderEntity.this.getTarget() == null) {
                        VoidShredderEntity.this.getLookControl().setLookAt((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}