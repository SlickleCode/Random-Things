package lumien.randomthings.debug;

import lumien.randomthings.debug.test.RTTestRunner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.event.server.ServerStartingEvent;

/** Registers all developer-only hooks. Only ever called when {@link DevMode#ENABLED}. */
public final class DevTools
{
	private DevTools()
	{
	}

	public static void init()
	{
		RTDebug.log("Developer tools enabled (-Drandomthings.debug=true)");

		MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.RegisterCommandsEvent event) -> RTDebugCommand.register(event.getDispatcher()));
		MinecraftForge.EVENT_BUS.addListener(RTTestRunner::onServerTick);
		lumien.randomthings.debug.test.ServerAutoTest.init();

		DistExecutor.runWhenOn(Dist.CLIENT, () -> lumien.randomthings.client.DevToolsClient::init);
	}
}
