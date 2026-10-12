package lumien.randomthings.client;

import lumien.randomthings.util.WeakSets;
import lumien.randomthings.asm.AsmHandler;
import lumien.randomthings.block.*;
import lumien.randomthings.client.renderer.*;
import lumien.randomthings.client.screen.ModScreens;
import lumien.randomthings.container.ModContainerTypes;
import lumien.randomthings.entity.*;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.handler.RTWorldInformationHandler;
import lumien.randomthings.item.*;
import lumien.randomthings.lib.IRTBlockColor;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.PlayedSoundMessage;
import lumien.randomthings.potion.ModEffects;
import lumien.randomthings.recipes.ModRecipeSerializers;
import lumien.randomthings.recipes.imbuing.ImbuingRecipeHandler;
import lumien.randomthings.tileentity.*;
import lumien.randomthings.util.EscapeRopeHandler;
import lumien.randomthings.util.InventoryUtil;
import lumien.randomthings.worldgen.ModFeatures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Random;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;

/**
 * Everything in the mod that touches client-only classes (renderers, screens, colour handlers, sound hooks).
 * <p>
 * Kept out of {@link lumien.randomthings.RandomThings} on purpose: the mod main class is loaded on a dedicated
 * server too, and any reference to a {@code net.minecraft.client} class from it makes the loader refuse the
 * mod ("Attempted to load class ... for invalid dist DEDICATED_SERVER"). Only reached through
 * {@code DistExecutor.runWhenOn(Dist.CLIENT, ...)}.
 */
public class ClientSetup {
    public static void init() {
        net.minecraftforge.eventbus.api.IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientSetup::setupClient);
        modBus.addListener(ClientSetup::registerModels);
        modBus.addListener(ClientSetup::onBlockColorHandler);
        modBus.addListener(ClientSetup::onItemColorHandler);
        modBus.addListener(ClientSetup::onPlainItemColorHandler);

