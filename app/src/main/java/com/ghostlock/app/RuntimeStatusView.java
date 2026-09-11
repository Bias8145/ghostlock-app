package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Compact manager status surface with a large, intentionally clipped watermark. */
public class RuntimeStatusView extends FrameLayout {
    private final TextView state, message, manager, action;
    private final ManagerWatermarkView watermark;
    private final LinearLayout content;

    public RuntimeStatusView(Context context) { this(context, null); }

    public RuntimeStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(true);
        setClipToPadding(true);

        watermark = new ManagerWatermarkView(context);
        LayoutParams watermarkParams = new LayoutParams(dp(112), dp(112), Gravity.END | Gravity.CENTER_VERTICAL);
        watermarkParams.setMargins(0, 0, -dp(8), 0);
        addView(watermark, watermarkParams);

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        // Keep enough breathing room for text while reserving a smaller right
        // gutter. The oversized watermark may crop at the card edge by design.
        content.setPadding(dp(15), dp(10), dp(88), dp(10));
        addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        state = text(14, true);
        content.addView(state, margin(-1, -2, 0, 0, 0, 3));

        message = text(11, false);
        message.setLineSpacing(0f, 1.08f);
        content.addView(message, margin(-1, -2, 0, 0, 0, 4));

        manager = text(10, true);
        manager.setLineSpacing(0f, 1.05f);
        content.addView(manager, margin(-1, -2, 0, 0, 0, 7));

        action = text(12, true);
        action.setGravity(Gravity.CENTER);
        action.setPadding(dp(10), 0, dp(10), 0);
        action.setMinHeight(dp(44));
        action.setMaxLines(1);
        action.setEllipsize(android.text.TextUtils.TruncateAt.END);
        action.setVisibility(View.GONE);
        content.addView(action, margin(-1, dp(44), 0, 0, 0, 0));
        refresh();
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refresh(); }
    @Override public void onWindowFocusChanged(boolean hasFocus) { super.onWindowFocusChanged(hasFocus); if (hasFocus) post(this::refresh); }

    public void refresh() {
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        boolean showInstall = false;
        int watermarkColor;
        switch (result.state) {
            case READY:
                state.setText("READY"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_success)); message.setText("Compatible manager detected and verified"); setSurface(R.color.status_success_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_success); break;
            case MANAGER_REQUIRED:
                state.setText("MANAGER REQUIRED"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.accent)); message.setText("Install a registered manager before running GhostLock"); setSurface(R.color.accent_container); showInstall = true; watermarkColor = ContextCompat.getColor(getContext(), R.color.accent); break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:
                state.setText("MANAGER NOT INSTALLED"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.accent)); message.setText("No registered manager is installed"); setSurface(R.color.accent_container); showInstall = true; watermarkColor = ContextCompat.getColor(getContext(), R.color.accent); break;
            case SPOOFED_MANAGER:
                state.setText("IDENTITY MISMATCH"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error)); message.setText("The detected manager identity could not be verified"); setSurface(R.color.status_error_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_error); break;
            case UNSUPPORTED_MANAGER:
                state.setText("UNSUPPORTED MANAGER"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error)); message.setText("The installed manager is not registered with GhostLock"); setSurface(R.color.status_error_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_error); break;
            default:
                state.setText("MANAGER STATUS UNAVAILABLE"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary)); message.setText("Manager information could not be determined"); setSurface(R.color.surface_container); watermarkColor = ContextCompat.getColor(getContext(), R.color.text_secondary); break;
        }
        watermark.setState(result.state, watermarkColor);
        String managerText = !result.manager.installed ? "Manager  ·  Not installed" : result.manager.spoofed ? "Manager  ·  " + result.manager.name + "  ·  Identity mismatch" : result.manager.identityVerified ? "Manager  ·  " + result.manager.name + "  ·  Verified" : "Manager  ·  " + result.manager.name + "  ·  Recognized";
        manager.setText(managerText);
        manager.setTextColor(ContextCompat.getColor(getContext(), result.manager.spoofed ? R.color.status_error : result.manager.installed ? R.color.text_primary : R.color.accent));
        action.setVisibility(showInstall ? View.VISIBLE : View.GONE);
        if (showInstall) {
            action.setText("Install supported manager");
            action.setTextColor(ContextCompat.getColor(getContext(), R.color.on_accent));
            action.setBackground(round(ContextCompat.getColor(getContext(), R.color.accent), 16));
            action.setOnClickListener(v -> showManagerPicker());
        } else {
            action.setOnClickListener(null);
        }
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers = ManagerCompatibility.registeredManagers(getContext());
        final android.app.Dialog dialog = new android.app.Dialog(getContext());
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(18), dp(20), dp(14));
        box.setBackground(round(ContextCompat.getColor(getContext(), R.color.surface), 24));
        TextView title = text(19, true); title.setText("Install supported manager"); box.addView(title);
        TextView subtitle = text(12, false); subtitle.setText("Select a registered manager to continue."); box.addView(subtitle, margin(-1, -2, 0, 5, 0, 10));
        for (ManagerCompatibility.ManagerInfo info : managers) {
            TextView row = text(14, true);
            row.setText(info.name + (info.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), 0, dp(14), 0);
            row.setBackground(round(ContextCompat.getColor(getContext(), R.color.surface_container_low), 15));
            row.setClickable(true); row.setFocusable(true);
            row.setOnClickListener(v -> { dialog.dismiss(); ManagerCompatibility.openInstaller(getContext(), info); });
            box.addView(row, margin(-1, 50, 0, 0, 0, 7));
        }
        TextView cancel = text(13, true); cancel.setText("Cancel"); cancel.setGravity(Gravity.CENTER); cancel.setTextColor(ContextCompat.getColor(getContext(), R.color.accent)); cancel.setOnClickListener(v -> dialog.dismiss()); box.addView(cancel, margin(-1, 44, 0, 3, 0, 0));
        dialog.setContentView(box);
        GhostLockModal.apply(dialog, false);
        dialog.setOnDismissListener(d -> GhostLockModal.clear(dialog));
        dialog.show();
    }

    private TextView text(int size, boolean bold) { TextView v = new TextView(getContext()); v.setTextSize(size); v.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary)); if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD); return v; }
    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h); p.setMargins(dp(l), dp(t), dp(r), dp(b)); return p; }
    private GradientDrawable round(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private void setSurface(int colorRes) { setBackground(round(ContextCompat.getColor(getContext(), colorRes), 22)); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
