package lumien.randomthings.debug.test;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

/**
 * The test player used on a dedicated server. Forge 1.16's {@link FakePlayer} reports its position as (0, 0, 0) from
 * {@code getPosition()} / {@code getPositionVec()} no matter where it is, which breaks every test (and every feature)
 * that looks up what is near the player; this one reports the real position and is otherwise a normal fake player.
 */
public class RTTestPlayer extends FakePlayer
{
	public RTTestPlayer(ServerWorld world)
	{
		super(world, new GameProfile(UUID.nameUUIDFromBytes("RTTestPlayer".getBytes()), "RTTestPlayer"));
	}

	@Override
	public Vector3d getPositionVec()
	{
		return new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ());
	}

	@Override
	public BlockPos getPosition()
	{
		return new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
	}
}
