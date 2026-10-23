package lumien.randomthings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Client half of {@link lumien.randomthings.util.FuelHelper}. */
public final class ClientFuel
{
	private ClientFuel()
	{
	}

	public static int burnTime(ItemStack stack)
	{
		return Minecraft.getInstance().level != null ? Minecraft.getInstance().level.fuelValues().burnDuration(stack) : 0;
	}
}
