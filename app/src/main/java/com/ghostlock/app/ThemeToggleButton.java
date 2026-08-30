package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.widget.ImageButton;

/** Theme control with a single guarded transition path for System/Light/Dark. */
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
        syncThemeFromPreference();
        updateIconState();
    }

    /** Apply the persisted selection without guessing the system's light/dark state. */
    private void syncThemeFromPreference() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        int mode = getSavedMode(activity);
        UiModeManager manager = getModeManager(activity);
        if (manager == null) return;
        int desired = resolveNightMode(mode);
        if (manager.getNightMode() != desired) manager.setApplicationNightMode(desired);
    }

    private void cycleThemeMode() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        int current = getSavedMode(activity);
        int next = (current + 1) % 3;
        if (!isValidMode(next)) next = SYSTEM;
        if (!applyThemeGuarded(activity, next)) return;
        animate().rotationBy(360f).setDuration(420L).start();
        activity.recreate();
    }

    /**
     * Validate the requested mode, apply it through UiModeManager, verify the
     * manager accepted it, then persist it. Preference is never committed first.
     */
    private boolean applyThemeGuarded(Activity activity, int mode) {
        if (!isValidMode(mode)) return false;
        UiModeManager manager = getModeManager(activity);
        if (manager == null) return false;

        int desired = resolveNightMode(mode);
        if (desired != UiModeManager.MODE_NIGHT_AUTO
                && desired != UiModeManager.MODE_NIGHT_YES
                && desired != UiModeManager.MODE_NIGHT_NO) return false;

        if (manager.getNightMode() != desired) manager.setApplicationNightMode(desired);
        if (manager.getNightMode() != desired) return false;

        // For SYSTEM, the selection is valid even while Android is resolving
        // the current light/dark configuration. Never infer a mode prematurely.
        if (mode != SYSTEM && resolveConfigurationNightMode(activity.getResources().getConfiguration()) == -1) {
            return false;
        }

        return activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(PREF_THEME, mode).commit();
    }

    private int getSavedMode(Activity activity) {
        int mode = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(PREF_THEME, SYSTEM);
        return isValidMode(mode) ? mode : SYSTEM;
    }

    private static boolean isValidMode(int mode) { return mode >= SYSTEM && mode <= DARK; }

    private static int resolveNightMode(int mode) {
        return mode == SYSTEM ? UiModeManager.MODE_NIGHT_AUTO
                : mode == DARK ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO;
    }

    /** Returns YES/NO only when Android explicitly reports one; -1 means unknown. */
    private static int resolveConfigurationNightMode(Configuration configuration) {
        int mask = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mask == Configuration.UI_MODE_NIGHT_YES) return UiModeManager.MODE_NIGHT_YES;
        if (mask == Configuration.UI_MODE_NIGHT_NO) return UiModeManager.MODE_NIGHT_NO;
        return -1;
    }

    private static UiModeManager getModeManager(Activity activity) {
        return (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
    }

    /** Resolve the icon from persisted selection + the actual Configuration. */
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
        // System mode is resolved from the new configuration, never from a cached boolean.
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
