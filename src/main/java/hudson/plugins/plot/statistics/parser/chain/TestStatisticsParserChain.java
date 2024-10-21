package hudson.plugins.plot.statistics.parser.chain;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import hudson.FilePath;
import hudson.plugins.plot.statistics.TestStatistics;
import hudson.plugins.plot.statistics.parser.AbstractTestStatisticsParser;

/**
 * Chain of responsibility for parsers.
 *
 * @author Nikita Osiptsov
 */
public final class TestStatisticsParserChain {
    private final List<AbstractTestStatisticsParser> parsers = new LinkedList<>();

    /**
     * Adds new parser to the end of chain.
     *
     * @param parser new parser, must not be {@code null}
     * @return this instance for chaining
     */
    public TestStatisticsParserChain add(AbstractTestStatisticsParser parser) {
        parsers.add(Objects.requireNonNull(parser));

        return this;
    }

    /**
     * Makes first of chain's parsers which can process given file
     * parse test statistics from it.
     *
     * @param path path to file
     * @return {@link TestStatistics} if chain was able to process given file,
     * {@code null} otherwise
     */
    public TestStatistics parse(FilePath path) {
        if (parsers.isEmpty()) {
            throw new IllegalStateException("Using unconfigured parser chain");
        }

        for (final AbstractTestStatisticsParser parser: parsers) {
            try {
                return parser.parse(path);
            } catch (ParseException pe) {
                // continue
            }
        }

        return null;
    }
}
