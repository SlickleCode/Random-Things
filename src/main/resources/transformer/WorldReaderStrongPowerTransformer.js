function initializeCoreMod() {
    return {
        'worldreaderstrongpowertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.IWorldReader'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // getStrongPower(BlockPos, Direction)I - a default method every
                    // IWorldReader implementor (World included - it doesn't override
                    // this one itself, confirmed via javap -p/-c) inherits unrouted.
                    // Same "wrap the value right before it returns" pattern as
                    // IBlockReaderTransformer's getLightValue patch and this port's
                    // own WorldRedstonePowerTransformer (getRedstonePower's weak-power
                    // sibling).
                    if (method.name !== "getStrongPower" || method.desc !== "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            // Stack here is [computedPower]. Push this (the
                            // IWorldReader receiver, param 0 on a default interface
                            // method), pos (param 1) and facing (param 2), and
                            // retarget through AsmHandler, which returns the
                            // (possibly overridden) value the original ireturn then
                            // returns unchanged.
                            var call = new InsnList();
                            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideStrongPower", "(ILnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I", false));
                            instructions.insertBefore(insn, call);
                            break;
                        }
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
