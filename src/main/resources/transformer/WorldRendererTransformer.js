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
                    var isRender = (method.name === "renderRainSnow" || method.name === "func_228438_a_") && method.desc === "(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";
                    var isParticles = (method.name === "addRainParticles" || method.name === "func_228436_a_") && method.desc === "(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V";

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

                        if (isRender && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/world/World" && (insn.name === "getBiome" || insn.name === "func_226691_t_") && insn.desc === biomeDesc) {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "getBiomeForRainRender";
                            insn.desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;";
                            insn.itf = false;
                            break;
                        } else if (isParticles && insn.getOpcode() === Opcodes.INVOKEINTERFACE && insn.owner === "net/minecraft/world/IWorldReader" && (insn.name === "getBiome" || insn.name === "func_226691_t_") && insn.desc === biomeDesc) {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "getBiomeForRainRender";
                            insn.desc = "(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;";
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$WorldRenderer$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
