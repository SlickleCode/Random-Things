function initializeCoreMod() {
    return {
        'magichoodtransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // canRenderName(T)Z - the generic, bounded-by-LivingEntity overload (not the
                    // Entity-typed bridge method that just delegates to it). Hides a player's
                    // nametag while wearing a Magic Hood, regardless of sneaking - no clean Forge
                    // event exists for this in this version (RenderNameplateEvent doesn't exist
                    // until later), unlike Magic Hood's particle-hiding half (see AsmHandler's
                    // javadoc / RandomThings's PotionColorCalculationEvent listener). Multiple
                    // early-exit branches means multiple ireturn sites (6 total) instead of the
                    // single-ireturn case FireBlockTransformer/SpawnPlacementTransformer had - same
                    // wrap-the-value-before-it-returns pattern, just applied at each one.
                    if (method.name !== "canRenderName" && method.name !== "func_177070_b" || method.desc !== "(Lnet/minecraft/entity/LivingEntity;)Z") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var returns = [];

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            returns.push(insn);
                        }
                    }

                    for (var r = 0; r < returns.length; r++) {
                        var call = new InsnList();
                        call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideCanRenderName", "(ZLnet/minecraft/entity/LivingEntity;)Z", false));
                        instructions.insertBefore(returns[r], call);
                    }

                    break;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["overrideCanRenderName"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$MagicHood$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
