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

    public static void main(String[] args) {
        List<String> rootPaths = new ArrayList<>();
        Map<String, Node> directories = new HashMap<>();
        List<String> deletions = new ArrayList<>();

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
                        });
            } catch (IOException e) {
                System.err.println("Cannot find path " + rootPath);
            }
        }

        deletions.forEach(p -> System.out.println(p));
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
