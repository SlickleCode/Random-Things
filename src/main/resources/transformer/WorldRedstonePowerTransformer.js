function initializeCoreMod() {
    return {
        'worldredstonepowertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.World'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // getRedstonePower(BlockPos, Direction)I - the single method every
                    // vanilla weak-power query (redstone wire, comparators,
                    // isBlockPowered, getRedstonePowerFromNeighbors, ...) ultimately
                    // funnels through (confirmed via javap -c: only one IRETURN, at
                    // the very end). Same "wrap the value right before it returns"
                    // pattern as IBlockReaderTransformer's getLightValue patch.
                    if (method.name !== "getRedstonePower" || method.desc !== "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            // Stack here is [computedPower]. Push this (the World
                            // receiver, param 0), pos (param 1) and facing (param 2),
                            // and retarget through AsmHandler, which returns the
                            // (possibly overridden) value the original ireturn then
                            // returns unchanged.
                            var call = new InsnList();
                            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideRedstonePower", "(ILnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I", false));
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
