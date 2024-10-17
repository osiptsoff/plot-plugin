package hudson.plugins.plot.statistics.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    @Override
    protected TestStatistics doParse(Path filePath) throws ParseException {
        try (final Stream<String> lines = Files.lines(filePath)) {
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
        }
    }

    @Override
    protected String getPattern() {
        return "**/surefire-reports/*.txt";
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

            final String parsableNumber = matcher.group(1).split(":\\s*")[1];
            statisticsMap.put(marker, Integer.parseInt(parsableNumber));
        }

        return new TestStatistics(statisticsMap.get(PASSED_MARKER),
            statisticsMap.get(SKIPPED_MARKER),
            statisticsMap.get(ERRORS_MARKER),
            statisticsMap.get(FAILURES_MARKER)
        );
    }
}
