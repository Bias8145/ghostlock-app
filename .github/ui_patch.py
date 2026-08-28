from pathlib import Path
import re
p = Path('app/src/main/java/com/ghostlock/app/MainActivity.java')
s = p.read_text()
s = s.replace('import android.view.MenuItem;\n', '')
s = s.replace('import android.widget.PopupMenu;\n', '')
s = s.replace('import android.widget.EditText;\n', 'import android.widget.EditText;\nimport android.widget.FrameLayout;\n')
s = s.replace('    private View rootView;\n', '    private View rootView;\n    private FrameLayout pageHost;\n    private TextView historyView;\n    private ScrollView historyScroll;\n    private View homePage;\n    private View settingsPage;\n')
s = s.replace('        applyWindowInsetsPadding();\n        deviceInfo.setText(buildDeviceSummary());', '        setupPages();\n        applyWindowInsetsPadding();\n        deviceInfo.setText(buildDeviceSummary());')
old_nav = '''        findViewById(R.id.navHome).setOnClickListener(v -> logScroll.smoothScrollTo(0, 0));
        findViewById(R.id.navHistory).setOnClickListener(v -> logScroll.smoothScrollTo(0, logView.getBottom()));
        findViewById(R.id.navSettings).setOnClickListener(v -> {
            setPanelBlur(false);
            if (advancedPanel.getVisibility() != View.VISIBLE) animateShow(advancedPanel);
            advancedPanel.post(() -> ((androidx.core.widget.NestedScrollView) rootView.findViewById(R.id.contentScroll)).smoothScrollTo(0, advancedPanel.getTop()));
        });'''
