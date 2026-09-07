package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Compact installation status driven directly by the live log.
 * During execution only one semantic stage is visible; after a completed run,
 * a concise summary and expandable timeline are shown.
 */
public final class InstallationProgressView extends LinearLayout {
    private static final int STATE_READY = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_SUCCESS = 2;
    private static final int STATE_FAILED = 3;

    private static final int ERROR_COLOR = 0xFFFF6B6B;
    private static final int SUCCESS_COLOR = 0xFF5FD68A;
    private static final int MUTED_COLOR = 0xFF8B8F98;
    private static final float ACTIVE_ALPHA = 0.45f;
    private static final String PREFS = "ghostlock_install_summary";
    private static final String PREF_RESULT = "result";
    private static final String PREF_KERNEL = "kernel";
    private static final String PREF_MANAGER = "manager";
    private static final String PREF_DURATION = "duration";
    private static final String PREF_TIMELINE = "timeline";

    private final ImageView statusIcon;
    private final TextView statusTitle;
    private final LinearLayout postRun;
    private final LinearLayout summaryBox;
    private final LinearLayout timelineBox;
    private final TextView summaryResult;
    private final TextView summaryKernel;
    private final TextView summaryManager;
    private final TextView summaryDuration;
    private final TextView timelineToggle;
    private final LinearLayout timelineRows;
    private final MaterialButton viewLogButton;
    private final LinkedHashMap<String, Stage> observedStages = new LinkedHashMap<>();
    private ValueAnimator breathingAnimator;
    private TextWatcher logWatcher;
    private int lastRunMarker = -1;
    private int lastState = -1;
    private int lastIconRes = -1;
    private int currentStageIconRes = R.drawable.ic_install_box_open;
    private String currentStageTitle = "Preparing";
    private long runStartedAt;
    private boolean timelineExpanded;
    private boolean hasTerminalResult;

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
        headingParams.bottomMargin = dp(7);
        addView(heading, headingParams);

        LinearLayout body = new LinearLayout(context);
        body.setOrientation(HORIZONTAL);
        body.setGravity(Gravity.CENTER_VERTICAL);
        body.setPadding(0, dp(2), 0, dp(2));

        statusIcon = new ImageView(context);
        statusIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LayoutParams iconParams = new LayoutParams(dp(24), dp(24));
        iconParams.rightMargin = dp(10);
        body.addView(statusIcon, iconParams);

