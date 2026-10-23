function initializeCoreMod() {
    return {
        'itemrenderertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.item.ItemModelResolver'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.21.4: vanilla's ItemRenderer no longer sees the ItemStack (item models are resolved into an ItemStackRenderState first), so the
                    // Dyeing Machine colour ("rtDye" tag, which 1.12.2 applied to every item model) is applied where the stack is still known: at the end of
                    // ItemModelResolver.appendItemLayers(state, stack, context, level, entity, seed), which every item draw (hand, GUI, ground, frames, our
                    // own Portkey camouflage) goes through. Locals: 1 = the ItemStackRenderState, 2 = the ItemStack.
                    if (method.name !== "appendItemLayers" || method.desc !== "(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var r = 0; r < instructions.size(); r++) {
                        var rn = instructions.get(r);
                        if (rn.getOpcode && rn.getOpcode() === Opcodes.RETURN) {
                            instructions.insertBefore(rn, new VarInsnNode(Opcodes.ALOAD, 1));
                            instructions.insertBefore(rn, new VarInsnNode(Opcodes.ALOAD, 2));
                            instructions.insertBefore(rn, new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "dyeItemLayers", "(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;)V", false));
                            r += 3;
                        }
                    }
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["dyeItemLayers"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$ItemRenderer$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
