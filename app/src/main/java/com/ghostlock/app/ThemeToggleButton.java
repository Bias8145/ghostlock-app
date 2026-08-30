package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.widget.ImageButton;

/** Theme control with guarded System/Light/Dark resolution. */
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
        applyMode(activity, getSavedMode(activity));
    }

    private void cycleThemeMode() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;

        int current = getSavedMode(activity);
        int next = (current + 1) % 3;
        if (!isValidMode(next)) next = SYSTEM;

        UiModeManager manager = getModeManager(activity);
        if (manager == null) return;

        int desired = resolveNightMode(next);
        // Guard: don't persist or recreate until the target system mode is valid.
        if (desired != UiModeManager.MODE_NIGHT_AUTO
                && desired != UiModeManager.MODE_NIGHT_YES
                && desired != UiModeManager.MODE_NIGHT_NO) {
            return;
        }

        if (!applyMode(activity, next)) return;

        animate().rotationBy(360f).setDuration(420L).start();
        if (next != current) activity.recreate();
    }

    private boolean applyMode(Activity activity, int mode) {
        if (!isValidMode(mode)) return false;
        UiModeManager manager = getModeManager(activity);
        if (manager == null) return false;

        int desired = resolveNightMode(mode);
        if (manager.getNightMode() != desired) {
            manager.setApplicationNightMode(desired);
        }

        // Persist only after the requested mode has been accepted by the manager.
        boolean committed = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(PREF_THEME, mode).commit();
        if (!committed) return false;

        // For SYSTEM, resolve the actual light/dark state from Configuration; never guess.
        int resolved = resolveConfigurationNightMode(activity.getResources().getConfiguration());
        if (resolved == -1) return false;
        return mode == SYSTEM || (mode == DARK && resolved == UiModeManager.MODE_NIGHT_YES)
                || (mode == LIGHT && resolved == UiModeManager.MODE_NIGHT_NO);
    }

    private int getSavedMode(Activity activity) {
        int mode = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(PREF_THEME, SYSTEM);
        return isValidMode(mode) ? mode : SYSTEM;
    }

    private static boolean isValidMode(int mode) {
        return mode >= SYSTEM && mode <= DARK;
    }

    private static int resolveNightMode(int mode) {
        return mode == SYSTEM ? UiModeManager.MODE_NIGHT_AUTO
                : mode == DARK ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO;
    }

    private static int resolveConfigurationNightMode(Configuration configuration) {
        int mask = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mask == Configuration.UI_MODE_NIGHT_YES) return UiModeManager.MODE_NIGHT_YES;
        if (mask == Configuration.UI_MODE_NIGHT_NO) return UiModeManager.MODE_NIGHT_NO;
        return -1;
    }

    private static UiModeManager getModeManager(Activity activity) {
        return (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
    }

    private void updateIconState() {
        Activity activity = findActivity(getContext());
        int mode = activity == null ? SYSTEM : getSavedMode(activity);
        int resolved = resolveConfigurationNightMode(getResources().getConfiguration());
        boolean dark = resolved == UiModeManager.MODE_NIGHT_YES;

        setImageResource(mode == SYSTEM ? R.drawable.ic_theme
                : mode == DARK ? R.drawable.ic_theme_moon : R.drawable.ic_theme_sun);
        int tint = dark ? R.color.theme_icon_dark : R.color.theme_icon_light;
        setImageTintList(ColorStateList.valueOf(getResources().getColor(tint)));
        setContentDescription(getContext().getString(mode == SYSTEM ? R.string.action_theme_system
                : mode == DARK ? R.string.action_theme_dark : R.string.action_theme_light));
    }

    @Override protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Re-inspect the system result instead of assuming the previous state.
        if (resolveConfigurationNightMode(newConfig) != -1) updateIconState();
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
