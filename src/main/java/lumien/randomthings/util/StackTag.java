package lumien.randomthings.util;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 1.20.5+ replaced the free-form item NBT ({@code ItemStack.getTag()}) with data components. Everything this mod stores on a stack
 * lives in the {@code minecraft:custom_data} component, under the same keys the 1.12.2-1.20.1 versions used in the item NBT.
 * <p>
 * {@link #get} and {@link #getOrCreate} return COPIES: to change the stack call {@link #edit} or {@link #set}.
 */
public final class StackTag
{
	private StackTag()
	{
	}

	/** The custom data of the stack, or null if it has none. A copy. */
	public static CompoundTag get(ItemStack stack)
	{
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null || data.isEmpty() ? null : data.copyTag();
	}

	public static boolean has(ItemStack stack)
	{
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && !data.isEmpty();
	}

	/** A copy of the custom data (empty if there is none) that the caller may fill and hand back through {@link #set}. */
	public static CompoundTag getOrCreate(ItemStack stack)
	{
		CompoundTag tag = get(stack);
		return tag == null ? new CompoundTag() : tag;
	}

	public static void set(ItemStack stack, CompoundTag tag)
	{
		if (tag == null || tag.isEmpty())
		{
			stack.remove(DataComponents.CUSTOM_DATA);
		}
		else
		{
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

	public static void edit(ItemStack stack, Consumer<CompoundTag> editor)
	{
		CustomData.update(DataComponents.CUSTOM_DATA, stack, editor);
	}

	// Read-only shortcuts that do not copy the whole tag (used by per-frame code such as tooltips, armor and glint colours).
	private static CompoundTag peek(ItemStack stack)
	{
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? null : data.copyTag();
	}

	public static boolean contains(ItemStack stack, String key)
	{
		CompoundTag tag = peek(stack);
		return tag != null && tag.contains(key);
	}

	public static int getInt(ItemStack stack, String key)
	{
		CompoundTag tag = peek(stack);
		return tag == null ? 0 : tag.getIntOr(key, 0);
	}

	public static boolean getBoolean(ItemStack stack, String key)
	{
		CompoundTag tag = peek(stack);
		return tag != null && tag.getBooleanOr(key, false);
	}

	public static String getString(ItemStack stack, String key)
	{
		CompoundTag tag = peek(stack);
		return tag == null ? "" : tag.getStringOr(key, "");
	}

	/** A copy of the sub-compound {@code key}, or null. */
	public static CompoundTag getElement(ItemStack stack, String key)
	{
		CompoundTag tag = get(stack);
		return tag != null && tag.contains(key) ? tag.getCompoundOrEmpty(key) : null;
	}

	/** The registry access item stacks are (de)serialized with: the running server's, else the client level's. */
	public static net.minecraft.core.HolderLookup.Provider provider()
	{
		net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server != null)
		{
			return server.registryAccess();
		}
		return lumien.randomthings.client.ClientHooks.registryAccess();
	}

	public static CompoundTag saveStack(ItemStack stack)
	{
		net.minecraft.nbt.Tag saved = ItemStack.OPTIONAL_CODEC.encodeStart(provider().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).result().orElse(new CompoundTag());
		return saved instanceof CompoundTag compound ? compound : new CompoundTag();
	}

	public static ItemStack loadStack(CompoundTag tag)
	{
		return ItemStack.OPTIONAL_CODEC.parse(provider().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
	}

	public static void remove(ItemStack stack, String key)
	{
		if (has(stack))
		{
			edit(stack, t -> t.remove(key));
		}
	}
}
