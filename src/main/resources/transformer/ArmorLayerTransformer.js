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
                    // 1.20.6: the colour of every armor layer is the three float arguments of the private
                    // renderModel(PoseStack, MultiBufferSource, int, Model, float r, float g, float b, ResourceLocation)
                    // (locals 5-7); patching its entry covers leather and every other armor in one place.
                    if (method.name === "renderModel" && method.desc === "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V") {
                        var tint = new InsnList();
                        for (var ch = 0; ch < 3; ch++) {
                            tint.add(new VarInsnNode(Opcodes.FLOAD, 5 + ch));
                            tint.add(new InsnNode(Opcodes.ICONST_0 + ch));
                            tint.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "armorTintChannel", "(FI)F", false));
                            tint.add(new VarInsnNode(Opcodes.FSTORE, 5 + ch));
                        }
                        method.instructions.insert(tint);
                        continue;
                    }

                    if (method.name !== "renderArmorPiece" || method.desc !== "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V") {
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

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["armorTintChannel", "clearRenderingStack", "setRenderingStackFromSlot"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$ArmorLayer$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
