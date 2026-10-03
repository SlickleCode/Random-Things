function initializeCoreMod() {
    return {
        'armorlayertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.layers.ArmorLayer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // renderArmorLayer(LivingEntity,F,F,F,F,F,F,F,EquipmentSlotType)V - ground-
                    // truthed via javap -c. Calls BipedModel.render(...) exactly twice: once inside
                    // the IDyeableArmorItem (leather) branch, with the vanilla dye color already
                    // applied via GlStateManager.color4f, and once unconditionally afterward (the
                    // only render() call at all for non-leather armor) preceded by its own plain
                    // color4f(colorR,colorG,colorB,alpha) call. That SECOND render() call is the one
                    // that always fires regardless of armor type - same call 1.12.2's own
                    // ClassTransformer#patchLayerArmorBase targeted via its "renderCounter == 1"
                    // (second occurrence) check on func_78088_a. This inserts
                    // ClientAsmHandler.armorColorHook(itemstack) - itemstack is local var 10 - right
                    // before that second render() call; the hook re-applies GL color, overriding
                    // whatever the vanilla call just set, only when the worn item carries a Dyeing
                    // Machine "rtDye" tag.
                    if (method.name !== "renderArmorLayer" || method.desc !== "(Lnet/minecraft/entity/LivingEntity;FFFFFFFLnet/minecraft/inventory/EquipmentSlotType;)V") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var renderCallIndex = 0;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/client/renderer/entity/model/BipedModel" && insn.name === "render" && insn.desc === "(Lnet/minecraft/entity/LivingEntity;FFFFFF)V") {
                            if (renderCallIndex === 1) {
                                var toInsert = new InsnList();
                                toInsert.add(new VarInsnNode(Opcodes.ALOAD, 10));
                                toInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "armorColorHook", "(Lnet/minecraft/item/ItemStack;)V", false));

                                instructions.insertBefore(insn, toInsert);
                                break;
                            }

                            renderCallIndex++;
                        }
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
