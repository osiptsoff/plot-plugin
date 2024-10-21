package hudson.plugins.plot.statistics.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
    private static final String PASSED_MARKER = "[Tt]ests run";
    private static final String ERRORS_MARKER = "[Ee]rrors";
    private static final String FAILURES_MARKER = "[Ff]ailures";
    private static final String SKIPPED_MARKER = "[Ss]kipped";

    /**
     * Creates new instance with system default charset.
     */
    public SurefireTxtReportParser() {
        super();
    }

    /**
     * Creates new instance with given charset.
     *
     * @param charset charset to be used while parsing;
     * may be {@code null}, in this case server default
     * charset will be used
     */
    public SurefireTxtReportParser(Charset charset) {
        super(charset);
    }

    @Override
    protected TestStatistics doParse(FilePath filePath) throws ParseException {
        final String charsetName = getCharset().name();

        try (final Reader reader = new InputStreamReader(filePath.read(), charsetName);
                final BufferedReader bufferedReader = new BufferedReader(reader)) {

            final Stream<String> lines = bufferedReader.lines();
            final Optional<TestStatistics> statistics = lines
                .map(this::parseFromLine)
                .filter(Objects::nonNull)
                .findFirst();

            if (!statistics.isPresent()) {
                throw new ParseException("File contains no test statistics.", 0);
            }

            return statistics.get();
        } catch (IOException ioe) {
            final ParseException pe = new ParseException("Failed to open a file.", 0);
            pe.initCause(ioe);

            throw pe;
        } catch (InterruptedException ie) {
            final ParseException pe = new ParseException("Interrupted reading a file.", 0);
            pe.initCause(ie);
            Thread.currentThread().interrupt();

            throw pe;
        }
    }

    @Override
    protected final String getPattern() {
        return "/**/surefire-reports/*.txt";
    }

    private TestStatistics parseFromLine(String line) {
        final String patternString = "%s:\\s*\\d+";
        final Map<String, Integer> statisticsMap = new HashMap<>();

        Integer substractFromPassed = 0;

        for (String marker: Arrays.asList(PASSED_MARKER, ERRORS_MARKER,
                FAILURES_MARKER, SKIPPED_MARKER)) {
            final String markerPatternString = String.format(patternString, marker);
            final Matcher matcher = Pattern.compile(markerPatternString).matcher(line);

            if (!matcher.find()) {
                return null;
            }

            final String parsableNumber = matcher.group().split(":\\s*")[1];

            try {
                final Integer value = Integer.parseInt(parsableNumber);
                if (!PASSED_MARKER.equals(marker)) {
                    substractFromPassed += value;
                }

                statisticsMap.put(marker, value);
            } catch (NumberFormatException mfe) {
                return null;
            }
        }

        return new TestStatistics(statisticsMap.get(PASSED_MARKER) - substractFromPassed,
            statisticsMap.get(SKIPPED_MARKER),
            statisticsMap.get(ERRORS_MARKER),
            statisticsMap.get(FAILURES_MARKER)
        );
    }
}
