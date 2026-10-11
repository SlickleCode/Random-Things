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

                    // 1.16 BlockRendererDispatcher.renderModel(BlockState, BlockPos, IBlockDisplayReader,
                    // MatrixStack, IVertexBuilder, boolean checkSides, Random, IModelData)Z - the method the
                    // chunk renderer calls for each block (the 7-arg overload delegates to it). Same "early
                    // conditional-IRETURN at method entry" idiom as before: call ClientAsmHandler first, and
                    // only fall through to the original body (real vanilla per-block rendering) when it says
                    // this position isn't redirected by any Light Redirector.
                    // Locals: 0 this, 1 state, 2 pos, 3 world, 4 matrixStack, 5 vertexBuilder, 6 checkSides, 7 random.
                    if (method.name !== "renderModel" || method.desc !== "(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z") {
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
                    prologue.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/ClientAsmHandler", "renderBlock", "(Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;)I", false));
                    prologue.add(new InsnNode(Opcodes.DUP));
                    prologue.add(new InsnNode(Opcodes.ICONST_2));
                    prologue.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, continueLabel));
                    prologue.add(new InsnNode(Opcodes.IRETURN));
                    prologue.add(continueLabel);
                    prologue.add(new InsnNode(Opcodes.POP));

                    instructions.insertBefore(first, prologue);

                    break;
                }

                return classNode;
            }
        }
    }
}
