/*
 * Copyright (c) 2007-2009 Yahoo! Inc.  All rights reserved.
 * The copyrights to the contents of this file are licensed under the MIT License
 * (http://www.opensource.org/licenses/mit-license.php)
 */
package hudson.plugins.plot;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.FilePath;

/**
 * Represents a plot data series configuration.
 *
 * @author Nigel Daley
 * @author Allen Reese
 */
public abstract class Series {
    private static final Pattern PAT_NAME = Pattern.compile("%name%");
    private static final Pattern PAT_INDEX = Pattern.compile("%index%");
    private static final Pattern PAT_BUILD_NUMBER = Pattern.compile("%build%");

    /**
     * Data series legend label. Optional.
     */
    @SuppressWarnings("visibilitymodifier")
    protected String label;

    protected String[] filenamePatterns;


    protected Series(String label, String ...filenamePatterns) {
        this.filenamePatterns = filenamePatterns;

        if (label == null) {
            label = Messages.Plot_Missing();
        }

        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Retrieves the plot data for one series after a build from the workspace.
     *
     * @param workspaceRootDir the root directory of the workspace
     * @param buildNumber      the build Number
     * @param logger           the logger to use
     * @return a PlotPoint array of points to plot
     */
    public abstract List<PlotPoint> loadSeries(FilePath workspaceRootDir,
                                               int buildNumber, PrintStream logger);


    /**
     * Return the url that should be used for this point.
     *
     * @param label       Name of the column
     * @param index       Index of the column
     * @param buildNumber The build number
     * @return url for the label.
     */
    protected String getUrl(String baseUrl, String label, int index, int buildNumber) {
        String resultUrl = baseUrl;
        if (resultUrl != null) {
            if (label == null) {
                // This implementation searches for tokens to replace.
                // If the argument was NULL then replacing the null with an empty string
                // should still produce the desired outcome.
                label = "";
            }
            /*
             * Check the name first, and do replacement upon it.
             */
            Matcher nameMatcher = PAT_NAME.matcher(resultUrl);
            if (nameMatcher.find()) {
                resultUrl = nameMatcher.replaceAll(label);
            }

            /*
             * Check the index, and do replacement on it.
             */
            Matcher indexMatcher = PAT_INDEX.matcher(resultUrl);
            if (indexMatcher.find()) {
                resultUrl = indexMatcher.replaceAll(String.valueOf(index));
            }

            /*
             * Check the build number first, and do replacement upon it.
             */
            Matcher buildNumberMatcher = PAT_BUILD_NUMBER.matcher(resultUrl);
            if (buildNumberMatcher.find()) {
                resultUrl = buildNumberMatcher.replaceAll(String
                        .valueOf(buildNumber));
            }
        }

        return resultUrl;
    }
}
