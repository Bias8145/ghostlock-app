package com.ghostlock.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/** Compact installation progress driven by the existing live log. */
public final class InstallationProgressView extends LinearLayout {
    private static final int STAGES = 6;
    private static final long MIN_STAGE_DURATION_MS = 650L;
    private static final String[] ACTIVE = {
            "Detecting device", "Checking manager", "Checking kernel",
            "Resolving kernel offsets", "Executing GhostLock", "Verifying result"
    };
    private static final String[] DETAIL = {
            "Checking device and environment...", "Detecting a compatible manager...",
            "Detecting and validating kernel...", "Validating offsets for the detected kernel...",
            "Running GhostLock...", "Checking the final execution result..."
    };

    private final ProgressStrip strip;
    private final TextView statusTitle;
    private final TextView statusDetail;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int stage;
    private boolean failed;
    private boolean started;
    private long stageStartedAt;
    private long runToken;
    private ValueAnimator dotsAnimator;

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
        headingParams.bottomMargin = dp(12);
        addView(heading, headingParams);

        strip = new ProgressStrip(context);
        addView(strip, new LayoutParams(LayoutParams.MATCH_PARENT, dp(24)));

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
        detailParams.topMargin = dp(4);
        body.addView(statusDetail, detailParams);

        card.addView(body);
        LayoutParams cardParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(10);
        cardParams.bottomMargin = dp(12);
        addView(card, cardParams);
        updateText();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    @Override protected void onDetachedFromWindow() {
        stopAnimation();
        handler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
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
            resetIdle();
            return;
        }

        // Only consume the current run. This makes a second Run start at stage 0
        // even when the previous run remains visible in the live log.
        String currentRun = s.substring(startMarker);
        if (!started || currentRun.indexOf("==== start ====") == 0 && !hasCurrentRunState(currentRun)) {
            beginRun();
        }
        if (!started) return;

