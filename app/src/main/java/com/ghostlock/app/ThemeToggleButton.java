package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;

/** Compact Light/Dark/System theme control used in the main header. */
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
        setOnClickListener(v -> cycleTheme());
        setScaleType(ScaleType.CENTER_INSIDE);
        setMinimumWidth(dp(48));
        setMinimumHeight(dp(48));
        setPadding(dp(10), dp(10), dp(10), dp(10));
        applySavedTheme();
        updateIconState();
    }

    private void applySavedTheme() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        int saved = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(PREF_THEME, SYSTEM);
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        int desired = saved == DARK ? UiModeManager.MODE_NIGHT_YES
                : saved == LIGHT ? UiModeManager.MODE_NIGHT_NO
                : UiModeManager.MODE_NIGHT_AUTO;
        if (manager.getNightMode() != desired) manager.setApplicationNightMode(desired);
    }

    /** Cycles System -> Light -> Dark -> System. */
    private void cycleTheme() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        int current = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(PREF_THEME, SYSTEM);
        int next = current == SYSTEM ? LIGHT : current == LIGHT ? DARK : SYSTEM;
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt(PREF_THEME, next).apply();
        int mode = next == DARK ? UiModeManager.MODE_NIGHT_YES
                : next == LIGHT ? UiModeManager.MODE_NIGHT_NO
                : UiModeManager.MODE_NIGHT_AUTO;
        manager.setApplicationNightMode(mode);
        activity.recreate();
    }

    private boolean isDarkMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void updateIconState() {
        Activity activity = findActivity(getContext());
        int mode = activity == null ? SYSTEM : activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(PREF_THEME, SYSTEM);
        boolean dark = isDarkMode();
        setImageResource(dark ? R.drawable.ic_theme_moon : R.drawable.ic_theme_sun);
        setColorFilter(ContextCompat.getColor(getContext(),
                dark ? R.color.theme_icon_dark : R.color.theme_icon_light));
        setAlpha(1f);
        setScaleType(ScaleType.CENTER_INSIDE);
        setPadding(dp(10), dp(10), dp(10), dp(10));
        setContentDescription(mode == SYSTEM ? "Theme: System. Tap to use light theme"
                : mode == LIGHT ? "Theme: Light. Tap to use dark theme"
                : "Theme: Dark. Tap to use system theme");
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
