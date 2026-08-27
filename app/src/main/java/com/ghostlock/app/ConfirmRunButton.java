package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

/** Primary Run action with themed confirmation and compatibility gating. */
public class ConfirmRunButton extends MaterialButton {
    private boolean confirmedClick;

    public ConfirmRunButton(Context context) { super(context); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override public boolean performClick() {
        if (confirmedClick) {
            confirmedClick = false;
            return super.performClick();
        }
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        if (!result.canRun()) {
            showBlocked(result);
            return true;
        }
        showConfirmation(result);
        return true;
    }

    private void showBlocked(ManagerCompatibility.Result result) {
        Dialog dialog = createDialog();
        LinearLayout box = box();
        TextView title = text(20, true);
        title.setText(blockTitle(result));
        box.addView(title, margin(-1, -2, 0, 0, 0, 7));
        TextView message = text(13, false);
        message.setText(blockMessage(result));
        message.setLineSpacing(0, 1.08f);
        box.addView(message, margin(-1, -2, 0, 0, 0, 16));

        if (result.state == ManagerCompatibility.State.MANAGER_REQUIRED) {
            MaterialButton install = actionButton("Install supported manager", true);
            install.setOnClickListener(v -> {
                dialog.dismiss();
                showManagerPicker();
            });
            box.addView(install, margin(-1, dp(54), 0, 0, 0, 14));
        }

        MaterialButton close = actionButton("Close", false);
        close.setOnClickListener(v -> dialog.dismiss());
        box.addView(close, margin(-1, dp(48), 0, 0, 0, 0));
        show(dialog, box);
    }

    private void showConfirmation(ManagerCompatibility.Result result) {
        Dialog dialog = createDialog();
        LinearLayout box = box();
        TextView title = text(20, true);
        title.setText("Run GhostLock");
        box.addView(title, margin(-1, -2, 0, 0, 0, 4));
        TextView subtitle = text(12, false);
        subtitle.setText("Execute kernel exploit & runtime");
        box.addView(subtitle, margin(-1, -2, 0, 0, 0, 16));

        LinearLayout info = new LinearLayout(getContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setBackground(round(0xFF25282D, 16));
        info.setPadding(dp(12), dp(10), dp(12), dp(10));
        info.addView(infoRow("Kernel", result.kernelSupported ? "Kernel supported" : "Kernel unsupported"),
                new LinearLayout.LayoutParams(-1, dp(44)));
        info.addView(infoRow("Manager", result.manager.name), margin(-1, dp(44), 0, 7, 0, 0));
        String identity = result.manager.identityVerified ? "Verified" : "Recognized";
        info.addView(infoRow("Identity", identity), margin(-1, dp(44), 0, 7, 0, 0));
        box.addView(info, margin(-1, -2, 0, 0, 0, 16));

        TextView message = text(12, false);
        message.setText("This operation will modify runtime state on the current device.");
        message.setLineSpacing(0, 1.08f);
        box.addView(message, margin(-1, -2, 0, 0, 0, 16));

        LinearLayout actions = new LinearLayout(getContext());
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        MaterialButton cancel = actionButton("Cancel", false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        MaterialButton run = actionButton("Run", true);
        run.setOnClickListener(v -> {
            dialog.dismiss();
            confirmedClick = true;
            performClick();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams runParams = new LinearLayout.LayoutParams(-2, dp(48));
        runParams.leftMargin = dp(8);
        actions.addView(run, runParams);
        box.addView(actions, new LinearLayout.LayoutParams(-1, dp(52)));
        show(dialog, box);
    }

    private void showManagerPicker() {
        Dialog dialog = createDialog();
        LinearLayout box = box();
        TextView title = text(19, true);
        title.setText("Install supported manager");
        box.addView(title);
        TextView subtitle = text(12, false);
        subtitle.setText("Select a registered manager to continue.");
        box.addView(subtitle, margin(-1, -2, 0, 6, 0, 14));

        for (ManagerCompatibility.ManagerInfo manager : ManagerCompatibility.registeredManagers(getContext())) {
            TextView row = text(14, true);
            row.setText(manager.name + (manager.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), 0, dp(14), 0);
            row.setBackground(round(0xFF2A2D32, 16));
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                dialog.dismiss();
                ManagerCompatibility.openInstaller(getContext(), manager);
            });
            box.addView(row, margin(-1, dp(54), 0, 0, 0, 9));
        }

        MaterialButton cancel = actionButton("Cancel", false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        box.addView(cancel, margin(-1, dp(48), 0, 4, 0, 0));
        show(dialog, box);
    }

    private LinearLayout infoRow(String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView labelView = text(12, true);
        labelView.setText(label);
        row.addView(labelView, new LinearLayout.LayoutParams(0, dp(44), 1));
        TextView valueView = text(12, true);
        valueView.setText(value);
        valueView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        valueView.setMaxLines(1);
        row.addView(valueView, new LinearLayout.LayoutParams(-2, dp(44)));
        return row;
    }

    private String blockTitle(ManagerCompatibility.Result result) {
        switch (result.state) {
            case MANAGER_REQUIRED: return "Manager required";
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED: return "Kernel and manager unavailable";
            case SPOOFED_MANAGER: return "Manager identity mismatch";
            case UNSUPPORTED_MANAGER: return "Unsupported manager";
            default: return "Kernel unsupported";
        }
    }

    private String blockMessage(ManagerCompatibility.Result result) {
        switch (result.state) {
            case MANAGER_REQUIRED: return "The kernel is supported, but a registered manager is not installed.";
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED: return "The current kernel is not supported and no manager is installed. Installing a manager will not make this kernel compatible.";
            case SPOOFED_MANAGER: return "The detected manager identity is not trusted. Check the package and signing certificate before running GhostLock.";
            case UNSUPPORTED_MANAGER: return "The installed manager is not registered with GhostLock.";
            default: return "The current kernel does not provide the capability required by GhostLock.";
        }
    }

    private Dialog createDialog() { return new Dialog(getContext()); }

    private LinearLayout box() {
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(20), dp(22), dp(16));
        box.setBackground(round(0xFF202124, 26));
        return box;
    }

    private void show(Dialog dialog, LinearLayout box) {
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

    private MaterialButton actionButton(String text, boolean primary) {
        MaterialButton button = new MaterialButton(getContext());
        button.setText(text);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setTextSize(13);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? 0xFFE7E8EB : 0xFF303238));
        button.setTextColor(primary ? 0xFF17181A : 0xFFE7E8EB);
        return button;
    }

    private TextView text(int size, boolean bold) {
        TextView view = new TextView(getContext());
        view.setTextSize(size);
        view.setTextColor(bold ? 0xFFF1F2F4 : 0xFFB8BBC2);
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

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
