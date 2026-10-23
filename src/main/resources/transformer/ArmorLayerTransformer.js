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
                    // Worn-armor enchant glint colour (1.12.2's armorEnchantmentHook): the static glint pass func_215338_a sets the purple glint colour with
                    // GlStateManager.color4f(0.38, 0.19, 0.608, 1) inside its 2-pass loop (the SECOND color4f in the method); right after it, let
                    // ClientAsmHandler.armorEnchantmentHook() replace that colour for tinted stacks. Ground-truthed via javap -c.
                    if (method.name === "func_215338_a") {
                        var ins2 = method.instructions;
                        var colorCalls = 0;
                        for (var q = 0; q < ins2.size(); q++) {
                            var n2 = ins2.get(q);
                            if (n2.getOpcode && n2.getOpcode() === Opcodes.INVOKESTATIC && n2.owner === "com/mojang/blaze3d/platform/GlStateManager" && (n2.name === "color4f" || n2.name === "func_179131_c")) {
                                colorCalls++;
                                if (colorCalls === 2) {
                                    ins2.insert(n2, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "armorEnchantmentHook", "()V", false));
                                    break;
                                }
                            }
                        }
                        continue;
                    }

                    if (method.name !== "renderArmorLayer" && method.name !== "func_188361_a" || method.desc !== "(Lnet/minecraft/entity/LivingEntity;FFFFFFFLnet/minecraft/inventory/EquipmentSlotType;)V") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var renderCallIndex = 0;

                    for (var g = 0; g < instructions.size(); g++) {
                        var gi = instructions.get(g);
                        if (gi.getOpcode && gi.getOpcode() === Opcodes.INVOKESTATIC && gi.name === "func_215338_a") {
                            var pre = new InsnList();
                            pre.add(new VarInsnNode(Opcodes.ALOAD, 10));
                            pre.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "setRenderingStack", "(Lnet/minecraft/item/ItemStack;)V", false));
                            instructions.insertBefore(gi, pre);
                            break;
                        }
                    }

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/client/renderer/entity/model/BipedModel" && (insn.name === "render" || insn.name === "func_78088_a") && insn.desc === "(Lnet/minecraft/entity/LivingEntity;FFFFFF)V") {
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

                    continue;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["armorColorHook", "armorEnchantmentHook", "setRenderingStack"];
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