        String[] lines = currentRun.split("\\n");
        int requestedStage = 0;
        String latest = "";
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].trim().isEmpty()) { latest = lines[i].trim(); break; }
        }

        for (String line : lines) {
            String entry = line.trim();
            if (containsAny(entry, "binary ready", "ksud ready")) requestedStage = Math.max(requestedStage, 1);
            if (containsAny(entry, "kernel", "uname", "supported kernel")) requestedStage = Math.max(requestedStage, 2);
            if (containsAny(entry, "offset", "pselect", "kallsyms", "phys", "init_task", "security_hook")) requestedStage = Math.max(requestedStage, 3);
            if (containsAny(entry, "running ghostlock", "preparing", "prepare")) requestedStage = Math.max(requestedStage, 4);
            if (containsAny(entry, "exit code=", "execution")) requestedStage = Math.max(requestedStage, 5);
        }

        boolean error = containsAny(latest, "error:", "failed", "unsupported", "exit code=137", "exit code=-1");
        boolean success = latest.contains("exit code=0");
        if (success) {
            requestStage(5, false, false);
        } else if (error) {
            setProgress(Math.min(requestedStage, 5), false, true);
        } else {
            requestStage(Math.min(requestedStage, 5), true, false);
        }
    }

    private boolean hasCurrentRunState(String currentRun) {
        return currentRun.length() > "==== start ====".length();
    }

    private void beginRun() {
        runToken++;
        handler.removeCallbacksAndMessages(null);
        started = true;
        failed = false;
        stage = 0;
        stageStartedAt = System.currentTimeMillis();
        strip.invalidate();
        updateText();
        startAnimation();
    }

    private void resetIdle() {
        if (started) {
            runToken++;
            handler.removeCallbacksAndMessages(null);
        }
        started = false;
        failed = false;
        stage = 0;
        stopAnimation();
        strip.invalidate();
        updateText();
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private void requestStage(int requestedStage, boolean active, boolean error) {
        if (requestedStage <= stage || error) {
            setProgress(stage, active, error);
            return;
        }

        long elapsed = System.currentTimeMillis() - stageStartedAt;
        long delay = Math.max(0L, MIN_STAGE_DURATION_MS - elapsed);
        long token = runToken;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            if (token != runToken || !started) return;
            stage = Math.min(stage + 1, requestedStage);
            stageStartedAt = System.currentTimeMillis();
            strip.invalidate();
            updateText();
            if (active) startAnimation();
        }, delay);
    }

    private void setProgress(int newStage, boolean active, boolean error) {
        stage = Math.max(0, Math.min(STAGES - 1, newStage));
        failed = error;
        if (error) {
            handler.removeCallbacksAndMessages(null);
        }
        strip.invalidate();
        updateText();
        if (active && !error) startAnimation(); else stopAnimation();
    }

    private void updateText() {
        if (failed) {
            statusTitle.setText("Installation failed");
            statusDetail.setText(stage == 2 ? "The detected kernel is not supported." : "GhostLock exited with an error.");
        } else if (started && stage == STAGES - 1 && !dotsAnimatorRunning()) {
            statusTitle.setText("Installation completed");
            statusDetail.setText("GhostLock executed successfully.");
        } else {
            statusTitle.setText(ACTIVE[stage]);
            statusDetail.setText(DETAIL[stage]);
        }
    }

    private boolean dotsAnimatorRunning() {
        return dotsAnimator != null && dotsAnimator.isRunning();
    }

    private void startAnimation() {
        if (dotsAnimator != null && dotsAnimator.isRunning()) return;
        dotsAnimator = ValueAnimator.ofFloat(0f, 1f);
        dotsAnimator.setDuration(900);
        dotsAnimator.setRepeatCount(ValueAnimator.INFINITE);
        dotsAnimator.addUpdateListener(a -> strip.setPhase((float) a.getAnimatedValue()));
        dotsAnimator.start();
    }

    private void stopAnimation() {
        if (dotsAnimator != null) {
            dotsAnimator.cancel();
            dotsAnimator = null;
        }
        strip.setPhase(0f);
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private final class ProgressStrip extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint icon = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float phase;

        ProgressStrip(Context context) {
            super(context);
            icon.setStyle(Paint.Style.STROKE);
            icon.setStrokeWidth(dp(1.8f));
            icon.setStrokeCap(Paint.Cap.ROUND);
        }

        void setPhase(float value) { phase = value; invalidate(); }

        @Override protected void onDraw(Canvas canvas) {
            float y = getHeight() / 2f;
            float start = dp(8);
            float end = getWidth() - dp(8);
            float step = (end - start) / (STAGES - 1);

            for (int i = 0; i < STAGES - 1; i++) {
                float x1 = start + step * i + dp(10);
                float x2 = start + step * (i + 1) - dp(10);
                drawDots(canvas, x1, x2, y, i < stage, i == stage && started && !failed);
            }
            for (int i = 0; i < STAGES; i++) {
                float x = start + step * i;
                if (i < stage || (i == stage && stage == STAGES - 1 && started && !failed && !dotsAnimatorRunning())) drawCheck(canvas, x, y);
                else if (i == stage && failed) drawCross(canvas, x, y);
                else if (i == stage && started) drawActive(canvas, x, y);
                else drawPending(canvas, x, y);
            }
        }

        private void drawDots(Canvas c, float x1, float x2, float y, boolean completed, boolean active) {
            int count = Math.max(2, Math.round((x2 - x1) / dp(7)));
            int base = getResources().getColor(completed ? R.color.accent : R.color.text_secondary, getContext().getTheme());
            for (int i = 0; i < count; i++) {
                float t = i / (float)(count - 1);
                float alpha = completed ? 1f : 0.28f;
                if (active) {
                    float wave = (t - phase + 1f) % 1f;
                    alpha = 0.22f + 0.78f * (1f - wave);
                }
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(base);
                paint.setAlpha(Math.round(alpha * 255));
                c.drawCircle(x1 + (x2 - x1) * t, y, dp(1.45f), paint);
            }
        }

        private void drawPending(Canvas c, float x, float y) {
            icon.setColor(getResources().getColor(R.color.text_secondary, getContext().getTheme()));
            icon.setAlpha(130);
            c.drawCircle(x, y, dp(5), icon);
        }

        private void drawActive(Canvas c, float x, float y) {
            icon.setColor(getResources().getColor(R.color.accent, getContext().getTheme()));
            icon.setAlpha(255);
            c.drawCircle(x, y, dp(5), icon);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.accent, getContext().getTheme()));
            paint.setAlpha(55);
            c.drawCircle(x, y, dp(8), paint);
        }

        private void drawCheck(Canvas c, float x, float y) {
            icon.setColor(getResources().getColor(R.color.status_success, getContext().getTheme()));
            icon.setAlpha(255);
            Path p = new Path();
            p.moveTo(x - dp(3), y);
            p.lineTo(x - dp(1), y + dp(2));
            p.lineTo(x + dp(3.5f), y - dp(3));
            c.drawPath(p, icon);
        }

        private void drawCross(Canvas c, float x, float y) {
            icon.setColor(getResources().getColor(R.color.status_error, getContext().getTheme()));
            icon.setAlpha(255);
            c.drawLine(x - dp(3), y - dp(3), x + dp(3), y + dp(3), icon);
            c.drawLine(x + dp(3), y - dp(3), x - dp(3), y + dp(3), icon);
        }
    }
}
