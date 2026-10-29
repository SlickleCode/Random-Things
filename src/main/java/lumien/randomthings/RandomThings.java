package lumien.randomthings;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.Boat;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ModDataComponents;
import lumien.randomthings.lib.ObjectHolderInjector;
import net.neoforged.neoforge.event.ServerChatEvent;

import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.UUID;


@Mod(ModConstants.MOD_ID)
public class RandomThings {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RNG = new Random();

    public static RandomThings INSTANCE;

    public RandomThings(net.neoforged.bus.api.IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        INSTANCE = this;

        modEventBus.addListener(this::setupCommon);
        modEventBus.addListener(this::registerEntityAttributes);
        // All client-only wiring lives in ClientSetup; this class must not reference client classes (dedicated server).
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            lumien.randomthings.client.ClientSetup.init(modEventBus);
        }

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, lumien.randomthings.config.RTConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(lumien.randomthings.handler.LootHandler::addLoot);
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.item.spectretools.SpectreItemTier::addReach);
        // Not FMLCommonSetupEvent: this Forge has no enqueueWork, and the vanilla village pool registry
        // it mutates isn't thread-safe. Server start is single-threaded, before any worldgen, and the
        // config is loaded by then.
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerAboutToStartEvent event) -> {
            lumien.randomthings.worldgen.PeaceCandleChurchPool.install(event.getServer());
        });

        // Developer-only tooling (commands, in-game tests, audit, GUI screenshots): nothing of it is
        // registered or even loaded unless -Drandomthings.debug=true, so normal play pays nothing.
        if (lumien.randomthings.debug.DevMode.ENABLED) {
            lumien.randomthings.debug.DevTools.init();
        }

        NeoForge.EVENT_BUS.addListener((BlockEvent.BlockToolModificationEvent event) -> {
            if (event.getItemAbility() != net.neoforged.neoforge.common.ItemAbilities.HOE_TILL) {
                return;
            }

            BlockState state = event.getState();

            if (state.getBlock() == ModBlocks.FERTILIZED_DIRT && !state.getValue(FertilizedDirtBlock.TILLED)) {
                event.setFinalState(state.setValue(FertilizedDirtBlock.TILLED, true));
            }
        });

        // A shovel right-clicked on a Slime Block compacts it into a Compressed Slime
        // Block (compression 0); right-clicking an existing Compressed Slime Block
        // compacts it further, up to compression 2. There's no generic Forge event
        // for "tool X used on block Y" the way UseHoeEvent covers hoes, so this
        // mirrors 1.12.2's own approach of hooking the raw right-click event directly.
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            ItemStack equipped = event.getItemStack();

            if (equipped.isEmpty() || !(equipped.getItem() instanceof ShovelItem)) {
                return;
            }

            Level world = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState targetState = world.getBlockState(pos);
            Player player = event.getEntity();

            if (targetState.getBlock() == Blocks.SLIME_BLOCK) {
                player.swing(event.getHand());

                if (!world.isClientSide) {
                    world.setBlockAndUpdate(pos, ModBlocks.COMPRESSED_SLIME_BLOCK.defaultBlockState());
                    world.playSound(null, pos, Blocks.SLIME_BLOCK.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.PLAYERS, 1.0F, 0.8F);
                    equipped.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(event.getHand()));
                }
            } else if (targetState.getBlock() == ModBlocks.COMPRESSED_SLIME_BLOCK) {
                int currentCompression = targetState.getValue(CompressedSlimeBlock.COMPRESSION);

                if (currentCompression < 2) {
                    player.swing(event.getHand());

                    if (!world.isClientSide) {
                        world.setBlockAndUpdate(pos, targetState.setValue(CompressedSlimeBlock.COMPRESSION, currentCompression + 1));
                        world.playSound(null, pos, Blocks.SLIME_BLOCK.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.PLAYERS, 1.0F, 0.8F - ((currentCompression + 1) * 0.2F));
                        equipped.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(event.getHand()));
                    }
                }
            }
        });

        // Spectre Sapling conversion: right-clicking any vanilla sapling (the minecraft:saplings
        // block tag, the equivalent of 1.12.2's "treeSapling" ore dictionary entry) with
        // Ectoplasm turns it into a Spectre Sapling and uses up one Ectoplasm (not in creative).
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            ItemStack equipped = event.getItemStack();

            if (equipped.isEmpty() || equipped.getItem() != ModItems.ECTOPLASM) {
                return;
            }

            Level world = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState targetState = world.getBlockState(pos);

            if (targetState.getBlock() == ModBlocks.SPECTRE_SAPLING) {
                return;
            }

            if (!targetState.is(net.minecraft.tags.TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:saplings")))) {
                return;
            }

            if (!world.isClientSide) {
                if (!event.getEntity().getAbilities().instabuild) {
                    equipped.shrink(1);
                }

                world.setBlockAndUpdate(pos, ModBlocks.SPECTRE_SAPLING.defaultBlockState());
            }

            event.setCanceled(true);
        });

        // Runic Pattern from paper: right-clicking a placed Rune Base with a piece of paper copies
        // its 4x4 pattern onto a Rune Pattern and uses up one paper; the pattern replaces the
        // paper if it was the only one, else it goes into the inventory (1.12.2 RTEventHandler.playerInteract).
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            ItemStack equipped = event.getItemStack();

            if (event.getLevel().isClientSide || equipped.isEmpty() || equipped.getItem() != Items.PAPER) {
                return;
            }

            Level world = event.getLevel();
            BlockPos pos = event.getPos();

            if (world.getBlockState(pos).getBlock() != ModBlocks.RUNE_BASE) {
                return;
            }

            BlockEntity te = world.getBlockEntity(pos);

            if (!(te instanceof RuneBaseTileEntity)) {
                return;
            }

            Player player = event.getEntity();
            ItemStack patternStack = new ItemStack(ModItems.RUNE_PATTERN);
            RunePatternItem.setPattern(patternStack, ((RuneBaseTileEntity) te).getRuneData());

            if (equipped.getCount() == 1) {
                player.setItemInHand(event.getHand(), patternStack);
            } else {
                equipped.shrink(1);

                if (!player.addItem(patternStack)) {
                    player.drop(patternStack, false);
                }
            }

            event.setCanceled(true);
        });

        // Contact Button/Lever aren't triggered by clicking them directly - they're
        // mounted facing another block, and right-clicking THAT block is what
        // activates them (like a hidden pressure sensor). Ported straight from
        // 1.12.2's RTEventHandler.playerInteract: right-clicking any block scans
        // its 6 neighbors for a Contact Button/Lever whose FACING points back at
        // the clicked block, and activates the first match found (button checked
        // before lever, matching the original's ordering).
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            Level world = event.getLevel();

            if (world.isClientSide || event.getHand() != InteractionHand.MAIN_HAND) {
                return;
            }

            BlockPos clickedPos = event.getPos();

            for (Direction facing : Direction.values()) {
                BlockPos neighborPos = clickedPos.relative(facing);
                BlockState neighborState = world.getBlockState(neighborPos);
                Block neighborBlock = neighborState.getBlock();

                if (neighborBlock instanceof ContactButtonBlock && neighborState.getValue(ContactButtonBlock.FACING) == facing.getOpposite()) {
                    ((ContactButtonBlock) neighborBlock).activate(world, neighborPos, facing.getOpposite());
                    break;
                } else if (neighborBlock instanceof ContactLeverBlock && neighborState.getValue(ContactLeverBlock.FACING) == facing.getOpposite()) {
                    ((ContactLeverBlock) neighborBlock).activate(world, neighborPos, facing.getOpposite());
                    break;
                }
            }
        });

        // Imbue Fire/Poison/Wither: whoever last drank an imbue potion applies its
        // on-hit effect to anything they deal direct damage to (not indirect, e.g.
        // arrows/thrown potions - matches the original's own
        // EntityDamageSource-but-not-IndirectEntityDamageSource check). Only one
        // imbue can be active at a time (see ImbueItem.clearImbues), so this is a
        // plain if/else-if chain, same priority order as 1.12.2's RTEventHandler.
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) -> {
            if (event.isCanceled() || event.getEntity().level().isClientSide || event.getSource().getEntity() == null || event.getSource().getDirectEntity() != event.getSource().getEntity()) {
                return;
            }

            net.minecraft.world.damagesource.DamageSource damageSource = event.getSource();

            if (!(damageSource.getEntity() instanceof LivingEntity)) {
                return;
            }

            LivingEntity attacker = (LivingEntity) damageSource.getEntity();

            if (attacker.hasEffect(ModEffects.IMBUE_FIRE)) {
                event.getEntity().igniteForSeconds(10);
            } else if (attacker.hasEffect(ModEffects.IMBUE_WITHER)) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, 5 * 20, 1));
            } else if (attacker.hasEffect(ModEffects.IMBUE_POISON)) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 1));
            }
        });

        // Imbue Experience: doubles whatever XP a kill would have dropped.
        NeoForge.EVENT_BUS.addListener((LivingExperienceDropEvent event) -> {
            if (event.getAttackingPlayer() != null && event.getAttackingPlayer().hasEffect(ModEffects.IMBUE_EXPERIENCE)) {
                event.setDroppedExperience(event.getDroppedExperience() + event.getOriginalExperience());
            }
        });

        // Super Lubricent Ice/Platform/Stone speed cap - see SuperLubricentPhysics's
        // javadoc. onEntityCollision doesn't fire for an entity merely resting on
        // top of a block (only for actual hitbox overlap), so the cap is enforced
        // here instead, via the same underfoot-block lookup vanilla's own friction
        // code uses in LivingEntity.travel: one full block below the entity's
        // bounding box, confirmed via javap -c disassembly. Registered
        // unconditionally (not gated on isRemote like the water-walking listener
        // above) since this is physics both sides need to agree on, matching how
        // the blocks' own slipperiness applies identically on client and server.
        // Also applies while wearing Super Lubricent Boots and not sneaking,
        // regardless of the block underfoot - AsmHandler#bootsMaxSlip makes every
        // surface maximally slippery for the wearer, so without this the boots
        // would accelerate without limit on ordinary ground. Same condition
        // (worn + not sneaking) as bootsMaxSlip itself, and the same cap the
        // blocks use, per user direction.
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) -> {
            if (!(event.getEntity() instanceof LivingEntity entity)) {
                return;
            }

            if (!entity.onGround()) {
                return;
            }

            boolean wearingLubricentBoots = !entity.isShiftKeyDown() && entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof SuperLubricentBootsItem;

            if (!wearingLubricentBoots && !SuperLubricentPhysics.isOnLubricentBlock(entity)) {
                return;
            }

            SuperLubricentPhysics.capHorizontalSpeed(entity);
        });

        // Same cap as directly above, but for a Boat (per explicit user
        // request: it should behave exactly like a player here - true zero
        // friction, never slows on its own, capped externally rather than a
        // reduced slipperiness value) - see SuperLubricentPhysics's class
        // javadoc for the full history. LivingUpdateEvent never fires for a
        // BoatEntity (a plain Entity, not LivingEntity), and no generic "any
        // entity ticked" Forge event exists in this version, so this sweeps
        // each world's own entity list once per tick instead, filtered down
        // to boats specifically (no other entity type reads block
        // slipperiness for its own movement, ground-truthed when this was
        // first written). Deliberately not gated on onGround - confirmed
        // unreliable for a resting boat.
        //
        // Real bug found and fixed, 2026-09-28 (reported by user: still
        // completely uncapped after this was first added): WorldTickEvent
        // never fires for the client world at all in this Forge version -
        // BasicEventHooks#onPostWorldTick hardcodes LogicalSide.SERVER
        // regardless of which World is passed in, confirmed via its own
        // source - so the block below only ever capped the server's own
        // boat instance, never what the controlling player's client
        // actually simulates and shows on screen (the thing that looked
        // "uncapped"). This exact bug, and this exact fix, already happened
        // once before in this same file for SpectreIlluminatorRelight - see
        // that ClientTickEvent listener's own comment a little further down
        // for the fuller story; missed re-applying that lesson here the
        // first time. Client side needs its own separate ClientTickEvent
        // listener instead, same as that one.
        //
        // Real bug found and fixed, 2026-09-28, same day: this cap alone
        // still let a boat "speed up at the smallest motion" - it was
        // backstopping real, if slower, exponential growth rather than
        // actually preventing it, since these blocks' slipperiness (1F /
        // 0.91F) isn't true zero friction for a boat specifically - see
        // SuperLubricentPhysics's own javadoc for the full derivation. Fixed
        // at the block level instead (each block's getSlipperiness override
        // now returns exactly 1.0F for a boat). This cap stays in place
        // unmodified - it's now a pure backstop for sustained real paddle
        // input climbing toward the limit, not compensating for anything
        // growing on its own anymore.
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel().isClientSide) {
                return;
            }

            for (Entity entity : ((ServerLevel) event.getLevel()).getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(net.minecraft.world.entity.vehicle.AbstractBoat.class), e -> true)) {
                if (entity instanceof net.minecraft.world.entity.vehicle.AbstractBoat && SuperLubricentPhysics.isOnLubricentBlock(entity)) {
                    SuperLubricentPhysics.capBoatMotion((net.minecraft.world.entity.vehicle.AbstractBoat) entity);
                }
            }
        });

        // Anti-trespass check for the Spectre Dimension: every server tick, snap any
        // non-creative player standing outside their own SpectreCube room back to their
        // own spawn tile (or fully back home if they don't have a cube at all). Direct
        // port of 1.12.2's RTEventHandler#livingUpdate's Spectre-dimension branch.
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) -> {
            if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof ServerPlayer)) {
                return;
            }

            ServerPlayer player = (ServerPlayer) event.getEntity();

            if (lumien.randomthings.handler.ModDimensions.isSpectre(player.level())) {
                lumien.randomthings.handler.spectre.SpectreHandler spectreHandler = lumien.randomthings.handler.spectre.SpectreHandler.getInstance(player.getServer());

                if (spectreHandler != null) {
                    spectreHandler.checkPosition(player);
                }
            }
        });

        // Drives StableEnderpearlItem's dropped-pearl countdown - see the javadoc on
        // StableEnderpearlItem.tickDroppedPearl for why this lives here instead of
        // on the item itself (Item.onEntityItemUpdate doesn't exist in this Forge
        // build).
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel().isClientSide) {
                return;
            }

            java.util.stream.StreamSupport.stream(((ServerLevel) event.getLevel()).getAllEntities().spliterator(), false).filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof StableEnderpearlItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((StableEnderpearlItem) e.getItem().getItem()).tickDroppedPearl(e));
        });

        // Drives FlooTokenItem's on-the-ground-long-enough-to-spawn-a-fireplace
        // countdown - same "no Item.onEntityItemUpdate hook in this Forge build"
        // finding as StableEnderpearlItem above, same fix.
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel().isClientSide) {
                return;
            }

            java.util.stream.StreamSupport.stream(((ServerLevel) event.getLevel()).getAllEntities().spliterator(), false).filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof FlooTokenItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((FlooTokenItem) e.getItem().getItem()).tickDroppedToken(e));
        });

        // Drives PortkeyItem's "sitting on the ground, priming" countdown - same
        // "no Item.onEntityItemUpdate hook in this Forge build" finding as
        // StableEnderpearlItem/FlooTokenItem above, same fix.
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel().isClientSide) {
                return;
            }

            java.util.stream.StreamSupport.stream(((ServerLevel) event.getLevel()).getAllEntities().spliterator(), false).filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof lumien.randomthings.item.PortkeyItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((lumien.randomthings.item.PortkeyItem) e.getItem().getItem()).tickDroppedPortkey(e));
        });

        // Teleports whoever picks up a primed, bound PortkeyItem to its target
        // instead of letting them collect it - matches 1.12.2's own
        // RTEventHandler#itemPickup exactly (same safe-landing-spot search: a
        // 5x5 column around the target, scanning down to 10 blocks below it for
        // a solid-topped position with 2 air blocks above).
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre event) -> {
            ItemEntity ei = event.getItemEntity();
            ItemStack stack = ei.getItem();

            if (ei.level().isClientSide || !(stack.getItem() instanceof lumien.randomthings.item.PortkeyItem)) {
                return;
            }

            CompoundTag compound = StackTag.get(stack);

            if (compound == null || !compound.getBoolean("hasTarget") || compound.getInt("dropCounter") <= 100) {
                return;
            }

            Player player = event.getPlayer();

            if (!(player instanceof ServerPlayer) || !player.level().dimension().location().toString().equals(compound.getString("dimension"))) {
                return;
            }

            int targetX = compound.getInt("targetX");
            int targetY = compound.getInt("targetY");
            int targetZ = compound.getInt("targetZ");

            java.util.List<BlockPos> possiblePositions = new java.util.ArrayList<>();

            for (int modX = -2; modX <= 2; modX++) {
                for (int modZ = -2; modZ <= 2; modZ++) {
                    for (int y = targetY; y >= ei.level().getMinY() && y >= targetY - 10; y--) {
                        BlockPos evPos = new BlockPos(targetX + modX, y, targetZ + modZ);
                        BlockState belowState = ei.level().getBlockState(evPos);

                        if (Block.canSupportCenter(ei.level(), evPos, net.minecraft.core.Direction.UP) && ei.level().isEmptyBlock(evPos.above()) && ei.level().isEmptyBlock(evPos.above().above())) {
                            possiblePositions.add(evPos);
                        }
                    }
                }
            }

            if (!possiblePositions.isEmpty()) {
                java.util.Collections.shuffle(possiblePositions);

                BlockPos teleportTarget = possiblePositions.get(0);
                ServerPlayer serverPlayer = (ServerPlayer) player;

                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
                serverPlayer.connection.teleport(teleportTarget.getX() + 0.5, teleportTarget.getY() + 1, teleportTarget.getZ() + 0.5, player.getYRot(), player.getXRot());

                ei.discard();
                event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
            }
        });

        // Spectre Anchor's "survive death" mechanic, part 2: the coremod
        // (PlayerEntityTransformer.js -> AsmHandler.dropAllItemsExceptAnchored)
        // skips dropping AND clearing any "spectreAnchor"-tagged stack still
        // in the OLD player's inventory array at death, so it's still sitting
        // there (in its original slot) by the time this fires. Matches
        // 1.12.2's own RTEventHandler#playerClone (HIGHEST priority, main
        // inventory only - Baubles isn't present in this port).
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, (net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) -> {
            if (!event.isWasDeath() || event.getOriginal() == null || event.getEntity() instanceof net.neoforged.neoforge.common.util.FakePlayer || ((ServerLevel) event.getEntity().level()).getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)) {
                return;
            }

            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = oldPlayer.getInventory().getItem(i);

                if (stack.isEmpty() || !StackTag.has(stack) || !StackTag.contains(stack, "spectreAnchor")) {
                    continue;
                }

                ItemStack newStackInSlot = newPlayer.getInventory().getItem(i);

                if (newStackInSlot.isEmpty()) {
                    newPlayer.getInventory().setItem(i, stack.copy());
                } else {
                    // Another mod put an ItemStack into the slot first.
                    int emptySlot = newPlayer.getInventory().getFreeSlot();

                    if (emptySlot != -1) {
                        newPlayer.getInventory().setItem(emptySlot, newStackInSlot);
                        newPlayer.getInventory().setItem(i, stack.copy());
                    } else {
                        LOGGER.info("Couldn't keep Anchored Item in the Inventory");
                        net.minecraft.world.Containers.dropItemStack(oldPlayer.level(), oldPlayer.getX(), oldPlayer.getY(), oldPlayer.getZ(), stack);
                    }
                }
            }
        });

        // Tracks a global "has the Ender Dragon ever been defeated" flag, and
        // rolls a small chance to spawn a SpiritEntity at the death location
        // of anything a real player kills - boosted once the dragon's been
        // beaten, and again at night under a full-ish moon while the sky is
        // visible. Matches 1.12.2's own RTEventHandler#livingDeath exactly
        // (same chance formula, same FakePlayer exclusion so things like
        // Advanced Item Collector's fake-player harvesting never trigger it).
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            if (event.getEntity().level().isClientSide) {
                return;
            }

            if (event.getEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
                RTWorldInformationHandler.get(event.getEntity().getServer()).setEnderDragonDefeated(true);
            }

            if (event.getSource().getEntity() == null || event.getSource().getEntity() instanceof net.neoforged.neoforge.common.util.FakePlayer || !(event.getSource().getEntity() instanceof Player) || event.getEntity() instanceof lumien.randomthings.entity.SpiritEntity) {
                return;
            }

            double chance = lumien.randomthings.config.RTConfig.SPIRIT_CHANCE_NORMAL.get();

            RTWorldInformationHandler worldInfo = RTWorldInformationHandler.get(event.getEntity().getServer());

            if (worldInfo.isDragonDefeated()) {
                chance += lumien.randomthings.config.RTConfig.SPIRIT_CHANCE_END_INCREASE.get();
            }

            Level spiritWorld = event.getEntity().level();

            if (spiritWorld.canSeeSkyFromBelowWater(event.getEntity().blockPosition()) && !spiritWorld.isDay()) {
                float moonFactor = net.minecraft.world.level.dimension.DimensionType.MOON_BRIGHTNESS_PER_PHASE[spiritWorld.dimensionType().moonPhase(spiritWorld.getDayTime())];

                chance += moonFactor / 100f * lumien.randomthings.config.RTConfig.SPIRIT_CHANCE_MOON_MULT.get();
            }

            if (Math.random() < chance) {
                spiritWorld.addFreshEntity(new lumien.randomthings.entity.SpiritEntity(spiritWorld, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ()));
            }
        });

        // Drives EscapeRopeHandler's "find the nearest path to daylight" search -
        // runs once per server tick regardless of how many dimensions are loaded,
        // matching the original's own ServerTickEvent call site.
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
                        EscapeRopeHandler.getInstance().tick();
        });

        // Drives SpectreLensHandler's every-60-ticks re-application of each lens's
        // bound Beacon effects to its owner - matching the original's own
        // ServerTickEvent call site (runs once per server tick regardless of which
        // dimension the owning player or the lens itself are actually in).
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
                        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();

            if (server != null) {
                lumien.randomthings.handler.spectrelens.SpectreLensHandler.get(server).tick(server);
            }
        });

        // Drains lumien.randomthings.entity.SpectreIlluminatorRelight's queued
        // light-recheck work a bounded amount per tick, on whichever World ticked -
        // server or client (WorldTickEvent fires for both, unlike ServerTickEvent) -
        // see that class's own javadoc for why this is spread out instead of done
        // all at once.
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel().isClientSide) {
                return;
            }

            lumien.randomthings.entity.SpectreIlluminatorRelight.tick(event.getLevel());
        });

        NeoForge.EVENT_BUS.addListener((ServerChatEvent event) -> {
            if (handleFlooChat(event)) {
                return;
            }

            boolean consumed = false;

            for (ChatDetectorTileEntity detector : WeakSets.snapshot(ChatDetectorTileEntity.detectors)) {
                if (detector.checkMessage(event.getPlayer(), event.getRawText())) {
                    consumed = true;
                }
            }

            for (GlobalChatDetectorTileEntity detector : WeakSets.snapshot(GlobalChatDetectorTileEntity.detectors)) {
                if (detector.checkMessage(event.getPlayer(), event.getRawText())) {
                    consumed = true;
                }
            }

            if (consumed) {
                event.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener(RedstoneObserverTileEntity::notifyNeighbor);

        // Drives RedstoneSignalHandler's timed-pulse expiry (Redstone Activator/Remote) -
        // same "no Item.onEntityItemUpdate hook" WorldTickEvent precedent used for
        // StableEnderpearlItem/FlooTokenItem/PortkeyItem above, just per-world state
        // instead of per-entity.
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel().isClientSide) {
                return;
            }

            lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler.get(event.getLevel()).tick(event.getLevel());
        });

        // Slime Cube's spawn-ALLOW and Lapis Lamp's spawn-prevention used to live here as
        // LivingSpawnEvent.CheckSpawn listeners, but that event fires too late for the ALLOW
        // direction: SlimeEntity's and MonsterEntity's own registered spawn-placement predicates
        // exit early and return false well before CheckSpawn ever gets a chance to run (confirmed
        // via javap -c - see TESTING_CHECKLIST.md #29/#110-111 for the full trace). Both are now
        // handled by AsmHandler#overrideSpawnResult instead, a coremod redirect
        // (SpawnPlacementTransformer.js) into EntitySpawnPlacementRegistry's own single shared
        // predicate-dispatch point, which intercepts before that early exit for every mob type at
        // once and subsumes the DENY direction too - no event listener needed here anymore.

        // Magic Hood's particle-hiding half - unlike its nametag half (see AsmHandler
        // #overrideCanRenderName's javadoc, needs a coremod), this one has a genuine clean Forge
        // event: PotionColorCalculationEvent already exists specifically to let something override
        // whether an entity's potion-particle swirl renders, confirmed via javap -c. No coremod
        // needed for this half at all.
        //
        // Real bug, found 2026-09-28 (reported by user): still saw faint particles with the hood
        // on. Root cause, ground-truthed from LivingEntity.tick()'s own decompiled source:
        // event.shouldHideParticles(true) sets the synced HIDE_PARTICLES flag, but that flag
        // doesn't suppress particles at all - it's what vanilla itself uses for genuinely
        // *ambient* effects (Beacon/Conduit Power), switching to the rarer, more transparent
        // AMBIENT_ENTITY_EFFECT particle type at 1/5 the normal spawn chance, not zero. The real
        // gate is the synced particle *color* (POTION_EFFECTS): tick() only spawns a particle at
        // all when that value is > 0, and it's set directly from event.getColor(). Forcing that
        // to exactly 0 is what actually stops every particle from spawning - shouldHideParticles
        // is left set too, in case anything else ever reads it, but it was never the real switch.
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent event) -> {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }

            ItemStack helmet = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);

            if (helmet.getItem() == ModItems.MAGIC_HOOD) {
                event.setVisible(false);
            }
        });

        // Magic Hood particle-hiding, continued: LivingEntity only recalculates the synced
        // HIDE_PARTICLES flag (firing the event above) from its own per-tick effect-duration
        // bookkeeping - confirmed via javap -c that it's gated behind a private
        // "potionsNeedUpdate" flag, set only when an effect is added/removed/expires, or every
        // 600 ticks for one still active. Putting the hood on WHILE an effect is already active
        // (the exact scenario reported broken - "still see very faint potion particles") doesn't
        // touch that flag at all, so the stale pre-hood value could stay synced for up to 30
        // seconds. No public API forces a recalculation and no clean event fills the gap either
        // (confirmed via javap -p), so this reflectively flips that one private boolean - the
        // same thing vanilla's own effect-changed path does - whenever the head slot's Magic
        // Hood state actually changes, letting the very next tick's already-correct vanilla
        // logic (and the listener above) do the real work.
        NeoForge.EVENT_BUS.addListener((LivingEquipmentChangeEvent event) -> {
            if (event.getEntity().level().isClientSide || event.getSlot() != EquipmentSlot.HEAD) {
                return;
            }

            boolean hadHood = event.getFrom().getItem() == ModItems.MAGIC_HOOD;
            boolean hasHood = event.getTo().getItem() == ModItems.MAGIC_HOOD;

            if (hadHood != hasHood) {
                lumien.randomthings.util.PotionMetadataUtil.forceRecalculation(event.getEntity());
            }
        });

        // Was ASM patch #12 (WorldGenAbstractTree.setDirtAt): natural tree growth used
        // to hard-revert the soil block under a sapling to plain Dirt. Forge fires a
        // BlockEvent for each block a world-gen feature places, so we can veto that
        // specific replacement without any bytecode patching.
        NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) -> {
            if (event.getPlacedBlock().getBlock() == net.minecraft.world.level.block.Blocks.DIRT && event.getBlockSnapshot().getState().getBlock() == ModBlocks.FERTILIZED_DIRT) {
                event.setCanceled(true);
            }
        });

        // Magnetic Enchantment: see handler/MagneticLootModifier (BlockEvent.HarvestDropsEvent is never
        // fired in this Forge build, so a global loot modifier replaces 1.12.2's ASM item catcher).
    }

    /** 1.16: living entities need their attribute map registered (it was set in {@code registerAttributes()} per instance before). */
    private void registerEntityAttributes(final net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.GOLDEN_CHICKEN, lumien.randomthings.entity.GoldenChickenEntity.registerAttributes().build());
        event.put(ModEntityTypes.SPIRIT, lumien.randomthings.entity.SpiritEntity.registerAttributes().build());
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        AsmHandler.modBlockLight(0F, 1);

        registerImbuingRecipes();
    }


    /**
     * Direct port of 1.12.2's {@code ModRecipes.register}'s Imbuing Station
     * section. Registered here in code (not as datapack JSON) matching the
     * original's own approach - this is a bespoke 3-ingredient-plus-center-item
     * matcher, not something the vanilla recipe/JSON pipeline models.
     */
    private void registerImbuingRecipes() {
        ItemStack waterBottle = lumien.randomthings.util.PotionHelper.potion(Potions.WATER);

        ImbuingRecipeHandler.addRecipe(waterBottle, new ItemStack(Items.VINE), new ItemStack(Items.BONE_MEAL), new ItemStack(Items.COBBLESTONE), new ItemStack(Items.MOSSY_COBBLESTONE));

        ImbuingRecipeHandler.addRecipe(new ItemStack(Items.COAL), new ItemStack(Items.FLINT), new ItemStack(Items.BLAZE_POWDER), waterBottle, new ItemStack(ModItems.IMBUE_FIRE));
        ImbuingRecipeHandler.addRecipe(new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.RED_MUSHROOM), waterBottle, new ItemStack(ModItems.IMBUE_POISON));
        ImbuingRecipeHandler.addRecipe(new ItemStack(ModItems.LESSER_MAGIC_BEAN), new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.GLOWSTONE_DUST), waterBottle, new ItemStack(ModItems.IMBUE_EXPERIENCE));
        ImbuingRecipeHandler.addRecipe(new ItemStack(Items.WITHER_SKELETON_SKULL), new ItemStack(Items.NETHER_BRICK), new ItemStack(Items.GHAST_TEAR), waterBottle, new ItemStack(ModItems.IMBUE_WITHER));
    }

    /**
     * The actual Floo-teleport trigger: standing inside a just-dropped {@link
     * lumien.randomthings.entity.FlooFireplaceEntity} (free) or directly on top
     * of a real {@link lumien.randomthings.block.FlooBrickBlock} fireplace
     * (consuming one {@link ModItems#FLOO_POWDER} held in the main hand, or one
     * charge from a carried {@link FlooPouchItem} - free in creative) and
     * typing a chat message sends that message as the destination name instead
     * of actually chatting. Direct port of 1.12.2's {@code
     * RTEventHandler.chatEvent}'s Floo-specific branch (the rest of that method
     * - Chat Detector/Global Chat Detector - already lives in the listener just
     * below this). Returns whether the message was consumed (either matched a
     * fireplace and shouldn't reach chat/Chat Detectors either way).
     */
    private static boolean handleFlooChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        BlockPos below = player.blockPosition().below();
        BlockState belowState = player.level().getBlockState(below);
        ItemStack heldFlooPowder = player.getMainHandItem();

        if (!player.level().getEntitiesOfClass(FlooFireplaceEntity.class, player.getBoundingBox().inflate(0.5)).isEmpty()) {
            FlooNetworkHandler.get(player.level()).teleport(player.level(), null, null, player, event.getRawText());
            event.setCanceled(true);
            return true;
        }

        if (belowState.getBlock() != ModBlocks.FLOO_BRICK) {
            return false;
        }

        ItemStack pouch = InventoryUtil.getPlayerInventoryItem(ModItems.FLOO_POUCH, player);
        boolean hasFlooInHand = !heldFlooPowder.isEmpty() && heldFlooPowder.getItem() == ModItems.FLOO_POWDER;
        boolean hasFlooPouch = !pouch.isEmpty() && FlooPouchItem.getFlooCount(pouch) > 0;

        if (!player.getAbilities().instabuild && !hasFlooPouch && !hasFlooInHand) {
            return false;
        }

        BlockEntity te = player.level().getBlockEntity(below);

        if (!(te instanceof FlooBrickTileEntity)) {
            return false;
        }

        UUID firePlaceUUID = ((FlooBrickTileEntity) te).getFirePlaceUid();

        if (firePlaceUUID == null) {
            return false;
        }

        FlooNetworkHandler networkHandler = FlooNetworkHandler.get(player.level());
        BlockEntity masterTe = networkHandler.getFirePlaceTE(player.level(), firePlaceUUID);

        if (!(masterTe instanceof FlooBrickTileEntity) || !((FlooBrickTileEntity) masterTe).isMaster()) {
            return false;
        }

        FlooBrickTileEntity masterBrick = (FlooBrickTileEntity) masterTe;
        boolean success = networkHandler.teleport(player.level(), masterBrick.getBlockPos(), masterBrick, player, event.getRawText());

        if (success && !player.getAbilities().instabuild) {
            if (hasFlooInHand) {
                heldFlooPowder.shrink(1);
            } else {
                FlooPouchItem.setFlooCount(pouch, FlooPouchItem.getFlooCount(pouch) - 1);
            }
        }

        event.setCanceled(true);
        return true;
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onRegister(final RegisterEvent event) {
            var key = event.getRegistryKey();
            if (key.equals(Registries.BLOCK)) {
                ModBlocks.registerBlocks(new RTRegistry<>(event, Registries.BLOCK));
            } else if (key.equals(Registries.ITEM)) {
                ModItems.registerItems(new RTRegistry<>(event, Registries.ITEM));
            } else if (key.equals(Registries.CREATIVE_MODE_TAB)) {
                event.register(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "randomthings"), ModItems::createItemGroup);
            } else if (key.equals(Registries.BLOCK_ENTITY_TYPE)) {
                ModTileEntityTypes.registerTypes(new RTRegistry<>(event, Registries.BLOCK_ENTITY_TYPE));
            } else if (key.equals(Registries.MENU)) {
                ModContainerTypes.registerContainerTypes(new RTRegistry<>(event, Registries.MENU));
            } else if (key.equals(Registries.RECIPE_SERIALIZER)) {
                ModRecipeSerializers.registerRecipeSerializers(new RTRegistry<>(event, Registries.RECIPE_SERIALIZER));
            } else if (key.equals(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS)) {
                event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "magnetic"), () -> lumien.randomthings.handler.MagneticLootModifier.CODEC);
            } else if (key.equals(Registries.MOB_EFFECT)) {
                ModEffects.registerEffects(new RTRegistry<>(event, Registries.MOB_EFFECT));
            } else if (key.equals(Registries.ENTITY_TYPE)) {
                ModEntityTypes.registerEntityTypes(new RTRegistry<>(event, Registries.ENTITY_TYPE));
            } else if (key.equals(Registries.CHUNK_GENERATOR)) {
                event.register(Registries.CHUNK_GENERATOR, lumien.randomthings.handler.ModDimensions.SPECTRE_ID, () -> lumien.randomthings.handler.spectre.SpectreChunkGenerator.CODEC);
            } else if (key.equals(Registries.LOOT_FUNCTION_TYPE)) {
                event.register(Registries.LOOT_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "random_biome"), () -> lumien.randomthings.handler.LootHandler.RANDOM_BIOME_TYPE);
            } else if (key.equals(Registries.DATA_COMPONENT_TYPE)) {
                ModDataComponents.register(event);
            } else if (key.equals(Registries.FEATURE)) {
                ModFeatures.registerFeatures(new RTRegistry<>(event, Registries.FEATURE));
            }
            ObjectHolderInjector.inject(key);
        }
    }
}
