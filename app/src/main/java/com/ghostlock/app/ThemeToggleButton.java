package com.ghostlock.app;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.ImageButton;

/** Compact Light/Dark theme toggle used in the main header. */
public class ThemeToggleButton extends ImageButton {
    public ThemeToggleButton(Context context) { super(context); init(); }
    public ThemeToggleButton(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public ThemeToggleButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setOnClickListener(v -> toggleTheme());
        setContentDescription("Toggle light and dark theme");
        updateIconState();
    }

    private void toggleTheme() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;
        UiModeManager manager = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
        if (manager == null) return;
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        manager.setApplicationNightMode(dark ? UiModeManager.MODE_NIGHT_NO : UiModeManager.MODE_NIGHT_YES);
        activity.recreate();
    }

    private void updateIconState() {
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        setImageResource(dark ? R.drawable.ic_theme_sun : R.drawable.ic_theme_moon);
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
}
