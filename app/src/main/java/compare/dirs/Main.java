package compare.dirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static List<String> rootPaths;
    private static Map<String, Double> directories;

    public static void main(String[] args) {
        rootPaths = new ArrayList<>();
        directories = new HashMap<>();

        for (String arg : args) {
            rootPaths.add(arg.strip());
        }

        System.out.println("Starting comparison");

        for (String rootPath : rootPaths) {
            try (Stream<Path> stream = Files.walk(Paths.get(rootPath))) {
                System.out.println("Checking path: " + rootPath);
                Map<String, Long> result = stream.map(f -> f.toFile())
                        .filter(f -> f.isDirectory())
                        .collect(Collectors.toMap(f -> f.getName(), f -> f.length(), (f1, f2) -> f2));

                System.out.println(result);
            } catch (IOException e) {
                System.err.println("Cannot find path " + rootPath);
            }
        }
    }
}
