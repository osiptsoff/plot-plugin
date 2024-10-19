package hudson.plugins.plot;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.plugins.plot.statistics.TestStatistics;
import hudson.plugins.plot.statistics.TestStatisticsAccumulator;
import hudson.plugins.plot.statistics.parser.SurefireTxtReportParser;
import hudson.plugins.plot.statistics.parser.XmlTestReportParser;
import hudson.plugins.plot.statistics.parser.chain.TestStatisticsParserChain;
import net.sf.json.JSONObject;

/**
 * Used for loading and combining all test statistics
 * across workspace.
 *
 * @author Nikita Osiptsov
 */
public class TestStatisticsSeries extends Series {
    private static final Logger LOGGER = LogManager.getLogger(TestStatisticsSeries.class);

    private TestStatisticsParserChain parserChain;

    @DataBoundConstructor
    public TestStatisticsSeries(String... filenamePatterns) {
        super("Build's test statistics", filenamePatterns);
    }

    @Override
    public List<PlotPoint> loadSeries(FilePath workspaceRootDir,
            int buildNumber, PrintStream logger) {
        final FileFinder fileFinder = new FileFinder(workspaceRootDir);
        final FilePath[] matchingPaths = fileFinder.findFiles(filenamePatterns);

        final Computer computer = workspaceRootDir.toComputer();
        parserChain = getChain(computer == null ? null : computer.getDefaultCharset());

        if (matchingPaths.length == 0) {
            logNoFilesFound(fileFinder.getBaseDir());
            return Collections.emptyList();
        }

        return Arrays.asList(matchingPaths).stream()
            .map(this::parseAndLogFail)
            .filter(Objects::nonNull)
            .collect(
                TestStatisticsAccumulator::new,
                TestStatisticsAccumulator::add,
                (firstAcc, secondAcc) -> firstAcc.add(secondAcc.getResult())
            ).getResult().toPlotPoints();
    }

    private TestStatistics parseAndLogFail(FilePath path) {
        final TestStatistics statistics = parserChain.parse(path);

        if (statistics == null) {
            LOGGER.warn(String.format("Failed to parse file '%s'", path.getRemote()));
        }

        return statistics;
    }

    private void logNoFilesFound(FilePath searchBaseDir) {
        final String errorMessage =
            "For patterns [%s] no matching files were found in fs tree with root '%s'.";

        final StringJoiner joiner = new StringJoiner(", ");
        for (final String pattern: filenamePatterns) {
            joiner.add(String.format("'%s'", pattern));
        }

        LOGGER.warn(String.format(errorMessage, joiner.toString(), searchBaseDir.getRemote()));
    }

    private TestStatisticsParserChain getChain(Charset charset) {
        return new TestStatisticsParserChain()
            .add(new XmlTestReportParser(charset))
            .add(new SurefireTxtReportParser(charset));
    }

    @Override
    public Descriptor<Series> getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Series> {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Plot_TestStatisticsSeries();
        }

        @Override
        public Series newInstance(StaplerRequest req, @NonNull JSONObject formData)
                throws FormException {
            return SeriesTransformUtil.createSeries(formData, req);
        }
    }
}
