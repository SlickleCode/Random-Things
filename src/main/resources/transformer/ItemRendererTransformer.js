function initializeCoreMod() {
    return {
        'itemrenderertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.ItemRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // renderQuads(BufferBuilder, List<BakedQuad>, int, ItemStack)V - ground-truthed
                    // via javap -c: for each quad, computes a per-quad color into local var 9 (the
                    // vanilla tintIndex/IItemColor result if the quad has a tint index, else the
                    // passed-in default color at local var 3), then calls LightUtil.renderQuadColor
                    // (buffer=1, quad=8, color=9). Right before that call, this pops the vanilla
                    // color and replaces it with AsmHandler.getColorFromItemStack(itemstack, color) -
                    // itemstack is local var 4 - so a Dyeing Machine result (tagged "rtDye") always
                    // recolors here regardless of whether its model even declares a tint index. Exact
                    // same "pop the about-to-be-passed color, reload itemstack+color, call the hook"
                    // shape 1.12.2's own ClassTransformer#patchRenderItem used at this call site (see
                    // that method's history in PORTING_PLAN.md), minus the luminous-item hooking
                    // 1.12.2 interleaved here - LuminousHandler doesn't exist in this port.
                    if (method.name !== "renderQuads" || method.desc !== "(Lnet/minecraft/client/renderer/BufferBuilder;Ljava/util/List;ILnet/minecraft/item/ItemStack;)V") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKESTATIC && insn.owner === "net/minecraftforge/client/model/pipeline/LightUtil" && insn.name === "renderQuadColor") {
                            var toInsert = new InsnList();
                            toInsert.add(new InsnNode(Opcodes.POP));
                            toInsert.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            toInsert.add(new VarInsnNode(Opcodes.ILOAD, 9));
                            toInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "getColorFromItemStack", "(Lnet/minecraft/item/ItemStack;I)I", false));

                            instructions.insertBefore(insn, toInsert);
                            break;
                        }
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
