package lumien.randomthings.client;

import net.minecraft.core.registries.Registries;

import java.io.File;
import java.util.List;

import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.debug.RenderScenes;
import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.client.Screenshot;

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
	private static boolean hoodPlayersAdded;
	private static final java.util.List<Integer> remotePlayers = new java.util.ArrayList<>();
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
				mc.options.hideGui = true;
				mc.player.connection.sendUnsignedCommand("gamemode creative");
				RTDebug.log("SceneShots: {} scenes", ids.size());
				for (String b : new String[] { "minecraft:plains", "minecraft:desert", "minecraft:snowy_plains" })
				{
					net.minecraft.world.level.biome.Biome biome = lumien.randomthings.util.BiomeUtil.byId(net.minecraft.resources.ResourceLocation.parse(b));
					RTDebug.log("SceneShots: biome color {} = {} (biome {})", b, biome == null ? "no biome" : Integer.toHexString(lumien.randomthings.util.BiomeColorUtil.getBiomeColor(null, biome, mc.player.blockPosition())), biome);
				}
				phase = Phase.SCENES;
				wait = 20;
				return false;
			case SCENES:
				if (requested && !hoodPlayersAdded && wait == 30 && index < ids.size() && ids.get(index).equals("magic_hood"))
				{
					addHoodPlayers(mc);
				}

				if (wait-- > 0)
				{
					return false;
				}

				if (index >= ids.size())
				{
					mc.options.hideGui = false;

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
					mc.player.connection.sendUnsignedCommand("rtdebug scene " + id);
					requested = true;
					wait = RenderScenes.waitTicks(id);
					return false;
				}

				save(mc, new File(mc.gameDirectory, "screenshots/randomthings_debug/scenes/" + id + ".png"));

				if (hoodPlayersAdded)
				{
					for (int entityId : remotePlayers)
					{
						mc.level.removeEntity(entityId, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
					}

					remotePlayers.clear();
					hoodPlayersAdded = false;
					mc.options.hideGui = true;
				}

				requested = false;
				index++;
				wait = 5;
				return false;
			case CREATIVE:
				if (wait-- > 0)
				{
					return false;
				}

				if (!(mc.screen instanceof CreativeModeInventoryScreen))
				{
					phase = Phase.DONE;
					return true;
				}

				save(mc, new File(mc.gameDirectory, "screenshots/randomthings_debug/scenes/items_" + creativePage + ".png"));
				creativePage++;

				if (creativePage >= creativePages)
				{
					mc.setScreen(null);
					phase = Phase.DONE;
					return true;
				}

				// one page = five rows
				for (int i = 0; i < 5; i++)
				{
					mc.screen.mouseScrolled(mc.screen.width / 2.0, mc.screen.height / 2.0, 0.0, -1.0);
				}

				wait = 10;
				return false;
			default:
				return true;
		}
	}

	/**
	 * Magic Hood scene: two other players in front of the camera, one wearing the hood. Vanilla draws a player's nameplate;
	 * the hood must hide it ({@code MagicHoodTransformer}), which only shows with the GUI visible.
	 */
	private static void addHoodPlayers(Minecraft mc)
	{
		double y = mc.player.getY() - 2.0;
		String[] names = { "NoHood", "WithHood" };

		for (int i = 0; i < 2; i++)
		{
			net.minecraft.client.player.RemotePlayer other = new net.minecraft.client.player.RemotePlayer(mc.level, new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes(names[i].getBytes()), names[i]));
			other.setPos(mc.player.getX() + (i == 0 ? -1.2 : 1.2), y, mc.player.getZ() + 4.0);
			other.setYRot(180.0F);
			other.yHeadRot = 180.0F;

			if (i == 1)
			{
				other.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(ModItems.MAGIC_HOOD));
			}

			int entityId = -9000 - i;
			mc.level.addEntity(other);
			remotePlayers.add(entityId);
		}

		hoodPlayersAdded = true;
		mc.options.hideGui = false;
	}

	private static void startCreative(Minecraft mc)
	{
		net.minecraft.world.flag.FeatureFlagSet flags = mc.player.connection.enabledFeatures();
		net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(flags, true, mc.level.registryAccess());
		java.util.Collection<ItemStack> stacks = ModItems.RT_ITEM_GROUP.getDisplayItems();
		int rows = (stacks.size() + 8) / 9;
		creativePages = Math.max(1, (rows - 5 + 4) / 5 + 1);
		creativePage = 0;

		CreativeModeInventoryScreen screen = new CreativeModeInventoryScreen(mc.player, flags, true);
		mc.setScreen(screen);

		try
		{
			// the tab selection is private; m_98560_ is its SRG id (remapped to the Mojang name in dev runs)
			java.lang.reflect.Method select = net.neoforged.fml.util.ObfuscationReflectionHelper.findMethod(CreativeModeInventoryScreen.class, "selectTab", net.minecraft.world.item.CreativeModeTab.class);
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

			try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget()))
			{
				image.writeToFile(out);
			}

			RTDebug.log("SceneShots: saved {}", out.getName());
		}
		catch (Exception e)
		{
			RTDebug.log("SceneShots: screenshot failed for {}: {}", out.getName(), e.toString());
		}
	}
}
