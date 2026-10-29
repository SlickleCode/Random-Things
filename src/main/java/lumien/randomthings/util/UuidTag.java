package lumien.randomthings.util;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

/**
 * Reads a UUID saved with {@code NbtUtils.createUUID} (an int array since 1.16). Worlds written by the 1.14.4 version saved it as a compound with the long
 * halves {@code M} and {@code L}: those are accepted too, so an old world keeps its Floo network and Block Breaker owners.
 */
public final class UuidTag
{
	private UuidTag()
	{
	}

	public static UUID read(Tag tag)
	{
		if (tag instanceof CompoundTag compound && compound.contains("M") && compound.contains("L"))
		{
			return new UUID(compound.getLong("M"), compound.getLong("L"));
		}

		return NbtUtils.loadUUID(tag);
	}
}
