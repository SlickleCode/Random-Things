package lumien.randomthings.client;

import net.minecraft.client.Minecraft;

/** Client-only helpers that common code reaches through a call, so the dedicated server never loads this class. */
public class ClientHooks
{
	public static net.minecraft.world.World level()
	{
		return Minecraft.getInstance().world;
	}
}
