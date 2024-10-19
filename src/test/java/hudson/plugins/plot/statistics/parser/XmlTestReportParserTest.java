package hudson.plugins.plot.statistics.parser;

import java.io.File;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import hudson.FilePath;
import hudson.plugins.plot.statistics.TestStatistics;

/**
 * Test xml test statistics parser.
 *
 * @author Nikita Osiptsov
 */
public class XmlTestReportParserTest {
    private static final String[] FILES = {
        "reports/Correct.xml",
        "reports/NoParameters.xml",
        "reports/Wrong.txt",
    };

    private final XmlTestReportParser parser = new XmlTestReportParser();

    @Test
    public void parseCorrectTest() throws ParseException {
        final FilePath path = getPath(FILES[0]);

        final TestStatistics actualStatistics = parser.doParse(path);

        Assert.assertEquals(31, actualStatistics.getPassed().intValue());
        Assert.assertEquals(15, actualStatistics.getSkipped().intValue());
        Assert.assertEquals(3, actualStatistics.getFailed().intValue());
        Assert.assertEquals(12, actualStatistics.getErrors().intValue());
    }

    @Test
    public void parseNoParametersTest() {
        final FilePath path = getPath(FILES[1]);

        Assert.assertThrows(ParseException.class, () -> parser.doParse(path));
    }

    @Test
    public void parseWrongTest() {
        final FilePath path = getPath(FILES[2]);

        Assert.assertThrows(ParseException.class, () -> parser.doParse(path));
    }

    private FilePath getPath(String name) {
        final File file = new File(String.format("target/test-classes/%s", name));

        return new FilePath(file);
    }
}
