function initializeCoreMod() {
    return {
        'gamerenderertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.GameRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // renderRainSnow(F)V - the falling rain/snow visual, ground-truthed via
                    // javap -c: its per-column loop calls World.getBiome(BlockPos) exactly
                    // once, then immediately checks biome.getPrecipitation() != NONE against
                    // whatever that call returned. Retargets that ONE call site (both stack
                    // args - world, pos - are already pushed exactly as needed, no extra
                    // instructions to insert) to AsmHandler.getBiomeForRainRender, which
                    // substitutes a real no-precipitation biome for columns inside an active
                    // Rain Shield's radius and returns the real biome everywhere else. No
                    // other World.getBiome call anywhere in the game is touched - this only
                    // ever fires from inside this one method.
                    if (method.name !== "renderRainSnow" || method.desc !== "(F)V") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (!insn.getOpcode) {
                            continue;
                        }

                        if (insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/world/World" && insn.name === "getBiome" && insn.desc === "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;") {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "getBiomeForRainRender";
                            insn.desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;";
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
