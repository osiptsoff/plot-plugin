/*
 * Copyright (c) 2008-2009 Yahoo! Inc.  All rights reserved.
 * The copyrights to the contents of this file are licensed under the MIT License
 * (http://www.opensource.org/licenses/mit-license.php)
 */

package hudson.plugins.plot;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * This class creates a Series class based on the data source
 *
 * @author areese, Alan.Harder@sun.com
 */
public class SeriesFactory {

    private SeriesFactory() {
    }

    /**
     * Using file and label and the Stapler request, create a TestStatisticsSeries
     *
     * @param formData JSON data for series
     */
    public static Series createSeries(JSONObject formData, StaplerRequest req) {
        String file = formData.getString("file");
        formData = formData.getJSONObject("fileType");
        formData.put("file", file);
        String type = formData.getString("value");
        Class<? extends Series> typeClass = null;

        // create series

        return typeClass != null ? req.bindJSON(typeClass, formData) : null;
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
