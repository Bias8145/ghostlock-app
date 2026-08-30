package com.ghostlock.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    private static final String PREFS = "ghostlock_prefs";
    private static final String PREF_CPU_PAIR = "cpu_pair";
    private static final String EXTRA_LABELS = "cpu_labels";
    private static final String EXTRA_PAIRS = "cpu_pairs";
    private Spinner cpuSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backButton = findViewById(R.id.backButton);
        cpuSpinner = findViewById(R.id.cpuSpinner);
        backButton.setOnClickListener(v -> finish());

        String[] labels = getIntent().getStringArrayExtra(EXTRA_LABELS);
        int[] flatPairs = getIntent().getIntArrayExtra(EXTRA_PAIRS);
        if (labels == null || flatPairs == null || flatPairs.length < labels.length * 2) {
            labels = new String[]{"0,1"};
            flatPairs = new int[]{0, 1};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_right, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cpuSpinner.setAdapter(adapter);

        int selected = findSavedPair(flatPairs);
        cpuSpinner.setSelection(selected);
        final int[] pairs = flatPairs;
        cpuSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position * 2 + 1 >= pairs.length) return;
                int a = pairs[position * 2];
                int b = pairs[position * 2 + 1];
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_CPU_PAIR, a + "," + b).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private int findSavedPair(int[] pairs) {
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_CPU_PAIR, null);
        if (saved == null || saved.equals("auto")) return 0;
        String[] parts = saved.split(",");
        if (parts.length != 2) return 0;
        try {
            int a = Integer.parseInt(parts[0].trim());
            int b = Integer.parseInt(parts[1].trim());
            for (int i = 0; i + 1 < pairs.length; i += 2) {
                if (pairs[i] == a && pairs[i + 1] == b) return i / 2;
            }
        } catch (NumberFormatException ignored) { }
        return 0;
    }

    public static String EXTRA_LABELS() { return EXTRA_LABELS; }
    public static String EXTRA_PAIRS() { return EXTRA_PAIRS; }
}
