package hudson.plugins.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/**
 * Plot {@link Builder} class for pipeline.
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link PlotBuilder} is created.
 * The created instance is persisted to the project configuration XML by using XStream,
 * so this allows you to use instance fields (like {@link #group}) to remember the configuration.
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 */
public class PlotBuilder extends Builder implements SimpleBuildStep {

    // Required fields
    private final String group;
    private final String style;

    @CheckForNull
    private String description;
    @CheckForNull
    private String numBuilds;
    @CheckForNull
    private String yaxis;
    @CheckForNull
    private String yaxisMinimum;
    @CheckForNull
    private String yaxisMaximum;
    private boolean useDescr;
    private boolean exclZero;
    private boolean logarithmic;
    private boolean keepRecords;
    private String csvFileName;

    private TestStatisticsSeries series;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Similarly, any optional @DataBoundSetter properties must match
    @DataBoundConstructor
    public PlotBuilder(String group, String style) {
        this.group = group;
        this.style = style;
    }

    public String getGroup() {
        return group;
    }

    public String getStyle() {
        return style;
    }

    @CheckForNull
    public String getNumBuilds() {
        return numBuilds;
    }

    @DataBoundSetter
    public final void setNumBuilds(@CheckForNull String numBuilds) {
        this.numBuilds = Util.fixEmptyAndTrim(numBuilds);
    }

    @CheckForNull
    public String getYaxis() {
        return yaxis;
    }

    @DataBoundSetter
    public final void setYaxis(@CheckForNull String yaxis) {
        this.yaxis = Util.fixEmptyAndTrim(yaxis);
    }

    public boolean getUseDescr() {
        return useDescr;
    }

    public String getCsvFileName() {
        return this.csvFileName;
    }

    @DataBoundSetter
    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }

    @DataBoundSetter
    public void setUseDescr(boolean useDescr) {
        this.useDescr = useDescr;
    }

    public boolean getExclZero() {
        return exclZero;
    }

    @DataBoundSetter
    public void setExclZero(boolean exclZero) {
        this.exclZero = exclZero;
    }

    public boolean getLogarithmic() {
        return logarithmic;
    }

    @DataBoundSetter
    public void setLogarithmic(boolean logarithmic) {
        this.logarithmic = logarithmic;
    }

    public boolean getKeepRecords() {
        return keepRecords;
    }

    @DataBoundSetter
    public void setKeepRecords(boolean keepRecords) {
        this.keepRecords = keepRecords;
    }

    @CheckForNull
    public String getYaxisMinimum() {
        return yaxisMinimum;
    }

    @DataBoundSetter
    public final void setYaxisMinimum(@CheckForNull String yaxisMinimum) {
        this.yaxisMinimum = Util.fixEmptyAndTrim(yaxisMinimum);
    }

    @CheckForNull
    public String getYaxisMaximum() {
        return yaxisMaximum;
    }

    @DataBoundSetter
    public final void setYaxisMaximum(@CheckForNull String yaxisMaximum) {
        this.yaxisMaximum = Util.fixEmptyAndTrim(yaxisMaximum);
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public final void setDescription(@CheckForNull String description) {
        this.description = Util.fixEmptyAndTrim(description);
    }

    public TestStatisticsSeries getSeries() {
        return this.series;
    }

    @DataBoundSetter
    public void setSeries(TestStatisticsSeries series) {
        this.series = Objects.requireNonNull(series);
    }

    @Override
    public void perform(@NonNull Run<?, ?> build, @NonNull FilePath workspace,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) {
        List<Plot> plots = new ArrayList<>();

        String title = Jenkins.get().getDisplayName();
        Plot plot = new Plot(title, yaxis, group, numBuilds, csvFileName, style,
                useDescr, keepRecords, exclZero, logarithmic,
                yaxisMinimum, yaxisMaximum, description);

        plot.series = Collections.singletonList(series);

        plot.addBuild(build, listener.getLogger(), workspace);
        plots.add(plot);
        PlotBuildAction buildAction = build.getAction(PlotBuildAction.class);
        if (buildAction == null) {
            build.addAction(new PlotBuildAction(build, plots));
        } else {
            buildAction.addPlots(plots);
        }
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor, you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link PlotBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    @Symbol("plot")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /*
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public String getCsvFileName() {
            return "plot-" + UUID.randomUUID().toString() + ".csv";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error("Please set a group");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the group too short?");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable group is used in the configuration screen.
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Plot_Publisher_DisplayName();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
    }
}
