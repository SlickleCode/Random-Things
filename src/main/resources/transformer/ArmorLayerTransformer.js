function initializeCoreMod() {
    return {
        'armorlayertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.21.2+: every worn piece is drawn by EquipmentLayerRenderer.renderLayers (the 7-argument overload only forwards to the 8-argument one).
                    // Locals: 4 = the worn ItemStack. The colour of each layer comes from IClientItemExtensions.getArmorLayerTintColor (NeoForge's patched
                    // version of vanilla's getColorForLayer): the Dyeing Machine colour ("rtDye" tag), which 1.12.2 applied to every armor piece, replaces it
                    // right after that call, so leather and every other armor are covered in one place.
                    if (method.name !== "renderLayers" || method.desc !== "(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEINTERFACE && insn.name === "getArmorLayerTintColor") {
                            instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "armorTintColor", "(I)I", false));
                            i++;
                        }
                    }

                    // Enchant-glint colour: remember the worn stack for the RenderType glint getters; cleared before every return.
                    for (var rr = 0; rr < instructions.size(); rr++) {
                        var rnode = instructions.get(rr);
                        if (rnode.getOpcode && rnode.getOpcode() === Opcodes.RETURN) {
                            instructions.insertBefore(rnode, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "clearRenderingStack", "()V", false));
                            rr++;
                        }
                    }

                    var head = new (Java.type("org.objectweb.asm.tree.InsnList"))();
                    head.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    head.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "setRenderingStack", "(Lnet/minecraft/world/item/ItemStack;)V", false));
                    instructions.insert(head);
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["armorTintColor", "clearRenderingStack", "setRenderingStack"];
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
