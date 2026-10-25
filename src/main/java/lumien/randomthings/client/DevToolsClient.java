package lumien.randomthings.client;

import net.minecraftforge.common.MinecraftForge;

/** Client half of {@link lumien.randomthings.debug.DevTools}; only loaded in dev instances. */
public final class DevToolsClient
{
	private DevToolsClient()
	{
	}

	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(GuiDebugScreenshots.class);
		MinecraftForge.EVENT_BUS.register(GuiOpenTriggerLog.class);
		MinecraftForge.EVENT_BUS.register(ParticleProbe.class);
		MinecraftForge.EVENT_BUS.register(CommandChannel.class);

		if (Boolean.getBoolean("randomthings.autotest"))
		{
			MinecraftForge.EVENT_BUS.register(AutoTest.class);
		}
	}
}
