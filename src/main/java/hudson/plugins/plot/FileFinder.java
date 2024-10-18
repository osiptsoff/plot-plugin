package hudson.plugins.plot;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import hudson.FilePath;

/**
 * Searches base directory for all required files on local machine.
 *
 * @author Nikita Osiptsov
 */
public class FileFinder {
    private static final Logger LOGGER = Logger.getLogger(FileFinder.class.getName());

    private final Path baseDir;

    /**
     * Creates instance with given base directory.
     *
     * @param baseDir base directory
     */
    public FileFinder(Path baseDir) {
        Objects.requireNonNull(baseDir);

        this.baseDir = baseDir.toAbsolutePath();
    }

    /**
     * Creates instance with given base directory.
     *
     * @param baseDir base directory
     */
    public FileFinder(FilePath baseDir) {
        Objects.requireNonNull(baseDir);

        final String baseDirPathString = baseDir.getRemote();
        final String fsSeparator = FileSystems.getDefault().getSeparator();
        final String[] splitPath = baseDirPathString.split(fsSeparator);

        this.baseDir = Paths.get(splitPath[0],
            Arrays.copyOfRange(splitPath, 1, splitPath.length));
    }

    /**
     * Searches local base directory for all files matching any of
     * given patterns. Note: patterns are expected to use {@code glob} path syntax.
     *
     * @param filenamePatterns path patterns
     * @return array of found paths; may be empty if found none
     * or failed to open base directory
     */
    public Path[] findFiles(String... filenamePatterns) {
        try (final Stream<Path> paths = Files.walk(baseDir)) {
            return paths
                .filter(p -> matchesOne(p, filenamePatterns))
                .toArray(Path[]::new);

        } catch (IOException ioe) {
            LOGGER.error(String.format("Failed to traverse fs tree with root '%s'.", baseDir),
                ioe);
            return new Path[0];
        }
    }

    /**
     * @return finder's base directory
     */
    public Path getBaseDir() {
        return baseDir;
    }

    private boolean matchesOne(Path path, String... filenamePatterns) {
        for (final String pattern: filenamePatterns) {
            final boolean matches = FileSystems.getDefault()
                .getPathMatcher(String.format("glob:%s", pattern))
                .matches(path);

            if (matches) {
                return true;
            }
        }

        return false;
    }
}
