package hudson.plugins.plot.statistics.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import hudson.FilePath;
import hudson.plugins.plot.statistics.TestStatistics;

/**
 * Parses test statistics from maven or gradle xml report. Although
 * reports may differ, they have common thing that this parser relies on:
 * test statistics is written as first tag properties.
 *
 * @author Nikita Osiptsov
 */
public class XmlTestReportParser extends AbstractTestStatisticsParser {
    private static final String PASSED_MARKER = "tests";
    private static final String ERRORS_MARKER = "errors";
    private static final String FAILURES_MARKER = "failures";
    private static final String SKIPPED_MARKER = "skipped";

    /**
     * Creates new instance with system default charset.
     */
    public XmlTestReportParser() {
        super();
    }

    /**
     * Creates new instance with given charset.
     *
     * @param charset charset to be used while parsing;
     * may be {@code null}, in this case server default
     * charset will be used
     */
    public XmlTestReportParser(Charset charset) {
        super(charset);
    }

    @Override
    protected TestStatistics doParse(FilePath filePath) throws ParseException {
        final String charsetName = getCharset().name();

        try (final Reader reader = new InputStreamReader(filePath.read(), charsetName);
            final BufferedReader bufferedReader = new BufferedReader(reader)) {

            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader xmlReader = factory.createXMLStreamReader(bufferedReader);

            if (!xmlReader.hasNext()) {
                throw new ParseException("Empty XML file.", 0);
            }

            int event;
            do {
                event = xmlReader.next();
            } while (event != XMLStreamReader.START_ELEMENT);

            final TestStatistics statistics = parseFromTag(xmlReader);

            if (statistics == null) {
                throw new ParseException("Outer tag has no valid statistics attributes.", 0);
            }

            return statistics;
        } catch (IOException ioe) {
            final ParseException pe = new ParseException("Failed to open a file.", 0);
            pe.initCause(ioe);

            throw pe;
        } catch (InterruptedException ie) {
            final ParseException pe = new ParseException("Interrupted reading a file.", 0);
            pe.initCause(ie);
            Thread.currentThread().interrupt();

            throw pe;
        } catch (XMLStreamException xsre) {
            final ParseException pe = new ParseException("Failed to process XML file.", 0);
            pe.initCause(xsre);

            throw pe;
        }
    }

    @Override
    protected final String getPattern() {
        return "/**/*report*/**/*.xml";
    }

    private TestStatistics parseFromTag(XMLStreamReader reader) {
        final Map<String, Integer> statisticsMap = new HashMap<>();

        Integer substractFromPassed = 0;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Integer value = parseIfNeeded(reader, i);
            final String attributeName = reader.getAttributeLocalName(i);

            if (value == -1) {
                continue;
            }
            if (!PASSED_MARKER.equals(attributeName)) {
                substractFromPassed += value;
            }

            statisticsMap.put(attributeName, value);
        }

        // number of TestStatistics fields
        if (statisticsMap.size() != 4) {
            return null;
        }

        return new TestStatistics(statisticsMap.get(PASSED_MARKER) - substractFromPassed,
            statisticsMap.get(SKIPPED_MARKER),
            statisticsMap.get(ERRORS_MARKER),
            statisticsMap.get(FAILURES_MARKER)
        );
    }

    private int parseIfNeeded(XMLStreamReader reader, int attributeIndex) {
        final String attributeName = reader.getAttributeLocalName(attributeIndex);
        final String attributeValue = reader.getAttributeValue(attributeIndex);

        if (!isAttributeNeeded(attributeName)) {
            return -1;
        }

        try {
            return Integer.parseInt(attributeValue);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private boolean isAttributeNeeded(String attributeName) {
        List<String> neededAttributes = Arrays.asList(PASSED_MARKER,
                ERRORS_MARKER, FAILURES_MARKER, SKIPPED_MARKER);

        return neededAttributes.contains(attributeName);
    }
}
