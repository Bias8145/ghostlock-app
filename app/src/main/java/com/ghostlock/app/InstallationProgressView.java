package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Compact installation status summary driven directly by the live log.
 * No timers or decorative progress animation are used.
 */
public final class InstallationProgressView extends LinearLayout {
    private final TextView statusTitle;
    private final TextView statusDetail;
    private int lastRunMarker = -1;

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
        body.setOrientation(VERTICAL);
        body.setPadding(dp(16), dp(12), dp(16), dp(12));

        statusTitle = new TextView(context);
        statusTitle.setTextColor(context.getColor(R.color.text_primary));
        statusTitle.setTextSize(14);
        statusTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        body.addView(statusTitle);

        statusDetail = new TextView(context);
        statusDetail.setTextColor(context.getColor(R.color.text_secondary));
        statusDetail.setTextSize(12);
        LayoutParams detailParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dp(3);
        body.addView(statusDetail, detailParams);

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
            setStatus("Installation completed", "GhostLock executed successfully. Exit code: 0");
            return;
        }
        if (containsAny(latest, "error:", "failed", "unsupported", "exit code=137", "exit code=-1")) {
            setStatus("Installation failed", latest);
            return;
        }

        String title = "Running installation";
        String detail = latest.isEmpty() ? "Waiting for installation output..." : latest;
        if (containsAny(latest, "binary ready", "ksud ready")) {
            title = "Checking manager";
            detail = latest;
        } else if (containsAny(latest, "kernel", "uname", "supported kernel")) {
            title = "Checking kernel";
            detail = latest;
        } else if (containsAny(latest, "offset", "pselect", "kallsyms", "phys", "init_task", "security_hook")) {
            title = "Resolving kernel offsets";
            detail = latest;
        } else if (containsAny(latest, "running ghostlock", "preparing", "prepare")) {
            title = "Executing GhostLock";
            detail = latest;
        } else if (containsAny(latest, "execution", "exit code=")) {
            title = "Verifying result";
            detail = latest;
        }
        setStatus(title, detail);
    }

    private void setStatus(String title, String detail) {
        statusTitle.setText(title);
        statusDetail.setText(detail);
    }

    public void resetStatus() {
        lastRunMarker = -1;
        setStatus("Ready to run", "Run GhostLock to begin installation.");
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
