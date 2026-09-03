package com.ghostlock.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/** Compact, log-driven installation progress indicator. */
public final class InstallationProgressView extends LinearLayout {
    private static final int STAGES = 6;
    private static final String[] TITLES = {
            "Device", "Manager", "Kernel", "Offsets", "Execute", "Verify"
    };
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
    private int stage = 0;
    private boolean failed;
    private boolean started;
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
        LayoutParams hp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        hp.bottomMargin = dp(12);
        addView(heading, hp);

        strip = new ProgressStrip(context);
        LayoutParams sp = new LayoutParams(LayoutParams.MATCH_PARENT, dp(24));
        addView(strip, sp);

        MaterialCardView card = new MaterialCardView(context);
        card.setCardBackgroundColor(context.getColor(R.color.surface_container_low));
        card.setCardElevation(0);
        card.setRadius(dp(18));
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
        LayoutParams dp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dp.topMargin = dp(3);
        body.addView(statusDetail, dp);

        card.addView(body);
        LayoutParams cp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        cp.topMargin = dp(10);
        cp.bottomMargin = dp(14);
        addView(card, cp);

        updateText();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    @Override protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    private void bindLog() {
        View root = getRootView();
        final TextView log = root.findViewById(R.id.logView);
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
        if (s.trim().isEmpty()) {
            setProgress(0, false, false);
            return;
        }
        if (s.contains("==== start ====")) started = true;
        if (!started) {
            setProgress(0, false, false);
            return;
        }

        int next = 0;
        if (containsAny(s, "ksud ready", "manager detected", "kernelsu", "resukisu", "supermanager")) next = Math.max(next, 1);
        if (containsAny(s, "kernel", "uname", "supported kernel", "kernel version")) next = Math.max(next, 2);
        if (containsAny(s, "offset", "pselect", "kallsyms", "phys", "init_task", "security_hook")) next = Math.max(next, 3);
        if (containsAny(s, "binary ready", "preparing", "prepare", "running ghostlock")) next = Math.max(next, 4);
        if (containsAny(s, "exit code=", "execution", "success", "failed", "error:")) next = Math.max(next, 5);

        boolean isFailure = containsAny(s, "error:", "failed", "unsupported", "exit code=137", "exit code=-1");
        boolean isSuccess = s.contains("exit code=0");
        if (isSuccess) {
            setProgress(5, false, false);
        } else {
            setProgress(Math.min(next, 5), true, isFailure);
        }
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private void setProgress(int newStage, boolean active, boolean error) {
        stage = Math.max(0, Math.min(STAGES - 1, newStage));
        failed = error;
        strip.invalidate();
        updateText();
        if (active && !error) startAnimation(); else stopAnimation();
    }

    private void updateText() {
        if (failed) {
            statusTitle.setText("Installation failed");
            statusDetail.setText(stage == 2 ? "The detected kernel is not supported." : "GhostLock exited with an error.");
        } else if (started && stage == STAGES - 1) {
            statusTitle.setText("Installation completed");
            statusDetail.setText("GhostLock executed successfully.");
        } else {
            statusTitle.setText(ACTIVE[stage]);
            statusDetail.setText(DETAIL[stage]);
        }
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
            strip.setPhase(0f);
        }
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private final class ProgressStrip extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float phase;

        ProgressStrip(Context context) {
            super(context);
            paint.setStrokeWidth(dp(1.5f));
            iconPaint.setStyle(Paint.Style.STROKE);
            iconPaint.setStrokeWidth(dp(1.8f));
            iconPaint.setStrokeCap(Paint.Cap.ROUND);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setPhase(float value) { phase = value; invalidate(); }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float y = getHeight() / 2f;
            float side = dp(9);
            float start = side;
            float end = width - side;
            float step = (end - start) / (STAGES - 1);

            for (int i = 0; i < STAGES - 1; i++) {
                float x1 = start + step * i + dp(10);
                float x2 = start + step * (i + 1) - dp(10);
                boolean completed = i < stage;
                boolean active = i == stage && !failed && started;
                drawDots(canvas, x1, x2, y, completed, active);
            }
            for (int i = 0; i < STAGES; i++) {
                float x = start + step * i;
                if (i < stage || (i == stage && started && !failed && stage == STAGES - 1)) {
                    drawCheck(canvas, x, y);
                } else if (i == stage && failed) {
                    drawCross(canvas, x, y);
                } else if (i == stage && started) {
                    drawActive(canvas, x, y);
                } else {
                    drawPending(canvas, x, y);
                }
            }
        }

        private void drawDots(Canvas c, float x1, float x2, float y, boolean completed, boolean active) {
            int count = Math.max(2, Math.round((x2 - x1) / dp(7)));
            for (int i = 0; i < count; i++) {
                float t = count == 1 ? 0 : i / (float)(count - 1);
                float x = x1 + (x2 - x1) * t;
                float alpha = 1f;
                if (active) {
                    float wave = (t - phase + 1f) % 1f;
                    alpha = 0.25f + 0.75f * (1f - wave);
                } else if (!completed) {
                    alpha = 0.28f;
                }
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(getResources().getColor(completed ? R.color.accent : R.color.text_secondary, getContext().getTheme()));
                paint.setAlpha(Math.round(alpha * 255));
                c.drawCircle(x, y, dp(1.45f), paint);
            }
        }

        private void drawPending(Canvas c, float x, float y) {
            iconPaint.setColor(getResources().getColor(R.color.text_secondary, getContext().getTheme()));
            iconPaint.setAlpha(150);
            c.drawCircle(x, y, dp(5), iconPaint);
        }

        private void drawActive(Canvas c, float x, float y) {
            iconPaint.setColor(getResources().getColor(R.color.accent, getContext().getTheme()));
            iconPaint.setAlpha(255);
            c.drawCircle(x, y, dp(5), iconPaint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.accent, getContext().getTheme()));
            paint.setAlpha(70);
            c.drawCircle(x, y, dp(8), paint);
        }

        private void drawCheck(Canvas c, float x, float y) {
            iconPaint.setColor(getResources().getColor(R.color.status_success, getContext().getTheme()));
            iconPaint.setAlpha(255);
            Path p = new Path();
            p.moveTo(x - dp(3), y);
            p.lineTo(x - dp(1), y + dp(2));
            p.lineTo(x + dp(3.5f), y - dp(3));
            c.drawPath(p, iconPaint);
        }

        private void drawCross(Canvas c, float x, float y) {
            iconPaint.setColor(getResources().getColor(R.color.status_error, getContext().getTheme()));
            iconPaint.setAlpha(255);
            c.drawLine(x - dp(3), y - dp(3), x + dp(3), y + dp(3), iconPaint);
            c.drawLine(x + dp(3), y - dp(3), x - dp(3), y + dp(3), iconPaint);
        }
    }
}
