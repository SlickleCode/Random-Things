package lumien.randomthings.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Minecraft 1.21.2 made the registry id part of {@code Block.Properties}/{@code Item.Properties} (it feeds the lang key, the loot table and the item model).
 * {@link RTRegistry#register(String, java.util.function.Supplier)} publishes the id of the object it is about to build; every properties builder in this
 * mod starts from here so the id is set without threading it through hundreds of constructors.
 */
public final class RTIds
{
	private static final ThreadLocal<Identifier> CURRENT = new ThreadLocal<>();

	private RTIds()
	{
	}

	static Identifier swap(Identifier id)
	{
		Identifier previous = CURRENT.get();

		if (id == null)
		{
			CURRENT.remove();
		}
		else
		{
			CURRENT.set(id);
		}

		return previous;
	}

	private static Identifier current()
	{
		Identifier id = CURRENT.get();

		if (id == null)
		{
			throw new IllegalStateException("Block/Item properties can only be created while registering (see RTRegistry.register with a Supplier)");
		}

		return id;
	}

	public static BlockBehaviour.Properties block()
	{
		return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, current()));
	}

	public static BlockBehaviour.Properties blockCopy(BlockBehaviour block)
	{
		return BlockBehaviour.Properties.ofFullCopy(block).setId(ResourceKey.create(Registries.BLOCK, current()));
	}

	public static Item.Properties item()
	{
		return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, current()));
	}
}
