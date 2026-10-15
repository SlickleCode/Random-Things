function initializeCoreMod() {
    return {
        'spawnplacementtransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.SpawnPlacements'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // func_223515_a(EntityType, IWorld, SpawnReason, BlockPos, Random)Z - the
                    // single shared dispatch point every registered entity's spawn-placement
                    // predicate result flows through (confirmed via javap -c: it looks up the
                    // EntityType's registered predicate and returns its raw boolean result, one
                    // ireturn total). Restores the ALLOW direction for Slime Cube and Lapis
                    // Lamp, which a LivingSpawnEvent.CheckSpawn listener can't do - SlimeEntity's
                    // and MonsterEntity's own predicates exit early and return false well before
                    // that event ever fires (see AsmHandler#overrideSpawnResult's javadoc for the
                    // full trace). Not a Mixin because the same "can't self-bootstrap from a
                    // mods-folder jar on this Forge version" problem as Batch 7's other half.
                    if (method.name !== "checkSpawnRules" && method.name !== "m_21759_" || method.desc !== "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.IRETURN) {
                            // Stack here is [computedBoolean]. Push the EntityType (param 0),
                            // IWorld (param 1) and BlockPos (param 3) and retarget through
                            // AsmHandler, which returns the (possibly overridden) boolean the
                            // original ireturn then returns unchanged.
                            var call = new InsnList();
                            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            call.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideSpawnResult", "(ZLnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;)Z", false));
                            instructions.insertBefore(insn, call);
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
