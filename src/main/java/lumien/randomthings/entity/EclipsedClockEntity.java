package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.TimeInABottleItem;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.EclipsedClockAnimationMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.minecraft.world.InteractionResult;

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
 * <p>
 * Deliberate deviation from 1.12.2, 2026-09-27 (explicit user request): the
 * original (and this port, at first) jumped straight to the target time in
 * one {@code setDayTime} call. Per request, the jump is now animated - see
 * {@link #fastForwardTarget}/{@link #FAST_FORWARD_RATE} - the world's day
 * time advances in large steps over several real seconds instead, so the
 * sun/moon visibly race across the sky like a time-lapse rather than
 * snapping instantly. Purely a server-side loop; the client needs no special
 * handling since it already receives the changing day time through
 * vanilla's own periodic time-sync packets and renders the sky from that,
 * same as it does for the ordinary day/night cycle.
 */
public class EclipsedClockEntity extends HangingEntity {
    private static final EntityDataAccessor<Integer> TARGET_TIME = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> HANGING_POS = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Direction> FACING = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.DIRECTION);

    /** Day-time ticks advanced per real (server) tick while fast-forwarding. */
    private static final long FAST_FORWARD_RATE = 100;

    private int timeDisplayCounter;
    private int animationCounter;
    private int cooldownCounter;

    /** -1 when not fast-forwarding; otherwise the day time to stop at. */
    private long fastForwardTarget = -1;

    public EclipsedClockEntity(EntityType<EclipsedClockEntity> type, Level world) {
        super(type, world);
    }

    public EclipsedClockEntity(Level world, BlockPos hangingPositionIn, Direction facing) {
        super(ModEntityTypes.ECLIPSED_CLOCK, world, hangingPositionIn);

        this.setDirection(facing);
        this.entityData.set(HANGING_POS, hangingPositionIn);
        this.entityData.set(FACING, facing);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_TIME, 0);
        builder.define(HANGING_POS, BlockPos.ZERO);
        builder.define(FACING, Direction.NORTH);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.level().isClientSide() && (key.equals(HANGING_POS) || key.equals(FACING))) {
            this.pos = this.entityData.get(HANGING_POS);
            this.setDirection(this.entityData.get(FACING));
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.addAdditionalSaveData(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtAddAdditionalSaveData(rtTag);
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtAddAdditionalSaveData(CompoundTag compound) {

        compound.putInt("targetTime", getTargetTime());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.readAdditionalSaveData(rtIn);
    	rtReadAdditionalSaveData(lumien.randomthings.util.TagIO.read(rtIn));
    }

    private void rtReadAdditionalSaveData(CompoundTag compound) {

        setTargetTime(compound.getIntOr("targetTime", 0));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.timeDisplayCounter > 0) {
                this.timeDisplayCounter--;
            }

            if (this.animationCounter > 0) {
                this.animationCounter--;
            }
        } else {
            if (cooldownCounter > 0) {
                cooldownCounter--;
            }

            if (fastForwardTarget >= 0) {
                long current = this.level().getDayTime();
                long remaining = fastForwardTarget - current;

                if (remaining <= FAST_FORWARD_RATE) {
                    ((net.minecraft.server.level.ServerLevel) this.level()).setDayTime(fastForwardTarget);
                    fastForwardTarget = -1;
                } else {
                    ((net.minecraft.server.level.ServerLevel) this.level()).setDayTime(current + FAST_FORWARD_RATE);
                }
            }
        }
    }

    public void triggerAnimation() {
        if (this.animationCounter == 0) {
            this.animationCounter = 100;

            for (int i = 0; i < 20; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double radius = this.random.nextDouble() * 0.3 + 0.1;

                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, this.getX() + Math.cos(angle) * radius, this.getY() + this.random.nextDouble() * 0.5, this.getZ() + Math.sin(angle) * radius, 0.5 - this.random.nextDouble(), this.random.nextDouble() * 0.2, 0.5 - this.random.nextDouble());
            }
        }
    }

    public boolean shouldDisplayTime() {
        return this.timeDisplayCounter > 0;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof TimeInABottleItem) {
            if (!this.level().isClientSide() && cooldownCounter == 0 && fastForwardTarget < 0) {
                int timeStored = TimeInABottleItem.getStoredTime(held);
                int dif = (getTargetTime() - (int) this.level().getDayTime()) % 24000;

                if (dif < 0) {
                    dif += 24000;
                }

                if (timeStored >= dif || player.getAbilities().instabuild) {
                    fastForwardTarget = this.level().getDayTime() + dif;

                    if (!player.getAbilities().instabuild) {
                        TimeInABottleItem.setStoredTime(held, timeStored - dif);
                    }

                    cooldownCounter = 110;

                    RTPacketHandler.sendToAllNear(new EclipsedClockAnimationMessage(this.getId()), this.level(), this.blockPosition(), 32F);

                    this.level().playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.3F, 1.2F);
                }
            }
        } else {
            if (!this.level().isClientSide()) {
                int targetTime = getTargetTime();

                if (player.isShiftKeyDown()) {
                    targetTime -= 20 * 30;
                } else {
                    targetTime += 20 * 30;
                }

                // 1.12.2 only did '% 24000' here, which goes negative when sneaking down from < 30s; wrap into 0..23999 instead
                targetTime = ((targetTime % 24000) + 24000) % 24000;

                setTargetTime(targetTime);
            } else {
                this.timeDisplayCounter = 60;
            }
        }

        return InteractionResult.SUCCESS;
    }

    public int getTargetTime() {
        return this.entityData.get(TARGET_TIME);
    }

    public void setTargetTime(int newTime) {
        this.entityData.set(TARGET_TIME, newTime);
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

    /** 1.21 replaced getWidth/getHeight (12x12 pixels = 0.75 blocks) with this box, built exactly like the Item Frame's. */
    @Override
    protected net.minecraft.world.phys.AABB calculateBoundingBox(net.minecraft.core.BlockPos pos, net.minecraft.core.Direction direction) {
        net.minecraft.world.phys.Vec3 center = net.minecraft.world.phys.Vec3.atCenterOf(pos).relative(direction, -0.46875D);
        net.minecraft.core.Direction.Axis axis = direction.getAxis();
        double x = axis == net.minecraft.core.Direction.Axis.X ? 0.0625D : 0.75D;
        double y = axis == net.minecraft.core.Direction.Axis.Y ? 0.0625D : 0.75D;
        double z = axis == net.minecraft.core.Direction.Axis.Z ? 0.0625D : 0.75D;
        return net.minecraft.world.phys.AABB.ofSize(center, x, y, z);
    }

    @Override
    public void dropItem(net.minecraft.server.level.ServerLevel serverLevel, Entity brokenEntity) {
        if (serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
            this.playSound(net.minecraft.sounds.SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);

            if (brokenEntity instanceof Player && ((Player) brokenEntity).getAbilities().instabuild) {
                return;
            }

            ItemStack drop = new ItemStack(ModItems.ECLIPSED_CLOCK);
            net.minecraft.nbt.CompoundTag dropTag = lumien.randomthings.util.StackTag.getOrCreate(drop);
            dropTag.putInt("targetTime", getTargetTime());
            lumien.randomthings.util.StackTag.set(drop, dropTag);
            this.spawnAtLocation(serverLevel, drop, 0.0F);
        }
    }

    @Override
    public void playPlacementSound() {
    }

    public int getAnimationCounter() {
        return animationCounter;
    }

}
