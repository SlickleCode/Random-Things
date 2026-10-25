package lumien.randomthings.asm.transform;

import org.objectweb.asm.tree.ClassNode;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;

/**
 * The mod's class transformers (service {@code net.neoforged.neoforgespi.transformation.ClassProcessorProvider}), the 1.21.9+ replacement of the JavaScript
 * coremods of the older branches. The descriptors and anchors were re-read in the 1.21.11 bytecode; see the port plan, milestone M3.
 */
public class RTTransformers implements ClassProcessorProvider
{
	/** Every transformer of the mod (also read by the {@code coremod.applied} test). */
	public static java.util.List<RTTransformer> all()
	{
		return java.util.List.of(new SpawnPlacement(), new MagicHood(), new RainShield(), new WorldRedstonePower(), new WorldReaderStrongPower(), new IBlockReader(), new BlockLightEngine(), new SuperLubricentBoots(), new PlayerEntity(), new FireBlock(),
				new RTClientTransformers.BlockRendererDispatcher(), new RTClientTransformers.WorldRenderer(), new RTClientTransformers.ItemRenderer(), new RTClientTransformers.ArmorLayer(),
				new RTClientTransformers.GlintGetters(), new RTClientTransformers.ItemGlint(), new RTClientTransformers.RenderTypeModulator());
	}

	@Override
	public void createProcessors(Context context, Collector collector)
	{
		for (RTTransformer transformer : all())
		{
			collector.add(transformer);
		}
	}

	/** SpawnPlacements.checkSpawnRules: the result goes through AsmHandler.overrideSpawnResult (Peace Candle, Lapis/Quartz Lamp, Slime Cube). */
	static class SpawnPlacement extends RTTransformer
	{
		SpawnPlacement()
		{
			super("spawn_placement", "SpawnPlacement", "net.minecraft.world.entity.SpawnPlacements", "overrideSpawnResult");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			hookReturns(classNode, "checkSpawnRules", "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z", "overrideSpawnResult",
					"(ZLnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;)Z", false, 0, 1, 3);
		}
	}

	/** LivingEntityRenderer.shouldShowName: the Magic Hood hides the name tag. */
	static class MagicHood extends RTTransformer
	{
		MagicHood()
		{
			super("magic_hood", "MagicHood", "net.minecraft.client.renderer.entity.LivingEntityRenderer", "overrideCanRenderName");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			hookReturns(classNode, "shouldShowName", "(Lnet/minecraft/world/entity/LivingEntity;D)Z", "overrideCanRenderName", "(ZLnet/minecraft/world/entity/LivingEntity;)Z", true, 1);
		}
	}

	/** Level.isRainingAt: the Rain Shield suppresses rain in its range. */
	static class RainShield extends RTTransformer
	{
		RainShield()
		{
			super("rain_shield", "RainShield", "net.minecraft.world.level.Level", "overrideIsRainingAt");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			hookReturns(classNode, "isRainingAt", "(Lnet/minecraft/core/BlockPos;)Z", "overrideIsRainingAt", "(ZLnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", true, 0, 1);
		}
	}

	/** SignalGetter.getSignal: the Redstone Interface mirrors power onto its target. */
	static class WorldRedstonePower extends RTTransformer
	{
		WorldRedstonePower()
		{
			super("world_redstone_power", "WorldRedstonePower", "net.minecraft.world.level.SignalGetter", "overrideRedstonePower");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			hookReturns(classNode, "getSignal", "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", "overrideRedstonePower",
					"(ILnet/minecraft/world/level/SignalGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", false, 0, 1, 2);
		}
	}

	/** SignalGetter.getDirectSignal: the strong power counterpart. */
	static class WorldReaderStrongPower extends RTTransformer
	{
		WorldReaderStrongPower()
		{
			super("world_reader_strong_power", "WorldReaderStrongPower", "net.minecraft.world.level.SignalGetter", "overrideStrongPower");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			hookReturns(classNode, "getDirectSignal", "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", "overrideStrongPower",
					"(ILnet/minecraft/world/level/SignalGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", false, 0, 1, 2);
		}
	}

	/** BlockGetter.getLightEmission: the Spectre Illuminator lights its chunk (the light engine itself bypasses this method since 1.20.1, see {@link BlockLightEngine}). */
	static class IBlockReader extends RTTransformer
	{
		IBlockReader()
		{
			super("block_getter", "IBlockReader", "net.minecraft.world.level.BlockGetter", "overrideLightValue");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			hookReturns(classNode, "getLightEmission", "(Lnet/minecraft/core/BlockPos;)I", "overrideLightValue", "(ILnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I", false, 0, 1);
		}
	}

	/**
	 * BlockLightEngine: both its getEmission(long, BlockState) and the propagateLightSources lambda call BlockState.getLightEmission(level, pos) directly. The
	 * call's arguments (state, level, pos) are already on the stack, so the call is retargeted to a static helper with the same three parameters.
	 */
	static class BlockLightEngine extends RTTransformer
	{
		BlockLightEngine()
		{
			super("block_light_engine", "BlockLightEngine", "net.minecraft.world.level.lighting.BlockLightEngine", "getLightEmissionOverridden");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			redirectCall(classNode, null, "net/minecraft/world/level/block/state/", "getLightEmission", "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I", ASM_HANDLER, "getLightEmissionOverridden",
					"(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I", false);
		}
	}

