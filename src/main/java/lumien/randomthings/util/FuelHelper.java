package lumien.randomthings.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Furnace fuel values moved from a static map ({@code AbstractFurnaceBlockEntity.getFuel()}) to {@code FuelValues}, which every level owns
 * (1.21.2). The Potion Vaporizer asks without a level at hand (its item handler is built before it is placed), so use the running server's
 * values and, on a client connected to a remote server, the client level's.
 */
public final class FuelHelper
{
	private FuelHelper()
	{
	}

	/** Burn time in ticks, 0 for a non-fuel (or when no world is loaded). */
	public static int burnTime(ItemStack stack)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

		if (server != null)
		{
			return server.fuelValues().burnDuration(stack);
		}

		return FMLEnvironment.dist.isClient() ? lumien.randomthings.client.ClientFuel.burnTime(stack) : 0;
	}
}
