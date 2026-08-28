package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
        setOnClickListener(v -> showThemeMenu());
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
        int saved = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(PREF_THEME, SYSTEM);
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        int desired = saved == DARK ? UiModeManager.MODE_NIGHT_YES
                : saved == LIGHT ? UiModeManager.MODE_NIGHT_NO : UiModeManager.MODE_NIGHT_AUTO;
        if (manager.getNightMode() != desired) manager.setApplicationNightMode(desired);
    }

    private void showThemeMenu() {
        PopupMenu menu = new PopupMenu(getContext(), this);
        menu.getMenu().add(0, SYSTEM, 0, "System");
        menu.getMenu().add(0, LIGHT, 1, "Light");
        menu.getMenu().add(0, DARK, 2, "Dark");
        int current = findActivity(getContext()) == null ? SYSTEM
                : findActivity(getContext()).getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(PREF_THEME, SYSTEM);
        menu.getMenu().getItem(current).setChecked(true);
        menu.setOnMenuItemClickListener(item -> {
            setThemeMode(item.getItemId());
            return true;
        });
        menu.show();
    }

    private void setThemeMode(int mode) {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(PREF_THEME, mode).apply();
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        manager.setApplicationNightMode(mode == DARK ? UiModeManager.MODE_NIGHT_YES
                : mode == LIGHT ? UiModeManager.MODE_NIGHT_NO : UiModeManager.MODE_NIGHT_AUTO);
        // Stay on the current page; Android recreates the Activity only when required by the mode change.
        updateIconState();
    }

    private boolean isDarkMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void updateIconState() {
        Activity activity = findActivity(getContext());
        int mode = activity == null ? SYSTEM : activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(PREF_THEME, SYSTEM);
        boolean dark = isDarkMode();
        setImageResource(dark ? R.drawable.ic_theme_moon : R.drawable.ic_theme_sun);
        setColorFilter(ContextCompat.getColor(getContext(), dark ? R.color.theme_icon_dark : R.color.theme_icon_light));
        setAlpha(1f);
        setScaleType(ScaleType.CENTER_INSIDE);
        setPadding(dp(10), dp(10), dp(10), dp(10));
        setContentDescription(mode == SYSTEM ? "Theme: System" : mode == LIGHT ? "Theme: Light" : "Theme: Dark");
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
