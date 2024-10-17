package hudson.plugins.plot.statistics;

/**
 * Gathers {@link TestStatistics} to resulting instance.
 *
 * @author Nikita Osiptsov
 */
public class TestStatisticsAccumulator {
    private Integer passed = 0;
    private Integer skipped = 0;
    private Integer errors = 0;
    private Integer failed = 0;

    /**
     * Adds the fields of the accepted instance to the total sum of each field.
     *
     * @param testStatistics added instance
     * @return this instance for chaining
     */
    public TestStatisticsAccumulator add(TestStatistics testStatistics) {
        this.passed += testStatistics.getPassed();
        this.skipped += testStatistics.getSkipped();
        this.errors += testStatistics.getErrors();
        this.failed += testStatistics.getFailed();

        return this;
    }

    /**
     * @return resulting test statistics
     */
    public TestStatistics getResult() {
        return new TestStatistics(passed, skipped, errors, failed);
    }
}
