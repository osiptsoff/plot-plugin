package hudson.plugins.plot.statistics.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import hudson.FilePath;
import hudson.plugins.plot.statistics.TestStatistics;

/**
 * Parses test statistics from maven surefire .txt report, e.g, from file
 * with contents like: <p>
 *
 * --------------------------------------------------------     <p>
 * Test set: foo.bar.bazTest                                    <p>
 * ---------------------------------------------------------    <p>
 * Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.1 s - in foo.bar.bazTest.
 *
 * @author Nikita Osiptsov
 */
public class SurefireTxtReportParser extends AbstractTestStatisticsParser {
    // used while searching for test statistics in file, are part of regex
    private static final String PASSED_MARKER = "tests run";
    private static final String ERRORS_MARKER = "errors";
    private static final String FAILURES_MARKER = "failures";
    private static final String SKIPPED_MARKER = "skipped";

    private Charset charset = Charset.defaultCharset();

    @Override
    protected TestStatistics doParse(FilePath filePath) throws ParseException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(filePath.read(), charset.name()))) {
            final Stream<String> lines = reader.lines();
            final Optional<TestStatistics> statistics = lines
                .map(this::parseFromLine)
                .filter(Objects::nonNull)
                .findFirst();

            if (!statistics.isPresent()) {
                throw new ParseException("File contains no test statistics", 0);
            }

            return statistics.get();
        } catch (IOException ioe) {
            final ParseException pe = new ParseException("Failed to open a file", 0);
            pe.initCause(ioe);

            throw pe;
        } catch (InterruptedException ie) {
            final ParseException pe = new ParseException("Interrupted reading a file", 0);
            pe.initCause(ie);
            Thread.currentThread().interrupt();

            throw pe;
        }
    }

    @Override
    protected String getPattern() {
        return "**/surefire-reports/*.txt";
    }

    /**
     * Specifies charset used for decoding read files.
     *
     * @param charset new charset
     */
    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    private TestStatistics parseFromLine(String line) {
        final String patternString = "%s:\\s*\\d+";
        final Map<String, Integer> statisticsMap = new HashMap<>();
        final String lowercaseLine = line.toLowerCase();

        for (String marker: Arrays.asList(PASSED_MARKER, ERRORS_MARKER,
                FAILURES_MARKER, SKIPPED_MARKER)) {
            final String markerPatternString = String.format(patternString, marker);
            final Matcher matcher = Pattern.compile(markerPatternString).matcher(lowercaseLine);

            if (!matcher.find()) {
                return null;
            }

            final String parsableNumber = matcher.group().split(":\\s*")[1];
            statisticsMap.put(marker, Integer.parseInt(parsableNumber));
        }

        return new TestStatistics(statisticsMap.get(PASSED_MARKER),
            statisticsMap.get(SKIPPED_MARKER),
            statisticsMap.get(ERRORS_MARKER),
            statisticsMap.get(FAILURES_MARKER)
        );
    }
}
