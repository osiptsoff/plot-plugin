package hudson.plugins.plot;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * This class creates a Series class from request
 *
 * @author areese, Alan.Harder@sun.com
 */
public class SeriesTransformUtil {
    private SeriesTransformUtil() {
    }

    /**
     * Using file and label and the Stapler request, create a subclass of series
     * that can process the type selected.
     *
     * @param formData JSON data for series
     */
    public static Series createSeries(JSONObject formData, StaplerRequest req) {
        final String[] filenamePatterns = (String[])formData
            .getJSONArray("filenamePatterns")
            .toArray(new String[0]);

        return new TestStatisticsSeries(filenamePatterns);
    }

    public static List<Series> createSeriesList(Object data, StaplerRequest req) {
        JSONArray list = getArray(data);
        List<Series> result = new ArrayList<>();
        for (Object series : list) {
            result.add(createSeries((JSONObject) series, req));
        }
        return result;
    }

    /**
     * Get data as JSONArray (wrap single JSONObject in array if needed).
     */
    public static JSONArray getArray(Object data) {
        JSONArray result;
        if (data instanceof JSONArray) {
            result = (JSONArray) data;
        } else {
            result = new JSONArray();
            if (data != null) {
                result.add(data);
            }
        }
        return result;
    }
}
