package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

/** Primary Run action with an in-app themed confirmation surface. */
public class ConfirmRunButton extends MaterialButton {
    private boolean confirmedClick;

    public ConfirmRunButton(Context context) { super(context); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    public boolean performClick() {
        if (confirmedClick) {
            confirmedClick = false;
            return super.performClick();
        }
        showConfirmation();
        return true;
    }

    private void showConfirmation() {
        Context context = getContext();
        Dialog dialog = new Dialog(context);
        dialog.getWindow();
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(22), dp(24), dp(18));
        box.setBackground(round(0xFF202124, 28));

        TextView icon = text("👻", 28, 0xFFE8EAED);
        icon.setGravity(Gravity.CENTER);
        box.addView(icon, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView title = text("Run GhostLock?", 20, 0xFFF5F5F5);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        box.addView(title, marginParams(-1, -2, 0, 6, 0, 0));
        TextView message = text("This will start the kernel exploit & runtime operation on this device.", 13, 0xFFB9BCC3);
        message.setGravity(Gravity.CENTER);
        box.addView(message, marginParams(-1, -2, 0, 14, 0, 0));

        LinearLayout actions = new LinearLayout(context);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        MaterialButton cancel = new MaterialButton(context);
        cancel.setText("Cancel");
        cancel.setAllCaps(false);
        cancel.setMinWidth(0);
        cancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF303238));
        cancel.setTextColor(0xFFE6E7EA);
        cancel.setOnClickListener(v -> dialog.dismiss());
        MaterialButton run = new MaterialButton(context);
        run.setText("▶  Run");
        run.setAllCaps(false);
        run.setMinWidth(0);
        run.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE6E7EA));
        run.setTextColor(0xFF17181A);
        run.setOnClickListener(v -> {
            dialog.dismiss();
            confirmedClick = true;
            performClick();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-2, dp(48));
        rp.leftMargin = dp(8);
        actions.addView(run, rp);
        box.addView(actions, new LinearLayout.LayoutParams(-1, dp(52)));

        dialog.setContentView(box);
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.68f);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.86f), -2);
        }
        dialog.setOnShowListener(d -> {
            android.view.Window w = dialog.getWindow();
            if (w != null) {
                w.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.86f), -2);
            }
        });
        dialog.show();
    }

    private TextView text(String value, int size, int color) {
        TextView v = new TextView(getContext());
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        return v;
    }

    private LinearLayout.LayoutParams marginParams(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
