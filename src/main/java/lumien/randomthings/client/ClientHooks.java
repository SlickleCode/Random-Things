package lumien.randomthings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;

/** Client-only helpers that common code reaches through a call, so the dedicated server never loads this class. */
public class ClientHooks
{
	public static net.minecraft.world.level.Level level()
	{
		return Minecraft.getInstance().level;
	}

	public static HolderLookup.Provider registryAccess()
	{
		if (Minecraft.getInstance().level != null)
		{
			return Minecraft.getInstance().level.registryAccess();
		}
		return RegistryAccess.EMPTY;
	}
}
