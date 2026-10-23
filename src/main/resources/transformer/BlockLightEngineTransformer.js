function initializeCoreMod() {
    return {
        'blocklightenginetransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.lighting.BlockLightEngine'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.21.4: the block-light engine no longer asks BlockGetter.getLightEmission(BlockPos) (the hook IBlockReaderTransformer used); its private
                    // getEmission(long, BlockState)I calls BlockState.getLightEmission(level, mutablePos) directly and has one IRETURN. The Spectre Illuminator
                    // override is applied to that value: the stack holds the emission, so push the engine's level (chunkSource.getLevel()) and the position it is
                    // asking about (the engine's mutablePos) and let AsmHandler.overrideLightValue return the possibly raised value.
                    if (method.name !== "getEmission" || method.desc !== "(JLnet/minecraft/world/level/block/state/BlockState;)I") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            var call = new InsnList();
                            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            call.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/level/lighting/LightEngine", "chunkSource", "Lnet/minecraft/world/level/chunk/LightChunkGetter;"));
                            call.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/world/level/chunk/LightChunkGetter", "getLevel", "()Lnet/minecraft/world/level/BlockGetter;", true));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            call.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/level/lighting/BlockLightEngine", "mutablePos", "Lnet/minecraft/core/BlockPos$MutableBlockPos;"));
                            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideLightValue", "(ILnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I", false));
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$BlockLightEngine$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
