function initializeCoreMod() {
    return {
        'teleportertransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.Teleporter'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
                var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
                var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var methods = classNode.methods;

                for (m in methods) {
                    var method = methods[m];

                    // makePortal(Entity)Z - the actual world-mutating half of vanilla's
                    // generic (non-Nether/End) dimension-travel portal placement; the
                    // other half, func_222268_a ("is there already a portal here"), is a
                    // harmless read-only scan that safely returns false in the Spectre
                    // void and needs no patch. This can't use the project's usual "wrap
                    // the value at the IRETURN" idiom (see AsmHandler#overrideMakePortal's
                    // javadoc) - the whole point is to skip the body's block placement
                    // entirely for the Spectre dimension, not just override what it
                    // returns after already running. Early-conditional-IRETURN at method
                    // entry instead: call AsmHandler first, and only fall through to the
                    // original (untouched) body when it says this isn't the Spectre
                    // dimension.
                    if (method.name !== "makePortal" || method.desc !== "(Lnet/minecraft/entity/Entity;)Z") {
                        continue;
                    }

                    var instructions = method.instructions;
                    var first = instructions.getFirst();
                    var continueLabel = new LabelNode();

                    var prologue = new InsnList();
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    prologue.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/Teleporter", "world", "Lnet/minecraft/world/server/ServerWorld;"));
                    prologue.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prologue.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "overrideMakePortal", "(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/entity/Entity;)Z", false));
                    prologue.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
                    prologue.add(new InsnNode(Opcodes.ICONST_1));
                    prologue.add(new InsnNode(Opcodes.IRETURN));
                    prologue.add(continueLabel);

                    instructions.insertBefore(first, prologue);

                    break;
                }

                return classNode;
            }
        }
    }
}
