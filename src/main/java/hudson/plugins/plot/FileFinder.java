package hudson.plugins.plot;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.apache.log4j.Logger;

import hudson.FilePath;

/**
 * Searches base directory for all required files.
 * Does not find directories.
 *
 * @author Nikita Osiptsov
 */
public class FileFinder {
    private static final Logger LOGGER = Logger.getLogger(FileFinder.class.getName());

    private final FilePath baseDir;

    /**
     * Creates instance with given base directory (search tree root).
     *
     * @param baseDir base directory
     */
    public FileFinder(FilePath baseDir) {
        Objects.requireNonNull(baseDir);

        this.baseDir = baseDir;
    }

    /**
     * Searches base directory for all files with names matching any of
     * given patterns. Note: patterns are expected to use {@code glob} path syntax.
     *
     * @param filenamePatterns path patterns
     * @return array of found paths; may be empty if found none
     * or failed to open any directory
     */
    public FilePath[] findFiles(String... filenamePatterns) {
        try {
            final List<FilePath> files = bfsFileTree(baseDir);

            return files.stream()
                .filter(p -> matchesOne(p, filenamePatterns))
                .toArray(FilePath[]::new);
        } catch (IOException ioe) {
            final String errorMessage = "Failed to walk file tree with root '%s'.";

            LOGGER.error(String.format(errorMessage, baseDir.getRemote()), ioe);

            return new FilePath[0];
        } catch (InterruptedException ie) {
            final String errorMessage = "Interrupted walking file tree with root '%s'.";

            LOGGER.warn(String.format(errorMessage, baseDir.getRemote()), ie);
            Thread.currentThread().interrupt();

            return new FilePath[0];
        }
    }

    /**
     * @return finder's base directory
     */
    public FilePath getBaseDir() {
        return baseDir;
    }

    private List<FilePath> bfsFileTree(FilePath treeRoot) throws IOException, InterruptedException {
        final Queue<FilePath> directoryQueue = new LinkedList<>();
        final List<FilePath> resultFiles = new LinkedList<>();

        directoryQueue.add(treeRoot);
        while (!directoryQueue.isEmpty()) {
            final FilePath path = directoryQueue.poll();

            for (final FilePath child: path.list()) {
                if (child.isDirectory()) {
                    directoryQueue.add(child);

                    continue;
                }

                resultFiles.add(child);
            }
        }

        return resultFiles;
    }

    private boolean matchesOne(FilePath path, String... filenamePatterns) {
        final Path remotePath = Paths.get(path.getRemote());

        for (final String pattern: filenamePatterns) {
            final boolean matches = FileSystems.getDefault()
                .getPathMatcher(String.format("glob:%s", pattern))
                .matches(remotePath);

            if (matches) {
                return true;
            }
        }

        return false;
    }
}
