function initializeCoreMod() {
    return {
        'worldrenderertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.WorldRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.16 moved the falling rain/snow visual from GameRenderer to WorldRenderer.
                    // renderRainSnow(LightTexture, F, D, D, D)V: its per-column loop calls
                    // World.getBiome(BlockPos) exactly once, then checks biome.getPrecipitation() != NONE
                    // against the result. That ONE call site (both stack args - world, pos - are already
                    // pushed as needed) is retargeted to AsmHandler.getBiomeForRainRender, which
                    // substitutes a no-precipitation biome for columns inside an active Rain Shield's
                    // radius and returns the real biome everywhere else.
                    // addRainParticles(ActiveRenderInfo)V (the splash particles and rain sound) does the
                    // same lookup through IWorldReader.getBiome and gets the same treatment, so a shielded
                    // column has neither rain streaks nor splashes.
                    var isRender = method.name === "renderRainSnow" && method.desc === "(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";
                    var isParticles = method.name === "addRainParticles" && method.desc === "(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V";

                    if (!isRender && !isParticles) {
                        continue;
                    }

                    var instructions = method.instructions;
                    var biomeDesc = "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;";

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (!insn.getOpcode) {
                            continue;
                        }

                        if (isRender && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/world/World" && insn.name === "getBiome" && insn.desc === biomeDesc) {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "getBiomeForRainRender";
                            insn.desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;";
                            insn.itf = false;
                            break;
                        } else if (isParticles && insn.getOpcode() === Opcodes.INVOKEINTERFACE && insn.owner === "net/minecraft/world/IWorldReader" && insn.name === "getBiome" && insn.desc === biomeDesc) {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "getBiomeForRainRender";
                            insn.desc = "(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;";
                            insn.itf = false;
                            break;
                        }
                    }
                }

                return classNode;
            }
        }
    }
}
