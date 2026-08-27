package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;

/** Compact manager identity indicator kept in the device card. */
public class KsuStatusView extends TextView {
    public KsuStatusView(Context context) { super(context); init(); }
    public KsuStatusView(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public KsuStatusView(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setTypeface(Typeface.DEFAULT);
        setTextSize(11);
        refreshStatus();
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refreshStatus(); }

    public void refreshStatus() {
        ManagerCompatibility.ManagerInfo m = ManagerCompatibility.detectManager(getContext());
        if (!m.installed) {
            setText("Manager  ·  Not installed");
            setTextColor(0xFFFFC94D);
        } else if (m.spoofed) {
            setText("Manager  ·  " + m.name + "  ·  Identity mismatch");
            setTextColor(0xFFFF7777);
        } else if (m.identityVerified) {
            setText("Manager  ·  " + m.name + "  ·  Verified");
            setTextColor(0xFF72D6A0);
        } else {
            setText("Manager  ·  " + m.name + "  ·  Recognized");
            setTextColor(0xFFE0E2E6);
        }
    }
}