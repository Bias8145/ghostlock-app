package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.widget.ImageButton;

/** Header theme control cycling System, Light, and Dark modes. */
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
        setOnClickListener(v -> cycleThemeMode());
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
        int saved = getSavedMode(activity);
        UiModeManager manager = getModeManager(activity);
        if (manager == null) return;
        int desired = saved == SYSTEM ? UiModeManager.MODE_NIGHT_AUTO
                : saved == DARK ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO;
        if (manager.getNightMode() != desired) manager.setApplicationNightMode(desired);
    }

    private void cycleThemeMode() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        int next = (getSavedMode(activity) + 1) % 3;
        UiModeManager manager = getModeManager(activity);
        if (manager == null) return;
        // Persist first so recreation always restores the mode selected by the user.
        boolean saved = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(PREF_THEME, next).commit();
        if (!saved) return;

        int nightMode = next == SYSTEM ? UiModeManager.MODE_NIGHT_AUTO
                : next == DARK ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO;
        if (manager.getNightMode() != nightMode) {
            manager.setApplicationNightMode(nightMode);
        }
        updateIconState();
        animate().rotationBy(360f).setDuration(420L).start();
        activity.recreate();
    }

    private int getSavedMode(Activity activity) {
        int mode = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(PREF_THEME, SYSTEM);
        return mode >= SYSTEM && mode <= DARK ? mode : SYSTEM;
    }

    private static UiModeManager getModeManager(Activity activity) {
        return (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
    }

    private void updateIconState() {
        Activity activity = findActivity(getContext());
        int mode = activity == null ? SYSTEM : getSavedMode(activity);
        setImageResource(mode == SYSTEM ? R.drawable.ic_theme : mode == DARK ? R.drawable.ic_theme_moon : R.drawable.ic_theme_sun);
        setColorFilter(getResources().getColor(mode == DARK ? R.color.theme_icon_dark : R.color.theme_icon_light));
        setContentDescription(getContext().getString(mode == SYSTEM ? R.string.action_theme_system
                : mode == DARK ? R.string.action_theme_dark : R.string.action_theme_light));
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
