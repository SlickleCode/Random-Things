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

                    if (method.name !== "dropInventory" && method.name !== "func_213337_cE") {
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

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/entity/player/PlayerInventory" && (insn.name === "dropAllItems" || insn.name === "func_70436_m")) {
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$PlayerEntity$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
