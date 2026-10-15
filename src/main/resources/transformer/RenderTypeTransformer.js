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
                var glintGetters = ["glint", "m_110490_", "entityGlint", "m_110496_", "armorGlint", "m_110481_", "armorEntityGlint", "m_110484_",
                    "glintDirect", "m_110493_", "entityGlintDirect", "m_110499_", "glintTranslucent", "m_110487_"];
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
