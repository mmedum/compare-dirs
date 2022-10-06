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
                            String name = f.getName();
                            String path = f.getPath();
                            long size = f.length();
                            if (directories.containsKey(name)) {
                                Node tempNode = directories.get(name);
                                if (size >= tempNode.getSize()) {
                                    System.out
                                            .println(tempNode.getName() + "@" + tempNode.getPath() + " to be deleted");
                                    directories.put(name, new Node(name, path, size));
                                } else {
                                    System.out.println(name + "@" + path + " to be deleted");
                                }
                            } else {
                                directories.put(f.getName(), new Node(f.getName(), f.getPath(), f.length()));
                            }
                        });
            } catch (IOException e) {
                System.err.println("Cannot find path " + rootPath);
            }
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
