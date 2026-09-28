function initializeCoreMod() {
    return {
        'iblockreadertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.IBlockReader'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // getLightValue(BlockPos)I - the default body every IBlockReader
                    // implementor (Chunk, World, ...) inherits unless it overrides this
                    // itself (none do - confirmed via javap -p on Chunk/ChunkPrimer).
                    // BlockLightEngine.getLightValue(long), the block-light engine's own
                    // lookup, calls exactly this method on a Chunk instance every time
                    // (confirmed via javap -c) - one shared choke point, same as
                    // SpawnPlacementTransformer's EntitySpawnPlacementRegistry redirect.
                    if (method.name !== "getLightValue" || method.desc !== "(Lnet/minecraft/util/math/BlockPos;)I") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            // Stack here is [computedLightValue]. Push this (the
                            // IBlockReader receiver, param 0 on a default interface
                            // method) and pos (param 1), and retarget through AsmHandler,
                            // which returns the (possibly overridden) value the original
                            // ireturn then returns unchanged.
                            var call = new InsnList();
                            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideLightValue", "(ILnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)I", false));
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
