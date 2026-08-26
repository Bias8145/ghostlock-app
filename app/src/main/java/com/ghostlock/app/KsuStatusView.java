package com.ghostlock.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/** Small non-invasive status indicator for the installed KSU manager. */
public class KsuStatusView extends TextView {
    private static final String KSU = "me.weishu.kernelsu";
    private static final String RESUKISU = "com.resukisu.resukisu";
    private static final String SUPER_MANAGER = "com.kowx712.supermanager";

    public KsuStatusView(Context context) {
        super(context);
        init();
    }

    public KsuStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KsuStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        setTextSize(12);
        refreshStatus();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshStatus();
    }

    public void refreshStatus() {
        String name = installedManager();
        if (name == null) {
            setText("KSU manager  ·  Not installed");
            setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            setText("KSU manager  ·  " + name + " installed");
            setTextColor(getResources().getColor(R.color.status_success));
        }
    }

    private String installedManager() {
        PackageManager pm = getContext().getPackageManager();
        if (isInstalled(pm, RESUKISU)) return "ReSukiSU";
        if (isInstalled(pm, KSU)) return "KernelSU";
        if (isInstalled(pm, SUPER_MANAGER)) return "KSU Manager";
        return null;
    }

    private boolean isInstalled(PackageManager pm, String packageName) {
        try {
            pm.getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