        statusTitle = new TextView(context);
        statusTitle.setTextColor(context.getColor(R.color.text_primary));
        statusTitle.setTextSize(14);
        statusTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusTitle.setGravity(Gravity.CENTER_VERTICAL);
        body.addView(statusTitle, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        addView(body, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        postRun = new LinearLayout(context);
        postRun.setOrientation(VERTICAL);
        postRun.setVisibility(GONE);
        LayoutParams postParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        postParams.topMargin = dp(10);
        addView(postRun, postParams);

        summaryBox = createCard(context);
        summaryResult = addSummaryRow(context, summaryBox, "Result");
        summaryKernel = addSummaryRow(context, summaryBox, "Kernel");
        summaryManager = addSummaryRow(context, summaryBox, "Manager");
        summaryDuration = addSummaryRow(context, summaryBox, "Duration");
        postRun.addView(summaryBox);

        viewLogButton = new MaterialButton(context);
        viewLogButton.setText("View Log");
        viewLogButton.setAllCaps(false);
        viewLogButton.setTextSize(12);
        viewLogButton.setMinHeight(0);
        viewLogButton.setMinimumHeight(0);
        viewLogButton.setInsetTop(0);
        viewLogButton.setInsetBottom(0);
        viewLogButton.setPadding(dp(14), 0, dp(14), 0);
        viewLogButton.setTextColor(context.getColor(R.color.text_primary));
        viewLogButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.surface_container_low)));
        viewLogButton.setCornerRadius(dp(13));
        viewLogButton.setVisibility(GONE);
        LayoutParams logButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, dp(40));
        logButtonParams.topMargin = dp(8);
        postRun.addView(viewLogButton, logButtonParams);
        viewLogButton.setOnClickListener(v -> showLog());

        timelineBox = new LinearLayout(context);
        timelineBox.setOrientation(VERTICAL);
        timelineBox.setVisibility(GONE);
        postRun.addView(timelineBox, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        timelineToggle = new TextView(context);
        timelineToggle.setText("Installation timeline  ·  Show");
        timelineToggle.setTextColor(context.getColor(R.color.text_secondary));
        timelineToggle.setTextSize(12);
        timelineToggle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        timelineToggle.setGravity(Gravity.CENTER_VERTICAL);
        timelineToggle.setPadding(dp(2), dp(12), dp(2), dp(8));
        timelineToggle.setClickable(true);
        timelineToggle.setFocusable(true);
        timelineToggle.setOnClickListener(v -> toggleTimeline());
        timelineBox.addView(timelineToggle);

        timelineRows = new LinearLayout(context);
        timelineRows.setOrientation(VERTICAL);
        timelineRows.setVisibility(GONE);
        timelineBox.addView(timelineRows);

        loadLastRun();
        resetStatus();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    @Override protected void onDetachedFromWindow() {
        stopBreathing();
        TextView log = getRootView().findViewById(R.id.logView);
        if (log != null && logWatcher != null) {
            log.removeTextChangedListener(logWatcher);
        }
        logWatcher = null;
        super.onDetachedFromWindow();
    }

    private void bindLog() {
        TextView log = getRootView().findViewById(R.id.logView);
        if (log == null) return;
        updateFromLog(log.getText() == null ? "" : log.getText().toString());
        if (logWatcher != null) return;
        logWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFromLog(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        log.addTextChangedListener(logWatcher);
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
            runStartedAt = SystemClock.elapsedRealtime();
            observedStages.clear();
            currentStageTitle = "Preparing";
            currentStageIconRes = R.drawable.ic_install_box_open;
            hasTerminalResult = false;
            hidePostRun();
            setRunButtonState(STATE_RUNNING);
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
            finishRun(STATE_SUCCESS, currentRun);
            return;
        }
        if (containsAny(latest, "error:", "failed", "unsupported", "exit code=137", "exit code=-1")) {
            finishRun(STATE_FAILED, currentRun);
            return;
        }

        Stage stage = stageFor(latest);
        currentStageTitle = stage.title;
        currentStageIconRes = stage.iconRes;
        observedStages.put(stage.id, stage);
        setStatus(stage.title, stage.iconRes, STATE_RUNNING);
    }

    private Stage stageFor(String latest) {
        if (containsAny(latest, "running ghostlock", "preparing", "prepare", "starting")) {
            return new Stage("preparing", "Preparing", R.drawable.ic_install_box_open);
        }
        if (containsAny(latest, "binary ready", "ksud ready", "manager", "kernelsu", "resukisu")) {
            return new Stage("manager", "Detecting manager", R.drawable.ic_install_shield);
        }
        if (containsAny(latest, "device", "uname", "kernel", "supported kernel")) {
            return new Stage("device", "Checking device", R.drawable.ic_install_mobile);
        }
        if (containsAny(latest, "offset", "pselect", "kallsyms", "phys", "init_task", "security_hook")) {
            return new Stage("offsets", "Loading offsets", R.drawable.ic_install_file_code);
        }
        if (containsAny(latest, "applying", "configuration", "config", "writing", "write")) {
            return new Stage("config", "Applying configuration", R.drawable.ic_install_gears);
        }
        if (containsAny(latest, "execution", "exit code=", "verifying", "verify")) {
            return new Stage("verify", "Verifying installation", R.drawable.ic_install_circle_check);
        }
        return new Stage("preparing", "Preparing", R.drawable.ic_install_box_open);
    }

    private void finishRun(int state, String currentRun) {
        if (hasTerminalResult && lastState == state) return;
        hasTerminalResult = true;
        stopBreathing();
        if (state == STATE_SUCCESS) {
            observedStages.put("verify", new Stage("verify", "Verifying installation", R.drawable.ic_install_circle_check));
            setStatus("Installation completed", R.drawable.ic_install_circle_check, STATE_SUCCESS);
        } else {
            setStatus(currentStageTitle, currentStageIconRes, STATE_FAILED);
        }
        long duration = runStartedAt > 0 ? Math.max(0, SystemClock.elapsedRealtime() - runStartedAt) : 0;
        String manager = detectManagerName();
        String kernel = System.getProperty("os.version", "unknown");
        saveLastRun(state == STATE_SUCCESS ? "Success" : "Failed", kernel, manager, duration, timelineString());
        renderPostRun(state, kernel, manager, duration, currentRun);
        setRunButtonState(state);
    }

    private void renderPostRun(int state, String kernel, String manager, long duration, String currentRun) {
        postRun.setVisibility(VISIBLE);
        summaryResult.setText(state == STATE_SUCCESS ? "Success" : "Failed");
        summaryResult.setTextColor(getContext().getColor(state == STATE_SUCCESS ? R.color.status_success : R.color.status_error));
        summaryKernel.setText(kernel);
        summaryManager.setText(manager);
        summaryDuration.setText(formatDuration(duration));
        viewLogButton.setVisibility(state == STATE_FAILED ? VISIBLE : GONE);
        rebuildTimeline();
    }

    private String detectManagerName() {
        try {
            ManagerCompatibility.ManagerInfo info = ManagerCompatibility.detectManager(getContext());
            if (info != null && info.installed && info.name != null && !info.name.isEmpty()) {
                return info.name;
            }
        } catch (Throwable ignored) {
        }
        return "Not detected";
    }

    private void rebuildTimeline() {
        timelineRows.removeAllViews();
        for (Stage stage : observedStages.values()) {
            timelineRows.addView(timelineRow(stage, true));
        }
        timelineBox.setVisibility(observedStages.isEmpty() ? GONE : VISIBLE);
        timelineToggle.setText("Installation timeline  ·  " + (timelineExpanded ? "Hide" : "Show"));
        timelineRows.setVisibility(timelineExpanded ? VISIBLE : GONE);
    }

    private View timelineRow(Stage stage, boolean completed) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(3), 0, dp(3));

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(completed ? R.drawable.ic_install_circle_check : stage.iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LayoutParams ip = new LayoutParams(dp(20), dp(20));
        ip.rightMargin = dp(10);
        row.addView(icon, ip);

        TextView title = new TextView(getContext());
        title.setText(stage.title);
        title.setTextColor(getContext().getColor(R.color.text_primary));
        title.setTextSize(12);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        return row;
    }

    private void toggleTimeline() {
        timelineExpanded = !timelineExpanded;
        timelineRows.setVisibility(timelineExpanded ? VISIBLE : GONE);
        timelineToggle.setText("Installation timeline  ·  " + (timelineExpanded ? "Hide" : "Show"));
    }

    private void showLog() {
        View root = getRootView();
        ScrollView scroll = root.findViewById(R.id.logScroll);
        TextView log = root.findViewById(R.id.logView);
        if (scroll != null) {
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            scroll.requestFocus();
        }
        if (log != null) log.requestFocus();
    }

    private void setRunButtonState(int state) {
        View button = getRootView().findViewById(R.id.runButton);
        if (!(button instanceof Button)) return;
        Button run = (Button) button;
        run.setEnabled(state != STATE_RUNNING);
        switch (state) {
            case STATE_RUNNING:
                run.setText("Running…");
                break;
            case STATE_SUCCESS:
                run.setText("Run Again");
                break;
            case STATE_FAILED:
                run.setText("Retry");
                break;
            default:
                run.setText("Run GhostLock");
                break;
        }
    }

    private void setStatus(String title, int iconRes, int state) {
        boolean changed = iconRes != lastIconRes || state != lastState || !title.equals(statusTitle.getText().toString());
        statusTitle.setText(title);
        setIcon(iconRes, state);
        lastIconRes = iconRes;
        lastState = state;
        if (changed && isAttachedToWindow()) animateState(state);
        if (state == STATE_RUNNING) startBreathing(); else stopBreathing();
    }

    private void setIcon(int iconRes, int state) {
        if (statusIcon.getDrawable() == null || iconRes != lastIconRes) statusIcon.setImageResource(iconRes);
        Drawable drawable = statusIcon.getDrawable();
        if (drawable != null) {
            int color = state == STATE_FAILED ? ERROR_COLOR : getContext().getColor(state == STATE_READY ? R.color.text_secondary : R.color.text_primary);
            drawable.setTint(color);
        }
        statusIcon.setAlpha(state == STATE_RUNNING ? ACTIVE_ALPHA : 1f);
    }

    private void animateState(int state) {
        stopBreathing();
        statusIcon.animate().cancel();
        statusTitle.animate().cancel();
        statusIcon.setAlpha(0.25f);
        statusIcon.setScaleX(0.84f);
        statusIcon.setScaleY(0.84f);
        statusTitle.setAlpha(0.55f);
        statusIcon.animate().alpha(state == STATE_RUNNING ? ACTIVE_ALPHA : 1f).scaleX(1f).scaleY(1f).setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        if (state == STATE_RUNNING) startBreathing();
                    }
                }).start();
        statusTitle.animate().alpha(1f).setDuration(180).start();
    }

    private void startBreathing() {
        if (!isAttachedToWindow() || breathingAnimator != null && breathingAnimator.isRunning()) return;
        breathingAnimator = ValueAnimator.ofFloat(1.0f, 1.04f, 1.0f);
        breathingAnimator.setDuration(1200);
        breathingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        breathingAnimator.addUpdateListener(animation -> {
            if (lastState != STATE_RUNNING) {
                stopBreathing();
                return;
            }
            float scale = (Float) animation.getAnimatedValue();
            statusIcon.setScaleX(scale);
            statusIcon.setScaleY(scale);
        });
        breathingAnimator.start();
    }

    private void stopBreathing() {
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
            breathingAnimator = null;
        }
        statusIcon.setScaleX(1f);
        statusIcon.setScaleY(1f);
    }

    public void resetStatus() {
        stopBreathing();
        lastRunMarker = -1;
        lastIconRes = -1;
        lastState = STATE_READY;
        currentStageTitle = "Preparing";
        currentStageIconRes = R.drawable.ic_install_box_open;
        observedStages.clear();
        hasTerminalResult = false;
        setStatus("Ready to run", R.drawable.ic_install_box_open, STATE_READY);
        if (getStoredResult() != null) {
            postRun.setVisibility(VISIBLE);
            timelineBox.setVisibility(GONE);
        } else {
            hidePostRun();
        }
        setRunButtonState(STATE_READY);
    }

    private void hidePostRun() {
        postRun.setVisibility(GONE);
        viewLogButton.setVisibility(GONE);
        timelineBox.setVisibility(GONE);
        timelineRows.setVisibility(GONE);
        timelineExpanded = false;
    }

    private void loadLastRun() {
        SharedPreferences p = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String result = p.getString(PREF_RESULT, null);
        if (result == null) return;
        summaryResult.setText(result);
        summaryResult.setTextColor(getContext().getColor("Success".equals(result) ? R.color.status_success : R.color.status_error));
        summaryKernel.setText(p.getString(PREF_KERNEL, "unknown"));
        summaryManager.setText(p.getString(PREF_MANAGER, "Not detected"));
        summaryDuration.setText(formatDuration(p.getLong(PREF_DURATION, 0)));
        restoreTimeline(p.getString(PREF_TIMELINE, ""));
        postRun.setVisibility(VISIBLE);
        viewLogButton.setVisibility("Failed".equals(result) ? VISIBLE : GONE);
    }

    private String getStoredResult() {
        return getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREF_RESULT, null);
    }

    private void saveLastRun(String result, String kernel, String manager, long duration, String timeline) {
        getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(PREF_RESULT, result)
                .putString(PREF_KERNEL, kernel)
                .putString(PREF_MANAGER, manager)
                .putLong(PREF_DURATION, duration)
                .putString(PREF_TIMELINE, timeline)
                .apply();
    }

    private void restoreTimeline(String value) {
        observedStages.clear();
        if (value == null || value.isEmpty()) return;
        for (String id : value.split(",")) {
            Stage stage = stageById(id);
            if (stage != null) observedStages.put(id, stage);
        }
    }

    private String timelineString() {
        StringBuilder out = new StringBuilder();
        for (String id : observedStages.keySet()) {
            if (out.length() > 0) out.append(',');
            out.append(id);
        }
        return out.toString();
    }

    private Stage stageById(String id) {
        switch (id) {
            case "preparing": return new Stage("preparing", "Preparing", R.drawable.ic_install_box_open);
            case "manager": return new Stage("manager", "Detecting manager", R.drawable.ic_install_shield);
            case "device": return new Stage("device", "Checking device", R.drawable.ic_install_mobile);
            case "offsets": return new Stage("offsets", "Loading offsets", R.drawable.ic_install_file_code);
            case "config": return new Stage("config", "Applying configuration", R.drawable.ic_install_gears);
            case "verify": return new Stage("verify", "Verifying installation", R.drawable.ic_install_circle_check);
            default: return null;
        }
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(VERTICAL);
        card.setPadding(dp(12), dp(9), dp(12), dp(9));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(context.getColor(R.color.surface_container_low));
        bg.setCornerRadius(dp(16));
        card.setBackground(bg);
        return card;
    }

    private TextView addSummaryRow(Context context, LinearLayout parent, String label) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView left = new TextView(context);
        left.setText(label);
        left.setTextColor(context.getColor(R.color.text_secondary));
        left.setTextSize(11);
        row.addView(left, new LayoutParams(0, dp(28), 1f));
        TextView right = new TextView(context);
        right.setTextColor(context.getColor(R.color.text_primary));
        right.setTextSize(11);
        right.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        right.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        right.setMaxLines(1);
        row.addView(right, new LayoutParams(0, dp(28), 2f));
        parent.addView(row);
        return right;
    }

    private String formatDuration(long millis) {
        long seconds = Math.max(0, millis / 1000L);
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class Stage {
        final String id;
        final String title;
        final int iconRes;
        Stage(String id, String title, int iconRes) {
            this.id = id;
            this.title = title;
            this.iconRes = iconRes;
        }
    }
}
