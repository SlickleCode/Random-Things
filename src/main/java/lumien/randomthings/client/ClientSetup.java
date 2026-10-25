package lumien.randomthings.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;

import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Random;
import java.util.UUID;
import net.neoforged.api.distmarker.Dist;

/**
 * Everything in the mod that touches client-only classes (renderers, screens, colour handlers, sound hooks).
 * <p>
 * Kept out of {@link lumien.randomthings.RandomThings} on purpose: the mod main class is loaded on a dedicated
 * server too, and any reference to a {@code net.minecraft.client} class from it makes the loader refuse the
 * mod ("Attempted to load class ... for invalid dist DEDICATED_SERVER"). Only reached through
 * {@code DistExecutor.runWhenOn(Dist.CLIENT, ...)}.
 */
public class ClientSetup {
    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(ClientSetup::setupClient);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::onBlockColorHandler);
        RTItemModels.init(modBus);
        modBus.addListener((net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) -> event.registerBlock(new lumien.randomthings.block.DiaphanousBlock.ClientExtensions(), lumien.randomthings.block.ModBlocks.DIAPHANOUS_BLOCK));

        // Spectre Anchor's tooltip: "Anchored" on any stack carrying the
        // "spectreAnchor" NBT tag, regardless of which item it is. Matches
        // 1.12.2's own RTEventHandler#itemTooltip (inserted at index 1, right
        // after the item's display name).
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) -> {
            ItemStack stack = event.getItemStack();

            if (StackTag.contains(stack, "spectreAnchor")) {
                event.getToolTip().add(1, Component.translatable("tooltip.randomthings.spectre_anchor.item").withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
            }
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
                if (entity instanceof net.minecraft.world.entity.vehicle.boat.AbstractBoat && SuperLubricentPhysics.isOnLubricentBlock(entity)) {
                    SuperLubricentPhysics.capBoatMotion((net.minecraft.world.entity.vehicle.boat.AbstractBoat) entity);
                }
            }
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            DiviningRodRenderer.get().tick();
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
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            lumien.randomthings.entity.SpectreIlluminatorRelight.tick(Minecraft.getInstance().level);
        });

    }

    private static void registerRenderers(final net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(lumien.randomthings.tileentity.ModTileEntityTypes.BIOME_RADAR, BiomeRadarTileEntityRenderer::new);
        event.registerBlockEntityRenderer(lumien.randomthings.tileentity.ModTileEntityTypes.RUNE_BASE, RuneBaseTileEntityRenderer::new);
        event.registerBlockEntityRenderer(lumien.randomthings.tileentity.ModTileEntityTypes.FLUID_DISPLAY, lumien.randomthings.client.renderer.FluidDisplayTileEntityRenderer::new);
        event.registerBlockEntityRenderer(lumien.randomthings.tileentity.ModTileEntityTypes.DIAPHANOUS_BLOCK, lumien.randomthings.client.renderer.DiaphanousBlockTileEntityRenderer::new);

        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.FLOO_FIREPLACE, FlooFireplaceEntityRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.ECLIPSED_CLOCK, EclipsedClockEntityRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.THROWN_WEATHER_EGG, net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.WEATHER_CLOUD, WeatherCloudEntityRenderer::new);
        // Custom renderer draws the bottle icon on all 6 faces of the target
        // block - per user request, 2026-09-27, replacing a brief SpriteRenderer
        // first pass (which can only billboard at one fixed spot) - see
        // TimeAcceleratorEntityRenderer's own javadoc.
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.TIME_ACCELERATOR, lumien.randomthings.client.renderer.TimeAcceleratorEntityRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.THROWN_GOLDEN_EGG, net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.GOLDEN_CHICKEN, lumien.randomthings.client.renderer.GoldenChickenEntityRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.ARTIFICIAL_END_PORTAL, lumien.randomthings.client.renderer.ArtificialEndPortalEntityRenderer::new);
        // Item-icon billboard via vanilla's SpriteRenderer, same mechanism already
        // used for ThrownGoldenEggEntity/ThrownWeatherEggEntity above - per user
        // request, 2026-09-27, replacing the earlier no-visible-model design (see
        // SpectreIlluminatorEntity's own javadoc).
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.SPECTRE_ILLUMINATOR, net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(lumien.randomthings.entity.ModEntityTypes.SPIRIT, lumien.randomthings.client.renderer.SpiritEntityRenderer::new);
    }

    private static void setupClient(final FMLClientSetupEvent event) {
        RenderLayerSetup.apply();

        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterLevel rwl) -> {
            DiviningRodRenderer.get().render(rwl.getPoseStack());
            RedstoneObserverLineRenderer.render(rwl.getPoseStack());
        });

        NeoForge.EVENT_BUS.addListener(ClientSetup::onPlaySound);
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
        LocalPlayer thePlayer = Minecraft.getInstance().player;

        if (thePlayer == null || event.getSound() == null) {
            return;
        }

        SoundInstance sound = event.getSound();

        if (sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.RECORDS || Minecraft.getInstance().options.getSoundSourceVolume(sound.getSource()) <= 0) {
            return;
        }

        Identifier soundLocation = sound.getIdentifier();
        BlockPos soundPosition = BlockPos.containing(sound.getX(), sound.getY(), sound.getZ());

        for (SoundDampenerTileEntity dampener : WeakSets.snapshot(SoundDampenerTileEntity.loadedDampeners)) {
            if (dampener.getLevel() == thePlayer.level() && !dampener.isRemoved() && dampener.getBlockPos().distSqr(soundPosition) < 20 * 20 && dampener.getMutedSounds().contains(soundLocation)) {
                lumien.randomthings.debug.RTDebug.log("muted sound {} (placed dampener)", soundLocation);
                event.setSound(null);
                return;
            }
        }

        for (int slot = 0; slot < thePlayer.getInventory().getNonEquipmentItems().size(); slot++) {
            ItemStack stack = thePlayer.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.SOUND_RECORDER && SoundRecorderItem.isRecording(stack)) {
                RTPacketHandler.sendToServer(new PlayedSoundMessage(soundLocation.toString(), slot));
            } else if (stack.getItem() == ModItems.PORTABLE_SOUND_DAMPENER) {
                if (mutesSound(PortableSoundDampenerItem.getInventory(stack), soundLocation)) {
                    lumien.randomthings.debug.RTDebug.log("muted sound {} (portable dampener)", soundLocation);
                    event.setSound(null);
                    return;
                }
            }
        }
    }

    private static boolean mutesSound(lumien.randomthings.itemhandler.IItemHandler inventory, Identifier soundLocation) {
        for (int s = 0; s < inventory.getSlots(); s++) {
            ItemStack patternStack = inventory.getStackInSlot(s);

            if (!patternStack.isEmpty()) {
                Identifier muted = SoundPatternItem.getSoundLocation(patternStack);

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
    public static void onBlockColorHandler(final RegisterColorHandlersEvent.Block event) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof IRTBlockColor && RegUtil.id(block) != null && ModConstants.MOD_ID.equals(RegUtil.id(block).getNamespace())) {
                event.getBlockColors().register((state, worldIn, pos, tintIndex) -> ((IRTBlockColor) block).colorMultiplier(state, worldIn, pos, tintIndex), block);
            }
        }
    }
}
