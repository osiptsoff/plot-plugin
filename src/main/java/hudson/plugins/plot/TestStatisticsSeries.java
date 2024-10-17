package hudson.plugins.plot;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.FilePath;
import hudson.plugins.plot.statistics.TestStatistics;
import hudson.plugins.plot.statistics.TestStatisticsAccumulator;
import hudson.plugins.plot.statistics.parser.SurefireTxtReportParser;
import hudson.plugins.plot.statistics.parser.chain.TestStatisticsParserChain;

/**
 * Used for loading and combining all test statistics
 * across workspace.
 *
 * @author Nikita Osiptsov
 */
public class TestStatisticsSeries extends Series {
    private static final Logger LOGGER = LogManager.getLogger(TestStatisticsSeries.class);
    private static final String URL = "test-statistics";

    private final TestStatisticsParserChain parserChain = new TestStatisticsParserChain()
        .add(new SurefireTxtReportParser());

    @DataBoundConstructor
    public TestStatisticsSeries(String... filenamePatterns) {
        super("Build's test statistics", filenamePatterns);
    }

    @Override
    public List<PlotPoint> loadSeries(FilePath workspaceRootDir,
            int buildNumber, PrintStream logger) {
        final FileFinder fileFinder = new FileFinder(workspaceRootDir);

        final Path[] matchingPaths = fileFinder.findFiles(filenamePatterns);

        if (matchingPaths.length == 0) {
            logNoFilesFound(fileFinder.getBaseDir());

            return Collections.emptyList();
        }

        final List<PlotPoint> points =  Arrays.asList(matchingPaths).stream()
            .map(this::parseAndLogFail)
            .filter(Objects::nonNull)
            .collect(
                TestStatisticsAccumulator::new,
                TestStatisticsAccumulator::add,
                (firstAcc, secondAcc) -> firstAcc.add(secondAcc.getResult())
            ).getResult().toPlotPoints();

        // the only purpose here is to assing url
        for (int i = 0; i < points.size(); i++) {
            final PlotPoint plotPoint = points.get(i);

            plotPoint.setUrl(getUrl(URL, plotPoint.getLabel(), i, buildNumber));
        }

        return points;
    }

    private TestStatistics parseAndLogFail(Path path) {
        final TestStatistics statistics = parserChain.parse(path);

        if (statistics == null) {
            LOGGER.warn(String.format("Failed to parse file '%s'", path.toString()));
        }

        return statistics;
    }

    private void logNoFilesFound(Path searchBaseDir) {
        final String errorMessage =
            "For patterns [%s] no matching files were found in fs tree with root '%s'.";

        final StringJoiner joiner = new StringJoiner(", ");
        for (final String pattern: filenamePatterns) {
            joiner.add(String.format("'%s'", pattern));
        }

        LOGGER.warn(String.format(errorMessage, joiner.toString(), searchBaseDir.toString()));
    }
}
