package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GitHub-Actions-style installation timeline driven only by the real live log.
 * No timers, fake percentages, or synthetic completion states are used.
 */
public final class InstallationProgressView extends LinearLayout {
    private static final int STATE_PENDING = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_DONE = 2;
    private static final int STATE_FAILED = 3;
    private static final long ROW_ANIMATION_MS = 220L;
    private static final long STATUS_ANIMATION_MS = 160L;

    private final LinearLayout steps;
    private final TextView overallStatus;
    private TextWatcher watcher;
    private int lastRunMarker = -1;
    private String lastStateSignature = "";

    public InstallationProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setClipChildren(false);
        setClipToPadding(false);

        LinearLayout heading = new LinearLayout(context);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text("INSTALLATION", 14, R.color.text_secondary, Typeface.BOLD);
        heading.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        overallStatus = text("Ready", 14, R.color.text_secondary, Typeface.NORMAL);
        overallStatus.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(overallStatus, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(heading, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        steps = new LinearLayout(context);
        steps.setOrientation(VERTICAL);
        LayoutParams stepsParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        stepsParams.topMargin = dp(13);
        addView(steps, stepsParams);

        resetStatus();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbindLog();
        super.onDetachedFromWindow();
    }

    private void bindLog() {
        TextView log = getRootView().findViewById(R.id.logView);
        if (log == null || watcher != null) return;
        watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFromLog(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        log.addTextChangedListener(watcher);
        updateFromLog(log.getText() == null ? "" : log.getText().toString());
    }

    private void unbindLog() {
        if (watcher == null) return;
        TextView log = getRootView().findViewById(R.id.logView);
        if (log != null) log.removeTextChangedListener(watcher);
        watcher = null;
    }

    private void updateFromLog(String log) {
        String s = log.toLowerCase(Locale.ROOT);
        int start = s.lastIndexOf("==== start ====");
        if (start < 0) {
            resetStatus();
            return;
        }
        if (start != lastRunMarker) lastRunMarker = start;

        String run = s.substring(start);
        String latest = latestLine(run);
        boolean success = run.contains("exit code=0");
        boolean processStarted = run.contains("running ghostlock") || run.contains("ghostlock-reader");
        boolean processFinished = run.contains("exit code=");
        boolean failed = containsAny(run,
                "error:",
                "fatal:",
                "segmentation fault",
                "exit code=137",
                "exit code=-1",
                "ksud not found",
                "app not installed");

        List<Step> state = new ArrayList<>();
        state.add(new Step("Prepare", "Preparing GhostLock runtime and native payload."));
        state.add(new Step("Check manager", "Locating a compatible KernelSU manager and ksud."));
        state.add(new Step("Check kernel", "Checking the running kernel and supported offsets."));
        state.add(new Step("Execute", "Running the GhostLock native operation."));
        state.add(new Step("Verify", "Waiting for the real process result."));

        if (run.contains("binary ready")) state.get(0).state = STATE_DONE;

        if (run.contains("ksud ready")) {
            state.get(1).state = STATE_DONE;
        } else if (run.contains("ksud not found") || run.contains("app not installed")) {
            state.get(1).state = STATE_FAILED;
        }

        if (containsAny(run,
                "supported kernel",
                "kernel supported",
                "offsets loaded",
                "offsets matched",
                "offsets verified")) {
            state.get(2).state = STATE_DONE;
        }

        if (processStarted) state.get(3).state = processFinished ? STATE_DONE : STATE_RUNNING;

        if (success) {
            state.get(4).state = STATE_DONE;
        } else if (failed) {
            int current = firstActive(state);
            if (current >= 0) state.get(current).state = STATE_FAILED;
        }

        for (int i = 1; i < state.size(); i++) {
            if (state.get(i).state == STATE_RUNNING || state.get(i).state == STATE_DONE) {
                for (int j = 0; j < i; j++) {
                    if (state.get(j).state == STATE_PENDING) state.get(j).state = STATE_DONE;
                }
            }
        }

        if (!success && !failed && processStarted && !processFinished) state.get(3).state = STATE_RUNNING;

        String status;
        if (success) status = "Completed";
        else if (failed) status = "Failed";
        else if (processStarted || hasActiveStage(state)) status = "Running";
        else status = "Preparing";

        boolean stateChanged = !stateSignature(state, status).equals(lastStateSignature);
        if (stateChanged) {
            animateStatusChange(status);
            lastStateSignature = stateSignature(state, status);
        } else {
            overallStatus.setText(status);
        }

        render(state, latest, stateChanged);
    }

    private boolean hasActiveStage(List<Step> state) {
        for (Step step : state) if (step.state == STATE_RUNNING) return true;
        return false;
    }

    private int firstActive(List<Step> state) {
        for (int i = 0; i < state.size(); i++) if (state.get(i).state == STATE_PENDING) return i;
        return -1;
    }

    private String stateSignature(List<Step> state, String status) {
        StringBuilder signature = new StringBuilder(status).append(':');
        for (Step step : state) signature.append(step.state).append(',');
        return signature.toString();
    }

    private void animateStatusChange(String status) {
        overallStatus.animate().cancel();
        overallStatus.setAlpha(0.55f);
        overallStatus.setTranslationY(dp(2));
        overallStatus.setText(status);
        overallStatus.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(STATUS_ANIMATION_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void render(List<Step> state, String latest, boolean animate) {
        steps.removeAllViews();
        for (int i = 0; i < state.size(); i++) {
            Step step = state.get(i);
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.TOP);
            row.setPadding(0, dp(3), 0, dp(i == state.size() - 1 ? 3 : 12));

            LinearLayout rail = new LinearLayout(getContext());
            rail.setOrientation(VERTICAL);
            rail.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView marker = text(marker(step.state), 24, markerColor(step.state), Typeface.BOLD);
            marker.setGravity(Gravity.CENTER);
            rail.addView(marker, new LayoutParams(dp(32), dp(32)));

            if (i < state.size() - 1) {
                View connector = new View(getContext());
                connector.setBackgroundColor(getContext().getColor(connectorColor(step.state)));
                LayoutParams connectorParams = new LayoutParams(dp(2), dp(30));
                connectorParams.topMargin = dp(2);
                rail.addView(connector, connectorParams);
            }

            LayoutParams railParams = new LayoutParams(dp(32), LayoutParams.MATCH_PARENT);
            railParams.rightMargin = dp(12);
            row.addView(rail, railParams);

            LinearLayout body = new LinearLayout(getContext());
            body.setOrientation(VERTICAL);
            TextView name = text(step.name, 16, step.state == STATE_RUNNING ? R.color.accent : R.color.text_primary, Typeface.BOLD);
            body.addView(name);

            TextView detail = text(step.detail, 14, R.color.text_secondary, Typeface.NORMAL);
            LayoutParams detailParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            detailParams.topMargin = dp(4);
            body.addView(detail, detailParams);

            if (step.state == STATE_RUNNING && latest != null && !latest.isEmpty()) {
                TextView live = text(latest, 13, R.color.text_secondary, Typeface.NORMAL);
                live.setMaxLines(4);
                LayoutParams liveParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                liveParams.topMargin = dp(7);
                body.addView(live, liveParams);
            }

            row.addView(body, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            steps.addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            if (animate) {
                row.setAlpha(0f);
                row.setTranslationY(dp(6));
                row.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(Math.min(i * 22L, 88L))
                        .setDuration(ROW_ANIMATION_MS)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    private int connectorColor(int state) { return state == STATE_DONE ? R.color.status_success : R.color.divider; }

    private String latestLine(String run) {
        String[] lines = run.split("\\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.equals("==== start ====")) return line;
        }
        return "";
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private String marker(int state) {
        switch (state) {
            case STATE_DONE: return "✓";
            case STATE_RUNNING: return "●";
            case STATE_FAILED: return "×";
            default: return "○";
        }
    }

    private int markerColor(int state) {
        switch (state) {
            case STATE_DONE: return R.color.status_success;
            case STATE_RUNNING: return R.color.accent;
            case STATE_FAILED: return R.color.status_error;
            default: return R.color.text_secondary;
        }
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(getContext().getColor(color));
        view.setTypeface(Typeface.DEFAULT, style);
        view.setIncludeFontPadding(false);
        return view;
    }

    public void resetStatus() {
        lastRunMarker = -1;
        lastStateSignature = "";
        overallStatus.setText("Ready");
        List<Step> state = new ArrayList<>();
        state.add(new Step("Prepare", "Waiting for GhostLock to start."));
        state.add(new Step("Check manager", "Waiting"));
        state.add(new Step("Check kernel", "Waiting"));
        state.add(new Step("Execute", "Waiting"));
        state.add(new Step("Verify", "Waiting"));
        lastStateSignature = stateSignature(state, "Ready");
        render(state, "", false);
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private static final class Step {
        final String name;
        final String detail;
        int state = STATE_PENDING;
        Step(String name, String detail) { this.name = name; this.detail = detail; }
    }
}
