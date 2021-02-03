package backtraceio.library.breadcrumbs;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import backtraceio.library.models.types.BacktraceBreadcrumbLevel;
import backtraceio.library.models.types.BacktraceBreadcrumbType;

public class BacktraceBreadcrumbs {

    /**
     * Which breadcrumb types are enabled?
     */
    private Set<BacktraceBreadcrumbType> enabledBreadcrumbTypes;

    /**
     * The Backtrace BroadcastReciever instance
     */
    private BacktraceBroadcastReceiver backtraceBroadcastReceiver;

    /**
     * The Backtrace ComponentCallbacks2 listener
     */
    private BacktraceComponentListener backtraceComponentListener;

    /**
     * The base directory of the breadcrumb logs
     */
    private String breadcrumbLogDirectory;

    public BacktraceBreadcrumbs(Context context) {
        // Create the breadcrumbs subdirectory for storing the breadcrumb logs
        breadcrumbLogDirectory = context.getFilesDir().getAbsolutePath() + "/breadcrumbs";
        File breadcrumbLogsDir = new File(breadcrumbLogDirectory);
        breadcrumbLogsDir.mkdir();
    }

    private void enableSystemBreadcrumbs(Context context) {
        backtraceBroadcastReceiver = new BacktraceBroadcastReceiver(this);
        context.registerReceiver(backtraceBroadcastReceiver, backtraceBroadcastReceiver.getMyIntentFilter());

        backtraceComponentListener = new BacktraceComponentListener(this);
        context.registerComponentCallbacks(backtraceComponentListener);
    }

    private void enableBreadcrumbType(Context context, BacktraceBreadcrumbType type) {
        switch (type)
        {
            case SYSTEM:
                enableSystemBreadcrumbs(context);
                break;
        }
    }

    public void setEnabledBreadcrumbTypes(Context context, Set<BacktraceBreadcrumbType> breadcrumbTypesToEnable) {
        if (breadcrumbTypesToEnable == null)
            return;

        // Register the new breadcrumb types
        for (BacktraceBreadcrumbType enabledType : breadcrumbTypesToEnable) {
            if (this.enabledBreadcrumbTypes == null ||
                    !this.enabledBreadcrumbTypes.contains(enabledType)) {
                enableBreadcrumbType(context, enabledType);
            }
        }

        this.enabledBreadcrumbTypes = breadcrumbTypesToEnable;
    }

    public void enableBreadcrumbs(Context context, Set<BacktraceBreadcrumbType> enabledBreadcrumbTypes)
    {
        initializeBreadcrumbs(breadcrumbLogDirectory);
        setEnabledBreadcrumbTypes(context, enabledBreadcrumbTypes);

        // We should log all breadcrumb configuration changes in the breadcrumbs
        addConfigurationBreadcrumb();
    }

    public void enableBreadcrumbs(Context context) {
        final Set<BacktraceBreadcrumbType> enabledBreadcrumbTypes = new HashSet<BacktraceBreadcrumbType>(){{
            add(BacktraceBreadcrumbType.CONFIGURATION);
            add(BacktraceBreadcrumbType.HTTP);
            add(BacktraceBreadcrumbType.LOG);
            add(BacktraceBreadcrumbType.MANUAL);
            add(BacktraceBreadcrumbType.NAVIGATION);
            add(BacktraceBreadcrumbType.SYSTEM);
            add(BacktraceBreadcrumbType.USER);
        }};
        enableBreadcrumbs(context, enabledBreadcrumbTypes);
    }

    private void disableSystemBreadcrumbs(Context context) {
        context.unregisterReceiver(backtraceBroadcastReceiver);
        backtraceBroadcastReceiver = null;

        context.unregisterComponentCallbacks(backtraceComponentListener);
        backtraceComponentListener = null;
    }

    private void disableBreadcrumbType(Context context, BacktraceBreadcrumbType type) {
        switch (type)
        {
            case SYSTEM:
                disableSystemBreadcrumbs(context);
                break;
        }
    }

    public void disableBreadcrumbs(Context context) {
        final Set<BacktraceBreadcrumbType> breadcrumbTypesToDisable = new HashSet<BacktraceBreadcrumbType>(){{
            add(BacktraceBreadcrumbType.CONFIGURATION);
            add(BacktraceBreadcrumbType.HTTP);
            add(BacktraceBreadcrumbType.LOG);
            add(BacktraceBreadcrumbType.MANUAL);
            add(BacktraceBreadcrumbType.NAVIGATION);
            add(BacktraceBreadcrumbType.SYSTEM);
            add(BacktraceBreadcrumbType.USER);
        }};

        for (BacktraceBreadcrumbType breadcrumbType : breadcrumbTypesToDisable)
        {
            disableBreadcrumbType(context, breadcrumbType);
        }

        enabledBreadcrumbTypes = null;

        // We should log all breadcrumb configuration changes in the breadcrumbs
        addConfigurationBreadcrumb();
    }

