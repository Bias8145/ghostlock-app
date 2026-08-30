package com.ghostlock.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.widget.ImageButton;

/** Passive system-theme indicator. GhostLock always follows Android's theme. */
public class ThemeToggleButton extends ImageButton {
    public ThemeToggleButton(Context context) { super(context); init(); }
    public ThemeToggleButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public ThemeToggleButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setClickable(false);
        setFocusable(false);
        setLongClickable(false);
        setScaleType(ScaleType.CENTER_INSIDE);
        setMinimumWidth(dp(48));
        setMinimumHeight(dp(48));
        setPadding(dp(10), dp(10), dp(10), dp(10));
        syncIndicator(getResources().getConfiguration());
    }

    private void syncIndicator(Configuration configuration) {
        int night = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean dark = night == Configuration.UI_MODE_NIGHT_YES;
        setImageResource(dark ? R.drawable.ic_theme_moon : R.drawable.ic_theme_sun);
        setImageTintList(ColorStateList.valueOf(getResources().getColor(
                dark ? R.color.theme_icon_dark : R.color.theme_icon_light)));
        setContentDescription(getContext().getString(
                dark ? R.string.action_theme_dark : R.string.action_theme_light));
    }

    @Override protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        syncIndicator(newConfig);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
