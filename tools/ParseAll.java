import com.sun.source.util.JavacTask;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Syntax-only check of every .java file under a directory, using the JDK compiler's parser (no Minecraft needed, so it
 * also covers the Minecraft glue, which the genetics self-check cannot compile). Run from the project root:
 *
 *   java tools/ParseAll.java src/main/java
 *
 * Prints "parsed N files, 0 errors"; exit code 1 on a syntax error. It does NOT type-check: only the real Gradle build does that.
 */
public class ParseAll {
    public static void main(String[] args) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diags, null, null);
        List<Path> files;
        try (Stream<Path> w = Files.walk(Path.of(args[0]))) {
            files = w.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
        JavacTask task = (JavacTask) compiler.getTask(null, fm, diags, List.of(), null,
                fm.getJavaFileObjectsFromPaths(files));
        task.parse();
        int errors = 0;
        for (Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics()) {
            if (d.getKind() == Diagnostic.Kind.ERROR) {
                errors++;
                System.out.println(d);
            }
        }
        System.out.println("parsed " + files.size() + " files, " + errors + " errors");
        System.exit(errors == 0 ? 0 : 1);
    }
}
