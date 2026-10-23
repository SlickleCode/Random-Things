function initializeCoreMod() {
    return {
        'superlubricentbootstransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.LivingEntity'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    if (method.name !== "travel" && method.name !== "m_7023_") {
                        continue;
                    }

                    var instructions = method.instructions;

                    // Restores real 1.12.2 Super Lubricent Boots behavior,
                    // ground-truthed from AsmHandler.slipFix in the original source:
                    // while worn and not sneaking, EVERY block is treated as
                    // maximally slippery, not just the three Super Lubricent blocks.
                    // Reproducing "every surface" requires intercepting the single
                    // friction lookup inside LivingEntity.travel itself - there's no
                    // Forge event for block slipperiness in this version to hook
                    // instead. Replaces the SpongePowered Mixin this project
                    // previously used for the exact same redirect (couldn't be made
                    // to work self-contained in a mods-folder jar on this Forge
                    // version - see the plan file's "Batch 7" section).
                    //
                    // The single BlockState.getSlipperiness call in travel already
                    // passes `this` (the LivingEntity executing travel) as its own
                    // 3rd argument (the "entity standing on this block" parameter) -
                    // no extra push needed, just retarget the call itself.
                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/world/level/block/state/BlockState" && insn.name === "getFriction") {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "bootsMaxSlip";
                            insn.desc = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F";
                            insn.itf = false;
                            break;
                        }
                    }

                    break;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = [];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$SuperLubricentBoots$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