        // Spectre Anchor's tooltip: "Anchored" on any stack carrying the
        // "spectreAnchor" NBT tag, regardless of which item it is. Matches
        // 1.12.2's own RTEventHandler#itemTooltip (inserted at index 1, right
        // after the item's display name).
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.entity.player.ItemTooltipEvent event) -> {
            ItemStack stack = event.getItemStack();

            if (stack.hasTag() && stack.getTag().contains("spectreAnchor")) {
                event.getToolTip().add(1, new TranslationTextComponent("tooltip.randomthings.spectre_anchor.item").applyTextStyle(net.minecraft.util.text.TextFormatting.DARK_AQUA));
            }
        });

        MinecraftForge.EVENT_BUS.addListener((ClientTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().world == null) {
                return;
            }

            for (Entity entity : Minecraft.getInstance().world.getAllEntities()) {
                if (entity instanceof BoatEntity && SuperLubricentPhysics.isOnLubricentBlock(entity)) {
                    SuperLubricentPhysics.capBoatMotion((BoatEntity) entity);
                }
            }
        });

        MinecraftForge.EVENT_BUS.addListener((ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                DiviningRodRenderer.get().tick();
            }
        });

        // Drains lumien.randomthings.entity.SpectreIlluminatorRelight's queued
        // light-recheck work on the CLIENT side specifically. Real bug found and
        // fixed, 2026-09-27 (reported by user, confirmed via log: server-side
        // draining completed in ~5 seconds every time, but the client-side queue
        // - keyed by the client's own separate ClientWorld instance, a totally
        // different object from the server's ServerWorld even in singleplayer's
        // integrated server - never showed a single drain in 30+ seconds; only a
        // full relog, which resyncs already-correct server light data fresh, ever
        // made the change visible). The WorldTickEvent listener below evidently
        // doesn't fire for the client world the way the other WorldTickEvent
        // listeners' defensive `isRemote` checks implied it might - ClientTickEvent
        // is what this project already uses elsewhere for exactly this "runs on
        // the client every tick" need (see DiviningRodRenderer.get().tick() right
        // above).
        MinecraftForge.EVENT_BUS.addListener((ClientTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().world == null) {
                return;
            }

            lumien.randomthings.entity.SpectreIlluminatorRelight.tick(Minecraft.getInstance().world);
        });

    }

    private static void setupClient(final FMLClientSetupEvent event) {
        ModScreens.register();

        ClientRegistry.bindTileEntitySpecialRenderer(BiomeRadarTileEntity.class, new BiomeRadarTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(RuneBaseTileEntity.class, new RuneBaseTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(lumien.randomthings.tileentity.FluidDisplayTileEntity.class, new lumien.randomthings.client.renderer.FluidDisplayTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(lumien.randomthings.tileentity.DiaphanousBlockTileEntity.class, new lumien.randomthings.client.renderer.DiaphanousBlockTileEntityRenderer());

        RenderingRegistry.registerEntityRenderingHandler(FlooFireplaceEntity.class, FlooFireplaceEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EclipsedClockEntity.class, EclipsedClockEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ThrownWeatherEggEntity.class, manager -> new net.minecraft.client.renderer.entity.SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(WeatherCloudEntity.class, WeatherCloudEntityRenderer::new);
        // Custom renderer draws the bottle icon on all 6 faces of the target
        // block - per user request, 2026-09-27, replacing a brief SpriteRenderer
        // first pass (which can only billboard at one fixed spot) - see
        // TimeAcceleratorEntityRenderer's own javadoc.
        RenderingRegistry.registerEntityRenderingHandler(TimeAcceleratorEntity.class, lumien.randomthings.client.renderer.TimeAcceleratorEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(lumien.randomthings.entity.ThrownGoldenEggEntity.class, manager -> new net.minecraft.client.renderer.entity.SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(lumien.randomthings.entity.GoldenChickenEntity.class, lumien.randomthings.client.renderer.GoldenChickenEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(lumien.randomthings.entity.ArtificialEndPortalEntity.class, lumien.randomthings.client.renderer.ArtificialEndPortalEntityRenderer::new);
        // Item-icon billboard via vanilla's SpriteRenderer, same mechanism already
        // used for ThrownGoldenEggEntity/ThrownWeatherEggEntity above - per user
        // request, 2026-09-27, replacing the earlier no-visible-model design (see
        // SpectreIlluminatorEntity's own javadoc).
        RenderingRegistry.registerEntityRenderingHandler(lumien.randomthings.entity.SpectreIlluminatorEntity.class, manager -> new net.minecraft.client.renderer.entity.SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(lumien.randomthings.entity.SpiritEntity.class, lumien.randomthings.client.renderer.SpiritEntityRenderer::new);

        MinecraftForge.EVENT_BUS.addListener((RenderWorldLastEvent rwl) -> {
            DiviningRodRenderer.get().render();
            RedstoneObserverLineRenderer.render();
        });

        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onPlaySound);
    }

    /**
     * Registers {@code randomthings:portkey_base} as an extra "special"
     * model - not tied to any block or item's own auto-resolved model, just
     * an auxiliary resource {@link lumien.randomthings.client.renderer.PortkeyItemRenderer}
     * fetches directly via {@code ModelManager#getModel} at render time. See
     * that class's javadoc for why this indirection exists (rendering the
     * plain, uncamouflaged Portkey appearance from inside its own {@code
     * ItemStackTileEntityRenderer} without recursing back into itself).
     */
    private static void registerModels(final net.minecraftforge.client.event.ModelRegistryEvent event) {
        net.minecraftforge.client.model.ModelLoader.addSpecialModel(new net.minecraft.client.renderer.model.ModelResourceLocation(new ResourceLocation(lumien.randomthings.lib.ModConstants.MOD_ID, "portkey_base"), "inventory"));
    }

    /**
     * Mutes any sound matched by a nearby {@link SoundDampenerTileEntity} (20
     * blocks) or a carried {@link PortableSoundDampenerItem} (anywhere in the
     * player's own inventory - the original checked both a Baubles body slot
     * and the main inventory, but this port dropped Baubles, so only the
     * carried check remains, the same fallback already used for Obsidian
     * Skull/Lava Charm), and separately feeds every sound heard to any
     * actively-recording {@link SoundRecorderItem} anywhere in the inventory
     * (not just the held item). Direct port of 1.12.2's
     * {@code RTEventHandler.playSoundEvent}.
     */
    private static void onPlaySound(PlaySoundEvent event) {
        ClientPlayerEntity thePlayer = Minecraft.getInstance().player;

        if (thePlayer == null || event.isCanceled() || event.getSound() == null) {
            return;
        }

        ISound sound = event.getSound();

        if (sound.getCategory() == SoundCategory.MUSIC || sound.getCategory() == SoundCategory.RECORDS || Minecraft.getInstance().gameSettings.getSoundLevel(sound.getCategory()) <= 0) {
            return;
        }

        ResourceLocation soundLocation = sound.getSoundLocation();
        BlockPos soundPosition = new BlockPos(sound.getX(), sound.getY(), sound.getZ());

        for (SoundDampenerTileEntity dampener : WeakSets.snapshot(SoundDampenerTileEntity.loadedDampeners)) {
            if (dampener.getWorld() == thePlayer.world && !dampener.isRemoved() && dampener.getPos().distanceSq(soundPosition) < 20 * 20 && dampener.getMutedSounds().contains(soundLocation)) {
                lumien.randomthings.debug.RTDebug.log("muted sound {} (placed dampener)", soundLocation);
                event.setResultSound(null);
                return;
            }
        }

        for (int slot = 0; slot < thePlayer.inventory.mainInventory.size(); slot++) {
            ItemStack stack = thePlayer.inventory.getStackInSlot(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.SOUND_RECORDER && SoundRecorderItem.isRecording(stack)) {
                RTPacketHandler.sendToServer(new PlayedSoundMessage(soundLocation.toString(), slot));
            } else if (stack.getItem() == ModItems.PORTABLE_SOUND_DAMPENER) {
                if (mutesSound(PortableSoundDampenerItem.getInventory(stack), soundLocation)) {
                    lumien.randomthings.debug.RTDebug.log("muted sound {} (portable dampener)", soundLocation);
                    event.setResultSound(null);
                    return;
                }
            }
        }
    }

    private static boolean mutesSound(net.minecraftforge.items.IItemHandler inventory, ResourceLocation soundLocation) {
        for (int s = 0; s < inventory.getSlots(); s++) {
            ItemStack patternStack = inventory.getStackInSlot(s);

            if (!patternStack.isEmpty()) {
                ResourceLocation muted = SoundPatternItem.getSoundLocation(patternStack);

                if (soundLocation.equals(muted)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Generic bridge from the mod's own {@link IRTBlockColor} interface to
     * Forge's {@code BlockColors} system: any registered randomthings block
     * implementing it gets wired up here automatically, so individual blocks
     * never need their own {@code ColorHandlerEvent} listener.
     */
    @SubscribeEvent
    public static void onBlockColorHandler(final ColorHandlerEvent.Block event) {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof IRTBlockColor && block.getRegistryName() != null && ModConstants.MOD_ID.equals(block.getRegistryName().getNamespace())) {
                event.getBlockColors().register((state, worldIn, pos, tintIndex) -> ((IRTBlockColor) block).colorMultiplier(state, worldIn, pos, tintIndex), block);
            }
        }
    }

    /**
     * Same bridge for item icon tinting: any randomthings {@link BlockItem}
     * whose block implements {@link IRTBlockColor} gets its inventory icon
     * tinted using the client player's current world/position, matching the
     * 1.12.2 behavior of {@code ItemBlockColored}.
     */
    @SubscribeEvent
    public static void onItemColorHandler(final ColorHandlerEvent.Item event) {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof BlockItem && item.getRegistryName() != null && ModConstants.MOD_ID.equals(item.getRegistryName().getNamespace())) {
                Block block = ((BlockItem) item).getBlock();

                if (block instanceof IRTBlockColor) {
                    event.getItemColors().register((stack, tintIndex) -> {
                        if (block instanceof lumien.randomthings.block.ColoredGrassBlock) {
                            return lumien.randomthings.block.ColoredGrassBlock.getTint(lumien.randomthings.block.ColoredGrassBlock.getColor(stack));
                        }

                        Minecraft mc = Minecraft.getInstance();

                        if (mc.world == null || mc.player == null) {
                            return 0xFFFFFF;
                        }

                        return ((IRTBlockColor) block).colorMultiplier(block.getDefaultState(), mc.world, mc.player.getPosition(), tintIndex);
                    }, item);
                }
            }
        }
    }

    /**
     * Same bridge, but for a plain {@link Item} that implements
     * {@link IRTItemColor} directly (not a {@link BlockItem}) - e.g.
     * {@link lumien.randomthings.item.BiomeCrystalItem}, which tints
     * itself from its own stored biome rather than from a block.
     */
    @SubscribeEvent
    public static void onPlainItemColorHandler(final ColorHandlerEvent.Item event) {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof IRTItemColor && item.getRegistryName() != null && ModConstants.MOD_ID.equals(item.getRegistryName().getNamespace())) {
                event.getItemColors().register((stack, tintIndex) -> ((IRTItemColor) item).getColorFromItemstack(stack, tintIndex), item);
            }
        }
    }
}
