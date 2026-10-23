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
                    if (method.name !== "getLightValue" && method.name !== "func_217298_h" || method.desc !== "(Lnet/minecraft/util/math/BlockPos;)I") {
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

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["overrideLightValue"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$IBlockReader$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
