package com.ghostlock.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.TextView;

/** Compact manager/status indicator. */
public class KsuStatusView extends TextView {
    private static final String KSU = "me.weishu.kernelsu";
    private static final String RESUKISU = "com.resukisu.resukisu";
    private static final String SUPER_MANAGER = "com.kowx712.supermanager";

    public KsuStatusView(Context context) { super(context); init(); }
    public KsuStatusView(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public KsuStatusView(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setTypeface(Typeface.DEFAULT);
        setTextSize(12);
        refreshStatus();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshStatus();
    }

    public void refreshStatus() {
        String manager = installedManager();
        boolean installed = manager != null;
        String status = installed ? "Installed" : "Uninstalled";
        String text = "Manager:  " + (installed ? manager : "—") + "  |  Status:  " + status;
        SpannableString styled = new SpannableString(text);
        int statusStart = text.lastIndexOf(status);
        styled.setSpan(new ForegroundColorSpan(getResources().getColor(
                installed ? R.color.status_success : R.color.status_error)),
                statusStart, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(styled);
        setTextColor(getResources().getColor(R.color.text_primary));
    }

    private String installedManager() {
        PackageManager pm = getContext().getPackageManager();
        if (isInstalled(pm, RESUKISU)) return "ReSukiSU";
        if (isInstalled(pm, KSU)) return "KernelSU";
        if (isInstalled(pm, SUPER_MANAGER)) return "KSU Manager";
        return null;
    }

    private boolean isInstalled(PackageManager pm, String packageName) {
        try { pm.getApplicationInfo(packageName, 0); return true; }
        catch (PackageManager.NameNotFoundException ignored) { return false; }
    }
}
