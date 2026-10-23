function initializeCoreMod() {
    return {
        'itemstackrendertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.item.ItemStackRenderState'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.21.4: the enchant glint of an item is picked while ItemStackRenderState.render(PoseStack, MultiBufferSource, int, int) draws its layers,
                    // where the ItemStack is no longer known. ClientAsmHandler.dyeItemLayers remembers it per render state (end of ItemModelResolver.appendItemLayers),
                    // so render starts by telling the RenderType glint hooks which stack this state came from and clears it again before every return.
                    if (method.name !== "render" || method.desc !== "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var r = 0; r < instructions.size(); r++) {
                        var rn = instructions.get(r);
                        if (rn.getOpcode && rn.getOpcode() === Opcodes.RETURN) {
                            instructions.insertBefore(rn, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "clearRenderingStack", "()V", false));
                            r++;
                        }
                    }

                    var head = new (Java.type("org.objectweb.asm.tree.InsnList"))();
                    head.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    head.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "beginItemRender", "(Lnet/minecraft/client/renderer/item/ItemStackRenderState;)V", false));
                    instructions.insert(head);
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["beginItemRender", "clearRenderingStack"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$ItemStackRender$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