    public void clearBreadcrumbs() {
        // TODO: Not implemented
    }

    public boolean isBreadcrumbsEnabled()
    {
        if (enabledBreadcrumbTypes == null || enabledBreadcrumbTypes.isEmpty())
            return false;
        return true;
    }

    public String getBreadcrumbLogDirectory() {
        return this.breadcrumbLogDirectory;
    }

    /**
     * Add a breadcrumb of type "Manual" and level "Info" with the provided message string
     * @param message
     */
    public void addBreadcrumb(String message) {
        addBreadcrumb(message, null, BacktraceBreadcrumbType.MANUAL, BacktraceBreadcrumbLevel.INFO);
    }

    /**
     * Add a breadcrumb of type "Manual" and the desired level with the provided message string
     * @param message
     * @param level
     */
    public void addBreadcrumb(String message, BacktraceBreadcrumbLevel level) {
        addBreadcrumb(message, null, BacktraceBreadcrumbType.MANUAL, level);
    }

    /**
     * Add a breadcrumb of type "Manual" and level "Info" with the provided message string and attributes
     * @param message
     * @param attributes
     */
    public void addBreadcrumb(String message, Map<String, Object> attributes) {
        addBreadcrumb(message, attributes, BacktraceBreadcrumbType.MANUAL, BacktraceBreadcrumbLevel.INFO);
    }

    /**
     * Add a breadcrumb of type "Manual" and the desired level with the provided message string and attributes
     * @param message
     * @param attributes
     * @param level
     */
    public void addBreadcrumb(String message, Map<String, Object> attributes, BacktraceBreadcrumbLevel level) {
        addBreadcrumb(message, attributes, BacktraceBreadcrumbType.MANUAL, level);
    }

    /**
     * Add a breadcrumb of the desired type and level "Info" with the provided message string
     * @param message
     * @param type
     */
    public void addBreadcrumb(String message, BacktraceBreadcrumbType type) {
        addBreadcrumb(message, null, type, BacktraceBreadcrumbLevel.INFO);
    }

    /**
     * Add a breadcrumb of the desired level and type with the provided message string
     * @param message
     * @param type
     * @param level
     */
    public void addBreadcrumb(String message, BacktraceBreadcrumbType type, BacktraceBreadcrumbLevel level) {
        addBreadcrumb(message, null, type, level);
    }

    /**
     * Add a breadcrumb of the desired type and level "Info" with the provided message string and attributes
     * @param message
     * @param attributes
     * @param type
     */
    public void addBreadcrumb(String message, Map<String, Object> attributes, BacktraceBreadcrumbType type) {
        addBreadcrumb(message, attributes, type, BacktraceBreadcrumbLevel.INFO);
    }

    /**
     * Add a breadcrumb of the desired level and type with the provided message string and attributes
     * @param message
     * @param attributes
     * @param type
     * @param level
     */
    public void addBreadcrumb(String message, Map<String, Object> attributes, BacktraceBreadcrumbType type, BacktraceBreadcrumbLevel level) {
        // We use currentTimeMillis in the BacktraceReport too, so for consistency
        // we will use it here.
        long time = System.currentTimeMillis();

        String serializedAttributes = "";
        if (attributes != null) {
            for (Map.Entry<String, Object> entry : attributes.entrySet())
            {
                serializedAttributes += " attr " + entry.getKey() + " " + entry.getValue() + " ";
            }
        }

        addBreadcrumb(time,
                type.ordinal(),
                level.ordinal(),
                message,
                serializedAttributes);
    }

    // Create a breadcrumb which reflects the current breadcrumb configuration
    private void addConfigurationBreadcrumb()
    {
        Map<String, Object> attributes = new HashMap<String, Object>();

        for (BacktraceBreadcrumbType enabledType : BacktraceBreadcrumbType.values())
        {
            if (enabledBreadcrumbTypes != null &&
                    enabledBreadcrumbTypes.contains(enabledType))
            {
                attributes.put(enabledType.name(), "enabled");
            }
            else
            {
                attributes.put(enabledType.name(), "disabled");
            }
        }

        addBreadcrumb("Breadcrumbs configuration", attributes, BacktraceBreadcrumbType.CONFIGURATION);
    }

    /**
     * Flushes the breadcrumb log file and returns the current breadcrumb count
     * This is wrapped into one call to prevent multiple round-trips through the JNI
     */
    public native int prepareToSendBreadcrumbsLog();

    private native void initializeBreadcrumbs(String directory);
    private native void addBreadcrumb(long timestamp, int type, int level, String message, String serializedAttributes);
}
