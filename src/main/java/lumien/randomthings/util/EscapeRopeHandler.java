package lumien.randomthings.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lumien.randomthings.item.EscapeRopeItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Drives {@link EscapeRopeItem}'s "find the nearest path to daylight"
 * search. Direct port of 1.12.2's {@code EscapeRopeHandler}: a flood-fill
 * outward from the player, throttled to 4 block-checks/server-tick so a long
 * search never causes a visible hitch, teleporting the player up once it
 * finds a position that can see sky (landing on the first solid block
 * looking straight down from there, or the spot itself if none is found).
 * Ticked from a {@code TickEvent.ServerTickEvent} listener in
 * {@code RandomThings} (matches the original's own call site - this needs
 * to run once per server tick regardless of how many dimensions are loaded,
 * not once per world).
 */
public class EscapeRopeHandler
{
	private static EscapeRopeHandler INSTANCE;

	private final List<Task> runningTasks = new ArrayList<>();

	private static class Task
	{
		WeakReference<ServerPlayerEntity> player;
		ArrayList<BlockPos> toCheck = new ArrayList<>();
		HashSet<BlockPos> alreadyChecked = new HashSet<>();
	}

	public void addTask(ServerPlayerEntity player)
	{
		Task t = new Task();
		t.player = new WeakReference<>(player);
		t.toCheck.add(player.getPosition());

		this.runningTasks.add(t);
	}

	public void tick()
	{
		Iterator<Task> iterator = runningTasks.iterator();

		while (iterator.hasNext())
		{
			Task t = iterator.next();
			ServerPlayerEntity actualPlayer = t.player.get();

			if (actualPlayer == null || actualPlayer.world == null)
			{
				iterator.remove();
				continue;
			}

			ItemStack holding = actualPlayer.getActiveItemStack();

			if (holding.isEmpty() || !(holding.getItem() instanceof EscapeRopeItem))
			{
				continue;
			}

			ArrayList<BlockPos> toCheck = t.toCheck;
			HashSet<BlockPos> alreadyChecked = t.alreadyChecked;
			World world = actualPlayer.world;

			for (int runs = 0; runs < 4; runs++)
			{
				boolean finished = false;

				if (toCheck.isEmpty() || alreadyChecked.size() > 10000)
				{
					finished = true;

					actualPlayer.dropItem(holding, false);
					actualPlayer.setHeldItem(actualPlayer.getActiveHand(), ItemStack.EMPTY);
				}
				else
				{
					BlockPos nextPos = toCheck.remove(toCheck.size() - 1);

					while (alreadyChecked.contains(nextPos) && toCheck.size() > 0)
					{
						nextPos = toCheck.remove(toCheck.size() - 1);
					}

					if (!alreadyChecked.contains(nextPos))
					{
						if (world.chunkExists(nextPos.getX() >> 4, nextPos.getZ() >> 4) && (world.isAirBlock(nextPos) || world.getBlockState(nextPos).getCollisionShape(world, nextPos).isEmpty()))
						{
							if (world.canBlockSeeSky(nextPos))
							{
								// Excludes actualPlayer from this first sound (played at the old
								// position, before teleporting) - their own client plays the
								// teleport sound independently once it receives the new position,
								// so this is just for anyone else nearby. The second call below
								// (after teleporting) passes null so the traveling player also
								// hears their own arrival.
								world.playSound(actualPlayer, actualPlayer.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1);

								boolean foundSolid = false;

								for (int y = nextPos.getY(); y >= 0; y--)
								{
									BlockPos target = new BlockPos(nextPos.getX(), y, nextPos.getZ());

									if (world.getBlockState(target).isSolid())
									{
										actualPlayer.connection.setPlayerLocation(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, actualPlayer.rotationYaw, actualPlayer.rotationPitch);
										foundSolid = true;
										break;
									}
								}

								if (!foundSolid)
								{
									actualPlayer.connection.setPlayerLocation(nextPos.getX() + 0.5, nextPos.getY() + 1, nextPos.getZ() + 0.5, actualPlayer.rotationYaw, actualPlayer.rotationPitch);
								}

								actualPlayer.resetActiveHand();

								world.playSound(null, actualPlayer.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1);

								holding.damageItem(1, actualPlayer, (p) -> p.sendBreakAnimation(actualPlayer.getActiveHand()));
								finished = true;
							}
							else
							{
								alreadyChecked.add(nextPos);

								if (!alreadyChecked.contains(nextPos.offset(Direction.DOWN)))
								{
									toCheck.add(nextPos.offset(Direction.DOWN));
								}

								for (Direction facing : Direction.Plane.HORIZONTAL)
								{
									BlockPos addPos = nextPos.offset(facing);

									if (!alreadyChecked.contains(addPos))
									{
										toCheck.add(addPos);
									}
								}

								if (!alreadyChecked.contains(nextPos.offset(Direction.UP)))
								{
									toCheck.add(nextPos.offset(Direction.UP));
								}
							}
						}
					}
				}

				if (finished)
				{
					iterator.remove();
					break;
				}
			}
		}
	}

	public static EscapeRopeHandler getInstance()
	{
		return INSTANCE != null ? INSTANCE : (INSTANCE = new EscapeRopeHandler());
	}
}
