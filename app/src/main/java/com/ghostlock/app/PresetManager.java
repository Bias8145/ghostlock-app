package com.ghostlock.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PresetManager {
    private static final String PREFS_NAME = "ghostlock_presets";
    private static final String KEY_PRESETS = "presets";
    private static final String KEY_ACTIVE_PRESET = "active_preset";

    private final SharedPreferences prefs;

    public PresetManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class Preset {
        public String name;
        public String cpuPair;
        public long timestamp;

        public Preset(String name, String cpuPair) {
            this.name = name;
            this.cpuPair = cpuPair;
            this.timestamp = System.currentTimeMillis();
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("cpuPair", cpuPair);
            obj.put("timestamp", timestamp);
            return obj;
        }

        public static Preset fromJSON(JSONObject obj) throws JSONException {
            Preset preset = new Preset(
                obj.getString("name"),
                obj.getString("cpuPair")
            );
            preset.timestamp = obj.optLong("timestamp", System.currentTimeMillis());
            return preset;
        }
    }

    public void savePreset(String name, String cpuPair) {
        try {
            List<Preset> presets = getPresets();

            // Remove existing preset with same name
            presets.removeIf(p -> p.name.equals(name));

            // Add new preset
            presets.add(new Preset(name, cpuPair));

            // Save to preferences
            JSONArray array = new JSONArray();
            for (Preset p : presets) {
                array.put(p.toJSON());
            }

            prefs.edit().putString(KEY_PRESETS, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Preset> getPresets() {
        List<Preset> presets = new ArrayList<>();
        String data = prefs.getString(KEY_PRESETS, "[]");

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                presets.add(Preset.fromJSON(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return presets;
    }

    public void deletePreset(String name) {
        try {
            List<Preset> presets = getPresets();
            presets.removeIf(p -> p.name.equals(name));

            JSONArray array = new JSONArray();
            for (Preset p : presets) {
                array.put(p.toJSON());
            }

            prefs.edit().putString(KEY_PRESETS, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setActivePreset(String name) {
        prefs.edit().putString(KEY_ACTIVE_PRESET, name).apply();
    }

    public String getActivePreset() {
        return prefs.getString(KEY_ACTIVE_PRESET, null);
    }

    public Preset getPresetByName(String name) {
        for (Preset p : getPresets()) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }
}
