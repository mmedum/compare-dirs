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

/** Main class. */
public final class Main {

  private static final Map<String, Node> directories = new HashMap<>();
  private static final List<String> deletions = new ArrayList<>();
  private static final List<String> rays = new ArrayList<>();

  /**
   * Main method.
   *
   * @param args Path for Files
   */
  public static void main(final String[] args) {
    final List<String> rootPaths = new ArrayList<>();

    for (final String arg : args) {
      rootPaths.add(arg.strip());
    }

    System.out.println("Starting comparison");

    for (final String rootPath : rootPaths) {
      try (Stream<Path> stream = Files.list(Paths.get(rootPath))) {
        System.out.println("Checking path: " + rootPath);
        stream
            .map(f -> f.toFile())
            .filter(f -> f.isDirectory())
            .forEach(
                f -> {
                  final String name = f.getName().toString().toLowerCase();
                  final String path = f.getPath().toString();
                  findDuplicates(name, path);
                  findRays(name, path);
                });
      } catch (final IOException e) {
        System.err.println("Cannot find path " + rootPath);
      }
    }

    System.out.println("Deletions:");
    deletions.forEach(p -> System.out.println(p));

    System.out.println("Rays:");
    rays.forEach(p -> System.out.println(p));
  }

  private static void findRays(final String name, final String path) {
    try (Stream<Path> subStream = Files.list(Paths.get(path))) {

      subStream
          .filter(p -> Files.isDirectory(p))
          .filter(p -> p.getFileName().toString().equalsIgnoreCase("BDMV"))
          .forEach(p -> rays.add(path));
    } catch (final IOException e) {
      System.err.println("Not possible to find subpath");
    }
  }

  private static void findDuplicates(final String name, final String path) {
    long size = Long.MIN_VALUE;

    try (Stream<Path> subStream = Files.list(Paths.get(path))) {
      size =
          subStream
              .filter(p -> Files.isRegularFile(p))
              .filter(p -> p.getFileName().toString().endsWith(".mkv"))
              .mapToLong(p -> p.toFile().length())
              .sum();
    } catch (final IOException e) {
      System.err.println("Not possible to find subpath");
    }

    if (size > 0 && directories.containsKey(name)) {
      final Node tempNode = directories.get(name);
      if (size >= tempNode.size()) {
        deletions.add(tempNode.path());
        directories.put(name, new Node(path, size));
      } else {
        deletions.add(path);
      }
    } else {
      directories.put(name, new Node(path, size));
    }
  }

  private static record Node(String path, long size) {}
}
