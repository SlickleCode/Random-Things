function initializeCoreMod() {
    return {
        'superlubricentbootstransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.entity.LivingEntity'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    if (method.name !== "travel") {
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

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/block/BlockState" && insn.name === "getSlipperiness") {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "bootsMaxSlip";
                            insn.desc = "(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)F";
                            insn.itf = false;
                            break;
                        }
                    }

                    break;
                }

                return classNode;
            }
        }
    }
}
