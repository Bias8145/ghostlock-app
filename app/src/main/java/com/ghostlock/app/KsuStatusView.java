package com.ghostlock.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small non-invasive status indicator for the installed KSU manager. */
public class KsuStatusView extends TextView {
    private static final String KSU = "me.weishu.kernelsu";
    private static final String RESUKISU = "com.resukisu.resukisu";
    private static final String SUPER_MANAGER = "com.kowx712.supermanager";

    public KsuStatusView(Context context) { super(context); init(); }
    public KsuStatusView(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public KsuStatusView(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        setTextSize(12);
        setOnClickListener(v -> showModderAbout());
        refreshStatus();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshStatus();
    }

    public void refreshStatus() {
        String name = installedManager();
        String status = name == null ? "Uninstalled" : "Installed";
        setText("Manager:  " + (name == null ? "—" : name) + "  |  Status:  " + status);
        setTextColor(getResources().getColor(name == null ? R.color.text_secondary : R.color.status_success));
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

    private void showModderAbout() {
        Context context = getContext();
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        content.setPadding(pad, dp(4), pad, 0);

        TextView description = new TextView(context);
        description.setText(R.string.about_modder_message);
        description.setTextColor(getResources().getColor(R.color.text_secondary));
        description.setTextSize(12);
        description.setLineSpacing(dp(2), 1f);
        content.addView(description);

        LinearLayout links = new LinearLayout(context);
        links.setGravity(Gravity.CENTER);
        links.setOrientation(LinearLayout.HORIZONTAL);
        links.setPadding(0, dp(14), 0, dp(4));

        TextView telegram = link("  Telegram");
        telegram.setOnClickListener(v -> openLink("https://t.me/VOLD_NAMESPACE"));
        links.addView(telegram, new LinearLayout.LayoutParams(0, dp(44), 1));

        TextView github = link("  GitHub");
        github.setOnClickListener(v -> openLink("https://github.com/Bias8145"));
        LinearLayout.LayoutParams githubParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        githubParams.setMarginStart(dp(12));
        links.addView(github, githubParams);
        content.addView(links);

        new AlertDialog.Builder(context)
                .setTitle(R.string.about_modder_title)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private TextView link(String label) {
        TextView view = new TextView(getContext());
        view.setText(label);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(13);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.accent));
        view.setBackgroundResource(android.R.drawable.btn_default);
        return view;
    }

    private void openLink(String url) {
        try { getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception ignored) { }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
