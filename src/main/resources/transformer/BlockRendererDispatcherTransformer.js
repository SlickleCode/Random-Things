function initializeCoreMod() {
    return {
        'blockrendererdispatchertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.BlockRendererDispatcher'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
                var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
                var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // renderBlock(BlockState, BlockPos, IEnviromentBlockReader, BufferBuilder,
                    // Random, IModelData)Z - the real method ChunkRender actually calls (confirmed
                    // via source, not the deprecated 5-arg func_215330_a overload it delegates
                    // from). Same "early conditional-IRETURN at method entry" idiom as
                    // TeleporterTransformer.js: call ClientAsmHandler first, and only fall through
                    // to the original body (real vanilla per-block rendering) when it says this
                    // position isn't redirected by any Light Redirector.
                    if (method.name !== "renderBlock" || method.desc !== "(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IEnviromentBlockReader;Lnet/minecraft/client/renderer/BufferBuilder;Ljava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var first = instructions.getFirst();
                    var continueLabel = new LabelNode();

                    var prologue = new InsnList();
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 5));
                    prologue.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "renderBlock", "(Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IEnviromentBlockReader;Lnet/minecraft/client/renderer/BufferBuilder;Ljava/util/Random;)I", false));
                    prologue.add(new InsnNode(Opcodes.DUP));
                    prologue.add(new InsnNode(Opcodes.ICONST_2));
                    prologue.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, continueLabel));
                    prologue.add(new InsnNode(Opcodes.IRETURN));
                    prologue.add(continueLabel);
                    prologue.add(new InsnNode(Opcodes.POP));

                    instructions.insertBefore(first, prologue);

                    break;
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["renderBlock"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$BlockRendererDispatcher$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}
