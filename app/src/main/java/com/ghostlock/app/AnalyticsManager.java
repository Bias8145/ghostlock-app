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
    private static final String KEY_SUCCESS_COUNT = "success_count";
    private static final String KEY_FAILURE_COUNT = "failure_count";
    private static final int MAX_HISTORY = 100;

    private final SharedPreferences prefs;

    public AnalyticsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class RunRecord {
        public long timestamp;
        public boolean success;
        public String kernel;
        public String cpuPair;
        public String errorMessage;

        public RunRecord(boolean success, String kernel, String cpuPair, String errorMessage) {
            this.timestamp = System.currentTimeMillis();
            this.success = success;
            this.kernel = kernel;
            this.cpuPair = cpuPair;
            this.errorMessage = errorMessage;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("timestamp", timestamp);
            obj.put("success", success);
            obj.put("kernel", kernel);
            obj.put("cpuPair", cpuPair);
            if (errorMessage != null) {
                obj.put("error", errorMessage);
            }
            return obj;
        }

        public static RunRecord fromJSON(JSONObject obj) throws JSONException {
            RunRecord record = new RunRecord(
                obj.getBoolean("success"),
                obj.optString("kernel", ""),
                obj.optString("cpuPair", ""),
                obj.optString("error", null)
            );
            record.timestamp = obj.getLong("timestamp");
            return record;
        }
    }

    public void recordRun(boolean success, String kernel, String cpuPair, String errorMessage) {
        try {
            // Update counters
            int totalRuns = prefs.getInt(KEY_TOTAL_RUNS, 0) + 1;
            int successCount = prefs.getInt(KEY_SUCCESS_COUNT, 0) + (success ? 1 : 0);
            int failureCount = prefs.getInt(KEY_FAILURE_COUNT, 0) + (success ? 0 : 1);

            // Add to history
            List<RunRecord> history = getRunHistory();
            history.add(0, new RunRecord(success, kernel, cpuPair, errorMessage));

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
                .putInt(KEY_SUCCESS_COUNT, successCount)
                .putInt(KEY_FAILURE_COUNT, failureCount)
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

    public int getSuccessCount() {
        return prefs.getInt(KEY_SUCCESS_COUNT, 0);
    }

    public int getFailureCount() {
        return prefs.getInt(KEY_FAILURE_COUNT, 0);
    }

    public float getSuccessRate() {
        int total = getTotalRuns();
        if (total == 0) return 0f;
        return (float) getSuccessCount() / total * 100f;
    }

    public void clearHistory() {
        prefs.edit()
            .remove(KEY_RUN_HISTORY)
            .remove(KEY_TOTAL_RUNS)
            .remove(KEY_SUCCESS_COUNT)
            .remove(KEY_FAILURE_COUNT)
            .apply();
    }
}
