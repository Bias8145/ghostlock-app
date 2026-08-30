package com.ghostlock.app;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;

/** Opens the dedicated Settings page while remaining compatible with MainActivity's existing listener. */
public class SettingsButton extends androidx.appcompat.widget.AppCompatImageButton {
    public SettingsButton(Context context) { super(context); init(); }
    public SettingsButton(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public SettingsButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        super.setOnClickListener(v -> openSettings());
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        // MainActivity historically installs a listener for the old floating panel.
        // Keep that call harmless and route the button directly to the dedicated page.
        super.setOnClickListener(v -> openSettings());
    }

    private void openSettings() {
        Context context = getContext();
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
}
