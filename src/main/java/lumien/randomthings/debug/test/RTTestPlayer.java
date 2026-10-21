package lumien.randomthings.debug.test;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * The test player used on a dedicated server. Forge 1.16's {@link FakePlayer} reports its position as (0, 0, 0) from
 * {@code getPosition()} / {@code getPositionVec()} no matter where it is, which breaks every test (and every feature)
 * that looks up what is near the player; this one reports the real position and is otherwise a normal fake player.
 */
public class RTTestPlayer extends FakePlayer
{
	public RTTestPlayer(ServerLevel world)
	{
		super(world, new GameProfile(UUID.nameUUIDFromBytes("RTTestPlayer".getBytes()), "RTTestPlayer"));
	}

	@Override
	public Vec3 position()
	{
		return new Vec3(this.getX(), this.getY(), this.getZ());
	}

	@Override
	public BlockPos blockPosition()
	{
		return BlockPos.containing(this.getX(), this.getY(), this.getZ());
	}
}
