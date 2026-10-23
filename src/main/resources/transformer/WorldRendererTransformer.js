function initializeCoreMod() {
    return {
        'worldrenderertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.WeatherEffectRenderer'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // 1.21.4 moved the falling rain/snow streaks and the splash particles/rain sound out of LevelRenderer into WeatherEffectRenderer,
                    // and both ask the same private getPrecipitationAt(Level, BlockPos)Biome$Precipitation, which looks the biome up with exactly one
                    // Level.getBiome(BlockPos). That ONE call site (world and pos are already on the stack) is retargeted to
                    // AsmHandler.getBiomeForRainRender, which substitutes a no-precipitation biome for columns inside an active Rain Shield's radius
                    // and returns the real biome everywhere else, so a shielded column has neither rain streaks nor splashes.
                    if (method.name !== "getPrecipitationAt" || method.desc !== "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;") {
                        continue;
                    }

                    var instructions = method.instructions;

                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);

                        if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.owner === "net/minecraft/world/level/Level" && (insn.name === "getBiome" || insn.name === "m_204166_") && insn.desc === "(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;") {
                            insn.setOpcode(Opcodes.INVOKESTATIC);
                            insn.owner = "lumien/randomthings/asm/AsmHandler";
                            insn.name = "getBiomeForRainRender";
                            insn.desc = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;";
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
