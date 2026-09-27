package lumien.randomthings.asm;

import lumien.randomthings.block.BlazingFireBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.MagicHoodItem;
import lumien.randomthings.item.SuperLubricentBootsItem;
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
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

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
     * direction too (Quartz Lamp, powered Slime Cube), so the two
     * {@code LivingSpawnEvent.CheckSpawn} listeners this used to live in
     * ({@code RandomThings}'s constructor) were removed entirely rather
     * than kept alongside this as duplicate logic.
     */
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
            for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
                Block block = world.getBlockState(p).getBlock();

                if (block == ModBlocks.LAPIS_LAMP) {
                    return true;
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
}
