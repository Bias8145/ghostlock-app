package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Compatibility status surface driven by kernel capability and manager identity. */
public class RuntimeStatusView extends LinearLayout {
    private final TextView state;
    private final TextView message;
    private final TextView kernel;
    private final TextView manager;
    private final TextView action;

    public RuntimeStatusView(Context c) { this(c, null); }
    public RuntimeStatusView(Context c, android.util.AttributeSet a) {
        super(c, a);
        setOrientation(VERTICAL);
        setPadding(dp(18), dp(16), dp(18), dp(16));
        state = text(15, true);
        addView(state);
        message = text(12, false);
        addView(message, margin(-1, -2, 0, 5, 0, 0));
        kernel = text(11, true);
        addView(kernel, margin(-1, -2, 0, 10, 0, 0));
        manager = text(11, true);
        addView(manager, margin(-1, -2, 0, 4, 0, 0));
        action = text(12, true);
        action.setGravity(Gravity.CENTER);
        action.setPadding(dp(12), 0, dp(12), 0);
        action.setVisibility(View.GONE);
        addView(action, margin(-1, dp(42), 0, 12, 0, 0));
        refresh();
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refresh(); }

    public void refresh() {
        ManagerCompatibility.Result r = ManagerCompatibility.evaluate(getContext());
        switch (r.state) {
            case READY:
                state.setText("WORKING"); state.setTextColor(0xFF72D6A0);
                message.setText("Kernel supported and compatible manager detected"); setSurface(0xFF1E2924); break;
            case MANAGER_REQUIRED:
                state.setText("MANAGER REQUIRED"); state.setTextColor(0xFFFFC94D);
                message.setText("Install a supported manager before running GhostLock"); setSurface(0xFF2C281E); break;
            case SPOOFED_MANAGER:
                state.setText("IDENTITY MISMATCH"); state.setTextColor(0xFFFF7777);
                message.setText("The detected manager identity could not be verified"); setSurface(0xFF2B2022); break;
            case UNSUPPORTED_MANAGER:
                state.setText("UNSUPPORTED MANAGER"); state.setTextColor(0xFFFF7777);
                message.setText("The installed manager is not registered with GhostLock"); setSurface(0xFF2B2022); break;
            default:
                state.setText("KERNEL UNSUPPORTED"); state.setTextColor(0xFFFF7777);
                message.setText("This kernel does not provide the required capability"); setSurface(0xFF2B2022); break;
        }
        kernel.setText("Kernel   ·   " + (r.kernelSupported ? "Supported" : "Unsupported"));
        kernel.setTextColor(r.kernelSupported ? 0xFF72D6A0 : 0xFFFF7777);
        String managerText;
        if (!r.manager.installed) managerText = "Manager  ·   Not installed";
        else if (r.manager.spoofed) managerText = "Manager  ·   " + r.manager.name + "  ·  Unregistered";
        else if (r.manager.identityVerified) managerText = "Manager  ·   " + r.manager.name + "  ·  Verified";
        else managerText = "Manager  ·   " + r.manager.name + "  ·  Recognized";
        manager.setText(managerText);
        manager.setTextColor(r.manager.spoofed ? 0xFFFF7777 : (r.manager.installed ? 0xFFE0E2E6 : 0xFFFFC94D));
        action.setVisibility(r.state == ManagerCompatibility.State.MANAGER_REQUIRED ? View.VISIBLE : View.GONE);
        if (r.state == ManagerCompatibility.State.MANAGER_REQUIRED) {
            action.setText("Install supported manager"); action.setTextColor(0xFF17181A);
            action.setBackground(round(0xFFFFC94D, 21)); action.setOnClickListener(v -> showManagerPicker());
        }
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers = ManagerCompatibility.registeredManagers(getContext());
        android.app.Dialog d = new android.app.Dialog(getContext());
        LinearLayout box = new LinearLayout(getContext()); box.setOrientation(VERTICAL);
        box.setPadding(dp(22), dp(20), dp(22), dp(16)); box.setBackground(round(0xFF202124, 26));
        TextView title = text(19, true); title.setText("Install supported manager"); box.addView(title);
        TextView sub = text(12, false); sub.setText("Select a registered manager to continue."); box.addView(sub, margin(-1,-2,0,4,0,12));
        for (ManagerCompatibility.ManagerInfo m : managers) {
            TextView row = text(14, true); row.setText(m.name + (m.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14), 0, dp(14), 0); row.setBackground(round(0xFF2A2D32, 16));
            row.setClickable(true); row.setOnClickListener(v -> { d.dismiss(); ManagerCompatibility.openInstaller(getContext(), m); });
            box.addView(row, margin(-1, dp(54), 0, 0, 0, 8));
        }
        TextView cancel = text(13, true); cancel.setText("Cancel"); cancel.setGravity(Gravity.CENTER); cancel.setOnClickListener(v -> d.dismiss());
        box.addView(cancel, margin(-1, dp(44), 0, 2, 0, 0));
        d.setContentView(box);
        d.setOnShowListener(x -> { if (d.getWindow() != null) { d.getWindow().setBackgroundDrawableResource(android.R.color.transparent); d.getWindow().setDimAmount(.68f); d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND); d.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f), -2); }});
        d.show();
    }

    private TextView text(int size, boolean bold) { TextView v = new TextView(getContext()); v.setTextSize(size); v.setTextColor(0xFFE9EAED); if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD); return v; }
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private GradientDrawable round(int c,int r) { GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(dp(r)); return d; }
    private void setSurface(int color) { setBackground(round(color, 22)); }
    private int dp(int v) { return Math.round(v*getResources().getDisplayMetrics().density); }
}