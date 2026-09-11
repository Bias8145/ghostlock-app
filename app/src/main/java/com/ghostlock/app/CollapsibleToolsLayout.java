package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/**
 * Tools trigger that presents the existing utility controls in a floating
 * panel instead of expanding the Home layout inline.
 */
public class CollapsibleToolsLayout extends LinearLayout {
    private View header;
    private View content;
    private ViewGroup contentParent;
    private ViewGroup.LayoutParams originalContentLayoutParams;
    private int contentIndex = -1;
    private Dialog toolsDialog;

    public CollapsibleToolsLayout(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public CollapsibleToolsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public CollapsibleToolsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            return;
        }
        header = getChildAt(0);
        content = getChildAt(1);
        contentParent = this;
        contentIndex = indexOfChild(content);
        originalContentLayoutParams = content.getLayoutParams();

        header.setClickable(true);
        header.setFocusable(true);
        header.setContentDescription("Open tools");
        content.setVisibility(GONE);
        header.setOnClickListener(v -> showToolsPanel());
    }

    private void showToolsPanel() {
        if (content == null || contentParent == null) {
            return;
        }
        if (toolsDialog != null && toolsDialog.isShowing()) {
            return;
        }

        final Dialog dialog = new Dialog(getContext());
        toolsDialog = dialog;

        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(20), dp(12), dp(20), dp(16));
        panel.setBackground(round(color(R.color.surface_container), 26));

        LinearLayout handleRow = new LinearLayout(getContext());
        handleRow.setGravity(Gravity.CENTER);
        TextView handle = new TextView(getContext());
        handle.setText("—");
        handle.setTextColor(color(R.color.text_secondary));
        handle.setTextSize(18);
        handle.setGravity(Gravity.CENTER);
        handleRow.addView(handle, new LinearLayout.LayoutParams(dp(48), dp(24)));
        panel.addView(handleRow, new LinearLayout.LayoutParams(-1, dp(28)));

        LinearLayout titleRow = new LinearLayout(getContext());
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(getContext());
        title.setText("Tools");
        title.setTextColor(color(R.color.text_primary));
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1f));

        TextView close = new TextView(getContext());
        close.setText("×");
        close.setTextColor(color(R.color.text_secondary));
        close.setTextSize(28);
        close.setGravity(Gravity.CENTER);
        close.setClickable(true);
        close.setFocusable(true);
        close.setContentDescription("Close tools");
        titleRow.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        panel.addView(titleRow, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView subtitle = new TextView(getContext());
        subtitle.setText("Utility actions for offsets and kernel images");
        subtitle.setTextColor(color(R.color.text_secondary));
        subtitle.setTextSize(12);
        panel.addView(subtitle, margin(-1, -2, 0, 0, 0, 14));

        // Keep the original LayoutParams so the view can be restored safely.
        contentParent.removeView(content);
        content.setVisibility(VISIBLE);
        panel.addView(content, new LinearLayout.LayoutParams(-1, -2));

        close.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> restoreContent(dialog, panel));
        dialog.setContentView(panel);

        dialog.setOnShowListener(d -> configureWindow(dialog));
        dialog.show();
        configureWindow(dialog);
    }

    private void configureWindow(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setDimAmount(.60f);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * .94f), -2);
    }

    private void restoreContent(Dialog dialog, ViewGroup panel) {
        // Dismissal can happen from the close button, back button, or outside tap.
        // Remove the view from the dialog's panel before attaching it back to Home.
        if (content != null && content.getParent() == panel) {
            panel.removeView(content);
        }

        if (content != null && contentParent != null && content.getParent() == null) {
            int index = Math.max(0, Math.min(contentIndex, contentParent.getChildCount()));
            ViewGroup.LayoutParams params = originalContentLayoutParams;
            if (params != null) {
                contentParent.addView(content, index, params);
            } else {
                contentParent.addView(content, index);
            }
            content.setVisibility(GONE);
        }

        if (toolsDialog == dialog) {
            toolsDialog = null;
        }
    }

    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    private GradientDrawable round(int c, int r) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(c);
        d.setCornerRadius(dp(r));
        return d;
    }

    private int color(int id) {
        return ContextCompat.getColor(getContext(), id);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
