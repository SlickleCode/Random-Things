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
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.16 ItemRenderer.renderQuads(MatrixStack, IVertexBuilder, List<BakedQuad>, ItemStack,
                    // int light, int overlay)V - ground-truthed via javap -c: for each quad the colour is
                    // computed into local 11 (-1, or the vanilla tint / IItemColor result when the quad has a
                    // tint index) and then split into r/g/b floats starting with `iload 11; bipush 16; ishr`.
                    // Right before that split, local 11 is replaced with
                    // AsmHandler.getColorFromItemStack(stack, color) (stack = local 4), so a Dyeing Machine
                    // result (tagged "rtDye") always recolors here regardless of whether its model declares a
                    // tint index. Same intent as the 1.14.4 patch of the LightUtil.renderQuadColor call.
                    if (method.name !== "renderQuads" || method.desc !== "(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Ljava/util/List;Lnet/minecraft/item/ItemStack;II)V") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.ILOAD && insn.var === 11) {
                            var next = insn.getNext();

                            if (next !== null && next.getOpcode && next.getOpcode() === Opcodes.BIPUSH && next.operand === 16) {
                                var toInsert = new InsnList();
                                toInsert.add(new VarInsnNode(Opcodes.ALOAD, 4));
                                toInsert.add(new VarInsnNode(Opcodes.ILOAD, 11));
                                toInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "getColorFromItemStack", "(Lnet/minecraft/item/ItemStack;I)I", false));
                                toInsert.add(new VarInsnNode(Opcodes.ISTORE, 11));

                                instructions.insertBefore(insn, toInsert);
                                break;
                            }
                        }
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
