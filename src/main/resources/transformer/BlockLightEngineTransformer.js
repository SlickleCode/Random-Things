function initializeCoreMod() {
    return {
        'blocklightenginetransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.lighting.BlockLightEngine'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

                var methods = classNode.methods;

                for (var m = 0; m < methods.size(); m++) {
                    var instructions = methods.get(m).instructions;

                    // The block-light engine no longer asks BlockGetter.getLightEmission(BlockPos) (the hook IBlockReaderTransformer used): both its
                    // getEmission(long, BlockState) and the propagateLightSources lambda call BlockState.getLightEmission(BlockGetter, BlockPos) directly.
                    // The call's arguments (state, level, pos) are already on the stack, so the call itself is retargeted to a static helper with the same
                    // three parameters; that keeps the hook independent of the engine's field names (obfuscated in a production Forge jar) and covers both sites.
                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner.indexOf("net/minecraft/world/level/block/") === 0
                            && insn.desc === "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I" && insn.owner.indexOf("BlockState") >= 0) {
                            instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "getLightEmissionOverridden", "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I", false));
                        }
                    }
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["getLightEmissionOverridden"];
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
