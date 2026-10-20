package lumien.randomthings.debug.test;

import java.util.List;
import java.util.Objects;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

/** What a test step gets: the world, a private build area, and assertion helpers. */
public final class TestCtx
{
	public static final class Failure extends RuntimeException
	{
		public Failure(String message)
		{
			super(message);
		}
	}

	/** Thrown to mark a test as not applicable (counted separately from failures). */
	public static final class Skip extends RuntimeException
	{
		public Skip(String message)
		{
			super(message);
		}
	}

	private static final int RADIUS = 7;

	public final ServerLevel world;
	public final BlockPos origin;
	public final ServerPlayer player;

	TestCtx(ServerLevel world, BlockPos origin, ServerPlayer player)
	{
		this.world = world;
		this.origin = origin;
		this.player = player;
	}

	public BlockPos at(int x, int y, int z)
	{
		return origin.offset(x, y, z);
	}

	/** Places a block with normal neighbor updates (so redstone/neighbor logic reacts). */
	public void set(BlockPos pos, BlockState state)
	{
		world.setBlock(pos, state, 3);
	}

	public void set(BlockPos pos, Block block)
	{
		set(pos, block.defaultBlockState());
	}

	/** Places a block without neighbor reactions (for scaffolding). */
	public void setQuiet(BlockPos pos, BlockState state)
	{
		world.setBlock(pos, state, 18);
	}

	public void remove(BlockPos pos)
	{
		world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
	}

	/** A stone floor one block below the origin plane. */
	public void floor()
	{
		for (int x = -RADIUS; x <= RADIUS; x++)
		{
			for (int z = -RADIUS; z <= RADIUS; z++)
			{
				setQuiet(at(x, 0, z), Blocks.STONE.defaultBlockState());
			}
		}
	}

	AABB area()
	{
		return new AABB(origin.offset(-RADIUS, -2, -RADIUS), origin.offset(RADIUS + 1, RADIUS + 1, RADIUS + 1));
	}

	/** Empties the whole build area (blocks, tile entities, loose entities). */
	void clear()
	{
		for (int x = -RADIUS; x <= RADIUS; x++)
		{
			for (int y = -2; y <= RADIUS; y++)
			{
				for (int z = -RADIUS; z <= RADIUS; z++)
				{
					BlockPos pos = origin.offset(x, y, z);

					if (!world.isEmptyBlock(pos))
					{
						world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
					}
				}
			}
		}

		for (Entity e : world.getEntitiesOfClass(Entity.class, area(), entity -> !(entity instanceof Player)))
		{
			e.discard();
		}
	}

	public ItemEntity spawnItem(double x, double y, double z, ItemStack stack)
	{
		ItemEntity entity = new ItemEntity(world, origin.getX() + x, origin.getY() + y, origin.getZ() + z, stack);
		entity.setDeltaMovement(0, 0, 0);
		world.addFreshEntity(entity);
		return entity;
	}

	public List<ItemEntity> itemEntities()
	{
		return world.getEntitiesOfClass(ItemEntity.class, area(), ItemEntity::isAlive);
	}

	/** The item handler at a position (any side), wrapping plain inventories if needed. */
	public IItemHandler inventory(BlockPos pos)
	{
		BlockEntity te = world.getBlockEntity(pos);

		if (te == null)
		{
			throw new Failure("no tile entity at " + pos);
		}

		IItemHandler handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);

		if (handler == null && te instanceof Container)
		{
			handler = new InvWrapper((Container) te);
		}

		if (handler == null)
		{
			throw new Failure("tile entity at " + pos + " (" + te.getClass().getSimpleName() + ") has no item handler");
		}

		return handler;
	}

	public static int count(IItemHandler handler, Item item)
	{
		int total = 0;

		for (int i = 0; i < handler.getSlots(); i++)
		{
			ItemStack stack = handler.getStackInSlot(i);

			if (stack.getItem() == item)
			{
				total += stack.getCount();
			}
		}

		return total;
	}

	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> T tile(BlockPos pos, Class<T> type)
	{
		BlockEntity te = world.getBlockEntity(pos);

		if (!type.isInstance(te))
		{
			throw new Failure("expected " + type.getSimpleName() + " at " + pos + " but found " + (te == null ? "nothing" : te.getClass().getSimpleName()));
		}

		return (T) te;
	}

	public void check(boolean condition, String message)
	{
		if (!condition)
		{
			throw new Failure(message);
		}
	}

	public void eq(Object expected, Object actual, String what)
	{
		if (!Objects.equals(expected, actual))
		{
			throw new Failure(what + ": expected <" + expected + "> but was <" + actual + ">");
		}
	}
}
