function initializeCoreMod() {
    return {
        'rainshieldtransformer': {
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

                    // isRainingAt(BlockPos)Z - 1.14.4's single unified method covering what
                    // 1.12.2 patched as two separate ASM entry points (World.shouldRain and
                    // World.canSnowAt, both of which just delegated to TileEntityRainShield
                    // .shouldRain) - see AsmHandler#overrideIsRainingAt's javadoc. 4 early-exit
                    // branches, 4 ireturn sites - same collect-then-wrap pattern as
                    // MagicHoodTransformer's canRenderName redirect.
                    if (method.name !== "isRainingAt" && method.name !== "func_175727_C" || method.desc !== "(Lnet/minecraft/util/math/BlockPos;)Z") {
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
                        call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideIsRainingAt", "(ZLnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
                        instructions.insertBefore(returns[r], call);
                    }

                    break;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["overrideIsRainingAt"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$RainShield$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
