package hudson.plugins.plot;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Searches base directory for all required files.
 * 
 * @author Nikita Osiptsov
 */
public class FileFinder {
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
     * Searches base directory for all files matching any of
     * given patterns.
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
            return new Path[0];
        }
    }

    private boolean matchesOne(Path path, String ...filenamePatterns) {
        for(final String pattern: filenamePatterns) {
            final boolean matches = FileSystems.getDefault()
                .getPathMatcher(String.format("glob:%s", pattern))
                .matches(path);

            if(matches) {
                return true;
            }
        }

        return false;
    }
}
