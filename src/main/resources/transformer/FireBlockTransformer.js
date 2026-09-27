function initializeCoreMod() {
    return {
        'firetransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.block.FireBlock'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    if (method.name !== "tryCatchFire") {
                        continue;
                    }

                    var instructions = method.instructions;

                    // Restores 1.12.2 BlazingFireBlock's catch-chance/age-growth
                    // tweaks, which live inside tryCatchFire's private internals in
                    // this Forge version and aren't reachable by a normal override.
                    // Replaces the SpongePowered Mixin this project previously used
                    // for the exact same three redirects (couldn't be made to work
                    // self-contained in a mods-folder jar on this Forge version -
                    // see the plan file's "Batch 7" section) - same ground-truthed
                    // bytecode targets, just re-expressed as a coremod. Every
                    // redirect target in lumien.randomthings.asm.AsmHandler checks
                    // `instanceof BlazingFireBlock` itself, so vanilla fire and any
                    // other FireBlock subclass are left completely untouched.
                    //
                    // Collect matching instruction NODES (not indices) first, then
                    // mutate/insert relative to those node references - inserting
                    // while iterating by index would shift every later index out
                    // from under this loop.
                    var flammabilityCall = null;
                    var nextIntCalls = [];

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (!insn.getOpcode) {
                            continue;
                        }

                        if (flammabilityCall === null && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/block/BlockState" && insn.name === "getFlammability") {
                            flammabilityCall = insn;
                        } else if (insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "java/util/Random" && insn.name === "nextInt" && insn.desc === "(I)I") {
                            nextIntCalls.push(insn);
                        }
                    }

                    // 1) BlockState.getFlammability(IBlockReader, BlockPos, Direction)I,
                    // the only call to it in this method - full @Redirect: push
                    // `this` (FireBlock) and retarget the call to
                    // AsmHandler.boostFlammability(BlockState, IBlockReader, BlockPos, Direction, FireBlock)I.
                    if (flammabilityCall !== null) {
                        var pushThis = new InsnList();
                        pushThis.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        instructions.insertBefore(flammabilityCall, pushThis);

                        flammabilityCall.setOpcode(Opcodes.INVOKESTATIC);
                        flammabilityCall.owner = "lumien/randomthings/asm/AsmHandler";
                        flammabilityCall.name = "boostFlammability";
                        flammabilityCall.desc = "(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;Lnet/minecraft/block/FireBlock;)I";
                        flammabilityCall.itf = false;
                    }

                    // 2) & 3) the 2nd and 3rd Random.nextInt(I)I calls in this method
                    // (the 1st, the initial catch-chance roll, is untouched) - plain
                    // @ModifyArg: leave the nextInt calls themselves alone, just
                    // rewrite the int argument each pushes right before it fires.
                    if (nextIntCalls.length >= 3) {
                        var dieOutCall = new InsnList();
                        dieOutCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        dieOutCall.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "lowerDieOutBound", "(ILnet/minecraft/block/FireBlock;)I", false));
                        instructions.insertBefore(nextIntCalls[1], dieOutCall);

                        var growthCall = new InsnList();
                        growthCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        growthCall.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "fasterAgeGrowth", "(ILnet/minecraft/block/FireBlock;)I", false));
                        instructions.insertBefore(nextIntCalls[2], growthCall);
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
