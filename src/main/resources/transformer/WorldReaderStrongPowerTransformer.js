function initializeCoreMod() {
    return {
        'worldreaderstrongpowertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.LevelReader'
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
                    if (method.name !== "getDirectSignal" && method.name !== "m_46852_" || method.desc !== "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I") {
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
                            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideStrongPower", "(ILnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", false));
                            instructions.insertBefore(insn, call);
                            break;
                        }
                    }

                    break;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["overrideStrongPower"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$WorldReaderStrongPower$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
