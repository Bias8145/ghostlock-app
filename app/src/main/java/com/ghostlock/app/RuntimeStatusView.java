package com.ghostlock.app;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Compact manager status surface. */
public class RuntimeStatusView extends FrameLayout {
    private final TextView state, message, manager, action;
    private final LinearLayout content;

    public RuntimeStatusView(Context context) { this(context, null); }
    public RuntimeStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs); setBackground(null);
        content = new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(16), dp(10), dp(16), dp(10));
        addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        state = text(14, true); content.addView(state, margin(-1, -2, 0, 0, 0, 3));
        message = text(11, false); message.setLineSpacing(0f, 1.08f); content.addView(message, margin(-1, -2, 0, 0, 0, 4));
        manager = text(10, true); manager.setLineSpacing(0f, 1.05f); content.addView(manager, margin(-1, -2, 0, 0, 0, 6));
        action = text(12, true); action.setGravity(Gravity.CENTER); action.setPadding(dp(10), 0, dp(10), 0); action.setMinHeight(dp(40)); action.setMaxLines(1); action.setEllipsize(android.text.TextUtils.TruncateAt.END); action.setVisibility(View.GONE); content.addView(action, margin(-1, dp(40), 0, 0, 0, 0));
        refresh();
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refresh(); }
    @Override public void onWindowFocusChanged(boolean hasFocus) { super.onWindowFocusChanged(hasFocus); if (hasFocus) post(this::refresh); }

    public void refresh() {
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        boolean showInstall = false;
        int watermarkColor = ContextCompat.getColor(getContext(), R.color.text_secondary);
        int watermarkIcon = R.drawable.ic_manager_status_unknown;
        switch (result.state) {
            case READY:
                state.setText("READY"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_success)); message.setText("Compatible manager detected and verified"); setSurface(R.color.status_success_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_success); watermarkIcon = R.drawable.ic_install_circle_check; break;
            case MANAGER_REQUIRED:
                state.setText("MANAGER REQUIRED"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.accent)); message.setText("Install a registered manager before running GhostLock"); setSurface(R.color.accent_container); showInstall = true; watermarkColor = ContextCompat.getColor(getContext(), R.color.accent); watermarkIcon = R.drawable.ic_install_box_open; break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:
                state.setText("MANAGER NOT INSTALLED"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.accent)); message.setText("No registered manager is installed"); setSurface(R.color.accent_container); showInstall = true; watermarkColor = ContextCompat.getColor(getContext(), R.color.accent); watermarkIcon = R.drawable.ic_install_box_open; break;
            case KERNEL_UNSUPPORTED:
                state.setText("KERNEL UNSUPPORTED"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error)); message.setText("The current kernel is not supported by GhostLock"); setSurface(R.color.status_error_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_error); watermarkIcon = R.drawable.ic_install_shield; break;
            case SPOOFED_MANAGER:
                state.setText("IDENTITY MISMATCH"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error)); message.setText("The detected manager identity could not be verified"); setSurface(R.color.status_error_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_error); watermarkIcon = R.drawable.ic_install_shield; break;
            case UNSUPPORTED_MANAGER:
                state.setText("UNSUPPORTED MANAGER"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error)); message.setText("The installed manager is not registered with GhostLock"); setSurface(R.color.status_error_bg); watermarkColor = ContextCompat.getColor(getContext(), R.color.status_error); watermarkIcon = R.drawable.ic_install_shield; break;
            default:
                state.setText("MANAGER STATUS UNAVAILABLE"); state.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary)); message.setText("Manager information could not be determined"); setSurface(R.color.surface_container); break;
        }
        updateWatermark(watermarkColor, watermarkIcon);
        String managerText = !result.manager.installed ? "Manager  ·  Not installed" : result.manager.spoofed ? "Manager  ·  " + result.manager.name + "  ·  Identity mismatch" : result.manager.identityVerified ? "Manager  ·  " + result.manager.name + "  ·  Verified" : "Manager  ·  " + result.manager.name + "  ·  Recognized";
        manager.setText(managerText); manager.setTextColor(ContextCompat.getColor(getContext(), result.manager.spoofed ? R.color.status_error : result.manager.installed ? R.color.text_primary : R.color.accent));
        action.setVisibility(showInstall ? View.VISIBLE : View.GONE);
        if (showInstall) { action.setText("Install supported manager"); action.setTextColor(ContextCompat.getColor(getContext(), R.color.on_accent)); action.setBackground(round(ContextCompat.getColor(getContext(), R.color.accent), 16)); action.setOnClickListener(v -> showManagerPicker()); } else action.setOnClickListener(null);
    }

    private void updateWatermark(int color, int icon) {
        if (!(getParent() instanceof View)) return;
        ImageView watermark = ((View) getParent()).findViewById(R.id.managerWatermark);
        if (watermark == null) return;
        watermark.setImageResource(icon); watermark.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) watermark.getLayoutParams(); lp.width = LayoutParams.MATCH_PARENT; lp.height = LayoutParams.MATCH_PARENT; lp.gravity = Gravity.END | Gravity.BOTTOM; lp.setMargins(0, 0, 0, 0); watermark.setLayoutParams(lp);
        watermark.setScaleType(ImageView.ScaleType.MATRIX); watermark.setTranslationX(0); watermark.setTranslationY(0); watermark.setAlpha(0.045f); watermark.setClickable(false); watermark.setFocusable(false);
        watermark.post(() -> positionWatermark(watermark));
    }

    private void positionWatermark(ImageView watermark) {
        Drawable drawable = watermark.getDrawable();
        if (drawable == null || watermark.getWidth() <= 0 || watermark.getHeight() <= 0) return;
        float targetSize = dp(125);
        float intrinsicWidth = Math.max(1, drawable.getIntrinsicWidth());
        float intrinsicHeight = Math.max(1, drawable.getIntrinsicHeight());
        float scale = targetSize / Math.max(intrinsicWidth, intrinsicHeight);
        float visualWidth = intrinsicWidth * scale;
        float visualHeight = intrinsicHeight * scale;
        float centerX = watermark.getWidth() - dp(30);
        float centerY = watermark.getHeight() - dp(24);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(centerX - visualWidth / 2f, centerY - visualHeight / 2f);
        watermark.setImageMatrix(matrix);
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers = ManagerCompatibility.registeredManagers(getContext());
        final android.app.Dialog dialog = new android.app.Dialog(getContext());
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(22), dp(24), dp(18));
        box.setBackground(round(ContextCompat.getColor(getContext(), R.color.surface), 26));

        TextView title = text(20, true);
        title.setText("Install supported manager");
        box.addView(title);

        TextView subtitle = text(13, false);
        subtitle.setText("Select a registered manager to continue.");
        subtitle.setLineSpacing(0f, 1.08f);
        box.addView(subtitle, margin(-1, -2, 0, 6, 0, 16));

        for (ManagerCompatibility.ManagerInfo info : managers) {
            TextView row = text(15, true);
            row.setText(info.name + (info.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(18), 0, dp(18), 0);
            row.setMinHeight(dp(60));
            row.setBackground(round(ContextCompat.getColor(getContext(), R.color.surface_container_low), 17));
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> { dialog.dismiss(); ManagerCompatibility.openInstaller(getContext(), info); });
            box.addView(row, margin(-1, 60, 0, 0, 0, 10));
        }

        TextView cancel = text(14, true);
        cancel.setText("Cancel");
        cancel.setGravity(Gravity.CENTER);
        cancel.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
        cancel.setMinHeight(dp(48));
        cancel.setOnClickListener(v -> dialog.dismiss());
        box.addView(cancel, margin(-1, 48, 0, 5, 0, 0));

        dialog.setContentView(box);
        GhostLockModal.apply(dialog, false);
        dialog.setOnDismissListener(d -> GhostLockModal.clear(dialog));
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            int width = Math.min((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), dp(390));
            window.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    private TextView text(int size, boolean bold) { TextView v = new TextView(getContext()); v.setTextSize(size); v.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary)); if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD); return v; }
    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h); p.setMargins(dp(l), dp(t), dp(r), dp(b)); return p; }
    private GradientDrawable round(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private void setSurface(int colorRes) { GradientDrawable surface = round(ContextCompat.getColor(getContext(), colorRes), 22); if (getParent() instanceof View) ((View) getParent()).setBackground(surface); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
