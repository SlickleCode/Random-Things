package lumien.randomthings.asm;

import lumien.randomthings.block.BlazingFireBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.entity.SpectreIlluminatorEntity;
import lumien.randomthings.item.MagicHoodItem;
import lumien.randomthings.item.SuperLubricentBootsItem;
import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import lumien.randomthings.tileentity.RainShieldTileEntity;
import lumien.randomthings.tileentity.SlimeCubeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/**
 * Static targets the coremod transformers in {@code src/main/resources/transformer/} redirect
 * bytecode calls to. Kept as plain static methods (no ASM/Mixin types in the signatures) so this
 * class compiles and can be unit-reasoned-about like any other Java code - only the transformer
 * JS files themselves need to know this is where the redirected calls land.
 */
public class AsmHandler {
    public static float modBlockLight(float originalValue, int tintIndex) {

        if (tintIndex == 12340) {
            return 0.0073243305104143F;
        } else {
            return originalValue;
        }
    }

    /**
     * Redirect target for {@code FireBlock.tryCatchFire}'s single {@code
     * BlockState.getFlammability} call - see {@code FireBlockTransformer.js}.
     * 4x catch-chance multiplier, {@code BlazingFireBlock} only. Ground-truth
     * unchanged from the Mixin this replaces (see {@code
     * BlazingFireBlock}'s own class javadoc for the original derivation).
     */
    public static int boostFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face, FireBlock fireBlock) {
        int flammability = state.getFlammability(world, pos, face);
        return fireBlock instanceof BlazingFireBlock ? flammability * 4 : flammability;
    }

    /**
     * Redirect target for {@code FireBlock.tryCatchFire}'s second {@code
     * Random.nextInt(int)} call (the die-out check, called with {@code age +
     * 10}) - see {@code FireBlockTransformer.js}. Reconstructs {@code age}
     * from the already-computed {@code age + 10} argument value, same as the
     * Mixin this replaces.
     */
    public static int lowerDieOutBound(int agePlusTen, FireBlock fireBlock) {
        if (!(fireBlock instanceof BlazingFireBlock)) {
            return agePlusTen;
        }

        int age = agePlusTen - 10;
        return age / 2 + 1;
    }

    /**
     * Redirect target for {@code FireBlock.tryCatchFire}'s third {@code
     * Random.nextInt(int)} call (the age-growth roll, called with a literal
     * {@code 5}) - see {@code FireBlockTransformer.js}. {@code
     * random.nextInt(8) / 4} has the same 50/50 {0, 1} distribution as
     * {@code random.nextInt(2)}, so widening the bound to 8 reproduces the
     * faster growth without touching the division that follows it, same as
     * the Mixin this replaces.
     */
    public static int fasterAgeGrowth(int original, FireBlock fireBlock) {
        return fireBlock instanceof BlazingFireBlock ? 8 : original;
    }

    /**
     * Redirect target for {@code LivingEntity.travel}'s single {@code
     * BlockState.getSlipperiness} call - see {@code
     * SuperLubricentBootsTransformer.js}. While wearing Super Lubricent
     * Boots and not sneaking, every block is treated as maximally
     * slippery, not just the three Super Lubricent blocks - ground-truthed
     * from 1.12.2's {@code AsmHandler.slipFix}, same behavior the Mixin
     * this replaces implemented (and which was never actually verified
     * running in-game, since Mixin couldn't be gotten working at all in
     * this project - the coremod path was written specifically to unblock
     * that).
     */
    public static float bootsMaxSlip(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
        float original = state.getSlipperiness(world, pos, entity);

        if (entity.isSneaking() || !(entity instanceof LivingEntity)) {
            return original;
        }

        ItemStack boots = ((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.FEET);

        return boots.getItem() instanceof SuperLubricentBootsItem ? 1F / 0.91F : original;
    }

    /**
     * Redirect target for {@code EntitySpawnPlacementRegistry}'s single,
     * shared dispatch point for every registered entity's spawn-placement
     * predicate - see {@code SpawnPlacementTransformer.js}. Every hostile
     * mob's (and Slime's) own placement predicate exits early and returns
     * {@code false} well before Forge's {@code LivingSpawnEvent.CheckSpawn}
     * ever fires, so a plain event listener can only ever narrow an
     * already-allowed spawn to denied, never widen an already-rejected one
     * back to allowed - confirmed via {@code javap -c} disassembly of
     * {@code SlimeEntity}'s and {@code MonsterEntity}'s own registered
     * predicates (see {@code TESTING_CHECKLIST.md} #29/#110-111 for the
     * full trace). Intercepting this one shared dispatch point instead -
     * the single place every predicate's raw boolean result flows through,
     * regardless of which mob - fixes the ALLOW direction for both Slime
     * Cube and Lapis Lamp at the actual source, and subsumes the DENY
     * direction too (Quartz Lamp, powered Slime Cube).
     * <p>
     * Lapis Lamp/Quartz Lamp's mechanism history, 2026-09-27 (two reverts in
     * one day, both real bugs, both found by the user's own testing): this
     * force-ALLOW/DENY via a 4-block proximity scan is what originally
     * shipped, but it bypassed vanilla's own placement predicate entirely -
     * including the natural spawn cycle's peaceful-difficulty gate - so mobs
     * could spawn under a Lapis Lamp even on Peaceful, only to be instantly
     * removed again by vanilla's own peaceful despawn check a tick later
     * (bug #1, {@code TESTING_CHECKLIST.md} #29). Tried switching to 1.12.2's
     * actual original mechanism instead (a per-side {@code getLightValue}
     * trick - report 0 to the server so the *real* placement predicate does
     * the ALLOW/DENY itself, respecting every other rule for free) - reverted
     * the same day after real in-game testing showed it backwards (Lapis
     * blocking spawns, Quartz not blocking them; bug #2). Root cause of bug
     * #2: {@code net.minecraftforge.fml.common.thread.EffectiveSide#get()}
     * returns {@code LogicalSide.CLIENT} for any thread that isn't part of
     * FML's own {@code SidedThreadGroup} (read straight from its source -
     * {@code return group instanceof SidedThreadGroup ? ... : LogicalSide.CLIENT;}),
     * and 1.14.4's block-light propagation doesn't reliably run on that
     * specific thread, so the "server" branch silently never fired where it
     * mattered - not a viable mechanism for this Forge version regardless of
     * how faithfully it matches 1.12.2's source. **Back to this force-ALLOW/
     * DENY scan, now with an explicit {@code Difficulty.PEACEFUL} guard on
     * the ALLOW branch** - fixes bug #1's actual root cause (a missing check)
     * without depending on side-detection that's proven unreliable here.
     * <p>
     * Peace Candle's "no natural mob spawning in a 3 chunk radius" (see
     * {@code PeaceCandleTileEntity}) reuses this exact same dispatch point
     * rather than a new coremod - it's just another DENY source for {@code
     * MONSTER}-classified spawns, checked before the lamp scan (tracked-TE
     * lookup instead of a block scan, since a 3-chunk radius is 2016 blocks
     * on a side - far too wide to scan per spawn attempt).
     */
    private static final int PEACE_CANDLE_CHUNK_RADIUS = 3;

    public static boolean overrideSpawnResult(boolean original, EntityType<?> type, IWorld iWorld, BlockPos pos) {
        World world = iWorld.getWorld();

        if (type == EntityType.SLIME) {
            ChunkPos chunkPos = new ChunkPos(pos);

            for (SlimeCubeTileEntity cube : SlimeCubeTileEntity.cubes) {
                if (cube.isInChunk(world, chunkPos)) {
                    return !cube.isPowered();
                }
            }

            return original;
        }

        if (type.getClassification() == EntityClassification.MONSTER) {
            for (PeaceCandleTileEntity candle : PeaceCandleTileEntity.candles) {
                if (candle.isInRange(world, pos, PEACE_CANDLE_CHUNK_RADIUS)) {
                    return false;
                }
            }

            for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
                Block block = world.getBlockState(p).getBlock();

                if (block == ModBlocks.LAPIS_LAMP) {
                    return world.getWorldInfo().getDifficulty() != Difficulty.PEACEFUL;
                }

                if (block == ModBlocks.QUARTZ_LAMP) {
                    return false;
                }
            }
        }

        return original;
    }

    /**
     * Redirect target for every {@code ireturn} in {@code LivingRenderer
     * .canRenderName(T)} - see {@code MagicHoodTransformer.js}. Hides a
     * player's nametag while they're wearing a Magic Hood, regardless of
     * sneaking, same as 1.12.2's {@code ItemMagicHood}. Confirmed via
     * {@code javap -c} that this Forge version has no clean event for this
     * (unlike the particle-hiding half, which uses a real {@code
     * PotionColorCalculationEvent} listener instead - see {@code
     * RandomThings}'s constructor) - {@code RenderNameplateEvent} doesn't
     * exist until a later Forge version, so a coremod is the only option
     * here, same reasoning as every other Batch 7 redirect.
     */
    public static boolean overrideCanRenderName(boolean original, LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return original;
        }

        ItemStack helmet = entity.getItemStackFromSlot(EquipmentSlotType.HEAD);

        return helmet.getItem() instanceof MagicHoodItem ? false : original;
    }

    /**
     * Redirect target for every {@code ireturn} in {@code World.isRainingAt
     * (BlockPos)} - see {@code RainShieldTransformer.js}. 1.14.4 unified
     * 1.12.2's two separate ASM entry points ({@code World.shouldRain}/
     * {@code canSnowAt}, both of which just called {@code TileEntityRainShield
     * .shouldRain}) into this one method, so only one redirect is needed here
     * (see {@code RainShieldBlock}'s javadoc for the client-rendering half
     * this deliberately doesn't cover). Matches 1.12.2's own horizontal-only
     * distance check and fixed 80-block range exactly.
     */
    private static final double RAIN_SHIELD_RANGE = 80.0;

    public static boolean overrideIsRainingAt(boolean original, World world, BlockPos pos) {
        if (!original) {
            return false;
        }

        for (RainShieldTileEntity shield : RainShieldTileEntity.shields) {
            if (shield.isInRange(world, pos, RAIN_SHIELD_RANGE)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Redirect target for the one {@code World.getBiome} call inside {@code
     * GameRenderer.renderRainSnow(float)}'s per-column loop - see {@code
     * GameRendererTransformer.js}. Closes the gap {@code RainShieldBlock}'s
     * own javadoc previously disclosed as not worth attempting: the falling
     * rain/snow visual itself isn't gated through {@code World.isRainingAt}
     * at all - {@code renderRainSnow} computes its own per-column {@code
     * biome.getPrecipitation() != NONE} check directly against whatever
     * {@code Biome} this call returns (ground-truthed via {@code javap -c}:
     * offset 368, {@code invokevirtual World.getBiome}, immediately followed
     * by {@code astore} into the local the precipitation check reads right
     * after). Rather than patching mid-loop to skip a whole iteration (this
     * project's usual "wrap the returned value" idiom doesn't fit a ~300-line
     * method's inline loop body), this redirects that one call itself: real
     * biome normally, or {@code Biomes.DESERT} - a real, already-registered
     * biome with {@code RainType.NONE} baked in (confirmed via its own real
     * source), used purely as a "no precipitation here" substitute - whenever
     * the column falls inside an active Rain Shield's radius. Every other use
     * of {@code World.getBiome} everywhere else in the game is untouched;
     * this only ever fires from inside that one call site.
     */
    public static Biome getBiomeForRainRender(World world, BlockPos pos) {
        Biome real = world.getBiome(pos);

        for (RainShieldTileEntity shield : RainShieldTileEntity.shields) {
            if (shield.isInRange(world, pos, RAIN_SHIELD_RANGE)) {
                return net.minecraft.world.biome.Biomes.DESERT;
            }
        }

        return real;
    }

    /**
     * Redirect target for the {@code ireturn} in {@code IBlockReader
     * .getLightValue(BlockPos)}'s default body - see {@code
     * IBlockReaderTransformer.js}. Ground-truthed via {@code javap -c} that
     * {@code BlockLightEngine.getLightValue(long)} (the block-light engine's
     * own light lookup) calls exactly this default method
     * ({@code getBlockState(pos).getLightValue()}), on a {@code Chunk}
     * instance every time - a single shared choke point inherited by every
     * {@code IBlockReader} implementor with no override of its own, same
     * "one dispatch point" pattern as {@code overrideSpawnResult}. 1.12.2's
     * equivalent ASM-patched {@code Block.getLightValue(state, world, pos)}
     * directly; that 3-argument overload doesn't exist in this version
     * ({@code Block.getLightValue} now takes only a {@code BlockState}, with
     * no {@code world}/{@code pos} to inspect at all - confirmed via
     * {@code javap -p}), which is why this redirect had to move up to the
     * caller instead. See {@link SpectreIlluminatorEntity}'s own javadoc for
     * the full simplification story (this also replaces 1.12.2's separate
     * persisted "illuminated chunks" registry + client-sync network
     * message - both sides just scan the same live-entity set).
     */
    public static int overrideLightValue(int original, IBlockReader world, BlockPos pos) {
        if (!(world instanceof Chunk)) {
            return original;
        }

        World realWorld = ((Chunk) world).getWorld();

        if (realWorld == null) {
            return original;
        }

        ChunkPos chunkPos = new ChunkPos(pos);

        for (SpectreIlluminatorEntity illuminator : SpectreIlluminatorEntity.ILLUMINATORS) {
            if (illuminator.isInChunk(realWorld, chunkPos)) {
                return 14;
            }
        }

        return original;
    }

    /**
     * Injection target for {@code Teleporter.makePortal(Entity)}'s method
     * entry - see {@code TeleporterTransformer.js}. Ground-truthed via
     * {@code javap -c}/source that this Forge version (28.2.26) has no
     * {@code ITeleporter} hook at all (that's a later Forge addition) -
     * {@code ServerWorld.getDefaultTeleporter()} always returns one plain
     * {@code Teleporter} per world, unconditionally, with no override point.
     * 1.12.2's own {@code SpectreHandler} sidestepped this entirely by
     * passing a custom {@code SimpleTeleporter} into {@code
     * PlayerList.transferPlayerToDimension}, a parameter this version's
     * {@code PlayerList} no longer even has (confirmed via {@code javap -p} -
     * only {@code recreatePlayerEntity} remains). Without a patch here,
     * {@code Entity/ServerPlayerEntity#changeDimension}'s generic (non-
     * Nether/End) travel path would search for a real nether-portal frame at
     * the scaled destination coordinate and, finding none in the Spectre
     * void, physically carve one out of obsidian - this early-returns
     * {@code true} (success, no search/build needed) and places the entity
     * at a safe filler position instead, whenever the *destination* world
     * (the {@code Teleporter} instance's own world, not the entity's - this
     * needs to catch travel in *both* directions, into and back out of
     * Spectre) is the Spectre dimension. {@link lumien.randomthings.handler.spectre.SpectreHandler}
     * immediately overwrites this filler position with the real target via
     * {@code connection.setPlayerLocation} right after {@code
     * changeDimension} returns, matching 1.12.2's own transfer-then-
     * explicit-reposition pattern. Every other dimension (Nether/End/
     * Overworld, and any other mod's) falls through to vanilla's completely
     * unpatched original behavior.
     */
    public static boolean overrideMakePortal(net.minecraft.world.server.ServerWorld destWorld, Entity entity) {
        if (destWorld.getDimension().getType() != lumien.randomthings.handler.ModDimensions.SPECTRE_TYPE) {
            return false;
        }

        entity.setLocationAndAngles(8.5D, 1.0D, 8.5D, entity.rotationYaw, entity.rotationPitch);
        entity.setMotion(net.minecraft.util.math.Vec3d.ZERO);

        return true;
    }

    /**
     * Injection target for the sole {@code IRETURN} in {@code World
     * .getRedstonePower(BlockPos, Direction)} - see {@code
     * WorldRedstonePowerTransformer.js}. Matches the max of vanilla's own
     * computed weak power against both wireless-redstone sources this port
     * adds: {@link lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity}
     * (Basic/Advanced Redstone Interface, a live per-tick sensor+broadcaster)
     * and {@link lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler}
     * (Redstone Activator/Remote, a fixed-duration timed pulse). Direct port
     * of 1.12.2's {@code AsmHandler#getRedstonePower} - same two sources,
     * same {@code Math.max}, same client-side short-circuit for the signal
     * handler (server-only {@code WorldSavedData}, never synced - the
     * Redstone Interface registry IS synced per-TE, so it stays live on both
     * sides). No 1.14.4 API change forced anything here; this is purely new
     * scope (Redstone Interface family was never ported before this slice).
     */
    public static int overrideRedstonePower(int computed, World world, BlockPos pos, Direction facing) {
        int fromInterfaces = lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity.getWeakPower(world, pos, facing);
        int fromSignals = world.isRemote ? 0 : lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler.get(world).getStrongPower(world, pos, facing);

        return Math.max(computed, Math.max(fromInterfaces, fromSignals));
    }

    /**
     * Injection target for the sole {@code IRETURN} in {@code IWorldReader
     * .getStrongPower(BlockPos, Direction)} (a default method - {@code World}
     * doesn't override it, confirmed via {@code javap -c}: the only concrete
     * {@code getStrongPower} overload actually declared on {@code World}
     * itself takes just a {@code BlockPos}, aggregating all 6 directions
     * internally by calling this default once per direction) - see {@code
     * WorldReaderStrongPowerTransformer.js}. Same two sources and same
     * {@code Math.max} as {@link #overrideRedstonePower}, just against the
     * strong-power side of each source instead of the weak-power side.
     * {@code IWorldReader} covers more than just {@code World} (e.g.
     * structure-generation-time block readers), so this only applies the
     * override when the receiver is actually a real {@code World} - anything
     * else can't sensibly hold either registry.
     */
    public static int overrideStrongPower(int computed, IWorldReader worldReader, BlockPos pos, Direction facing) {
        if (!(worldReader instanceof World)) {
            return computed;
        }

        World world = (World) worldReader;

        int fromInterfaces = lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity.getStrongPower(world, pos, facing);
        int fromSignals = world.isRemote ? 0 : lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler.get(world).getStrongPower(world, pos, facing);

        return Math.max(computed, Math.max(fromInterfaces, fromSignals));
    }

    /**
     * Redirect target for the single {@code INVOKEVIRTUAL PlayerInventory
     * .dropAllItems} call inside {@code PlayerEntity.dropInventory()} - see
     * {@code PlayerEntityTransformer.js}. Reimplements that same per-slot
     * drop+clear loop (ground-truthed from its own bytecode via {@code javap
     * -c}: iterate every slot across main/armor/offhand, and for each
     * non-empty one, drop it into the world then clear the slot) but skips
     * - leaves untouched, no drop, no clear - any stack tagged {@code
     * spectreAnchor}, matching 1.12.2's own ASM patch exactly. The skipped
     * stack stays sitting in the (soon-to-be-discarded) dying player's
     * inventory array, which {@code RandomThings}'s {@code PlayerEvent
     * .Clone} listener then copies onto the respawned player - the same
     * two-piece "leave it in place, then copy it across" shape 1.12.2 used,
     * just via a coremod that has to reimplement the loop itself since 1.12.2's
     * own version could get away with a single early-return special case
     * inside the original loop, and this version's loop has no comparable
     * per-item exit point to patch without redoing the whole thing anyway.
     */
    public static void dropAllItemsExceptAnchored(PlayerInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.hasTag() && stack.getTag().contains("spectreAnchor")) {
                continue;
            }

            inventory.player.dropItem(stack, true, false);
            inventory.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    /**
     * Redirect target for {@code ItemRenderer.renderQuads}'s per-quad color
     * computation - see {@code ItemRendererTransformer.js}. Direct port of
     * 1.12.2's own {@code AsmHandler#getColorFromItemStack}: a Dyeing
     * Machine result tags the dyed item with {@code rtDye} (see {@code
     * DyeingMachineContainer#onCraftMatrixChanged}), and this overrides
     * whatever color the quad would otherwise render with (vanilla tint,
     * untinted default, or a mod-supplied {@code IItemColor}) whenever that
     * tag is present - letting the Dyeing Machine recolor arbitrary items,
     * not just ones with their own tint-index/IItemColor support.
     */
    public static int getColorFromItemStack(ItemStack stack, int originalColor) {
        if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("rtDye")) {
            return stack.getTag().getInt("rtDye") | 0xFF000000;
        }

        return originalColor;
    }
}
