package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.mikepenz.iconics.IconicsDrawable;

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
    private static final long ROW_ANIMATION_MS = 240L;
    private static final long STATUS_ANIMATION_MS = 180L;

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
        overallStatus = text("Ready", 13, R.color.text_secondary, Typeface.BOLD);
        heading.addView(overallStatus, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(heading, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        steps = new LinearLayout(context);
        steps.setOrientation(VERTICAL);
        LayoutParams stepsParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        stepsParams.topMargin = dp(8);
        addView(steps, stepsParams);
        resetStatus();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    @Override protected void onDetachedFromWindow() {
        unbindLog();
        super.onDetachedFromWindow();
    }

    private void bindLog() {
        TextView log = getRootView().findViewById(R.id.logView);
        if (log == null || watcher != null) return;
        watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateFromLog(s == null ? "" : s.toString()); }
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
        if (start < 0) { resetStatus(); return; }
        if (start != lastRunMarker) lastRunMarker = start;

        String run = s.substring(start);
        String latest = latestLine(run);
        boolean success = run.contains("exit code=0");
        boolean processStarted = run.contains("running ghostlock") || run.contains("ghostlock-reader");
        boolean processFinished = run.contains("exit code=");
        boolean failed = containsAny(run, "error:", "fatal:", "segmentation fault", "exit code=137", "exit code=-1", "ksud not found", "app not installed");

        List<Step> state = new ArrayList<>();
        state.add(new Step("Prepare", "Preparing GhostLock runtime and native payload."));
        state.add(new Step("Check manager", "Locating a compatible KernelSU manager and ksud."));
        state.add(new Step("Check kernel", "Checking the running kernel and supported offsets."));
        state.add(new Step("Execute", "Running the GhostLock native operation."));
        state.add(new Step("Verify", "Waiting for the real process result."));

        if (run.contains("binary ready")) state.get(0).state = STATE_DONE;
        if (run.contains("ksud ready")) state.get(1).state = STATE_DONE;
        else if (run.contains("ksud not found") || run.contains("app not installed")) state.get(1).state = STATE_FAILED;
        if (containsAny(run, "supported kernel", "kernel supported", "offsets loaded", "offsets matched", "offsets verified")) state.get(2).state = STATE_DONE;
        if (processStarted) state.get(3).state = processFinished ? STATE_DONE : STATE_RUNNING;
        if (success) state.get(4).state = STATE_DONE;
        else if (failed) {
            int current = firstActive(state);
            if (current >= 0) state.get(current).state = STATE_FAILED;
        }
        for (int i = 1; i < state.size(); i++) {
            if (state.get(i).state == STATE_RUNNING || state.get(i).state == STATE_DONE) {
                for (int j = 0; j < i; j++) if (state.get(j).state == STATE_PENDING) state.get(j).state = STATE_DONE;
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
        } else overallStatus.setText(status);
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
        overallStatus.animate().alpha(1f).translationY(0f).setDuration(STATUS_ANIMATION_MS).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void render(List<Step> state, String latest, boolean animate) {
        steps.removeAllViews();
        for (int i = 0; i < state.size(); i++) {
            Step step = state.get(i);
            MaterialCardView card = new MaterialCardView(getContext());
            card.setCardBackgroundColor(getContext().getColor(R.color.surface_container_low));
            card.setCardElevation(0f);
            card.setRadius(dp(16));
            card.setStrokeWidth(dp(1));
            card.setStrokeColor(getContext().getColor(step.state == STATE_RUNNING ? R.color.accent : R.color.divider));

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(11), dp(12), dp(11));

            TextView icon = new TextView(getContext());
            icon.setGravity(Gravity.CENTER);
            icon.setIncludeFontPadding(false);
            icon.setContentDescription(step.name);
            icon.setAlpha(step.state == STATE_PENDING ? 0.34f : 1f);
            icon.setCompoundDrawablesWithIntrinsicBounds(createStepIcon(step, 21), null, null, null);
            row.addView(icon, new LayoutParams(dp(32), dp(32)));

            LinearLayout body = new LinearLayout(getContext());
            body.setOrientation(VERTICAL);
            LayoutParams bodyParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
            bodyParams.leftMargin = dp(11);
            row.addView(body, bodyParams);

            TextView name = text(step.name, 14, step.state == STATE_RUNNING ? R.color.accent : R.color.text_primary, Typeface.BOLD);
            body.addView(name);

            TextView detail = text(step.detail, 11, R.color.text_secondary, Typeface.NORMAL);
            LayoutParams detailParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            detailParams.topMargin = dp(4);
            body.addView(detail, detailParams);

            if (step.state == STATE_RUNNING && latest != null && !latest.isEmpty()) {
                TextView live = text(latest, 10, R.color.text_secondary, Typeface.NORMAL);
                live.setMaxLines(2);
                LayoutParams liveParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                liveParams.topMargin = dp(6);
                body.addView(live, liveParams);
            }

            card.addView(row, new MaterialCardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            LayoutParams cardParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dp(i == state.size() - 1 ? 4 : 8);
            steps.addView(card, cardParams);

            if (animate) {
                card.setAlpha(0f);
                card.setTranslationY(dp(4));
                card.animate().alpha(1f).translationY(0f).setStartDelay(Math.min(i * 20L, 80L)).setDuration(ROW_ANIMATION_MS).setInterpolator(new DecelerateInterpolator()).start();
            }
        }
    }

    private Drawable createStepIcon(Step step, int sizeDp) {
        String icon;
        switch (step.name) {
            case "Prepare": icon = "faw-box-open"; break;
            case "Check manager": icon = "faw-user-shield"; break;
            case "Check kernel": icon = "faw-microchip"; break;
            case "Execute": icon = "faw-terminal"; break;
            case "Verify": icon = "faw-shield-alt"; break;
            default: icon = "faw-circle"; break;
        }
        int color;
        switch (step.state) {
            case STATE_DONE: color = getContext().getColor(R.color.status_success); break;
            case STATE_RUNNING: color = getContext().getColor(R.color.accent); break;
            case STATE_FAILED: color = getContext().getColor(R.color.status_error); break;
            default: color = getContext().getColor(R.color.text_secondary); break;
        }
        IconicsDrawable drawable = new IconicsDrawable(getContext(), icon);
        drawable.setTint(color);
        int sizePx = dp(sizeDp);
        drawable.setBounds(0, 0, sizePx, sizePx);
        return drawable;
    }

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
