package hudson.plugins.plot.statistics.parser;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import hudson.FilePath;
import hudson.plugins.plot.statistics.TestStatistics;

/**
 * Parses {@link TestStatistics} from given file.
 *
 * @author Nikita Osiptsov
 */
public abstract class AbstractTestStatisticsParser {
    private Charset charset = Charset.defaultCharset();

    protected AbstractTestStatisticsParser() {
    }

    protected AbstractTestStatisticsParser(Charset charset) {
        this.charset = charset;
    }

    /**
     * Parses test statistics from given file.
     *
     * @param filePath path to file containing test statistics
     * @return statistics from given file
     * @throws ParseException if file cannot be parsed with this parser or
     * error occured while parsing
     */
    public final TestStatistics parse(FilePath filePath) throws ParseException {
        if (!isPathAcceptable(filePath)) {
            throw new ParseException("File cannot be parsed with this parser.", 0);
        }

        return doParse(filePath);
    }

    /**
     * Finds out if file can be parsed with this parser.
     *
     * @param filePath path to file
     * @return {@code true} if file can be parsed, {@code false} otherwise
     */
    public final boolean isPathAcceptable(FilePath filePath) {
        final String pattern = String.format("glob:%s", getPattern());
        final Path remotePath = Paths.get(filePath.getRemote());

        return FileSystems.getDefault()
            .getPathMatcher(pattern)
            .matches(remotePath);
    }

    /**
     * Specifies charset used for decoding files.
     * Defaults to server's default charset.
     * If given charset is {@code null}, no changes applied.
     *
     * @param charset new charset
     */
    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    /**
     * @return parser's charset
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Parses statistics from given file.
     *
     * @param filePath path to file
     * @return parsed statistics
     * @throws ParseException if error occured while parsing
     */
    protected abstract TestStatistics doParse(FilePath filePath) throws ParseException;

    /**
     * Note: pattern must be of {@code glob} syntax
     * (see {@link java.nio.file.FileSystem}'s getPathMatcher()).
     *
     * @return pattern of acceptable path
     */
    protected abstract String getPattern();
}
