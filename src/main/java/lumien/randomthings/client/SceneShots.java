package lumien.randomthings.client;

import java.io.File;
import java.util.List;

import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.debug.RenderScenes;
import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ScreenShotHelper;

/**
 * Developer-only client driver for the render screenshot pass ({@code RT_AUTOTEST_SCENES=true}): asks the server to
 * build each {@link RenderScenes} scene, waits for the chunks/entities to render, saves a screenshot with the HUD
 * hidden to {@code screenshots/randomthings_debug/scenes/<id>.png}, then pages through the Random Things creative tab
 * (item icons) saving one screenshot per page.
 */
public final class SceneShots
{
	private enum Phase
	{
		START, SCENES, CREATIVE, DONE
	}

	private static Phase phase = Phase.START;
	private static List<String> ids;
	private static int index;
	private static int wait;
	private static boolean requested;
	private static int creativePage;
	private static int creativePages;

	private SceneShots()
	{
	}

	/** @return true when everything has been shot */
	public static boolean tick(Minecraft mc)
	{
		switch (phase)
		{
			case START:
				ids = RenderScenes.ids();
				String only = System.getProperty("randomthings.autotest.scenes.only", "").trim();

				if (!only.isEmpty())
				{
					ids.retainAll(java.util.Arrays.asList(only.split(",")));
				}
				mc.gameSettings.hideGUI = true;
				mc.player.sendChatMessage("/gamemode creative");
				RTDebug.log("SceneShots: {} scenes", ids.size());
				for (String b : new String[] { "minecraft:plains", "minecraft:desert", "minecraft:snowy_tundra" })
				{
					net.minecraft.world.biome.Biome biome = net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.util.ResourceLocation(b));
					RTDebug.log("SceneShots: biome color {} = {} (biome {})", b, biome == null ? "no biome" : Integer.toHexString(lumien.randomthings.util.BiomeColorUtil.getBiomeColor(null, biome, mc.player.getPosition())), biome);
				}
				phase = Phase.SCENES;
				wait = 20;
				return false;
			case SCENES:
				if (wait-- > 0)
				{
					return false;
				}

				if (index >= ids.size())
				{
					mc.gameSettings.hideGUI = false;

					if (!System.getProperty("randomthings.autotest.scenes.only", "").trim().isEmpty())
					{
						phase = Phase.DONE;
						return true;
					}

					startCreative(mc);
					return false;
				}

				String id = ids.get(index);

				if (!requested)
				{
					mc.player.sendChatMessage("/rtdebug scene " + id);
					requested = true;
					wait = RenderScenes.waitTicks(id);
					return false;
				}

				save(mc, new File(mc.gameDir, "screenshots/randomthings_debug/scenes/" + id + ".png"));
				requested = false;
				index++;
				wait = 5;
				return false;
			case CREATIVE:
				if (wait-- > 0)
				{
					return false;
				}

				if (!(mc.currentScreen instanceof CreativeScreen))
				{
					phase = Phase.DONE;
					return true;
				}

				save(mc, new File(mc.gameDir, "screenshots/randomthings_debug/scenes/items_" + creativePage + ".png"));
				creativePage++;

				if (creativePage >= creativePages)
				{
					mc.displayGuiScreen(null);
					phase = Phase.DONE;
					return true;
				}

				// one page = five rows
				for (int i = 0; i < 5; i++)
				{
					mc.currentScreen.mouseScrolled(mc.currentScreen.width / 2.0, mc.currentScreen.height / 2.0, -1.0);
				}

				wait = 10;
				return false;
			default:
				return true;
		}
	}

	private static void startCreative(Minecraft mc)
	{
		NonNullList<ItemStack> stacks = NonNullList.create();
		ModItems.RT_ITEM_GROUP.fill(stacks);
		int rows = (stacks.size() + 8) / 9;
		creativePages = Math.max(1, (rows - 5 + 4) / 5 + 1);
		creativePage = 0;

		CreativeScreen screen = new CreativeScreen(mc.player);
		mc.displayGuiScreen(screen);
		net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue(CreativeScreen.class, null, ModItems.RT_ITEM_GROUP.getIndex(), "field_147058_w");
		try
		{
			java.lang.reflect.Method select = net.minecraftforge.fml.common.ObfuscationReflectionHelper.findMethod(CreativeScreen.class, "func_147050_b", net.minecraft.item.ItemGroup.class);
			select.invoke(screen, ModItems.RT_ITEM_GROUP);
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
		RTDebug.log("SceneShots: {} items in the creative tab, {} pages", stacks.size(), creativePages);
		phase = Phase.CREATIVE;
		wait = 20;
	}

	private static void save(Minecraft mc, File out)
	{
		try
		{
			out.getParentFile().mkdirs();

			try (NativeImage image = ScreenShotHelper.createScreenshot(mc.getMainWindow().getFramebufferWidth(), mc.getMainWindow().getFramebufferHeight(), mc.getFramebuffer()))
			{
				image.write(out);
			}

			RTDebug.log("SceneShots: saved {}", out.getName());
		}
		catch (Exception e)
		{
			RTDebug.log("SceneShots: screenshot failed for {}: {}", out.getName(), e.toString());
		}
	}
}
