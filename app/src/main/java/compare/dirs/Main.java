package compare.dirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Main {

    private static Map<String, Node> directories;
    private static List<String> deletions;
    private static List<String> rays;

    public static void main(String[] args) {
        List<String> rootPaths = new ArrayList<>();
        directories = new HashMap<>();
        deletions = new ArrayList<>();
        rays = new ArrayList<>();

        for (String arg : args) {
            rootPaths.add(arg.strip());
        }

        System.out.println("Starting comparison");

        for (String rootPath : rootPaths) {
            try (Stream<Path> stream = Files.walk(Paths.get(rootPath))) {
                System.out.println("Checking path: " + rootPath);
                stream.map(f -> f.toFile())
                        .filter(f -> f.isDirectory())
                        .forEach(f -> {
                            String name = f.getName().toString();
                            String path = f.getPath().toString();
                            findDuplicates(name, path);
                            findRays(name, path);
                        });
            } catch (IOException e) {
                System.err.println("Cannot find path " + rootPath);
            }
        }

        System.out.println("Deletions:");
        deletions.forEach(p -> System.out.println(p));

        System.out.println("Rays:");
        //rays.forEach(p -> System.out.println(p));

    }

    private static void findRays(String name, String path) {
        try (Stream<Path> subStream = Files.walk(Paths.get(path))) {
            subStream.filter(p -> Files.isDirectory(p))
                    .filter(p -> p.getFileName().toString().equals("BDMV"))
                    .forEach(p -> System.out.println(p));
        } catch (IOException e) {
            System.err.println("Not possible to find subpath");
        }
    }

    private static void findDuplicates(String name, String path) {
        long size = Long.MIN_VALUE;

        try (Stream<Path> subStream = Files.walk(Paths.get(path))) {
            size = subStream.filter(p -> Files.isRegularFile(p))
                    .filter(p -> p.getFileName().toString().endsWith(".mkv"))
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            System.err.println("Not possible to find subpath");
        }

        if (size > 0 && directories.containsKey(name)) {
            Node tempNode = directories.get(name);
            if (size >= tempNode.getSize()) {
                deletions.add(tempNode.getPath() + "@" + tempNode.getName());
                directories.put(name, new Node(name, path, size));
            } else {
                deletions.add(path + "@" + name);
            }
        } else {
            directories.put(name, new Node(name, path, size));
        }
    }

    private static class Node {

        private String name;
        private String path;
        private long size;

        public Node(String name, String path, long size) {
            this.name = name;
            this.path = path;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }
    }
}
