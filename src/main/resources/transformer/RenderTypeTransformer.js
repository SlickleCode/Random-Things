function initializeCoreMod() {
    return {
        'rendertypetransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.RenderType'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

                // The seven glint RenderType getters (dev name, SRG name). Each just returns a static field; the returned type is passed through
                // ClientAsmHandler.tintGlint(RenderType), which swaps in a tinted wrapper for stacks with a custom glint colour (GlintColors).
                var glintGetters = ["getGlint", "func_228653_j_", "getEntityGlint", "func_228655_k_", "getArmorGlint", "func_239270_k_", "getArmorEntityGlint", "func_239271_l_",
                    "getGlintDirect", "func_239273_n_", "getEntityGlintDirect", "func_239274_p_", "getGlintTranslucent", "func_243501_m"];
                var desc = "()Lnet/minecraft/client/renderer/RenderType;";

                for (m in classNode.methods) {
                    var method = classNode.methods[m];

                    if (method.desc !== desc || glintGetters.indexOf(method.name) < 0) {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.ARETURN) {
                            instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "tintGlint", "(Lnet/minecraft/client/renderer/RenderType;)Lnet/minecraft/client/renderer/RenderType;", false));
                            i++;
                        }
                    }
                }

                return classNode;
            }
        }
    }
}
