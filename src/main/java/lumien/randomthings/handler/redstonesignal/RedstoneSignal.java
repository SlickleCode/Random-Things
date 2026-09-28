package lumien.randomthings.handler.redstonesignal;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

/**
 * A single timed "phantom" redstone signal - see {@link RedstoneSignalHandler}
 * for how these get queried. Direct port of 1.12.2's {@code RedstoneSignal},
 * minus its {@code dimension} field: this port stores one handler per world
 * (see {@link RedstoneSignalHandler#get}) rather than one global handler
 * keyed by dimension id, so every signal is already scoped to the right
 * world by construction.
 */
public class RedstoneSignal {
    private BlockPos position;
    private int duration;
    private int age;
    private int redstoneStrength;

    public RedstoneSignal() {
    }

    public RedstoneSignal(BlockPos position, int duration, int redstoneStrength) {
        this.position = position;
        this.duration = duration;
        this.redstoneStrength = redstoneStrength;
        this.age = 0;
    }

    public boolean tick() {
        this.age++;
        return this.age >= this.duration;
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getRedstoneStrength() {
        return redstoneStrength;
    }

    public void write(CompoundNBT compound) {
        compound.putInt("x", position.getX());
        compound.putInt("y", position.getY());
        compound.putInt("z", position.getZ());
        compound.putInt("redstoneStrength", redstoneStrength);
        compound.putInt("duration", duration);
        compound.putInt("age", age);
    }

    public void read(CompoundNBT compound) {
        this.position = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        this.redstoneStrength = compound.getInt("redstoneStrength");
        this.duration = compound.getInt("duration");
        this.age = compound.getInt("age");
    }
}
