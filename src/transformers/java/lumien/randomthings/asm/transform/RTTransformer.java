package lumien.randomthings.asm.transform;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;

/**
 * Base of the mod's class transformers. NeoForge 21.9+ no longer runs JavaScript coremods ({@code META-INF/coremods.json} + {@code transformer/*.js}); class
 * transformers are Java {@code ClassProcessor}s offered through a {@code ClassProcessorProvider} service ({@link RTTransformers}). Each transformer here is the
 * port of the JS file of the same name on the older branches, built from the same two moves: insert a call to a static hook in front of a method's returns
 * ({@link #hookReturns}) or retarget one call instruction to a static hook ({@link #redirectCall}).
 * <p>
 * Every transformer adds a hook-site marker, a synthetic field {@code rt$hooks$<Tag>$<count>} with the number of calls into {@code lumien/randomthings/asm}
 * that it left in the class (the {@code coremod.applied} test reads it, so a transformer whose anchor vanished fails a test instead of passing silently).
 */
public abstract class RTTransformer extends SimpleClassProcessor
{
	private static final String HOOKS = "lumien/randomthings/asm/";
	protected static final String ASM_HANDLER = "lumien/randomthings/asm/AsmHandler";
	protected static final String CLIENT_ASM_HANDLER = "lumien/randomthings/asm/ClientAsmHandler";

	private final String id;
	private final String tag;
	private final String target;
	private final String[] hookNames;

	/**
	 * @param id        processor name path
	 * @param tag       marker tag ({@code rt$hooks$<tag>$<count>})
	 * @param target    dotted name of the vanilla class
	 * @param hookNames names of the hook methods counted in the marker (none: every call into the asm package counts)
	 */
	protected RTTransformer(String id, String tag, String target, String... hookNames)
	{
		this.id = id;
		this.tag = tag;
		this.target = target;
		this.hookNames = hookNames;
	}

	public String tag()
	{
		return tag;
	}

	public String targetClass()
	{
		return target;
	}

	@Override
	public ProcessorName name()
	{
		return new ProcessorName("randomthings", id);
	}

	@Override
	public Set<Target> targets()
	{
		return Set.of(new Target(target));
	}

	@Override
	public final void transform(ClassNode classNode, SimpleTransformationContext context)
	{
		apply(classNode);

		int hooks = 0;

		for (MethodNode method : classNode.methods)
		{
			for (AbstractInsnNode insn : method.instructions)
			{
				if (insn instanceof MethodInsnNode call && call.owner.startsWith(HOOKS) && (hookNames.length == 0 || java.util.Arrays.asList(hookNames).contains(call.name)))
				{
					hooks++;
				}
			}
		}

		classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "rt$hooks$" + tag + "$" + hooks, "Z", null, null));
	}

	protected abstract void apply(ClassNode classNode);

	protected static MethodNode findMethod(ClassNode classNode, String name, String desc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(name) && method.desc.equals(desc))
			{
				return method;
			}
		}

		return null;
	}

	/**
	 * Inserts {@code <loads> invokestatic hook} in front of the return instructions of the method (the value to return is on the stack and the hook gets
	 * it as its first argument, then the loaded locals).
	 *
	 * @param loads local variable slots pushed with ALOAD (reference locals; slot 0 is {@code this} of an instance method)
	 * @param all   true for every return, false for the first one only
	 */
	protected static void hookReturns(ClassNode classNode, String method, String desc, String hookName, String hookDesc, boolean all, int... loads)
	{
		MethodNode target = findMethod(classNode, method, desc);

		if (target == null)
		{
			return;
		}

		int returnOpcode = Type.getReturnType(desc).getOpcode(Opcodes.IRETURN);

		for (AbstractInsnNode insn : target.instructions.toArray())
		{
			if (insn.getOpcode() == returnOpcode)
			{
				InsnList call = new InsnList();

				for (int slot : loads)
				{
					call.add(new VarInsnNode(Opcodes.ALOAD, slot));
				}

				call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASM_HANDLER, hookName, hookDesc, false));
				target.instructions.insertBefore(insn, call);

				if (!all)
				{
					break;
				}
			}
		}
	}

	/**
	 * Retargets calls to {@code owner.name} (any method of the class, or just {@code inMethod}) to the static hook. The call's arguments stay where they
	 * are, so the hook takes the receiver (of an instance call) followed by the call's own arguments.
	 *
	 * @param firstOnly retarget only the first matching call of each method
	 */
	protected static int redirectCall(ClassNode classNode, String inMethod, String ownerPrefix, String name, String desc, String hookOwner, String hookName, String hookDesc, boolean firstOnly)
	{
		int replaced = 0;

		for (MethodNode method : classNode.methods)
		{
			if (inMethod != null && !method.name.equals(inMethod))
			{
				continue;
			}

			for (AbstractInsnNode insn : method.instructions.toArray())
			{
				if (insn instanceof MethodInsnNode call && call.owner.startsWith(ownerPrefix) && call.name.equals(name) && (desc == null || call.desc.equals(desc)))
				{
					method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, hookOwner, hookName, hookDesc, false));
					replaced++;

					if (firstOnly)
					{
						break;
					}
				}
			}
		}

		return replaced;
	}

	/** True for the instruction nodes that are real instructions (not labels / line numbers / frames). */
	protected static boolean isReal(AbstractInsnNode insn)
	{
		return insn.getOpcode() >= 0;
	}

	protected static List<FieldInsnNode> unused()
	{
		return List.of();
	}
}