	/** LivingEntity: the friction lookup of the movement code (Super Lubricent Boots make every surface slippery). */
	static class SuperLubricentBoots extends RTTransformer
	{
		SuperLubricentBoots()
		{
			super("super_lubricent_boots", "SuperLubricentBoots", "net.minecraft.world.entity.LivingEntity");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			redirectCall(classNode, null, "net/minecraft/world/level/block/state/BlockState", "getFriction", "(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F", ASM_HANDLER, "bootsMaxSlip",
					"(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F", true);
		}
	}

	/** Player.dropEquipment: Inventory.dropAll is replaced by a version that keeps Spectre Anchor items. */
	static class PlayerEntity extends RTTransformer
	{
		PlayerEntity()
		{
			super("player_entity", "PlayerEntity", "net.minecraft.world.entity.player.Player");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			redirectCall(classNode, "dropEquipment", "net/minecraft/world/entity/player/Inventory", "dropAll", "()V", ASM_HANDLER, "dropAllItemsExceptAnchored", "(Lnet/minecraft/world/entity/player/Inventory;)V", true);
		}
	}

	/**
	 * FireBlock (Blazing Fire): checkBurnOut's flammability lookup and its 2nd and 3rd {@code RandomSource.nextInt} argument, and every call of the
	 * private static getFireTickDelay (which becomes {@code AsmHandler.fireTickCooldown(random, this)}). All hooks check for the Blazing Fire block
	 * themselves, so vanilla fire is left untouched.
	 */
	static class FireBlock extends RTTransformer
	{
		FireBlock()
		{
			super("fire_block", "FireBlock", "net.minecraft.world.level.block.FireBlock", "fasterAgeGrowth", "lowerDieOutBound");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			org.objectweb.asm.tree.MethodNode burnOut = findMethod(classNode, "checkBurnOut", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;ILnet/minecraft/core/Direction;)V");

			if (burnOut != null)
			{
				org.objectweb.asm.tree.MethodInsnNode flammabilityCall = null;
				java.util.List<org.objectweb.asm.tree.MethodInsnNode> nextIntCalls = new java.util.ArrayList<>();

				for (org.objectweb.asm.tree.AbstractInsnNode insn : burnOut.instructions)
				{
					if (insn instanceof org.objectweb.asm.tree.MethodInsnNode call)
					{
						if (flammabilityCall == null && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL && call.owner.equals("net/minecraft/world/level/block/state/BlockState") && call.name.equals("getFlammability"))
						{
							flammabilityCall = call;
						}
						else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE && call.owner.equals("net/minecraft/util/RandomSource") && call.name.equals("nextInt") && call.desc.equals("(I)I"))
						{
							nextIntCalls.add(call);
						}
					}
				}

				// BlockState.getFlammability(BlockGetter, BlockPos, Direction)I is retargeted to AsmHandler.boostFlammability(state, level, pos, direction, this)
				if (flammabilityCall != null)
				{
					burnOut.instructions.insertBefore(flammabilityCall, new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
					burnOut.instructions.set(flammabilityCall, new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, ASM_HANDLER, "boostFlammability",
							"(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/FireBlock;)I", false));
				}

				// the 2nd and 3rd nextInt argument is rewritten right before the call
				if (nextIntCalls.size() >= 3)
				{
					org.objectweb.asm.tree.InsnList dieOut = new org.objectweb.asm.tree.InsnList();
					dieOut.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
					dieOut.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, ASM_HANDLER, "lowerDieOutBound", "(ILnet/minecraft/world/level/block/FireBlock;)I", false));
					burnOut.instructions.insertBefore(nextIntCalls.get(1), dieOut);

					org.objectweb.asm.tree.InsnList growth = new org.objectweb.asm.tree.InsnList();
					growth.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
					growth.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, ASM_HANDLER, "fasterAgeGrowth", "(ILnet/minecraft/world/level/block/FireBlock;)I", false));
					burnOut.instructions.insertBefore(nextIntCalls.get(2), growth);
				}
			}

			// the delay between fire ticks: getFireTickDelay(RandomSource) is called from tick() and onPlace(); Blazing Fire gets its shorter delay
			for (org.objectweb.asm.tree.MethodNode method : classNode.methods)
			{
				for (org.objectweb.asm.tree.AbstractInsnNode insn : method.instructions.toArray())
				{
					if (insn instanceof org.objectweb.asm.tree.MethodInsnNode call && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC && call.owner.equals("net/minecraft/world/level/block/FireBlock") && call.name.equals("getFireTickDelay")
							&& call.desc.equals("(Lnet/minecraft/util/RandomSource;)I"))
					{
						method.instructions.insertBefore(call, new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
						method.instructions.set(call, new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, ASM_HANDLER, "fireTickCooldown", "(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/block/FireBlock;)I", false));
					}
				}
			}
		}
	}
}
