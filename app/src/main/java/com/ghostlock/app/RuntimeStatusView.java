package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Manager status surface; kernel capability is presented separately in the header. */
public class RuntimeStatusView extends LinearLayout {
    private final TextView state;
    private final TextView message;
    private final TextView manager;
    private final TextView action;

    public RuntimeStatusView(Context context) { this(context, null); }
    public RuntimeStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setPadding(dp(18), dp(16), dp(18), dp(16));
        state = text(15, true);
        addView(state);
        message = text(12, false);
        addView(message, margin(-1, -2, 0, 7, 0, 0));
        manager = text(11, true);
        addView(manager, margin(-1, -2, 0, 13, 0, 0));
        action = text(12, true);
        action.setGravity(Gravity.CENTER);
        action.setPadding(dp(12), 0, dp(12), 0);
        action.setMinHeight(dp(44));
        action.setVisibility(View.GONE);
        addView(action, margin(-1, 44, 0, 14, 0, 0));
        refresh();
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refresh(); }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) post(this::refresh);
    }

    public void refresh() {
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        boolean showInstall = false;
        switch (result.state) {
            case READY:
                state.setText("READY");
                state.setTextColor(0xFF72D6A0);
                message.setText("Compatible manager detected and verified");
                setSurface(0xFF1E2924);
                break;
            case MANAGER_REQUIRED:
                state.setText("MANAGER REQUIRED");
                state.setTextColor(0xFFFFC94D);
                message.setText("Install a registered manager before running GhostLock");
                setSurface(0xFF2C281E);
                showInstall = true;
                break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:
                state.setText("MANAGER NOT INSTALLED");
                state.setTextColor(0xFFFFC94D);
                message.setText("No registered manager is installed");
                setSurface(0xFF2C281E);
                showInstall = true;
                break;
            case SPOOFED_MANAGER:
                state.setText("IDENTITY MISMATCH");
                state.setTextColor(0xFFFF7777);
                message.setText("The detected manager identity could not be verified");
                setSurface(0xFF2B2022);
                break;
            case UNSUPPORTED_MANAGER:
                state.setText("UNSUPPORTED MANAGER");
                state.setTextColor(0xFFFF7777);
                message.setText("The installed manager is not registered with GhostLock");
                setSurface(0xFF2B2022);
                break;
            default:
                state.setText("MANAGER STATUS UNAVAILABLE");
                state.setTextColor(0xFFB8BBC2);
                message.setText("Manager information could not be determined");
                setSurface(0xFF24262B);
                break;
        }

        String managerText;
        if (!result.manager.installed) {
            managerText = "Manager  ·   Not installed";
        } else if (result.manager.spoofed) {
            managerText = "Manager  ·   " + result.manager.name + "  ·  Identity mismatch";
        } else if (result.manager.identityVerified) {
            managerText = "Manager  ·   " + result.manager.name + "  ·  Verified";
        } else {
            managerText = "Manager  ·   " + result.manager.name + "  ·  Recognized";
        }
        manager.setText(managerText);
        manager.setTextColor(result.manager.spoofed ? 0xFFFF7777 : (result.manager.installed ? 0xFFE0E2E6 : 0xFFFFC94D));

        action.setVisibility(showInstall ? View.VISIBLE : View.GONE);
        if (showInstall) {
            action.setText("Install supported manager");
            action.setTextColor(0xFF17181A);
            action.setBackground(round(0xFFFFC94D, 21));
            action.setOnClickListener(v -> showManagerPicker());
        } else {
            action.setOnClickListener(null);
        }
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers = ManagerCompatibility.registeredManagers(getContext());
        android.app.Dialog dialog = new android.app.Dialog(getContext());
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(VERTICAL);
        box.setPadding(dp(22), dp(20), dp(22), dp(16));
        box.setBackground(round(0xFF202124, 26));
        TextView title = text(19, true);
        title.setText("Install supported manager");
        box.addView(title);
        TextView subtitle = text(12, false);
        subtitle.setText("Select a registered manager to continue.");
        box.addView(subtitle, margin(-1, -2, 0, 6, 0, 14));
        for (ManagerCompatibility.ManagerInfo managerInfo : managers) {
            TextView row = text(14, true);
            row.setText(managerInfo.name + (managerInfo.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), 0, dp(14), 0);
            row.setBackground(round(0xFF2A2D32, 16));
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                dialog.dismiss();
                ManagerCompatibility.openInstaller(getContext(), managerInfo);
            });
            box.addView(row, margin(-1, 54, 0, 0, 0, 9));
        }
        TextView cancel = text(13, true);
        cancel.setText("Cancel");
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(v -> dialog.dismiss());
        box.addView(cancel, margin(-1, 44, 0, 4, 0, 0));
        dialog.setContentView(box);
        dialog.setOnShowListener(x -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(.68f);
                dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * .88f), -2);
            }
        });
        dialog.show();
    }

    private TextView text(int size, boolean bold) {
        TextView view = new TextView(getContext());
        view.setTextSize(size);
        view.setTextColor(0xFFE9EAED);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private void setSurface(int color) { setBackground(round(color, 22)); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
