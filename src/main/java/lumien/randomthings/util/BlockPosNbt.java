package lumien.randomthings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** The pre-1.20.5 block position tag ({@code {X,Y,Z}}); {@code NbtUtils.writeBlockPos} now writes an int array, but the mod's saved data keeps the compound form. */
public final class BlockPosNbt
{
	private BlockPosNbt()
	{
	}

	public static CompoundTag write(BlockPos pos)
	{
		CompoundTag tag = new CompoundTag();
		tag.putInt("X", pos.getX());
		tag.putInt("Y", pos.getY());
		tag.putInt("Z", pos.getZ());
		return tag;
	}

	public static BlockPos read(CompoundTag tag)
	{
		return new BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0));
	}
}
