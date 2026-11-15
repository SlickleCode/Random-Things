function initializeCoreMod() {
    return {
        'fallingblocktransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.block.FallingBlock'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // canFallThrough(BlockState)Z: the static test "may a falling block move into this state" (called by FallingBlock.tick,
                    // animateTick and FallingBlockEntity.tick). 1.14.4 got Trigger Glass' sand/gravel pass-through from a getMaterial
                    // override, which no longer exists, so the single ireturn is wrapped: result || (open Trigger Glass).
                    // Collect the return nodes first, then insert (same pattern as RainShieldTransformer).
                    if ((method.name !== "canFallThrough" && method.name !== "func_185759_i") || method.desc !== "(Lnet/minecraft/block/BlockState;)Z") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var returns = [];

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            returns.push(insn);
                        }
                    }

                    for (var r = 0; r < returns.length; r++) {
                        var call = new InsnList();
                        call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "openTriggerGlass", "(ZLnet/minecraft/block/BlockState;)Z", false));
                        instructions.insertBefore(returns[r], call);
                    }

                    break;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["openTriggerGlass"];
                var rtHooks = 0;
                for (var rtM = 0; rtM < classNode.methods.size(); rtM++) {
                    var rtIt = classNode.methods.get(rtM).instructions.iterator();
                    while (rtIt.hasNext()) {
                        var rtN = rtIt.next();
                        if (rtN.getType() === 5 && rtN.owner.indexOf("lumien/randomthings/asm/") === 0 && (rtNames.length === 0 || rtNames.indexOf(rtN.name) >= 0)) {
                            rtHooks++;
                        }
                    }
                }
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$FallingBlock$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
