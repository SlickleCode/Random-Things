function initializeCoreMod() {
    return {
        'armorlayertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer'
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

                    // Dyeing Machine colour ("rtDye" tag), which 1.12.2 applied to every armor piece:
                    // 1.21: the colour of every armor layer is the int (ARGB) argument of the private
                    // renderModel(PoseStack, MultiBufferSource, int, Model, int color, ResourceLocation) (local 5);
                    // patching its entry covers leather and every other armor in one place. (1.20.6 passed three floats.)
                    if (method.name === "renderModel" && method.desc === "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V") {
                        var tint = new InsnList();
                        tint.add(new VarInsnNode(Opcodes.ILOAD, 5));
                        tint.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "armorTintColor", "(I)I", false));
                        tint.add(new VarInsnNode(Opcodes.ISTORE, 5));
                        method.instructions.insert(tint);
                        continue;
                    }

                    // 1.21 (NeoForge) renders through the 12-argument overload (the 6-argument one only forwards), so both are patched.
                    var armorPrefix = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;";
                    if (method.name !== "renderArmorPiece" || (method.desc !== armorPrefix + ")V" && method.desc !== armorPrefix + "FFFFFF)V")) {
                        continue;
                    }

                    var instructions = method.instructions;

                    // renderArmorPiece(PoseStack, MultiBufferSource, LivingEntity, EquipmentSlot, int, HumanoidModel): locals 3/4 = entity/slot.
                    // Enchant-glint colour: remember the worn stack (entity.getItemBySlot(slot)) for the RenderType glint getters; cleared before every return.
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
                    // (the slot lookup lives in Java so the method name is not hand-matched here)
                    armorHead.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "setRenderingStackFromSlot", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", false));
                    instructions.insert(armorHead);
                }

                return classNode;
            }
        }
    }
}
