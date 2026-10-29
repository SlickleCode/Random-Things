package lumien.randomthings.util;

import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;

/**
 * Reads a UUID saved with {@code NBTUtil.func_240626_a_} (an int array since 1.16). Worlds written by the 1.14.4 version saved it as a compound with the long
 * halves {@code M} and {@code L}: those are accepted too, so an old world keeps its Floo network and Block Breaker owners.
 */
public final class UuidTag
{
	private UuidTag()
	{
	}

	public static UUID read(INBT tag)
	{
		if (tag instanceof CompoundNBT)
		{
			CompoundNBT compound = (CompoundNBT) tag;

			if (compound.contains("M") && compound.contains("L"))
			{
				return new UUID(compound.getLong("M"), compound.getLong("L"));
			}
		}

		return NBTUtil.readUniqueId(tag);
	}
}
