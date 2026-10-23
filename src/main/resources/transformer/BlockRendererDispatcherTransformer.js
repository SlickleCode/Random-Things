function initializeCoreMod() {
    return {
        'blockrendererdispatchertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.block.BlockRenderDispatcher'
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

                    // 1.20.1: the 9-arg Forge overload renderBatched(..., RandomSource, ModelData, RenderType)V (1.19.2 had a trailing boolean) (the chunk rebuild calls it; it returns void, so the early return is a plain RETURN and renderBlock's result is only used as "2 = render normally"). Was 1.16 renderModel(BlockState, BlockPos, IBlockDisplayReader,
                    // MatrixStack, IVertexBuilder, boolean checkSides, Random, IModelData)Z - the method the
                    // chunk renderer calls for each block (the 7-arg overload delegates to it). Same "early
                    // conditional-IRETURN at method entry" idiom as before: call ClientAsmHandler first, and
                    // only fall through to the original body (real vanilla per-block rendering) when it says
                    // this position isn't redirected by any Light Redirector.
                    // Locals: 0 this, 1 state, 2 pos, 3 world, 4 matrixStack, 5 vertexBuilder, 6 checkSides, 7 random.
                    if (method.name !== "renderBatched" || method.desc !== "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V") {
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
                    prologue.add(new VarInsnNode(Opcodes.ILOAD, 6));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 7));
                    prologue.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "renderBlock", "(Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)I", false));
                    prologue.add(new InsnNode(Opcodes.DUP));
                    prologue.add(new InsnNode(Opcodes.ICONST_2));
                    prologue.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, continueLabel));
                    prologue.add(new InsnNode(Opcodes.POP));
                    prologue.add(new InsnNode(Opcodes.RETURN));
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
