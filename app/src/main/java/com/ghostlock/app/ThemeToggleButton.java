package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;

/** Compact Light/Dark theme toggle used in the main header. */
public class ThemeToggleButton extends ImageButton {
    private static final String PREFS = "ghostlock_prefs";
    private static final String PREF_THEME = "theme_mode";
    private static final int SYSTEM = 0;
    private static final int LIGHT = 1;
    private static final int DARK = 2;

    public ThemeToggleButton(Context context) { super(context); init(); }
    public ThemeToggleButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public ThemeToggleButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setOnClickListener(v -> toggleTheme());
        setContentDescription("Toggle light and dark theme");
        setScaleType(ScaleType.CENTER_INSIDE);
        setMinimumWidth(dp(48));
        setMinimumHeight(dp(48));
        setPadding(dp(10), dp(10), dp(10), dp(10));
        applySavedThemeIfNeeded();
        updateIconState();
    }

    private void applySavedThemeIfNeeded() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        int saved = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(PREF_THEME, SYSTEM);
        if (saved == SYSTEM) return;
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        int desired = saved == DARK ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO;
        if (manager.getNightMode() != desired) manager.setApplicationNightMode(desired);
    }

    private void toggleTheme() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        boolean dark = isDarkMode();
        int next = dark ? LIGHT : DARK;
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(PREF_THEME, next).apply();
        manager.setApplicationNightMode(dark ? UiModeManager.MODE_NIGHT_NO : UiModeManager.MODE_NIGHT_YES);
        activity.recreate();
    }

    private boolean isDarkMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private void updateIconState() {
        boolean dark = isDarkMode();
        setImageResource(dark ? R.drawable.ic_theme_moon : R.drawable.ic_theme_sun);
        setColorFilter(ContextCompat.getColor(getContext(), dark ? R.color.theme_icon_dark : R.color.theme_icon_light));
        setAlpha(1f);
        setScaleType(ScaleType.CENTER_INSIDE);
        setPadding(dp(10), dp(10), dp(10), dp(10));
        setContentDescription(dark ? "Switch to light theme" : "Switch to dark theme");
    }

    @Override protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateIconState();
    }

    private static Activity findActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) return (Activity) current;
            Context base = ((ContextWrapper) current).getBaseContext();
            if (base == current) break;
            current = base;
        }
        return current instanceof Activity ? (Activity) current : null;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
