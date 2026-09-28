function initializeCoreMod() {
    return {
        'playerentitytransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.entity.player.PlayerEntity'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    if (method.name !== "dropInventory") {
                        continue;
                    }

                    var instructions = method.instructions;

                    // Spectre Anchor's "survive death" mechanic (matching 1.12.2's own
                    // ASM patch, ground-truthed from AsmHandler's real source: a redirect
                    // into InventoryPlayer.dropAllItems itself). Ground-truthed via
                    // javap -c on this Forge version's own dropInventory() that it's a
                    // short, self-contained method: (unless keepInventory) destroy
                    // vanishing-curse items, then a single INVOKEVIRTUAL
                    // PlayerInventory.dropAllItems()V call - retargeting that one call
                    // to a static method taking the same receiver is enough to skip
                    // dropping (and clearing) any stack tagged "spectreAnchor", leaving
                    // it in the inventory array for a later PlayerEvent.Clone listener to
                    // copy across to the respawned player. No Forge event covers this -
                    // PlayerDropsEvent no longer exists in this version, LivingDropsEvent
                    // is a different (mob-loot) code path entirely, and ItemTossEvent only
                    // fires for the manual Q-key drop overload, not this one - confirmed
                    // via javap -c on all three before concluding a coremod was needed.
                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/entity/player/PlayerInventory" && insn.name === "dropAllItems") {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "dropAllItemsExceptAnchored";
                            insn.desc = "(Lnet/minecraft/entity/player/PlayerInventory;)V";
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
