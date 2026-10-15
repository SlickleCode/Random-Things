import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import javax.script.*;
import java.nio.file.*;
import java.util.zip.*;

/**
 * Offline coremod check: for every transformer script, loads its target class from the mapped jar, runs the real JS through Nashorn and
 * reports whether the class exists and whether any method was changed (a transformer that matches nothing fails silently at runtime).
 * usage: java -cp "<asm;asm-tree;nashorn-core;...>;out" CoremodCheck <mapped-forge-jar> <transformer-dir>
 * Found on 2026-10-15: a double-applied class rename had pointed ItemRendererTransformer at ItemEntityRenderer.
 */
public class CoremodCheck {
    static int size(ClassNode cn) {
        int n = 0;
        for (MethodNode m : cn.methods) n += m.instructions.size();
        return n;
    }

    /** every instruction's opcode and, for calls and field accesses, owner/name/descriptor: catches in-place redirects too */
    static String fingerprint(ClassNode cn) {
        StringBuilder sb = new StringBuilder();
        for (MethodNode m : cn.methods) {
            sb.append(m.name).append(m.desc).append('{');
            for (AbstractInsnNode i : m.instructions) {
                sb.append(i.getOpcode());
                if (i instanceof MethodInsnNode) { MethodInsnNode x = (MethodInsnNode) i; sb.append(x.owner).append(x.name).append(x.desc); }
                if (i instanceof FieldInsnNode) { FieldInsnNode x = (FieldInsnNode) i; sb.append(x.owner).append(x.name); }
                sb.append(';');
            }
            sb.append('}');
        }
        return sb.toString();
    }

    public static void main(String[] a) throws Exception {
        ZipFile z = new ZipFile(a[0]);
        int bad = 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(a[1]), "*.js")) {
            for (Path js : ds) {
                ScriptEngine e = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory().getScriptEngine();
                e.eval(new String(Files.readAllBytes(js)));
                ScriptObjectMirror defs = (ScriptObjectMirror) ((Invocable) e).invokeFunction("initializeCoreMod");
                for (String k : defs.keySet()) {
                    ScriptObjectMirror d = (ScriptObjectMirror) defs.get(k);
                    String target = (String) ((ScriptObjectMirror) d.get("target")).get("name");
                    ZipEntry ze = z.getEntry(target.replace('.', '/') + ".class");
                    if (ze == null) { System.out.println("MISSING CLASS " + js.getFileName() + " -> " + target); bad++; continue; }
                    ClassNode cn = new ClassNode();
                    new ClassReader(z.getInputStream(ze).readAllBytes()).accept(cn, 0);
                    String before = fingerprint(cn);
                    int sizeBefore = size(cn);
                    ((ScriptObjectMirror) d.get("transformer")).call(null, cn);
                    boolean changed = !fingerprint(cn).equals(before);
                    System.out.println((changed ? "ok        " : "UNCHANGED ") + js.getFileName() + " -> " + target + " (" + (size(cn) - sizeBefore) + " instructions added)");
                    if (!changed) bad++;
                }
            }
        }
        System.exit(bad == 0 ? 0 : 1);
    }
}
