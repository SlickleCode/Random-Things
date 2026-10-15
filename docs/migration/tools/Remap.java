import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/** Type-aware source remapper: MCP 1.16.5 names -> Mojang names, driven by javac attribution against the 1.16.5 classpath. */
public class Remap {
    static Map<String, String> classes = new HashMap<>();
    static Map<String, String> methods = new HashMap<>(); // binaryClass#name
    static Map<String, String> fields = new HashMap<>();
    static Trees trees;
    static Elements elements;
    static Map<String, Integer> stats = new TreeMap<>();
    static int unresolved = 0;

    static void load(String dir) throws IOException {
        for (String l : Files.readAllLines(Paths.get(dir, "classes.tsv"))) { String[] p = l.split("\t"); classes.put(p[0], p[1]); }
        for (String l : Files.readAllLines(Paths.get(dir, "methods.tsv"))) { String[] p = l.split("\t"); methods.put(p[0] + "#" + p[1], p[2]); }
        for (String l : Files.readAllLines(Paths.get(dir, "fields.tsv"))) { String[] p = l.split("\t"); fields.put(p[0] + "#" + p[1], p[2]); }
    }

    public static void main(String[] a) throws Exception {
        String dir = a[0];
        String cp = new String(Files.readAllBytes(Paths.get(a[1])), StandardCharsets.UTF_8).trim();
        Path src = Paths.get(a[2]);
        load(dir);
        List<File> files;
        try (Stream<Path> s = Files.walk(src)) { files = s.filter(p -> p.toString().endsWith(".java")).map(Path::toFile).collect(Collectors.toList()); }
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        List<String> opts = Arrays.asList("-proc:none", "-cp", cp, "-source", "8", "-target", "8", "-Xlint:none", "-Xmaxerrs", "100000", "-implicit:none");
        int[] errs = {0};
        DiagnosticListener<JavaFileObject> dl = d -> { if (d.getKind() == Diagnostic.Kind.ERROR) { errs[0]++; if (errs[0] < 15) System.err.println(d); } };
        JavacTask task = (JavacTask) jc.getTask(null, fm, dl, opts, null, fm.getJavaFileObjectsFromFiles(files));
        trees = Trees.instance(task);
        elements = task.getElements();
        SourcePositions sp = trees.getSourcePositions();
        Iterable<? extends CompilationUnitTree> units = task.parse();
        task.analyze();
        System.err.println("javac errors during analysis: " + errs[0]);
        for (CompilationUnitTree cu : units) {
            String text = cu.getSourceFile().getCharContent(true).toString();
            List<int[]> ed = new ArrayList<>();
            List<String> et = new ArrayList<>();
            Scan sc = new Scan(cu, text, sp, ed, et);
            sc.scan(cu, null);
            for (ImportTree it : cu.getImports()) sc.handleImport(it);
            if (ed.isEmpty()) continue;
            Integer[] idx = new Integer[ed.size()];
            for (int i = 0; i < idx.length; i++) idx[i] = i;
            Arrays.sort(idx, (x, y) -> ed.get(y)[0] != ed.get(x)[0] ? ed.get(y)[0] - ed.get(x)[0] : ed.get(y)[1] - ed.get(x)[1]);
            StringBuilder sb = new StringBuilder(text);
            int lastStart = Integer.MAX_VALUE;
            Set<String> seen = new HashSet<>();
            for (int i : idx) {
                int[] e = ed.get(i);
                if (e[1] > lastStart) continue;
                if (!seen.add(e[0] + ":" + e[1])) continue;
                sb.replace(e[0], e[1], et.get(i));
                lastStart = e[0];
            }
            Files.write(Paths.get(cu.getSourceFile().toUri()), sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        System.err.println("stats: " + stats + " unresolved=" + unresolved);
    }

    static class Scan extends TreePathScanner<Void, Void> {
        final CompilationUnitTree cu;
        final String text;
        final SourcePositions sp;
        final List<int[]> ed;
        final List<String> et;

        Scan(CompilationUnitTree cu, String text, SourcePositions sp, List<int[]> ed, List<String> et) { this.cu = cu; this.text = text; this.sp = sp; this.ed = ed; this.et = et; }

        void edit(int s, int e, String n, String kind) { ed.add(new int[]{s, e}); et.add(n); stats.merge(kind, 1, Integer::sum); }

        void handleImport(ImportTree it) {
            Tree q = it.getQualifiedIdentifier();
            if (!(q instanceof MemberSelectTree)) return;
            MemberSelectTree ms = (MemberSelectTree) q;
            TreePath base = new TreePath(new TreePath(cu), it);
            int s = (int) sp.getStartPosition(cu, q), en = (int) sp.getEndPosition(cu, q);
            if (!it.isStatic()) {
                Element e = trees.getElement(new TreePath(base, q));
                if (e instanceof TypeElement) {
                    String moj = classes.get(elements.getBinaryName((TypeElement) e).toString());
                    if (moj != null) {
                        String n = moj.replace('$', '.');
                        if (!text.substring(s, en).equals(n)) edit(s, en, n, "import");
                    }
                }
            } else {
                ExpressionTree cls = ms.getExpression();
                Element ce = trees.getElement(new TreePath(base, cls));
                if (ce instanceof TypeElement) {
                    String bin = elements.getBinaryName((TypeElement) ce).toString();
                    String moj = classes.get(bin);
                    String member = ms.getIdentifier().toString();
                    String newMember = member;
                    String ff = fields.get(bin + "#" + member), mm = methods.get(bin + "#" + member);
                    if (ff != null) newMember = ff; else if (mm != null) newMember = mm;
                    String clsText = moj != null ? moj.replace('$', '.') : text.substring((int) sp.getStartPosition(cu, cls), (int) sp.getEndPosition(cu, cls));
                    String n = clsText + "." + newMember;
                    if (!text.substring(s, en).equals(n)) edit(s, en, n, "staticimport");
                }
            }
        }

        @Override public Void visitImport(ImportTree n, Void v) { return null; }

        @Override public Void visitIdentifier(IdentifierTree n, Void v) {
            String name = n.getName().toString();
            if (!name.equals("this") && !name.equals("super")) {
                int s = (int) sp.getStartPosition(cu, n);
                handle(trees.getElement(getCurrentPath()), s, s + name.length(), name);
            }
            return super.visitIdentifier(n, v);
        }

        @Override public Void visitMemberSelect(MemberSelectTree n, Void v) {
            String name = n.getIdentifier().toString();
            if (!name.equals("this") && !name.equals("class") && !name.equals("super")) {
                int en = (int) sp.getEndPosition(cu, n);
                if (en - name.length() >= 0 && text.startsWith(name, en - name.length())) handle(trees.getElement(getCurrentPath()), en - name.length(), en, name);
            }
            return super.visitMemberSelect(n, v);
        }

        @Override public Void visitMemberReference(MemberReferenceTree n, Void v) {
            String name = n.getName().toString();
            if (!name.equals("<init>")) {
                int en = (int) sp.getEndPosition(cu, n);
                if (text.startsWith(name, en - name.length())) handle(trees.getElement(getCurrentPath()), en - name.length(), en, name);
            }
            return super.visitMemberReference(n, v);
        }

        @Override public Void visitMethod(MethodTree n, Void v) {
            String name = n.getName().toString();
            if (!name.equals("<init>")) {
                int from = n.getReturnType() != null ? (int) sp.getEndPosition(cu, n.getReturnType()) : (int) sp.getStartPosition(cu, n);
                Matcher m = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\(").matcher(text);
                if (from >= 0 && m.find(from)) handle(trees.getElement(getCurrentPath()), m.start(), m.start() + name.length(), name);
            }
            return super.visitMethod(n, v);
        }

        void handle(Element e, int s, int en, String cur) {
            if (e == null) { unresolved++; return; }
            if (e instanceof TypeElement) {
                String moj = classes.get(elements.getBinaryName((TypeElement) e).toString());
                if (moj != null) {
                    String simple = moj.substring(Math.max(moj.lastIndexOf('.'), moj.lastIndexOf('$')) + 1);
                    if (!simple.equals(cur)) edit(s, en, simple, "type");
                }
            } else if (e instanceof ExecutableElement && e.getKind() == ElementKind.METHOD) {
                String n = methodName((ExecutableElement) e);
                if (n != null && !n.equals(cur)) edit(s, en, n, "method");
            } else if (e instanceof VariableElement && (e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT)) {
                String n = fields.get(elements.getBinaryName((TypeElement) e.getEnclosingElement()) + "#" + cur);
                if (n != null && !n.equals(cur)) edit(s, en, n, "field");
            }
        }
    }

    static String methodName(ExecutableElement m) {
        TypeElement owner = (TypeElement) m.getEnclosingElement();
        String r = methods.get(elements.getBinaryName(owner) + "#" + m.getSimpleName());
        if (r != null) return r;
        Deque<TypeElement> q = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        q.add(owner);
        while (!q.isEmpty()) {
            TypeElement t = q.poll();
            if (!seen.add(t.getQualifiedName().toString())) continue;
            if (t != owner) {
                for (Element x : t.getEnclosedElements()) {
                    if (x.getKind() == ElementKind.METHOD && x.getSimpleName().equals(m.getSimpleName()) && elements.overrides(m, (ExecutableElement) x, owner)) {
                        String rr = methods.get(elements.getBinaryName(t) + "#" + x.getSimpleName());
                        if (rr != null) return rr;
                    }
                }
            }
            TypeMirror sup = t.getSuperclass();
            if (sup.getKind() == TypeKind.DECLARED) q.add((TypeElement) ((DeclaredType) sup).asElement());
            for (TypeMirror i : t.getInterfaces()) if (i.getKind() == TypeKind.DECLARED) q.add((TypeElement) ((DeclaredType) i).asElement());
        }
        return null;
    }
}
