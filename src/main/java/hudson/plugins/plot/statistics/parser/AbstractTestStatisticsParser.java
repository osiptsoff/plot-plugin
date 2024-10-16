package hudson.plugins.plot.statistics.parser;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.ParseException;

import hudson.plugins.plot.statistics.TestStatistics;

/**
 * Parses {@link TestStatistics} from given file.
 * 
 * @author Nikita Osiptsov
 */
public abstract class AbstractTestStatisticsParser {
    /**
     * Parses test statistics from given file.
     * 
     * @param filePath path to file with containing test statistics
     * @return statistics from given file
     * @throws ParseException if file cannot be parsed with this parser or
     * error occured while parsing
     */
    public final TestStatistics parse(Path filePath) throws ParseException {
        if(!isPathAcceptable(filePath)) {
            throw new ParseException("File cannot be parsed with this matcher.", 0);
        }

        return doParse(filePath);
    }

    /**
     * Finds out if file can be parsed with this parser.
     * 
     * @param filePath path to file
     * @return {@code true} if file can be parsed, {@code false} otherwise
     */
    public final boolean isPathAcceptable(Path filePath) {
        final String pattern = String.format("glob:%s", getPattern());

        return FileSystems.getDefault()
            .getPathMatcher(pattern)
            .matches(filePath);
    }
    /**
     * Parses statistics from given file.
     * 
     * @param filePath path to file
     * @return parsed statistics
     * @throws ParseException if error occured while parsing
     */
    protected abstract TestStatistics doParse(Path filePath) throws ParseException;

    /**
     * Note: pattern must be of {@code glob} syntax
     * (see {@link java.nio.file.FileSystem}'s getPathMatcher()).
     * 
     * @return pattern of acceptable path
     */
    protected abstract String getPattern();
}
