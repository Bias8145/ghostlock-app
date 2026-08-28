package com.ghostlock.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * MainActivity entry point that applies GhostLock's UI scale through density,
 * never through Configuration.fontScale. This keeps the Android/system text
 * scale untouched while allowing the whole application UI to scale together.
 */
public final class ScaledMainActivity extends MainActivity {
    private static final String PREFS = "ghostlock_prefs";
    private static final String PREF_APP_SCALE = "app_scale";
    private static final String LEGACY_SCALE = "font_scale";

    @Override
    protected void attachBaseContext(Context base) {
        float scale = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getFloat(PREF_APP_SCALE,
                        base.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                                .getFloat(LEGACY_SCALE, 1.0f));
        if (!Float.isFinite(scale)) scale = 1.0f;
        scale = Math.max(0.80f, Math.min(1.30f, scale));

        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        int baseDensity = configuration.densityDpi;
        configuration.densityDpi = Math.max(120, Math.round(baseDensity * scale));
        super.attachBaseContext(base.createConfigurationContext(configuration));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
