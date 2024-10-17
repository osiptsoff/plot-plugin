package hudson.plugins.plot.statistics;

import java.util.ArrayList;
import java.util.List;

import hudson.plugins.plot.PlotPoint;

/**
 * Represents the general statistics of tests.
 * 
 * @author Nikita Osiptsov
 */
public class TestStatistics {
    private final Integer passed;
    private final Integer skipped;
    private final Integer errors;
    private final Integer failed;

    /**
     * Creates new test statistics with given parameters
     */
    public TestStatistics(Integer passed, Integer skipped,
            Integer errors, Integer failed) {
        this.passed = passed;
        this.skipped = skipped;
        this.errors = errors;
        this.failed = failed;
    }

    /**
     * @return passed tests count
     */
    public Integer getPassed() {
        return this.passed;
    }

    /**
     * @return skipped tests count
     */
    public Integer getSkipped() {
        return this.skipped;
    }

    /**
     * @return number of tests expirienced error
     */
    public Integer getErrors() {
        return this.errors;
    }

    /**
     * @return failed tests count
     */
    public Integer getFailed() {
        return this.failed;
    }
    
    /**
     * Converts test statistics to list of plot points, one
     * point per test state.
     * 
     * @return list of four (passed, skipped, errors, failed) plot points
     */
    public List<PlotPoint> toPlotPoints() {
        final ArrayList<PlotPoint> result = new ArrayList<>(4);

        result.add(new PlotPoint(passed.toString(), null, "passed"));
        result.add(new PlotPoint(skipped.toString(), null, "skipped"));
        result.add(new PlotPoint(errors.toString(), null, "errors"));
        result.add(new PlotPoint(failed.toString(), null, "failed"));

        return result;
    }
}
