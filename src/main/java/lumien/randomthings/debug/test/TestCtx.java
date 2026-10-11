package lumien.randomthings.debug.test;

import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.CapabilityItemHandler;
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

	public final ServerWorld world;
	public final BlockPos origin;
	public final ServerPlayerEntity player;

	TestCtx(ServerWorld world, BlockPos origin, ServerPlayerEntity player)
	{
		this.world = world;
		this.origin = origin;
		this.player = player;
	}

	public BlockPos at(int x, int y, int z)
	{
		return origin.add(x, y, z);
	}

	/** Places a block with normal neighbor updates (so redstone/neighbor logic reacts). */
	public void set(BlockPos pos, BlockState state)
	{
		world.setBlockState(pos, state, 3);
	}

	public void set(BlockPos pos, Block block)
	{
		set(pos, block.getDefaultState());
	}

	/** Places a block without neighbor reactions (for scaffolding). */
	public void setQuiet(BlockPos pos, BlockState state)
	{
		world.setBlockState(pos, state, 18);
	}

	public void remove(BlockPos pos)
	{
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
	}

	/** A stone floor one block below the origin plane. */
	public void floor()
	{
		for (int x = -RADIUS; x <= RADIUS; x++)
		{
			for (int z = -RADIUS; z <= RADIUS; z++)
			{
				setQuiet(at(x, 0, z), Blocks.STONE.getDefaultState());
			}
		}
	}

	AxisAlignedBB area()
	{
		return new AxisAlignedBB(origin.add(-RADIUS, -2, -RADIUS), origin.add(RADIUS + 1, RADIUS + 1, RADIUS + 1));
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
					BlockPos pos = origin.add(x, y, z);

					if (!world.isAirBlock(pos))
					{
						world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
					}
				}
			}
		}

		for (Entity e : world.getEntitiesWithinAABB(Entity.class, area(), entity -> !(entity instanceof PlayerEntity)))
		{
			e.remove();
		}
	}

	public ItemEntity spawnItem(double x, double y, double z, ItemStack stack)
	{
		ItemEntity entity = new ItemEntity(world, origin.getX() + x, origin.getY() + y, origin.getZ() + z, stack);
		entity.setMotion(0, 0, 0);
		world.addEntity(entity);
		return entity;
	}

	public List<ItemEntity> itemEntities()
	{
		return world.getEntitiesWithinAABB(ItemEntity.class, area(), ItemEntity::isAlive);
	}

	/** The item handler at a position (any side), wrapping plain inventories if needed. */
	public IItemHandler inventory(BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);

		if (te == null)
		{
			throw new Failure("no tile entity at " + pos);
		}

		IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);

		if (handler == null && te instanceof IInventory)
		{
			handler = new InvWrapper((IInventory) te);
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
	public <T extends TileEntity> T tile(BlockPos pos, Class<T> type)
	{
		TileEntity te = world.getTileEntity(pos);

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
