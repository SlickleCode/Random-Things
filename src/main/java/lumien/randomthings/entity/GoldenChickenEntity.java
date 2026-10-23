package lumien.randomthings.entity;

import com.google.common.collect.Sets;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;

/**
 * A cosmetically-reskinned chicken (see {@link
 * lumien.randomthings.client.renderer.GoldenChickenEntityRenderer}) hatched
 * by a thrown {@code GoldenEggItem}. Deliberately extends {@code AnimalEntity}
 * directly rather than vanilla's {@code ChickenEntity} - matching 1.12.2's own
 * {@code EntityGoldenChicken} (itself a from-scratch copy of vanilla's
 * chicken, not a subclass of it), most likely because the egg-laying timer
 * and wing-animation fields it needs to repurpose are private on the vanilla
 * class in both versions. Its whole gimmick: no breeding AI goal (unlike
 * vanilla/1.12.2's chicken, matching the original's own apparent omission),
 * and its "egg timer" instead periodically drops 3 gold ingots, resetting
 * only once it notices a dropped item matching the (now tag-based, replacing
 * 1.12.2's removed {@code OreDictionary}) {@code forge:ores/gold} tag nearby
 * and consumes one of it. Direct port of 1.12.2's {@code EntityGoldenChicken}.
 * <p>
 * Disclosed simplification: skips re-overriding {@code getEyeHeight()} (a
 * trivial AI look-target height tweak) since {@code Entity.getEyeHeight()} is
 * {@code final} in 1.14.4 - vanilla's default eye height for this hitbox size
 * is used instead, with no gameplay-visible difference.
 */
public class GoldenChickenEntity extends Animal {
    private static final Set<net.minecraft.world.item.Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    private static final Ingredient TEMPTATION_INGREDIENT = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);

    public float wingRotation;
    public float destPos;
    public float oFlapSpeed;
    public float oFlap;
    public float wingRotDelta = 1.0F;
    public int ingotDropTimer;
    public boolean chickenJockey;

    public GoldenChickenEntity(EntityType<? extends GoldenChickenEntity> type, Level world) {
        super(type, world);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, TEMPTATION_INGREDIENT, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0D).add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.oFlap = this.wingRotation;
        this.oFlapSpeed = this.destPos;
        this.destPos = (float) ((double) this.destPos + (double) (this.onGround() ? -1 : 4) * 0.3D);
        this.destPos = Mth.clamp(this.destPos, 0.0F, 1.0F);

        if (!this.onGround() && this.wingRotDelta < 1.0F) {
            this.wingRotDelta = 1.0F;
        }

        this.wingRotDelta = (float) ((double) this.wingRotDelta * 0.9D);

        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }

        this.wingRotation += this.wingRotDelta * 2.0F;

        if (!this.level().isClientSide && ingotDropTimer > 0 && --this.ingotDropTimer <= 0) {
            this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F);
            this.spawnAtLocation(new ItemStack(Items.GOLD_INGOT, 3));
        }

        if (this.ingotDropTimer == 0) {
            List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5));

            for (ItemEntity itemEntity : items) {
                ItemStack stack = itemEntity.getItem();

                if (!stack.isEmpty() && stack.is(Tags.Items.ORES_GOLD)) {
                    stack.shrink(1);
                    itemEntity.setItem(stack);

                    this.ingotDropTimer = 600 + this.random.nextInt(600);
                    break;
                }
            }
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState blockIn) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.2F);
    }

    @Override
    protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
        return net.minecraft.world.entity.EntityType.CHICKEN.getDefaultLootTable();
    }

    @Override
    public GoldenChickenEntity getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
        return new GoldenChickenEntity(ModEntityTypes.GOLDEN_CHICKEN, this.level());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return TEMPTATION_ITEMS.contains(stack.getItem());
    }

    @Override
    public int getExperienceReward() {
        return this.isChickenJockey() ? 10 : super.getExperienceReward();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.chickenJockey = compound.getBoolean("IsChickenJockey");
        this.ingotDropTimer = compound.getInt("ingotDropTimer");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsChickenJockey", this.chickenJockey);
        compound.putInt("ingotDropTimer", this.ingotDropTimer);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.isChickenJockey() && !this.isVehicle();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);

        if (passenger instanceof LivingEntity) {
            ((LivingEntity) passenger).yBodyRot = this.yBodyRot;
        }
    }

    public boolean isChickenJockey() {
        return this.chickenJockey;
    }

    public void setChickenJockey(boolean jockey) {
        this.chickenJockey = jockey;
    }
}