s = s.replace(old_nav, '        setupNavigation();')
s = s.replace('            int side = dp(20);\n            v.setPadding(side, top + dp(12), side, bottom + dp(12));', '            v.setPadding(0, top + dp(12), 0, bottom);')
s = re.sub(r'\n    private void showActionsMenu\(View anchor\) \{.*?\n    \}\n\n    private void setPanelBlur', '\n    private void setPanelBlur', s, flags=re.S)
marker = '    private void setPanelBlur(boolean enabled) {'
methods = '''    private void setupPages() {
        LinearLayout root = (LinearLayout) rootView;
        View content = findViewById(R.id.contentScroll);
        homePage = content;
        int index = root.indexOfChild(content);
        root.removeView(content);
        pageHost = new FrameLayout(this);
        pageHost.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(pageHost, index);
        content.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        pageHost.addView(content);

        historyScroll = new ScrollView(this);
        historyScroll.setFillViewport(true);
        historyScroll.setPadding(dp(4), dp(8), dp(4), dp(12));
        LinearLayout historyBody = new LinearLayout(this);
        historyBody.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("History");
        title.setTextSize(22);
        title.setTextColor(getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(dp(12), dp(8), dp(12), dp(12));
        historyBody.addView(title);
        historyView = new TextView(this);
        historyView.setTextSize(12);
        historyView.setTextColor(getColor(R.color.log_text));
        historyView.setTypeface(android.graphics.Typeface.MONOSPACE);
        historyView.setTextIsSelectable(true);
        historyView.setPadding(dp(16), dp(16), dp(16), dp(16));
        historyBody.addView(historyView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        com.google.android.material.button.MaterialButton clear = new com.google.android.material.button.MaterialButton(this);
        clear.setText("Clear history");
        clear.setAllCaps(false);
        clear.setOnClickListener(v -> { getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove("run_history").apply(); refreshHistory(); });
        historyBody.addView(clear);
        historyScroll.addView(historyBody);
        pageHost.addView(historyScroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ScrollView settingsScroll = new ScrollView(this);
        settingsScroll.setFillViewport(true);
        settingsScroll.setPadding(dp(4), dp(8), dp(4), dp(12));
        LinearLayout settingsBody = new LinearLayout(this);
        settingsBody.setOrientation(LinearLayout.VERTICAL);
        TextView st = new TextView(this);
        st.setText("Settings");
        st.setTextSize(22);
        st.setTextColor(getColor(R.color.text_primary));
        st.setTypeface(null, android.graphics.Typeface.BOLD);
        st.setPadding(dp(12), dp(8), dp(12), dp(12));
        settingsBody.addView(st);
        ViewGroup oldParent = (ViewGroup) advancedPanel.getParent();
        oldParent.removeView(advancedPanel);
        advancedPanel.setVisibility(View.VISIBLE);
        settingsBody.addView(advancedPanel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        settingsScroll.addView(settingsBody);
        settingsPage = settingsScroll;
        pageHost.addView(settingsScroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        refreshHistory();
        showPage(0);
    }

    private void setupNavigation() {
        com.google.android.material.button.MaterialButton home = findViewById(R.id.navHome);
        com.google.android.material.button.MaterialButton history = findViewById(R.id.navHistory);
        com.google.android.material.button.MaterialButton settings = findViewById(R.id.navSettings);
        home.setIconResource(R.drawable.ic_nav_home);
        history.setIconResource(R.drawable.ic_nav_history);
        settings.setIconResource(R.drawable.ic_nav_settings);
        home.setText("Home");
        history.setText("History");
        settings.setText("Settings");
        home.setOnClickListener(v -> showPage(0));
        history.setOnClickListener(v -> showPage(1));
        settings.setOnClickListener(v -> showPage(2));
    }

    private void showPage(int page) {
        if (pageHost == null) return;
        homePage.setVisibility(page == 0 ? View.VISIBLE : View.GONE);
        historyScroll.setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        settingsPage.setVisibility(page == 2 ? View.VISIBLE : View.GONE);
        com.google.android.material.button.MaterialButton home = findViewById(R.id.navHome);
        com.google.android.material.button.MaterialButton history = findViewById(R.id.navHistory);
        com.google.android.material.button.MaterialButton settings = findViewById(R.id.navSettings);
        home.setAlpha(page == 0 ? 1f : 0.62f);
        history.setAlpha(page == 1 ? 1f : 0.62f);
        settings.setAlpha(page == 2 ? 1f : 0.62f);
        if (page == 1) refreshHistory();
    }

    private void refreshHistory() {
        if (historyView == null) return;
        String history = getSharedPreferences(PREFS, MODE_PRIVATE).getString("run_history", "");
        historyView.setText(history.isEmpty() ? "No runs recorded yet." : history);
    }

    private void saveRunHistory(boolean success) {
        String snapshot;
        synchronized (logBuffer) { snapshot = logBuffer.toString().trim(); }
        String entry = (success ? "✓ SUCCESS" : "✕ FAILED") + "  " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new java.util.Date()) + "\\n" + snapshot;
        String old = getSharedPreferences(PREFS, MODE_PRIVATE).getString("run_history", "");
        String combined = entry + (old.isEmpty() ? "" : "\\n\\n──────────────\\n\\n" + old);
        String[] blocks = combined.split("\\n\\n──────────────\\n\\n", -1);
        if (blocks.length > 5) combined = String.join("\\n\\n──────────────\\n\\n", java.util.Arrays.copyOf(blocks, 5));
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("run_history", combined).apply();
        ui.post(this::refreshHistory);
    }

'''
s = s.replace(marker, methods + marker)
s = s.replace('        runButton.setEnabled(state != RunState.RUNNING);\n        runButton.setText(state == RunState.RUNNING ? R.string.action_running : R.string.action_run);', '        runButton.setEnabled(state != RunState.RUNNING);\n        runButton.setText(state == RunState.RUNNING ? R.string.action_running : R.string.action_run);\n        if (state == RunState.SUCCESS || state == RunState.FAILED) saveRunHistory(state == RunState.SUCCESS);')
p.write_text(s)

vectors = {
'ic_nav_home.xml': '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="@color/icon_tint" android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/></vector>',
'ic_nav_history.xml': '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="@color/icon_tint" android:pathData="M13,3a9,9 0,1 0,8.94 10H20a7,7 0,1 1,-7 -8v3l4,-4 -4,-4zM12,7v6l5,3 1,-1.5 -4,-2.5V7z"/></vector>',
'ic_nav_settings.xml': '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="@color/icon_tint" android:pathData="M12,8a4,4 0,1 0,0 8,4 4,0 0,0 0,-8zM21,13v-2l-2,-0.5a7.7,7.7 0,0 0,-0.7,-1.7l1.1,-1.7 -1.4,-1.4 -1.7,1.1a7.7,7.7 0,0 0,-1.7,-0.7L14,4h-2l-0.5,2.1a7.7,7.7 0,0 0,-1.7,0.7L8.1,5.7 6.7,7.1l1.1,1.7a7.7,7.7 0,0 0,-0.7,1.7L5,11v2l2.1,0.5c0.2,0.6 0.4,1.2 0.7,1.7l-1.1,1.7 1.4,1.4 1.7,-1.1c0.5,0.3 1.1,0.5 1.7,0.7L12,20h2l0.5,-2.1a7.7,7.7 0,0 0,1.7,-0.7l1.7,1.1 1.4,-1.4 -1.1,-1.7c0.3,-0.5 0.5,-1.1 0.7,-1.7L21,13z"/></vector>'
}
for name, text in vectors.items():
    Path('app/src/main/res/drawable/' + name).write_text(text)
