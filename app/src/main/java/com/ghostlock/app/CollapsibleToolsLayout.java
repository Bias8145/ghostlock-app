package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Tools trigger that presents utility controls in a Material 3 bottom sheet. */
public class CollapsibleToolsLayout extends LinearLayout {
    private View header;
    private View content;
    private ViewGroup contentParent;
    private ViewGroup.LayoutParams originalContentLayoutParams;
    private int contentIndex = -1;
    private Dialog toolsDialog;

    public CollapsibleToolsLayout(Context context) { super(context); setOrientation(VERTICAL); }
    public CollapsibleToolsLayout(Context context, AttributeSet attrs) { super(context, attrs); setOrientation(VERTICAL); }
    public CollapsibleToolsLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); setOrientation(VERTICAL); }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) return;
        header = getChildAt(0);
        content = getChildAt(1);
        contentParent = this;
        contentIndex = indexOfChild(content);
        originalContentLayoutParams = content.getLayoutParams();
        header.setBackground(roundBackground(R.color.accent_container, 18));
        header.setClickable(true);
        header.setFocusable(true);
        header.setContentDescription("Open tools");
        content.setVisibility(GONE);
        header.setOnClickListener(v -> showToolsPanel());
    }

    private void showToolsPanel() {
        if (content == null || contentParent == null || (toolsDialog != null && toolsDialog.isShowing())) return;
        final Dialog dialog = new Dialog(getContext());
        toolsDialog = dialog;
        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(18), dp(12), dp(18), dp(14));
        panel.setBackground(roundBackground(R.color.surface, 28));

        LinearLayout handleRow = new LinearLayout(getContext());
        handleRow.setGravity(Gravity.CENTER);
        TextView handle = new TextView(getContext());
        handle.setText("•••");
        handle.setTextColor(color(R.color.text_secondary));
        handle.setTextSize(11);
        handle.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        handle.setGravity(Gravity.CENTER);
        handle.getPaint().setAntiAlias(true);
        handleRow.addView(handle, new LinearLayout.LayoutParams(dp(48), dp(24)));
        panel.addView(handleRow, new LinearLayout.LayoutParams(-1, dp(28)));

        TextView title = new TextView(getContext());
        title.setText("Tools");
        title.setTextColor(color(R.color.text_primary));
        title.setTextSize(18);
        title.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        title.getPaint().setAntiAlias(true);
        panel.addView(title, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView subtitle = new TextView(getContext());
        subtitle.setText("Utility actions for offsets and kernel images");
        subtitle.setTextColor(color(R.color.text_secondary));
        subtitle.setTextSize(11);
        subtitle.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        subtitle.getPaint().setAntiAlias(true);
        panel.addView(subtitle, margin(-1, -2, 0, 0, 0, 12));

        contentParent.removeView(content);
        content.setVisibility(VISIBLE);
        panel.addView(content, new LinearLayout.LayoutParams(-1, -2));
        dialog.setOnDismissListener(d -> { GhostLockModal.clear(dialog); restoreContent(dialog, panel); });
        dialog.setContentView(panel);
        dialog.setOnShowListener(d -> configureWindow(dialog));
        dialog.show();
        configureWindow(dialog);
    }

    private void configureWindow(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) return;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setDimAmount(.56f);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * .94f), -2);
        GhostLockModal.apply(dialog);
    }

    private void restoreContent(Dialog dialog, ViewGroup panel) {
        if (content != null && content.getParent() == panel) panel.removeView(content);
        if (content != null && contentParent != null && content.getParent() == null) {
            int index = Math.max(0, Math.min(contentIndex, contentParent.getChildCount()));
            ViewGroup.LayoutParams params = originalContentLayoutParams;
            if (params != null) contentParent.addView(content, index, params); else contentParent.addView(content, index);
            content.setVisibility(GONE);
        }
        if (toolsDialog == dialog) toolsDialog = null;
    }

    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h); p.setMargins(dp(l), dp(t), dp(r), dp(b)); return p; }
    private int color(int id) { return androidx.core.content.ContextCompat.getColor(getContext(), id); }
    private GradientDrawable roundBackground(int colorRes, int radiusDp) { GradientDrawable bg = new GradientDrawable(); bg.setColor(color(colorRes)); bg.setCornerRadius(dp(radiusDp)); return bg; }\n    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
