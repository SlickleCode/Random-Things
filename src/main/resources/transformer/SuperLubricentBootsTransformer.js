function initializeCoreMod() {
    return {
        'superlubricentbootstransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.LivingEntity'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.21.2+: the friction lookup moved out of travel() into the private travelInAir(Vec3). NeoForge's patched LivingEntity calls
                    // BlockState.getFriction(LevelReader, BlockPos, Entity) there (vanilla's own is Block.getFriction()), whose 3rd argument is already `this`.
                    // Restores real 1.12.2 Super Lubricent Boots behavior: while worn and not sneaking EVERY block is maximally slippery.
                    var instructions = method.instructions;

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
