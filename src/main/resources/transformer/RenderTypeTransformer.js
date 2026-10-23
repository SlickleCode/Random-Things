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

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["tintGlint"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$RenderType$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
