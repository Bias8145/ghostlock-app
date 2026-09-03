package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Compact installation status summary driven directly by the live log.
 * Uses subtle state transitions only; there are no timers or fake progress values.
 */
public final class InstallationProgressView extends LinearLayout {
    private final TextView statusIcon;
    private final TextView statusTitle;
    private final TextView statusDetail;
    private int lastRunMarker = -1;
    private String lastTitle = "";
    private String lastDetail = "";

    public InstallationProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setClipChildren(false);
        setClipToPadding(false);

        TextView heading = new TextView(context);
        heading.setText("INSTALLATION");
        heading.setTextColor(context.getColor(R.color.text_secondary));
        heading.setTextSize(11);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LayoutParams headingParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        headingParams.bottomMargin = dp(8);
        addView(heading, headingParams);

        MaterialCardView card = new MaterialCardView(context);
        card.setCardBackgroundColor(context.getColor(R.color.surface_container_low));
        card.setCardElevation(0);
        card.setRadius(dp(20));
        card.setStrokeWidth(0);

        LinearLayout body = new LinearLayout(context);
        body.setOrientation(HORIZONTAL);
        body.setGravity(android.view.Gravity.CENTER_VERTICAL);
        body.setPadding(dp(16), dp(12), dp(16), dp(12));

        statusIcon = new TextView(context);
        statusIcon.setTextColor(context.getColor(R.color.text_secondary));
        statusIcon.setTextSize(14);
        statusIcon.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusIcon.setGravity(android.view.Gravity.CENTER);
        LayoutParams iconParams = new LayoutParams(dp(24), dp(24));
        iconParams.rightMargin = dp(10);
        body.addView(statusIcon, iconParams);

        LinearLayout text = new LinearLayout(context);
        text.setOrientation(VERTICAL);
        text.setGravity(android.view.Gravity.CENTER_VERTICAL);

        statusTitle = new TextView(context);
        statusTitle.setTextColor(context.getColor(R.color.text_primary));
        statusTitle.setTextSize(14);
        statusTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        text.addView(statusTitle);

        statusDetail = new TextView(context);
        statusDetail.setTextColor(context.getColor(R.color.text_secondary));
        statusDetail.setTextSize(12);
        LayoutParams detailParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dp(3);
        text.addView(statusDetail, detailParams);

        body.addView(text, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        card.addView(body);
        LayoutParams cardParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        addView(card, cardParams);
        resetStatus();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    private void bindLog() {
        TextView log = getRootView().findViewById(R.id.logView);
        if (log == null) return;
        updateFromLog(log.getText() == null ? "" : log.getText().toString());
        log.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFromLog(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void updateFromLog(String log) {
        String s = log.toLowerCase(Locale.ROOT);
        int startMarker = s.lastIndexOf("==== start ====");
        if (startMarker < 0) {
            resetStatus();
            return;
        }
        if (startMarker != lastRunMarker) {
            lastRunMarker = startMarker;
        }

        String currentRun = s.substring(startMarker);
        String[] lines = currentRun.split("\\n");
        String latest = "";
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].trim().isEmpty()) {
                latest = lines[i].trim();
                break;
            }
        }

        if (latest.contains("exit code=0")) {
            setStatus("Installation completed", "GhostLock executed successfully. Exit code: 0", "✓");
            return;
        }
        if (containsAny(latest, "error:", "failed", "unsupported", "exit code=137", "exit code=-1")) {
            setStatus("Installation failed", latest, "×");
            return;
        }

        String title = "Running installation";
        String detail = latest.isEmpty() ? "Waiting for installation output..." : latest;
        if (containsAny(latest, "binary ready", "ksud ready")) {
            title = "Checking manager";
        } else if (containsAny(latest, "kernel", "uname", "supported kernel")) {
            title = "Checking kernel";
        } else if (containsAny(latest, "offset", "pselect", "kallsyms", "phys", "init_task", "security_hook")) {
            title = "Resolving kernel offsets";
        } else if (containsAny(latest, "running ghostlock", "preparing", "prepare")) {
            title = "Executing GhostLock";
        } else if (containsAny(latest, "execution", "exit code=")) {
            title = "Verifying result";
        }
        setStatus(title, detail, "●");
    }

    private void setStatus(String title, String detail, String icon) {
        boolean changed = !title.equals(lastTitle) || !icon.equals(statusIcon.getText().toString());
        statusTitle.setText(title);
        statusDetail.setText(detail);
        statusIcon.setText(icon);

        if (changed && isAttachedToWindow()) {
            animateState();
        }
        lastTitle = title;
        lastDetail = detail;
    }

    private void animateState() {
        statusIcon.animate().cancel();
        statusTitle.animate().cancel();
        statusDetail.animate().cancel();

        statusIcon.setAlpha(0.35f);
        statusIcon.setScaleX(0.82f);
        statusIcon.setScaleY(0.82f);
        statusTitle.setAlpha(0.55f);
        statusDetail.setAlpha(0.55f);

        statusIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).start();
        statusTitle.animate().alpha(1f).setDuration(180).start();
        statusDetail.animate().alpha(1f).setDuration(220).start();
    }

    public void resetStatus() {
        lastRunMarker = -1;
        lastTitle = "";
        lastDetail = "";
        setStatus("Ready to run", "Run GhostLock to begin installation.", "○");
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
