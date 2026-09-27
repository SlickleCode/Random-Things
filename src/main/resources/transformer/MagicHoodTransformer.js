function initializeCoreMod() {
    return {
        'magichoodtransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // canRenderName(T)Z - the generic, bounded-by-LivingEntity overload (not the
                    // Entity-typed bridge method that just delegates to it). Hides a player's
                    // nametag while wearing a Magic Hood, regardless of sneaking - no clean Forge
                    // event exists for this in this version (RenderNameplateEvent doesn't exist
                    // until later), unlike Magic Hood's particle-hiding half (see AsmHandler's
                    // javadoc / RandomThings's PotionColorCalculationEvent listener). Multiple
                    // early-exit branches means multiple ireturn sites (6 total) instead of the
                    // single-ireturn case FireBlockTransformer/SpawnPlacementTransformer had - same
                    // wrap-the-value-before-it-returns pattern, just applied at each one.
                    if (method.name !== "canRenderName" || method.desc !== "(Lnet/minecraft/entity/LivingEntity;)Z") {
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
                        call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideCanRenderName", "(ZLnet/minecraft/entity/LivingEntity;)Z", false));
                        instructions.insertBefore(returns[r], call);
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
