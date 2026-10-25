package lumien.randomthings.util;

import com.mojang.serialization.MapCodec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Bridge between the CompoundTag based save/load code of the 1.12.2 era and the ValueInput/ValueOutput API of 1.21.6+: a whole compound is stored as
 * the fields of the output (so the saved format stays the same as before) and read back as one compound.
 */
public final class TagIO
{
	private static final MapCodec<CompoundTag> COMPOUND = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);

	private TagIO()
	{
	}

	public static void write(ValueOutput output, CompoundTag tag)
	{
		output.store(COMPOUND, tag);
	}

	public static CompoundTag read(ValueInput input)
	{
		return input.read(COMPOUND).orElseGet(CompoundTag::new);
	}

	/** A UUID as the int array vanilla stores it in NBT (the old NbtUtils.createUUID). */
	public static net.minecraft.nbt.Tag writeUuid(java.util.UUID uuid)
	{
		return new net.minecraft.nbt.IntArrayTag(net.minecraft.core.UUIDUtil.uuidToIntArray(uuid));
	}

	/** The UUID of an int array tag, or null (the old NbtUtils.loadUUID). */
	public static java.util.UUID readUuid(net.minecraft.nbt.Tag tag)
	{
		return tag instanceof net.minecraft.nbt.IntArrayTag array && array.size() == 4 ? net.minecraft.core.UUIDUtil.uuidFromIntArray(array.getAsIntArray()) : null;
	}

	/** What Entity.save(CompoundTag) used to give: the entity as a compound (1.21.6+ saves through a ValueOutput). */
	public static CompoundTag saveEntity(net.minecraft.world.entity.Entity entity)
	{
		net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
		entity.save(output);
		return output.buildResult();
	}

	/** What EntityType.create(CompoundTag, Level, reason) used to do. */
	public static java.util.Optional<net.minecraft.world.entity.Entity> createEntity(CompoundTag tag, net.minecraft.world.level.Level level)
	{
		ValueInput input = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), tag);
		return net.minecraft.world.entity.EntityType.create(input, level, net.minecraft.world.entity.EntitySpawnReason.LOAD);
	}
}
