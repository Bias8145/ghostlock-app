package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** Compact status surface driven by the current kernel and installed manager. */
public class RuntimeStatusView extends LinearLayout {
    private final TextView state;
    private final TextView message;
    private final TextView support;

    public RuntimeStatusView(Context c) { this(c, null); }
    public RuntimeStatusView(Context c, android.util.AttributeSet a) {
        super(c, a);
        setOrientation(VERTICAL);
        setPadding(dp(18), dp(16), dp(18), dp(16));
        setBackground(round(0xFF202226, 22));
        LinearLayout row = new LinearLayout(c);
        row.setGravity(Gravity.CENTER_VERTICAL);
        state = text(15, true);
        row.addView(state, new LinearLayout.LayoutParams(-1, -2));
        addView(row);
        message = text(12, false);
        addView(message, margin(-1, -2, 0, 4, 0, 0));
        support = text(12, true);
        addView(support, margin(-1, -2, 0, 14, 0, 0));
        refresh();
    }

    public void refresh() {
        boolean kernelOk = isKernelSupported();
        boolean managerOk = hasManager();
        state.setText(kernelOk && managerOk ? "●  WORKING" : "●  NOT WORKING");
        state.setTextColor(kernelOk && managerOk ? 0xFF72D6A0 : 0xFFFF7777);
        message.setText(kernelOk && managerOk ? "Kernel interface & runtime ready" : (!kernelOk ? "This kernel is not supported" : "No supported manager was detected"));
        message.setTextColor(0xFFB8BBC2);
        support.setText(managerOk ? "SUPPORT  ·  Manager detected" : "UNSUPPORTED  ·  No compatible manager detected");
        support.setTextColor(managerOk ? 0xFFE0E2E6 : 0xFFFF7777);
        setBackground(round(managerOk ? 0xFF202226 : 0xFF2B2022, 22));
    }

    private boolean hasManager() {
        String[] pkgs = {"me.weishu.kernelsu", "com.resukisu.resukisu", "com.kowx712.supermanager"};
        for (String p : pkgs) {
            try { getContext().getPackageManager().getApplicationInfo(p, 0); return true; }
            catch (Throwable ignored) { }
        }
        return false;
    }

    private boolean isKernelSupported() {
        String version = System.getProperty("os.version", "");
        for (String s : SupportedKernels.UNAMES) if (s.equals(version)) return true;
        return false;
    }

    private TextView text(int size, boolean bold) {
        TextView v = new TextView(getContext());
        v.setTextSize(size); v.setTextColor(0xFFE9EAED);
        if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD);
        return v;
    }
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private GradientDrawable round(int c,int r) { GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(dp(r)); return d; }
    private int dp(int v) { return Math.round(v*getResources().getDisplayMetrics().density); }
}
