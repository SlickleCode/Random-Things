package lumien.randomthings.asm.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * The transformers of client-only vanilla classes (the class names are never loaded on a dedicated server, so these transformers never match there).
 * Hooks live in {@code ClientAsmHandler}.
 */
final class RTClientTransformers
{
	private RTClientTransformers()
	{
	}

	/**
	 * BlockRenderDispatcher.renderBatched (the chunk mesher's entry): a prologue asks {@code ClientAsmHandler.renderBlock} whether a Light Redirector sent this
	 * block to another position; if that drew the block, the method returns at once.
	 */
	static class BlockRendererDispatcher extends RTTransformer
	{
		BlockRendererDispatcher()
		{
			super("block_renderer_dispatcher", "BlockRendererDispatcher", "net.minecraft.client.renderer.block.BlockRenderDispatcher", "renderBlock");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			MethodNode method = findMethod(classNode, "renderBatched",
					"(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;ZLjava/util/List;)V");

			if (method == null)
			{
				return;
			}

			InsnList prologue = new InsnList();
			LabelNode vanilla = new LabelNode();

			for (int slot = 0; slot <= 5; slot++)
			{
				prologue.add(new VarInsnNode(Opcodes.ALOAD, slot));
			}

			prologue.add(new VarInsnNode(Opcodes.ILOAD, 6));
			prologue.add(new VarInsnNode(Opcodes.ALOAD, 7));
			prologue.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "renderBlock",
					"(Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;ZLjava/util/List;)Z",
					false));
			prologue.add(new JumpInsnNode(Opcodes.IFEQ, vanilla));
			prologue.add(new InsnNode(Opcodes.RETURN));
			prologue.add(vanilla);

