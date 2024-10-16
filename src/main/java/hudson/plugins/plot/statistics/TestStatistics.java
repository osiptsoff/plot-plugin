package hudson.plugins.plot.statistics;

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
}
