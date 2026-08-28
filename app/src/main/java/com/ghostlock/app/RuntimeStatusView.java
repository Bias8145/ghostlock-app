package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/** Manager status surface with light/dark theme-aware Material colors. */
public class RuntimeStatusView extends LinearLayout {
    private final TextView state;
    private final TextView message;
    private final TextView manager;
    private final TextView action;

    public RuntimeStatusView(Context context) {
        this(context, null);
    }

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
        action.setMinHeight(dp(56));
        action.setVisibility(View.GONE);
        addView(action, margin(-1, dp(56), 0, 18, 0, 0));
        refresh();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refresh();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) post(this::refresh);
    }

    public void refresh() {
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        boolean showInstall = false;

        switch (result.state) {
            case READY:
                state.setText("READY");
                state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_success));
                message.setText("Compatible manager detected and verified");
                setSurface(R.color.status_success_bg);
                break;
            case MANAGER_REQUIRED:
                state.setText("MANAGER REQUIRED");
                state.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
                message.setText("Install a registered manager before running GhostLock");
                setSurface(R.color.accent_container);
                showInstall = true;
                break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:
                state.setText("MANAGER NOT INSTALLED");
                state.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
                message.setText("No registered manager is installed");
                setSurface(R.color.accent_container);
                showInstall = true;
                break;
            case SPOOFED_MANAGER:
                state.setText("IDENTITY MISMATCH");
                state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error));
                message.setText("The detected manager identity could not be verified");
                setSurface(R.color.status_error_bg);
                break;
            case UNSUPPORTED_MANAGER:
                state.setText("UNSUPPORTED MANAGER");
                state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error));
                message.setText("The installed manager is not registered with GhostLock");
                setSurface(R.color.status_error_bg);
                break;
            default:
                state.setText("MANAGER STATUS UNAVAILABLE");
                state.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
                message.setText("Manager information could not be determined");
                setSurface(R.color.surface_container);
                break;
        }

        String managerText = !result.manager.installed
                ? "Manager  ·   Not installed"
                : result.manager.spoofed
                    ? "Manager  ·   " + result.manager.name + "  ·  Identity mismatch"
                    : result.manager.identityVerified
                        ? "Manager  ·   " + result.manager.name + "  ·  Verified"
                        : "Manager  ·   " + result.manager.name + "  ·  Recognized";
        manager.setText(managerText);
        manager.setTextColor(ContextCompat.getColor(getContext(),
                result.manager.spoofed
                        ? R.color.status_error
                        : result.manager.installed ? R.color.text_primary : R.color.accent));

        action.setVisibility(showInstall ? View.VISIBLE : View.GONE);
        if (showInstall) {
            action.setText("Install supported manager");
            action.setTextColor(ContextCompat.getColor(getContext(), R.color.on_accent));
            action.setBackground(round(ContextCompat.getColor(getContext(), R.color.accent), 18));
            action.setOnClickListener(v -> showManagerPicker());
        } else {
            action.setOnClickListener(null);
        }
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers =
                ManagerCompatibility.registeredManagers(getContext());
        final android.app.Dialog dialog = new android.app.Dialog(getContext());
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(VERTICAL);
        box.setPadding(dp(22), dp(20), dp(22), dp(16));
        box.setBackground(round(ContextCompat.getColor(getContext(), R.color.surface), 26));

        TextView title = text(19, true);
        title.setText("Install supported manager");
        box.addView(title);

        TextView subtitle = text(12, false);
        subtitle.setText("Select a registered manager to continue.");
        box.addView(subtitle, margin(-1, -2, 0, 6, 0, 14));

        for (ManagerCompatibility.ManagerInfo info : managers) {
            TextView row = text(14, true);
            row.setText(info.name + (info.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), 0, dp(14), 0);
            row.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            row.setBackground(round(ContextCompat.getColor(getContext(), R.color.surface_container_low), 16));
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                dialog.dismiss();
                ManagerCompatibility.openInstaller(getContext(), info);
            });
            box.addView(row, margin(-1, 54, 0, 0, 0, 9));
        }

        TextView cancel = text(13, true);
        cancel.setText("Cancel");
        cancel.setGravity(Gravity.CENTER);
        cancel.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
        cancel.setOnClickListener(v -> dialog.dismiss());
        box.addView(cancel, margin(-1, 44, 0, 4, 0, 0));

        dialog.setContentView(box);
        dialog.setOnShowListener(x -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(.68f);
                dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.getWindow().setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * .88f), -2);
            }
        });
        dialog.show();
    }

    private TextView text(int size, boolean bold) {
        TextView view = new TextView(getContext());
        view.setTextSize(size);
        view.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
        params.setMargins(dp(l), dp(t), dp(r), dp(b));
        return params;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private void setSurface(int colorRes) {
        setBackground(round(ContextCompat.getColor(getContext(), colorRes), 22));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
