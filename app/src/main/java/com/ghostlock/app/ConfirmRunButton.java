package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

/** Primary Run action with a themed confirmation and compatibility gate. */
public class ConfirmRunButton extends MaterialButton {
    private boolean confirmedClick;

    public ConfirmRunButton(Context context) { super(context); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override public boolean performClick() {
        if (confirmedClick) { confirmedClick = false; return super.performClick(); }
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        if (!result.canRun()) { showBlocked(result); return true; }
        showConfirmation(result);
        return true;
    }

    private void showBlocked(ManagerCompatibility.Result result) {
        Dialog d = createDialog();
        LinearLayout box = box();
        TextView title = text(20, true); title.setText(blockTitle(result)); box.addView(title, margin(-1,-2,0,0,0,5));
        TextView message = text(13, false); message.setText(blockMessage(result)); box.addView(message, margin(-1,-2,0,0,0,14));
        if (result.state == ManagerCompatibility.State.MANAGER_REQUIRED) {
            MaterialButton install = actionButton("Install supported manager", true);
            install.setOnClickListener(v -> { d.dismiss(); showManagerPicker(); });
            box.addView(install, margin(-1, dp(48), 0, 0, 0, 8));
        }
        MaterialButton close = actionButton("Close", false); close.setOnClickListener(v -> d.dismiss());
        box.addView(close, margin(-1, dp(48), 0, 0, 0, 0));
        show(d, box);
    }

    private void showConfirmation(ManagerCompatibility.Result result) {
        Dialog d = createDialog();
        LinearLayout box = box();
        TextView title = text(20, true); title.setText("Run GhostLock"); box.addView(title, margin(-1,-2,0,0,0,4));
        TextView subtitle = text(12, false); subtitle.setText("Execute kernel exploit & runtime"); box.addView(subtitle, margin(-1,-2,0,0,0,14));
        box.addView(infoRow("Kernel", result.kernelSupported ? "Supported" : "Unsupported", result.kernelSupported ? 0xFF72D6A0 : 0xFFFF7777));
        box.addView(infoRow("Manager", result.manager.name, 0xFFE0E2E6));
        String identity = result.manager.identityVerified ? "Verified" : "Recognized";
        box.addView(infoRow("Identity", identity, result.manager.identityVerified ? 0xFF72D6A0 : 0xFFFFC94D));
        TextView message = text(12, false); message.setText("This operation will modify runtime state on the current device."); box.addView(message, margin(-1,-2,0,14,0,0));
        LinearLayout actions = new LinearLayout(getContext()); actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        MaterialButton cancel = actionButton("Cancel", false); cancel.setOnClickListener(v -> d.dismiss());
        MaterialButton run = actionButton("Run", true); run.setOnClickListener(v -> { d.dismiss(); confirmedClick = true; performClick(); });
        actions.addView(cancel, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-2, dp(48)); rp.leftMargin = dp(8); actions.addView(run, rp);
        box.addView(actions, new LinearLayout.LayoutParams(-1, dp(52)));
        show(d, box);
    }

    private void showManagerPicker() {
        Dialog d = createDialog(); LinearLayout box = box();
        TextView title = text(19, true); title.setText("Install supported manager"); box.addView(title);
        TextView sub = text(12, false); sub.setText("Select a registered manager to continue."); box.addView(sub, margin(-1,-2,0,4,0,12));
        for (ManagerCompatibility.ManagerInfo m : ManagerCompatibility.registeredManagers(getContext())) {
            TextView row = text(14, true); row.setText(m.name + (m.installed ? "  ·  Installed" : "  ·  Not installed"));
            row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14),0,dp(14),0); row.setBackground(round(0xFF2A2D32,16)); row.setClickable(true);
            row.setOnClickListener(v -> { d.dismiss(); ManagerCompatibility.openInstaller(getContext(), m); });
            box.addView(row, margin(-1,dp(54),0,0,0,8));
        }
        MaterialButton cancel = actionButton("Cancel", false); cancel.setOnClickListener(v -> d.dismiss()); box.addView(cancel, margin(-1,dp(48),0,2,0,0));
        show(d, box);
    }

    private LinearLayout infoRow(String label, String value, int color) {
        LinearLayout row = new LinearLayout(getContext()); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),0,dp(12),0); row.setBackground(round(0xFF2A2D32,14));
        TextView l = text(12,true); l.setText(label); row.addView(l,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView v = text(12,true); v.setText(value); v.setTextColor(color); row.addView(v,new LinearLayout.LayoutParams(-2,dp(44)));
        return row;
    }

    private String blockTitle(ManagerCompatibility.Result r) {
        switch (r.state) {
            case MANAGER_REQUIRED: return "Manager required";
            case SPOOFED_MANAGER: return "Manager identity mismatch";
            case UNSUPPORTED_MANAGER: return "Unsupported manager";
            default: return "Kernel unsupported";
        }
    }

    private String blockMessage(ManagerCompatibility.Result r) {
        switch (r.state) {
            case MANAGER_REQUIRED: return "The kernel is supported, but a registered manager is not installed.";
            case SPOOFED_MANAGER: return "The detected manager is not registered or its signing identity could not be verified.";
            case UNSUPPORTED_MANAGER: return "The installed manager is not registered with GhostLock.";
            default: return "The current kernel does not provide the capability required by GhostLock.";
        }
    }

    private Dialog createDialog() { return new Dialog(getContext()); }
    private LinearLayout box() { LinearLayout b=new LinearLayout(getContext()); b.setOrientation(VERTICAL); b.setPadding(dp(22),dp(20),dp(22),dp(16)); b.setBackground(round(0xFF202124,26)); return b; }
    private void show(Dialog d, LinearLayout box) { d.setContentView(box); d.setOnShowListener(x -> { if(d.getWindow()!=null){ d.getWindow().setBackgroundDrawableResource(android.R.color.transparent); d.getWindow().setDimAmount(.68f); d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND); d.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f),-2); }}); d.show(); }
    private MaterialButton actionButton(String s, boolean primary) { MaterialButton b=new MaterialButton(getContext()); b.setText(s); b.setAllCaps(false); b.setMinWidth(0); b.setTextSize(13); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary?0xFFE7E8EB:0xFF303238)); b.setTextColor(primary?0xFF17181A:0xFFE7E8EB); return b; }
    private TextView text(int size, boolean bold) { TextView v=new TextView(getContext()); v.setTextSize(size); v.setTextColor(0xFFE9EAED); if(bold)v.setTypeface(null,android.graphics.Typeface.BOLD); return v; }
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private GradientDrawable round(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}