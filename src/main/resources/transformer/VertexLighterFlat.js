function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.client.model.pipeline.VertexLighterFlat'
            },
            'transformer': function (classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
                var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
                var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
                
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;

                for (m in methods) 
                {
                    var method = methods[m];
                    if (method.name === "processQuad") 
                    {
                    	var insnList = new InsnList();
                    	
                    	insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    	insnList.add(new VarInsnNode(Opcodes.ILOAD, 8));
                    	insnList.add(new InsnNode(Opcodes.AALOAD));
                    	insnList.add(new InsnNode(Opcodes.ICONST_0));
                    	
                    	insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    	insnList.add(new VarInsnNode(Opcodes.ILOAD, 8));
                    	insnList.add(new InsnNode(Opcodes.AALOAD));
                    	insnList.add(new InsnNode(Opcodes.ICONST_0));
                    	insnList.add(new InsnNode(Opcodes.FALOAD));
                    	
                    	insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    	insnList.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraftforge/client/model/pipeline/VertexLighterFlat", "tint", "I"));
                    	
                    	insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lumien/randomthings/asm/AsmHandler", "modBlockLight", "(FI)F"));
						
						insnList.add(new InsnNode(Opcodes.FASTORE));
						
                    	api.insertInsnList(method, api.MethodType.VIRTUAL, "net/minecraftforge/client/model/pipeline/VertexLighterFlat", "updateColor", "([F[FFFFFI)V", insnList,api.InsertMode.INSERT_AFTER);
                        break;
                    }
                }

                // hook-site marker: how many calls into lumien/randomthings/asm this transformer left in the class, as a synthetic field
                // rt$hooks$<Tag>$<count> (the coremod.applied test reads it, so a transformer whose anchor vanished fails a test instead of passing silently)
                var rtNames = ["modBlockLight"];
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
                classNode.fields.add(new (Java.type("org.objectweb.asm.tree.FieldNode"))(0x0019, "rt$hooks$VertexLighterFlat$" + rtHooks, "Z", null, null));

                return classNode;
            }
        }
    }
}