			method.instructions.insert(prologue);
		}
	}

	/** WeatherEffectRenderer.getPrecipitationAt: the biome lookup is retargeted so columns inside an active Rain Shield read as having no precipitation. */
	static class WorldRenderer extends RTTransformer
	{
		WorldRenderer()
		{
			super("world_renderer", "WorldRenderer", "net.minecraft.client.renderer.WeatherEffectRenderer");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			redirectCall(classNode, "getPrecipitationAt", "net/minecraft/world/level/Level", "getBiome", "(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", ASM_HANDLER, "getBiomeForRainRender",
					"(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", true);
		}
	}

	/** ItemModelResolver.appendItemLayers: before the method returns, the layers just built are dyed when the stack carries the Dyeing Machine colour. */
	static class ItemRenderer extends RTTransformer
	{
		ItemRenderer()
		{
			super("item_renderer", "ItemRenderer", "net.minecraft.client.renderer.item.ItemModelResolver", "dyeItemLayers");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			MethodNode method = findMethod(classNode, "appendItemLayers",
					"(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V");

			if (method == null)
			{
				return;
			}

			for (AbstractInsnNode insn : method.instructions.toArray())
			{
				if (insn.getOpcode() == Opcodes.RETURN)
				{
					InsnList call = new InsnList();
					call.add(new VarInsnNode(Opcodes.ALOAD, 1));
					call.add(new VarInsnNode(Opcodes.ALOAD, 2));
					call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "dyeItemLayers", "(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;)V", false));
					method.instructions.insertBefore(insn, call);
				}
			}
		}
	}

	/**
	 * EquipmentLayerRenderer.renderLayers: the colour NeoForge's {@code IClientItemExtensions.getArmorLayerTintColor} returns for each layer is passed through
	 * {@code ClientAsmHandler.armorTintColor} (the Dyeing Machine colour of any armor piece); the worn stack (local 5) is remembered for it while the method runs.
	 */
	static class ArmorLayer extends RTTransformer
	{
		ArmorLayer()
		{
			super("armor_layer", "ArmorLayer", "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer", "armorTintColor");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			for (MethodNode method : classNode.methods)
			{
				boolean tinted = false;

				for (AbstractInsnNode insn : method.instructions.toArray())
				{
					if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEINTERFACE && call.name.equals("getArmorLayerTintColor"))
					{
						method.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "armorTintColor", "(I)I", false));
						tinted = true;
					}
				}

				if (!tinted)
				{
					continue;
				}

				for (AbstractInsnNode insn : method.instructions.toArray())
				{
					if (insn.getOpcode() == Opcodes.RETURN)
					{
						method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "clearRenderingStack", "()V", false));
					}
				}

				InsnList head = new InsnList();
				head.add(new VarInsnNode(Opcodes.ALOAD, 5));
				head.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "setRenderingStack", "(Lnet/minecraft/world/item/ItemStack;)V", false));
				method.instructions.insert(head);
			}
		}
	}

	/** RenderTypes glint getters (glint, glintTranslucent, entityGlint, armorEntityGlint): the returned type goes through {@code ClientAsmHandler.tintGlint}. */
	static class GlintGetters extends RTTransformer
	{
		GlintGetters()
		{
			super("glint_getters", "GlintGetters", "net.minecraft.client.renderer.rendertype.RenderTypes", "tintGlint");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			java.util.List<String> getters = java.util.List.of("glint", "glintTranslucent", "entityGlint", "armorEntityGlint");

			for (MethodNode method : classNode.methods)
			{
				if (!method.desc.equals("()Lnet/minecraft/client/renderer/rendertype/RenderType;") || !getters.contains(method.name))
				{
					continue;
				}

				for (AbstractInsnNode insn : method.instructions.toArray())
				{
					if (insn.getOpcode() == Opcodes.ARETURN)
					{
						method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "tintGlint",
								"(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lnet/minecraft/client/renderer/rendertype/RenderType;", false));
					}
				}
			}
		}
	}

	/** ItemRenderer.renderItem (what draws a submitted item): its glint colour comes from the layer's tint array for the duration of the call. */
	static class ItemGlint extends RTTransformer
	{
		ItemGlint()
		{
			super("item_glint", "ItemGlint", "net.minecraft.client.renderer.entity.ItemRenderer", "beginItemGlint");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			for (MethodNode method : classNode.methods)
			{
				if (!method.name.equals("renderItem") || !method.desc.contains("[I"))
				{
					continue;
				}

				for (AbstractInsnNode insn : method.instructions.toArray())
				{
					if (insn.getOpcode() == Opcodes.RETURN)
					{
						method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "clearRenderingStack", "()V", false));
					}
				}

				InsnList head = new InsnList();
				head.add(new VarInsnNode(Opcodes.ALOAD, 5));
				head.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "beginItemGlint", "([I)V", false));
				method.instructions.insert(head);
			}
		}
	}

	/** RenderType.draw: the colour modulator ({@code new Vector4f(1, 1, 1, 1)}) is passed through {@code ClientAsmHandler.modulate}, which tints glint copies. */
	static class RenderTypeModulator extends RTTransformer
	{
		RenderTypeModulator()
		{
			super("render_type_modulator", "RenderTypeModulator", "net.minecraft.client.renderer.rendertype.RenderType", "modulate");
		}

		@Override
		protected void apply(ClassNode classNode)
		{
			MethodNode draw = findMethod(classNode, "draw", "(Lcom/mojang/blaze3d/vertex/MeshData;)V");

			if (draw == null)
			{
				return;
			}

			for (AbstractInsnNode insn : draw.instructions.toArray())
			{
				if (insn instanceof MethodInsnNode init && init.getOpcode() == Opcodes.INVOKESPECIAL && init.owner.equals("org/joml/Vector4f") && init.name.equals("<init>") && init.desc.equals("(FFFF)V"))
				{
					InsnList call = new InsnList();
					call.add(new VarInsnNode(Opcodes.ALOAD, 0));
					call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_ASM_HANDLER, "modulate", "(Lorg/joml/Vector4f;Lnet/minecraft/client/renderer/rendertype/RenderType;)Lorg/joml/Vector4f;", false));
					draw.instructions.insert(init, call);
					break;
				}
			}
		}
	}
}
