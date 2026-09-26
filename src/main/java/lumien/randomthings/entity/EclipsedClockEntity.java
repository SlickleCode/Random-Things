package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.TimeInABottleItem;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.EclipsedClockAnimationMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A wall-mounted clock face (like an item frame) that shows a chosen target
 * time of day rather than the real one. Right-click cycles the target by 30
 * seconds (sneak reverses direction); right-click with a {@link
 * TimeInABottleItem} instead spends its stored time to fast-forward the real
 * world clock to match. Direct port of 1.12.2's {@code EntityEclipsedClock}.
 * <p>
 * Disclosed simplification: the original's time-skip triggered an elaborate
 * custom rotating color-triangle burst (its own bespoke immediate-mode-GL
 * "magic circle" rendering framework, {@code MKRRenderUtil}, shared by
 * several other decorative effects across the original mod and not otherwise
 * needed anywhere in this port yet). {@link
 * lumien.randomthings.client.renderer.EclipsedClockEntityRenderer} shows a
 * plain vanilla particle burst instead of porting that whole framework for
 * one cosmetic flourish.
 */
public class EclipsedClockEntity extends HangingEntity {
    private static final DataParameter<Integer> TARGET_TIME = EntityDataManager.createKey(EclipsedClockEntity.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> HANGING_POS = EntityDataManager.createKey(EclipsedClockEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Direction> FACING = EntityDataManager.createKey(EclipsedClockEntity.class, DataSerializers.DIRECTION);

    private int timeDisplayCounter;
    private int animationCounter;
    private int cooldownCounter;

    public EclipsedClockEntity(EntityType<EclipsedClockEntity> type, World world) {
        super(type, world);
    }

    public EclipsedClockEntity(World world, BlockPos hangingPositionIn, Direction facing) {
        super(ModEntityTypes.ECLIPSED_CLOCK, world, hangingPositionIn);

        this.updateFacingWithBoundingBox(facing);
        this.dataManager.set(HANGING_POS, hangingPositionIn);
        this.dataManager.set(FACING, facing);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(TARGET_TIME, 0);
        this.dataManager.register(HANGING_POS, BlockPos.ZERO);
        this.dataManager.register(FACING, Direction.NORTH);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (this.world.isRemote && (key.equals(HANGING_POS) || key.equals(FACING))) {
            this.hangingPosition = this.dataManager.get(HANGING_POS);
            this.updateFacingWithBoundingBox(this.dataManager.get(FACING));
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        compound.putInt("targetTime", getTargetTime());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        setTargetTime(compound.getInt("targetTime"));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isRemote) {
            if (this.timeDisplayCounter > 0) {
                this.timeDisplayCounter--;
            }

            if (this.animationCounter > 0) {
                this.animationCounter--;
            }
        } else if (cooldownCounter > 0) {
            cooldownCounter--;
        }
    }

    public void triggerAnimation() {
        if (this.animationCounter == 0) {
            this.animationCounter = 100;

            for (int i = 0; i < 20; i++) {
                double angle = this.rand.nextDouble() * Math.PI * 2;
                double radius = this.rand.nextDouble() * 0.3 + 0.1;

                this.world.addParticle(net.minecraft.particles.ParticleTypes.ENCHANT, this.posX + Math.cos(angle) * radius, this.posY + this.rand.nextDouble() * 0.5, this.posZ + Math.sin(angle) * radius, 0.5 - this.rand.nextDouble(), this.rand.nextDouble() * 0.2, 0.5 - this.rand.nextDouble());
            }
        }
    }

    public boolean shouldDisplayTime() {
        return this.timeDisplayCounter > 0;
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (held.getItem() instanceof TimeInABottleItem) {
            if (!this.world.isRemote && cooldownCounter == 0) {
                int timeStored = TimeInABottleItem.getStoredTime(held);
                int dif = (getTargetTime() - (int) this.world.getDayTime()) % 24000;

                if (dif < 0) {
                    dif += 24000;
                }

                if (timeStored >= dif || player.abilities.isCreativeMode) {
                    this.world.setDayTime(this.world.getDayTime() + dif);

                    if (!player.abilities.isCreativeMode) {
                        TimeInABottleItem.setStoredTime(held, timeStored - dif);
                    }

                    cooldownCounter = 110;

                    RTPacketHandler.sendToAllNear(new EclipsedClockAnimationMessage(this.getEntityId()), this.world, this.getPosition(), 32F);

                    this.world.playSound(null, this.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.BLOCKS, 0.3F, 1.2F);
                }
            }
        } else {
            if (!this.world.isRemote) {
                int targetTime = getTargetTime();

                if (player.isSneaking()) {
                    targetTime -= 20 * 30;
                } else {
                    targetTime += 20 * 30;
                }

                targetTime = targetTime % 24000;

                setTargetTime(targetTime);
            } else {
                this.timeDisplayCounter = 60;
            }
        }

        return true;
    }

    public int getTargetTime() {
        return this.dataManager.get(TARGET_TIME);
    }

    public void setTargetTime(int newTime) {
        this.dataManager.set(TARGET_TIME, newTime);
    }

    public String getStringTargetTime() {
        String myTime = "06:00";
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

        try {
            Date d = df.parse(myTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, (int) (1440 / 24000D * getTargetTime()));
            return df.format(cal.getTime());
        } catch (java.text.ParseException e) {
            return "XX:XX";
        }
    }

    @Override
    public int getWidthPixels() {
        return 12;
    }

    @Override
    public int getHeightPixels() {
        return 12;
    }

    @Override
    public void onBroken(Entity brokenEntity) {
        if (this.world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_ENTITY_DROPS)) {
            this.playSound(net.minecraft.util.SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);

            if (brokenEntity instanceof PlayerEntity && ((PlayerEntity) brokenEntity).abilities.isCreativeMode) {
                return;
            }

            this.entityDropItem(new ItemStack(ModItems.ECLIPSED_CLOCK), 0.0F);
        }
    }

    @Override
    public void playPlaceSound() {
    }

    public int getAnimationCounter() {
        return animationCounter;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
