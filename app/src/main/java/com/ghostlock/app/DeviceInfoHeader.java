package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Legacy compatibility view. Device Info is now a single always-expanded
 * snapshot panel; this view intentionally renders nothing.
 */
public class DeviceInfoHeader extends LinearLayout {
    public DeviceInfoHeader(Context context) { super(context); init(); }
    public DeviceInfoHeader(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public DeviceInfoHeader(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setVisibility(GONE);
        setClickable(false);
        setFocusable(false);
    }
}
