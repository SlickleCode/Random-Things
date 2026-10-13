function initializeCoreMod() {
    return {
        'armorlayertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.layers.BipedArmorLayer'
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

                    // 1.16 BipedArmorLayer: func_241739_a_(MatrixStack, IRenderTypeBuffer, LivingEntity,
                    // EquipmentSlotType, int, BipedModel)V draws one armor piece (local 7 = the ItemStack).
                    // 1.14.4 patched the second BipedModel.render call to re-apply the GL colour from the
                    // Dyeing Machine's "rtDye" tag; 1.16 passes the colour to renderModel(...) as three floats,
                    // so the same effect is two edits here:
                    //  1) leather (IDyeableArmorItem) branch: IDyeableArmorItem.getColor(stack) is redirected to
                    //     ClientAsmHandler.dyedColor, which returns the rtDye colour when the stack has one;
                    //  2) every other armor: the three constant 1.0F colour arguments of the LAST renderModel
                    //     call (the plain, non-leather branch) become ClientAsmHandler.armorChannel(stack, i, 1.0F).
                    // The "overlay" call keeps white, as in vanilla.
                    if (method.name !== "func_241739_a_" || method.desc !== "(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/inventory/EquipmentSlotType;ILnet/minecraft/client/renderer/entity/model/BipedModel;)V") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var renderModelCalls = [];

                    // Enchant-glint colour: remember the worn stack (entity.getItemStackFromSlot(slot)) for the RenderType glint getters; cleared before every return.
                    for (var rr = 0; rr < instructions.size(); rr++) {
                        var rnode = instructions.get(rr);
                        if (rnode.getOpcode && rnode.getOpcode() === Opcodes.RETURN) {
                            instructions.insertBefore(rnode, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "clearRenderingStack", "()V", false));
                            rr++;
                        }
                    }
                    var armorHead = new InsnList();
                    armorHead.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    armorHead.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    // (the slot lookup lives in Java so its dev/SRG method name is remapped by the build, not hand-matched here)
                    armorHead.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "setRenderingStackFromSlot", "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/inventory/EquipmentSlotType;)V", false));
                    instructions.insert(armorHead);

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (!insn.getOpcode) {
                            continue;
                        }

                        if (insn.getOpcode() === Opcodes.INVOKEINTERFACE && insn.owner === "net/minecraft/item/IDyeableArmorItem" && (insn.name === "getColor" || insn.name === "func_200886_f") && insn.desc === "(Lnet/minecraft/item/ItemStack;)I") {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/ClientAsmHandler";
                            insn.name = "dyedColor";
                            insn.desc = "(Lnet/minecraft/item/IDyeableArmorItem;Lnet/minecraft/item/ItemStack;)I";
                            insn.itf = false;
                        } else if (insn.getOpcode() === Opcodes.INVOKESPECIAL && insn.name === "renderModel") {
                            renderModelCalls.push(i);
                        }
                    }

                    if (renderModelCalls.length >= 2) {
                        var previousCall = renderModelCalls[renderModelCalls.length - 2];
                        var lastCall = renderModelCalls[renderModelCalls.length - 1];
                        var constants = [];

                        for (var j = previousCall + 1; j < lastCall; j++) {
                            var candidate = instructions.get(j);

                            if (candidate.getOpcode && candidate.getOpcode() === Opcodes.FCONST_1) {
                                constants.push(candidate);
                            }
                        }

                        if (constants.length === 3) {
                            for (var c = 0; c < 3; c++) {
                                var replacement = new InsnList();
                                replacement.add(new VarInsnNode(Opcodes.ALOAD, 7));
                                replacement.add(new InsnNode(Opcodes.ICONST_0 + c));
                                replacement.add(new InsnNode(Opcodes.FCONST_1));
                                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "armorChannel", "(Lnet/minecraft/item/ItemStack;IF)F", false));

                                instructions.insertBefore(constants[c], replacement);
                                instructions.remove(constants[c]);
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
