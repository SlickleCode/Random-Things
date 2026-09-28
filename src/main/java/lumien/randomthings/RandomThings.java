package lumien.randomthings;

import lumien.randomthings.asm.AsmHandler;
import lumien.randomthings.block.*;
import lumien.randomthings.client.renderer.*;
import lumien.randomthings.client.screen.ModScreens;
import lumien.randomthings.container.ModContainerTypes;
import lumien.randomthings.entity.*;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
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


@Mod(ModConstants.MOD_ID)
public class RandomThings {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RNG = new Random();

    public static RandomThings INSTANCE;

    public RandomThings() {
        INSTANCE = this;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModels);

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addListener((UseHoeEvent event) -> {
            ItemUseContext context = event.getContext();

            World world = context.getWorld();
            BlockPos pos = context.getPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() == ModBlocks.FERTILIZED_DIRT && !state.get(FertilizedDirtBlock.TILLED)) {
                event.setResult(Result.ALLOW);
                world.setBlockState(pos, state.with(FertilizedDirtBlock.TILLED, true));
                PlayerEntity playerentity = context.getPlayer();
                world.playSound(playerentity, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        });

        // A shovel right-clicked on a Slime Block compacts it into a Compressed Slime
        // Block (compression 0); right-clicking an existing Compressed Slime Block
        // compacts it further, up to compression 2. There's no generic Forge event
        // for "tool X used on block Y" the way UseHoeEvent covers hoes, so this
        // mirrors 1.12.2's own approach of hooking the raw right-click event directly.
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            ItemStack equipped = event.getItemStack();

            if (equipped.isEmpty() || !(equipped.getItem() instanceof ShovelItem)) {
                return;
            }

            World world = event.getWorld();
            BlockPos pos = event.getPos();
            BlockState targetState = world.getBlockState(pos);
            PlayerEntity player = event.getPlayer();

            if (targetState.getBlock() == Blocks.SLIME_BLOCK) {
                player.swingArm(event.getHand());

                if (!world.isRemote) {
                    world.setBlockState(pos, ModBlocks.COMPRESSED_SLIME_BLOCK.getDefaultState());
                    world.playSound(null, pos, Blocks.SLIME_BLOCK.getSoundType(targetState).getPlaceSound(), SoundCategory.PLAYERS, 1.0F, 0.8F);
                    equipped.damageItem(1, player, (p) -> p.sendBreakAnimation(event.getHand()));
                }
            } else if (targetState.getBlock() == ModBlocks.COMPRESSED_SLIME_BLOCK) {
                int currentCompression = targetState.get(CompressedSlimeBlock.COMPRESSION);

                if (currentCompression < 2) {
                    player.swingArm(event.getHand());

                    if (!world.isRemote) {
                        world.setBlockState(pos, targetState.with(CompressedSlimeBlock.COMPRESSION, currentCompression + 1));
                        world.playSound(null, pos, Blocks.SLIME_BLOCK.getSoundType(targetState).getPlaceSound(), SoundCategory.PLAYERS, 1.0F, 0.8F - ((currentCompression + 1) * 0.2F));
                        equipped.damageItem(1, player, (p) -> p.sendBreakAnimation(event.getHand()));
                    }
                }
            }
        });

        // Contact Button/Lever aren't triggered by clicking them directly - they're
        // mounted facing another block, and right-clicking THAT block is what
        // activates them (like a hidden pressure sensor). Ported straight from
        // 1.12.2's RTEventHandler.playerInteract: right-clicking any block scans
        // its 6 neighbors for a Contact Button/Lever whose FACING points back at
        // the clicked block, and activates the first match found (button checked
        // before lever, matching the original's ordering).
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            World world = event.getWorld();

            if (world.isRemote || event.getHand() != Hand.MAIN_HAND) {
                return;
            }

            BlockPos clickedPos = event.getPos();

            for (Direction facing : Direction.values()) {
                BlockPos neighborPos = clickedPos.offset(facing);
                BlockState neighborState = world.getBlockState(neighborPos);
                Block neighborBlock = neighborState.getBlock();

                if (neighborBlock instanceof ContactButtonBlock && neighborState.get(ContactButtonBlock.FACING) == facing.getOpposite()) {
                    ((ContactButtonBlock) neighborBlock).activate(world, neighborPos, facing.getOpposite());
                    break;
                } else if (neighborBlock instanceof ContactLeverBlock && neighborState.get(ContactLeverBlock.FACING) == facing.getOpposite()) {
                    ((ContactLeverBlock) neighborBlock).activate(world, neighborPos, facing.getOpposite());
                    break;
                }
            }
        });

        // Fire/lava protection: Obsidian Skull (carried anywhere in the inventory),
        // Obsidian Water Walking Boots and Lava Wader (worn as boots) all give a
        // chance to shrug off any fire-type damage entirely, scaling with how much
        // damage would've been dealt (chance = amount^3 / 100 - small burns are
        // usually blocked, a big single hit usually isn't). Lava Wader additionally
        // fully cancels LAVA damage specifically by spending its own charge meter
        // (see LavaWaderItem.onArmorTick), independent of the chance-based roll.
        MinecraftForge.EVENT_BUS.addListener((LivingAttackEvent event) -> {
            if (event.getEntityLiving().world.isRemote || event.isCanceled() || event.getAmount() <= 0 || !(event.getEntityLiving() instanceof PlayerEntity)) {
                return;
            }

            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            if (event.getSource() == DamageSource.LAVA) {
                ItemStack lavaProtector = ItemStack.EMPTY;
                ItemStack lavaCharm = InventoryUtil.getPlayerInventoryItem(ModItems.LAVA_CHARM, player);

                if (!lavaCharm.isEmpty()) {
                    lavaProtector = lavaCharm;
                }

                ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

                if (!boots.isEmpty() && boots.getItem() == ModItems.LAVA_WADER) {
                    lavaProtector = boots;
                }

                if (!lavaProtector.isEmpty() && lavaProtector.hasTag()) {
                    CompoundNBT compound = lavaProtector.getTag();
                    int charge = compound.getInt("charge");

                    if (charge > 0) {
                        compound.putInt("charge", charge - 1);
                        compound.putInt("chargeCooldown", 40);
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            if (event.getSource().isFireDamage() && event.getSource() != DamageSource.LAVA) {
                ItemStack inventorySkull = InventoryUtil.getPlayerInventoryItem(ModItems.OBSIDIAN_SKULL, player);
                ItemStack obsidianBoots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

                if (!obsidianBoots.isEmpty() && !(obsidianBoots.getItem() == ModItems.OBSIDIAN_WATER_WALKING_BOOTS || obsidianBoots.getItem() == ModItems.LAVA_WADER)) {
                    obsidianBoots = ItemStack.EMPTY;
                }

                ItemStack skull = inventorySkull.isEmpty() ? obsidianBoots : inventorySkull;

                if (!skull.isEmpty()) {
                    float amount = event.getAmount();
                    float chance = amount / 100 * amount * amount;

                    if (RNG.nextFloat() > chance) {
                        event.setCanceled(true);
                    }
                }
            }
        });

        // Imbue Fire/Poison/Wither: whoever last drank an imbue potion applies its
        // on-hit effect to anything they deal direct damage to (not indirect, e.g.
        // arrows/thrown potions - matches the original's own
        // EntityDamageSource-but-not-IndirectEntityDamageSource check). Only one
        // imbue can be active at a time (see ImbueItem.clearImbues), so this is a
        // plain if/else-if chain, same priority order as 1.12.2's RTEventHandler.
        MinecraftForge.EVENT_BUS.addListener((LivingHurtEvent event) -> {
            if (event.isCanceled() || event.getEntityLiving().world.isRemote || !(event.getSource() instanceof EntityDamageSource) || event.getSource() instanceof IndirectEntityDamageSource) {
                return;
            }

            EntityDamageSource damageSource = (EntityDamageSource) event.getSource();

            if (!(damageSource.getTrueSource() instanceof LivingEntity)) {
                return;
            }

            LivingEntity attacker = (LivingEntity) damageSource.getTrueSource();

            if (attacker.isPotionActive(ModEffects.IMBUE_FIRE)) {
                event.getEntityLiving().setFire(10);
            } else if (attacker.isPotionActive(ModEffects.IMBUE_WITHER)) {
                event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.WITHER, 5 * 20, 1));
            } else if (attacker.isPotionActive(ModEffects.IMBUE_POISON)) {
                event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.POISON, 10 * 20, 1));
            }
        });

        // Imbue Experience: doubles whatever XP a kill would have dropped.
        MinecraftForge.EVENT_BUS.addListener((LivingExperienceDropEvent event) -> {
            if (event.getAttackingPlayer() != null && event.getAttackingPlayer().isPotionActive(ModEffects.IMBUE_EXPERIENCE)) {
                event.setDroppedExperience(event.getDroppedExperience() + event.getOriginalExperience());
            }
        });

        // Water Walking Boots, Obsidian Water Walking Boots, and Lava Wader
        // (lava too, for the latter): while standing in the liquid with clear air
        // above, nudge the wearer up each tick instead of letting them sink - a
        // repeated small hop rather than true buoyancy. Passive by default (per
        // user direction, deviating from 1.12.2's hold-jump-to-float original) -
        // sneaking opts back out and lets the wearer sink normally. Client-side
        // only (this is a movement-prediction nicety, not physics the server needs
        // to arbitrate).
        MinecraftForge.EVENT_BUS.addListener((LivingEvent.LivingUpdateEvent event) -> {
            if (!event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof PlayerEntity)) {
                return;
            }

            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            if (player.isSneaking()) {
                return;
            }

            ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

            if (boots.isEmpty() || !(boots.getItem() == ModItems.WATER_WALKING_BOOTS || boots.getItem() == ModItems.OBSIDIAN_WATER_WALKING_BOOTS || boots.getItem() == ModItems.LAVA_WADER)) {
                return;
            }

            // Check slightly *below* the current feet position, not an exact
            // floor(posY) snapshot: once the player is resting right at the
            // liquid's surface, their feet Y sits exactly on the block
            // boundary, and a plain floor() there flickers between the liquid
            // block and the air block above it from one tick to the next
            // (floating-point noise from the previous tick's own nudge is
            // enough to flip it) - the "am I over liquid" check kept
            // alternating true/false, producing the reported jitter.
            BlockPos liquidPos = new BlockPos(player.posX, player.posY - 0.1, player.posZ);
            BlockPos airPos = new BlockPos(player.posX, player.posY + player.getHeight(), player.posZ);
            BlockState liquidState = player.world.getBlockState(liquidPos);
            Material liquidMaterial = liquidState.getMaterial();

            boolean overLiquid = liquidMaterial == Material.WATER || (boots.getItem() == ModItems.LAVA_WADER && liquidMaterial == Material.LAVA);

            if (overLiquid && player.world.getBlockState(airPos).getBlock().isAir(player.world.getBlockState(airPos), player.world, airPos)) {
                // Also stop fighting gravity: the previous version only ever
                // nudged position upward and never touched motionY, so normal
                // per-tick gravity kept accumulating downward velocity
                // underneath the nudge, fighting it and adding to the jitter.
                if (player.getMotion().y < 0) {
                    player.setMotion(player.getMotion().x, 0, player.getMotion().z);
                }

                player.move(MoverType.SELF, new Vec3d(0, 0.1, 0));
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
        MinecraftForge.EVENT_BUS.addListener((LivingEvent.LivingUpdateEvent event) -> {
            LivingEntity entity = event.getEntityLiving();

            if (!entity.onGround) {
                return;
            }

            boolean wearingLubricentBoots = !entity.isSneaking() && entity.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof SuperLubricentBootsItem;

            if (!wearingLubricentBoots) {
                BlockPos underfoot = new BlockPos(entity.posX, entity.getBoundingBox().minY - 1.0D, entity.posZ);
                Block block = entity.world.getBlockState(underfoot).getBlock();

                if (!(block instanceof SuperLubricentIceBlock || block instanceof SuperLubricentPlatformBlock || block instanceof SuperLubricentStoneBlock)) {
                    return;
                }
            }

            SuperLubricentPhysics.capHorizontalSpeed(entity);
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

        // Drives StableEnderpearlItem's dropped-pearl countdown - see the javadoc on
        // StableEnderpearlItem.tickDroppedPearl for why this lives here instead of
        // on the item itself (Item.onEntityItemUpdate doesn't exist in this Forge
        // build).
        MinecraftForge.EVENT_BUS.addListener((TickEvent.WorldTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
                return;
            }

            ((ServerWorld) event.world).getEntities().filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof StableEnderpearlItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((StableEnderpearlItem) e.getItem().getItem()).tickDroppedPearl(e));
        });

        // Drives FlooTokenItem's on-the-ground-long-enough-to-spawn-a-fireplace
        // countdown - same "no Item.onEntityItemUpdate hook in this Forge build"
        // finding as StableEnderpearlItem above, same fix.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.WorldTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
                return;
            }

            ((ServerWorld) event.world).getEntities().filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof FlooTokenItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((FlooTokenItem) e.getItem().getItem()).tickDroppedToken(e));
        });

        // Drives PortkeyItem's "sitting on the ground, priming" countdown - same
        // "no Item.onEntityItemUpdate hook in this Forge build" finding as
        // StableEnderpearlItem/FlooTokenItem above, same fix.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.WorldTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
                return;
            }

            ((ServerWorld) event.world).getEntities().filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof lumien.randomthings.item.PortkeyItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((lumien.randomthings.item.PortkeyItem) e.getItem().getItem()).tickDroppedPortkey(e));
        });

        // Teleports whoever picks up a primed, bound PortkeyItem to its target
        // instead of letting them collect it - matches 1.12.2's own
        // RTEventHandler#itemPickup exactly (same safe-landing-spot search: a
        // 5x5 column around the target, scanning down to 10 blocks below it for
        // a solid-topped position with 2 air blocks above).
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.entity.player.EntityItemPickupEvent event) -> {
            ItemEntity ei = event.getItem();
            ItemStack stack = ei.getItem();

            if (ei.world.isRemote || !(stack.getItem() instanceof lumien.randomthings.item.PortkeyItem)) {
                return;
            }

            CompoundNBT compound = stack.getTag();

            if (compound == null || !compound.getBoolean("hasTarget") || compound.getInt("dropCounter") <= 100) {
                return;
            }

            PlayerEntity player = event.getPlayer();

            if (!(player instanceof ServerPlayerEntity) || player.world.getDimension().getType().getId() != compound.getInt("dimension")) {
                return;
            }

            int targetX = compound.getInt("targetX");
            int targetY = compound.getInt("targetY");
            int targetZ = compound.getInt("targetZ");

            java.util.List<BlockPos> possiblePositions = new java.util.ArrayList<>();

            for (int modX = -2; modX <= 2; modX++) {
                for (int modZ = -2; modZ <= 2; modZ++) {
                    for (int y = targetY; y >= 0 && y >= targetY - 10; y--) {
                        BlockPos evPos = new BlockPos(targetX + modX, y, targetZ + modZ);
                        BlockState belowState = ei.world.getBlockState(evPos);

                        if (Block.hasSolidSide(belowState, ei.world, evPos, net.minecraft.util.Direction.UP) && ei.world.isAirBlock(evPos.up()) && ei.world.isAirBlock(evPos.up().up())) {
                            possiblePositions.add(evPos);
                        }
                    }
                }
            }

            if (!possiblePositions.isEmpty()) {
                java.util.Collections.shuffle(possiblePositions);

                BlockPos teleportTarget = possiblePositions.get(0);
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                player.world.playSound(null, player.getPosition(), net.minecraft.util.SoundEvents.ENTITY_ENDERMAN_TELEPORT, net.minecraft.util.SoundCategory.PLAYERS, 1, 1);
                serverPlayer.connection.setPlayerLocation(teleportTarget.getX() + 0.5, teleportTarget.getY() + 1, teleportTarget.getZ() + 0.5, player.rotationYaw, player.rotationPitch);

                ei.remove();
                event.setCanceled(true);
            }
        });

        // Spectre Anchor's "survive death" mechanic, part 2: the coremod
        // (PlayerEntityTransformer.js -> AsmHandler.dropAllItemsExceptAnchored)
        // skips dropping AND clearing any "spectreAnchor"-tagged stack still
        // in the OLD player's inventory array at death, so it's still sitting
        // there (in its original slot) by the time this fires. Matches
        // 1.12.2's own RTEventHandler#playerClone (HIGHEST priority, main
        // inventory only - Baubles isn't present in this port).
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST, (net.minecraftforge.event.entity.player.PlayerEvent.Clone event) -> {
            if (!event.isWasDeath() || event.isCanceled() || event.getOriginal() == null || event.getPlayer() instanceof net.minecraftforge.common.util.FakePlayer || event.getPlayer().world.getGameRules().getBoolean(net.minecraft.world.GameRules.KEEP_INVENTORY)) {
                return;
            }

            PlayerEntity oldPlayer = event.getOriginal();
            PlayerEntity newPlayer = event.getPlayer();

            for (int i = 0; i < oldPlayer.inventory.getSizeInventory(); i++) {
                ItemStack stack = oldPlayer.inventory.getStackInSlot(i);

                if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains("spectreAnchor")) {
                    continue;
                }

                ItemStack newStackInSlot = newPlayer.inventory.getStackInSlot(i);

                if (newStackInSlot.isEmpty()) {
                    newPlayer.inventory.setInventorySlotContents(i, stack.copy());
                } else {
                    // Another mod put an ItemStack into the slot first.
                    int emptySlot = newPlayer.inventory.getFirstEmptyStack();

                    if (emptySlot != -1) {
                        newPlayer.inventory.setInventorySlotContents(emptySlot, newStackInSlot);
                        newPlayer.inventory.setInventorySlotContents(i, stack.copy());
                    } else {
                        LOGGER.info("Couldn't keep Anchored Item in the Inventory");
                        net.minecraft.inventory.InventoryHelper.spawnItemStack(oldPlayer.world, oldPlayer.posX, oldPlayer.posY, oldPlayer.posZ, stack);
                    }
                }
            }
        });

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

        // Drives EscapeRopeHandler's "find the nearest path to daylight" search -
        // runs once per server tick regardless of how many dimensions are loaded,
        // matching the original's own ServerTickEvent call site.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            EscapeRopeHandler.getInstance().tick();
        });

        // Drains lumien.randomthings.entity.SpectreIlluminatorRelight's queued
        // light-recheck work a bounded amount per tick, on whichever World ticked -
        // server or client (WorldTickEvent fires for both, unlike ServerTickEvent) -
        // see that class's own javadoc for why this is spread out instead of done
        // all at once.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.WorldTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            lumien.randomthings.entity.SpectreIlluminatorRelight.tick(event.world);
        });

        MinecraftForge.EVENT_BUS.addListener((ServerChatEvent event) -> {
            if (handleFlooChat(event)) {
                return;
            }

            boolean consumed = false;

            for (ChatDetectorTileEntity detector : ChatDetectorTileEntity.detectors) {
                if (detector.checkMessage(event.getPlayer(), event.getMessage())) {
                    consumed = true;
                }
            }

            for (GlobalChatDetectorTileEntity detector : GlobalChatDetectorTileEntity.detectors) {
                if (detector.checkMessage(event.getPlayer(), event.getMessage())) {
                    consumed = true;
                }
            }

            if (consumed) {
                event.setCanceled(true);
            }
        });

        MinecraftForge.EVENT_BUS.addListener(RedstoneObserverTileEntity::notifyNeighbor);
        MinecraftForge.EVENT_BUS.addListener(lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity::notifyNeighbor);

        // Drives RedstoneSignalHandler's timed-pulse expiry (Redstone Activator/Remote) -
        // same "no Item.onEntityItemUpdate hook" WorldTickEvent precedent used for
        // StableEnderpearlItem/FlooTokenItem/PortkeyItem above, just per-world state
        // instead of per-entity.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.WorldTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
                return;
            }

            lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler.get(event.world).tick(event.world);
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
        MinecraftForge.EVENT_BUS.addListener((PotionColorCalculationEvent event) -> {
            if (!(event.getEntityLiving() instanceof PlayerEntity)) {
                return;
            }

            ItemStack helmet = event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.HEAD);

            if (helmet.getItem() == ModItems.MAGIC_HOOD) {
                event.shouldHideParticles(true);
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
        MinecraftForge.EVENT_BUS.addListener((LivingEquipmentChangeEvent event) -> {
            if (event.getEntityLiving().world.isRemote || event.getSlot() != EquipmentSlotType.HEAD) {
                return;
            }

            boolean hadHood = event.getFrom().getItem() == ModItems.MAGIC_HOOD;
            boolean hasHood = event.getTo().getItem() == ModItems.MAGIC_HOOD;

            if (hadHood != hasHood) {
                lumien.randomthings.util.PotionMetadataUtil.forceRecalculation(event.getEntityLiving());
            }
        });

        // Was ASM patch #12 (WorldGenAbstractTree.setDirtAt): natural tree growth used
        // to hard-revert the soil block under a sapling to plain Dirt. Forge fires a
        // BlockEvent for each block a world-gen feature places, so we can veto that
        // specific replacement without any bytecode patching.
        MinecraftForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) -> {
            if (event.getPlacedBlock().getBlock() == net.minecraft.block.Blocks.DIRT && event.getBlockSnapshot().getReplacedBlock().getBlock() == ModBlocks.FERTILIZED_DIRT) {
                event.setCanceled(true);
            }
        });
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        AsmHandler.modBlockLight(0F, 1);
        RTPacketHandler.register();

        registerImbuingRecipes();
        registerWorldgenFeatures();
    }

    /**
     * Injects this port's natural-surface-plant features into every already-
     * registered biome. Forge 1.14.4/28.2.26 has no {@code BiomeLoadingEvent}
     * (confirmed via a jar-content check - that's a later-Forge addition),
     * so the only mechanism available is mutating live {@code Biome}
     * instances directly, matching how this project's own (since-deleted)
     * Blood Rose feature already did this. Each feature's own {@code place}
     * does the actual biome/temperature gating (matching 1.12.2's {@code
     * WorldGenPlants} exactly, which checked conditions per-attempt rather
     * than filtering which biomes got the generator at all) rather than
     * filtering here, so registration stays the same simple unconditional
     * loop for all three, and behaves identically to the original per-biome.
     * <p>
     * Ancient Furnace is registered the same unconditional way - its "cold
     * biomes only" restriction (see the wiki) is a per-attempt {@code
     * biome.getTemperature(pos)} check inside {@link
     * lumien.randomthings.worldgen.AncientFurnaceFeature#place}, the same
     * division of labor {@link lumien.randomthings.worldgen.PitcherPlantFeature}
     * already uses for its own (opposite) "warm biomes only" restriction. The
     * {@code ChanceConfig} here is a hand-picked "rare" value - the wiki
     * gives no exact rarity to match.
     */
    private void registerWorldgenFeatures() {
        ForgeRegistries.BIOMES.forEach(biome -> {
            biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(ModFeatures.BEAN_SPROUT, IFeatureConfig.NO_FEATURE_CONFIG, Placement.CHANCE_HEIGHTMAP, new ChanceConfig(2)));
            biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(ModFeatures.PITCHER_PLANT, IFeatureConfig.NO_FEATURE_CONFIG, Placement.CHANCE_HEIGHTMAP, new ChanceConfig(10)));
            biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(ModFeatures.LOTUS, IFeatureConfig.NO_FEATURE_CONFIG, Placement.CHANCE_HEIGHTMAP, new ChanceConfig(10)));
            biome.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Biome.createDecoratedFeature(ModFeatures.ANCIENT_FURNACE, IFeatureConfig.NO_FEATURE_CONFIG, Placement.CHANCE_HEIGHTMAP, new ChanceConfig(400)));
        });
    }

    /**
     * Direct port of 1.12.2's {@code ModRecipes.register}'s Imbuing Station
     * section. Registered here in code (not as datapack JSON) matching the
     * original's own approach - this is a bespoke 3-ingredient-plus-center-item
     * matcher, not something the vanilla recipe/JSON pipeline models.
     */
    private void registerImbuingRecipes() {
        ItemStack waterBottle = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);

        ImbuingRecipeHandler.addRecipe(waterBottle, new ItemStack(Items.VINE), new ItemStack(Items.BONE_MEAL), new ItemStack(Items.COBBLESTONE), new ItemStack(Items.MOSSY_COBBLESTONE));

        ImbuingRecipeHandler.addRecipe(new ItemStack(Items.COAL), new ItemStack(Items.FLINT), new ItemStack(Items.BLAZE_POWDER), waterBottle, new ItemStack(ModItems.IMBUE_FIRE));
        ImbuingRecipeHandler.addRecipe(new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.RED_MUSHROOM), waterBottle, new ItemStack(ModItems.IMBUE_POISON));
        ImbuingRecipeHandler.addRecipe(new ItemStack(ModItems.LESSER_MAGIC_BEAN), new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.GLOWSTONE_DUST), waterBottle, new ItemStack(ModItems.IMBUE_EXPERIENCE));
        ImbuingRecipeHandler.addRecipe(new ItemStack(Items.WITHER_SKELETON_SKULL), new ItemStack(Items.NETHER_BRICK), new ItemStack(Items.GHAST_TEAR), waterBottle, new ItemStack(ModItems.IMBUE_WITHER));
    }

    private void setupClient(final FMLClientSetupEvent event) {
        ModScreens.register();

        ClientRegistry.bindTileEntitySpecialRenderer(BiomeRadarTileEntity.class, new BiomeRadarTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(RuneBaseTileEntity.class, new RuneBaseTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(lumien.randomthings.tileentity.FluidDisplayTileEntity.class, new lumien.randomthings.client.renderer.FluidDisplayTileEntityRenderer());

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

        MinecraftForge.EVENT_BUS.addListener((RenderWorldLastEvent rwl) -> {
            DiviningRodRenderer.get().render();
            RedstoneObserverLineRenderer.render();
        });

        MinecraftForge.EVENT_BUS.addListener(RandomThings::onPlaySound);
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
    private void registerModels(final net.minecraftforge.client.event.ModelRegistryEvent event) {
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

        for (SoundDampenerTileEntity dampener : SoundDampenerTileEntity.loadedDampeners) {
            if (dampener.getWorld() == thePlayer.world && !dampener.isRemoved() && dampener.getPos().distanceSq(soundPosition) < 20 * 20 && dampener.getMutedSounds().contains(soundLocation)) {
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
        ServerPlayerEntity player = event.getPlayer();
        BlockPos below = player.getPosition().down();
        BlockState belowState = player.world.getBlockState(below);
        ItemStack heldFlooPowder = player.getHeldItemMainhand();

        if (!player.world.getEntitiesWithinAABB(FlooFireplaceEntity.class, player.getBoundingBox().grow(0.5)).isEmpty()) {
            FlooNetworkHandler.get(player.world).teleport(player.world, null, null, player, event.getMessage());
            event.setCanceled(true);
            return true;
        }

        if (belowState.getBlock() != ModBlocks.FLOO_BRICK) {
            return false;
        }

        ItemStack pouch = InventoryUtil.getPlayerInventoryItem(ModItems.FLOO_POUCH, player);
        boolean hasFlooInHand = !heldFlooPowder.isEmpty() && heldFlooPowder.getItem() == ModItems.FLOO_POWDER;
        boolean hasFlooPouch = !pouch.isEmpty() && FlooPouchItem.getFlooCount(pouch) > 0;

        if (!player.abilities.isCreativeMode && !hasFlooPouch && !hasFlooInHand) {
            return false;
        }

        TileEntity te = player.world.getTileEntity(below);

        if (!(te instanceof FlooBrickTileEntity)) {
            return false;
        }

        UUID firePlaceUUID = ((FlooBrickTileEntity) te).getFirePlaceUid();

        if (firePlaceUUID == null) {
            return false;
        }

        FlooNetworkHandler networkHandler = FlooNetworkHandler.get(player.world);
        TileEntity masterTe = networkHandler.getFirePlaceTE(player.world, firePlaceUUID);

        if (!(masterTe instanceof FlooBrickTileEntity) || !((FlooBrickTileEntity) masterTe).isMaster()) {
            return false;
        }

        FlooBrickTileEntity masterBrick = (FlooBrickTileEntity) masterTe;
        boolean success = networkHandler.teleport(player.world, masterBrick.getPos(), masterBrick, player, event.getMessage());

        if (success && !player.abilities.isCreativeMode) {
            if (hasFlooInHand) {
                heldFlooPowder.shrink(1);
            } else {
                FlooPouchItem.setFlooCount(pouch, FlooPouchItem.getFlooCount(pouch) - 1);
            }
        }

        event.setCanceled(true);
        return true;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            ModBlocks.registerBlocks(blockRegistryEvent);
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            ModItems.initItemGroup();

            ModItems.registerItems(itemRegistryEvent);
        }

        @SubscribeEvent
        public static void onTileEntityTypesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityTypeRegistryEvent) {
            ModTileEntityTypes.registerTypes(tileEntityTypeRegistryEvent);
        }

        @SubscribeEvent
        public static void onContainerTypesRegistry(final RegistryEvent.Register<ContainerType<?>> containerTypeRegistryEvent) {
            ModContainerTypes.registerContainerTypes(containerTypeRegistryEvent);
        }

        @SubscribeEvent
        public static void onRecipeSerializersRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> recipeSerializerRegistryEvent) {
            ModRecipeSerializers.registerRecipeSerializers(recipeSerializerRegistryEvent);
        }

        @SubscribeEvent
        public static void onEffectsRegistry(final RegistryEvent.Register<Effect> effectRegistryEvent) {
            ModEffects.registerEffects(effectRegistryEvent);
        }

        @SubscribeEvent
        public static void onEntityTypesRegistry(final RegistryEvent.Register<EntityType<?>> entityTypeRegistryEvent) {
            ModEntityTypes.registerEntityTypes(entityTypeRegistryEvent);
        }

        @SubscribeEvent
        public static void onFeaturesRegistry(final RegistryEvent.Register<Feature<?>> featureRegistryEvent) {
            ModFeatures.registerFeatures(featureRegistryEvent);
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
         * 1.12.2 behavior of {@code ItemBlockColored}/{@code ItemBlockBiomeStone}.
         */
        @SubscribeEvent
        public static void onItemColorHandler(final ColorHandlerEvent.Item event) {
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item instanceof BlockItem && item.getRegistryName() != null && ModConstants.MOD_ID.equals(item.getRegistryName().getNamespace())) {
                    Block block = ((BlockItem) item).getBlock();

                    if (block instanceof IRTBlockColor) {
                        event.getItemColors().register((stack, tintIndex) -> {
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
}
