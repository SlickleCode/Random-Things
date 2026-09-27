package lumien.randomthings.entity;

import com.google.common.collect.Sets;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.Set;

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
public class GoldenChickenEntity extends AnimalEntity {
    private static final Set<net.minecraft.item.Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    private static final Ingredient TEMPTATION_INGREDIENT = Ingredient.fromItems(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);

    public float wingRotation;
    public float destPos;
    public float oFlapSpeed;
    public float oFlap;
    public float wingRotDelta = 1.0F;
    public int ingotDropTimer;
    public boolean chickenJockey;

    public GoldenChickenEntity(EntityType<? extends GoldenChickenEntity> type, World world) {
        super(type, world);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, TEMPTATION_INGREDIENT, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public void livingTick() {
        super.livingTick();
        this.oFlap = this.wingRotation;
        this.oFlapSpeed = this.destPos;
        this.destPos = (float) ((double) this.destPos + (double) (this.onGround ? -1 : 4) * 0.3D);
        this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);

        if (!this.onGround && this.wingRotDelta < 1.0F) {
            this.wingRotDelta = 1.0F;
        }

        this.wingRotDelta = (float) ((double) this.wingRotDelta * 0.9D);

        if (!this.onGround && this.getMotion().y < 0.0D) {
            this.setMotion(this.getMotion().mul(1.0, 0.6, 1.0));
        }

        this.wingRotation += this.wingRotDelta * 2.0F;

        if (!this.world.isRemote && ingotDropTimer > 0 && --this.ingotDropTimer <= 0) {
            this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F);
            this.entityDropItem(Items.GOLD_INGOT, 3);
        }

        if (this.ingotDropTimer == 0) {
            List<ItemEntity> items = this.world.getEntitiesWithinAABB(ItemEntity.class, this.getBoundingBox().grow(0.5));

            for (ItemEntity itemEntity : items) {
                ItemStack stack = itemEntity.getItem();

                if (!stack.isEmpty() && Tags.Items.ORES_GOLD.contains(stack.getItem())) {
                    stack.shrink(1);
                    itemEntity.setItem(stack);

                    this.ingotDropTimer = 600 + this.rand.nextInt(600);
                    break;
                }
            }
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.BlockState blockIn) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.2F);
    }

    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation("minecraft", "entities/chicken");
    }

    @Override
    public GoldenChickenEntity createChild(AgeableEntity ageable) {
        return new GoldenChickenEntity(ModEntityTypes.GOLDEN_CHICKEN, this.world);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return TEMPTATION_ITEMS.contains(stack.getItem());
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        return this.isChickenJockey() ? 10 : super.getExperiencePoints(player);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.chickenJockey = compound.getBoolean("IsChickenJockey");
        this.ingotDropTimer = compound.getInt("ingotDropTimer");
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("IsChickenJockey", this.chickenJockey);
        compound.putInt("ingotDropTimer", this.ingotDropTimer);
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return this.isChickenJockey() && !this.isBeingRidden();
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        float f = MathHelper.sin(this.renderYawOffset * 0.017453292F);
        float f1 = MathHelper.cos(this.renderYawOffset * 0.017453292F);
        passenger.setPosition(this.posX + (double) (0.1F * f), this.posY + (double) (this.getHeight() * 0.5F) + passenger.getYOffset(), this.posZ - (double) (0.1F * f1));

        if (passenger instanceof LivingEntity) {
            ((LivingEntity) passenger).renderYawOffset = this.renderYawOffset;
        }
    }

    public boolean isChickenJockey() {
        return this.chickenJockey;
    }

    public void setChickenJockey(boolean jockey) {
        this.chickenJockey = jockey;
    }
}
