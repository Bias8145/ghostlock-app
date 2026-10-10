package com.ghostlock.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsManager {
    private static final String PREFS_NAME = "ghostlock_analytics";
    private static final String KEY_RUN_HISTORY = "run_history";
    private static final String KEY_TOTAL_RUNS = "total_runs";
    private static final String KEY_EXIT_ZERO_COUNT = "success_count"; // Legacy preference key retained for existing installs.
    private static final String KEY_NON_ZERO_COUNT = "failure_count"; // Legacy preference key retained for existing installs.
    private static final int MAX_HISTORY = 100;

    private final SharedPreferences prefs;

    public AnalyticsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class RunRecord {
        public long timestamp;
        public boolean processExitZero;
        public String kernel;
        public String cpuPair;
        public String errorMessage;

        public RunRecord(boolean processExitZero, String kernel, String cpuPair, String errorMessage) {
            this.timestamp = System.currentTimeMillis();
            this.processExitZero = processExitZero;
            this.kernel = kernel;
            this.cpuPair = cpuPair;
            this.errorMessage = errorMessage;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("timestamp", timestamp);
            obj.put("processExitZero", processExitZero);
            obj.put("kernel", kernel);
            obj.put("cpuPair", cpuPair);
            if (errorMessage != null) {
                obj.put("error", errorMessage);
            }
            return obj;
        }

        public static RunRecord fromJSON(JSONObject obj) throws JSONException {
            RunRecord record = new RunRecord(
                obj.has("processExitZero") ? obj.getBoolean("processExitZero") : obj.optBoolean("success", false),
                obj.optString("kernel", ""),
                obj.optString("cpuPair", ""),
                obj.optString("error", null)
            );
            record.timestamp = obj.getLong("timestamp");
            return record;
        }
    }

    /** Records process completion only; exit code zero is not proof of root access. */
    public void recordProcessOutcome(ProcessOutcome outcome, String kernel, String cpuPair, String errorMessage) {
        boolean exitZero = outcome != null && outcome.kind() == ProcessOutcome.Kind.EXIT_ZERO_UNVERIFIED;
        try {
            // Update counters
            int totalRuns = prefs.getInt(KEY_TOTAL_RUNS, 0) + 1;
            int exitZeroCount = prefs.getInt(KEY_EXIT_ZERO_COUNT, 0) + (exitZero ? 1 : 0);
            int nonZeroCount = prefs.getInt(KEY_NON_ZERO_COUNT, 0) + (exitZero ? 0 : 1);

            // Add to history
            List<RunRecord> history = getRunHistory();
            history.add(0, new RunRecord(exitZero, kernel, cpuPair, errorMessage));

            // Limit history size
            if (history.size() > MAX_HISTORY) {
                history = history.subList(0, MAX_HISTORY);
            }

            // Save
            JSONArray array = new JSONArray();
            for (RunRecord r : history) {
                array.put(r.toJSON());
            }

            prefs.edit()
                .putInt(KEY_TOTAL_RUNS, totalRuns)
                .putInt(KEY_EXIT_ZERO_COUNT, exitZeroCount)
                .putInt(KEY_NON_ZERO_COUNT, nonZeroCount)
                .putString(KEY_RUN_HISTORY, array.toString())
                .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<RunRecord> getRunHistory() {
        List<RunRecord> history = new ArrayList<>();
        String data = prefs.getString(KEY_RUN_HISTORY, "[]");

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                history.add(RunRecord.fromJSON(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return history;
    }

    public int getTotalRuns() {
        return prefs.getInt(KEY_TOTAL_RUNS, 0);
    }

    public int getExitZeroCount() { return prefs.getInt(KEY_EXIT_ZERO_COUNT, 0); }

    public int getNonZeroCount() { return prefs.getInt(KEY_NON_ZERO_COUNT, 0); }

    public float getExitZeroRate() {
        int total = getTotalRuns();
        if (total == 0) return 0f;
        return (float) getExitZeroCount() / total * 100f;
    }

    /** Backward-compatible aliases; these do not represent verified root access. */
    @Deprecated public int getSuccessCount() { return getExitZeroCount(); }
    @Deprecated public int getFailureCount() { return getNonZeroCount(); }
    @Deprecated public float getSuccessRate() { return getExitZeroRate(); }

    /** Backward-compatible adapter for existing call sites. */
    @Deprecated public void recordRun(boolean success, String kernel, String cpuPair, String errorMessage) {
        recordProcessOutcome(ProcessOutcome.fromExitCode(success ? 0 : 1), kernel, cpuPair, errorMessage);
    }

    public void clearHistory() {
        prefs.edit()
            .remove(KEY_RUN_HISTORY)
            .remove(KEY_TOTAL_RUNS)
            .remove(KEY_EXIT_ZERO_COUNT)
            .remove(KEY_NON_ZERO_COUNT)
            .apply();
    }
}
