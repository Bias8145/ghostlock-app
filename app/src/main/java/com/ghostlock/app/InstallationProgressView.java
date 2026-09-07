package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Compact, single-line installation status driven directly by the live log.
 * The visible step changes dynamically; the installation workflow itself is untouched.
 */
public final class InstallationProgressView extends LinearLayout {
    private static final int STATE_READY = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_SUCCESS = 2;
    private static final int STATE_FAILED = 3;

    private static final int ERROR_COLOR = 0xFFFF6B6B;
    private static final float ACTIVE_ALPHA = 0.45f;

    private final ImageView statusIcon;
    private final TextView statusTitle;
    private ValueAnimator breathingAnimator;
    private TextWatcher logWatcher;
    private int lastRunMarker = -1;
    private int lastState = -1;
    private int lastIconRes = -1;

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

        // Fixed 24dp slot keeps every Font Awesome glyph optically aligned.
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
            setStatus("Verifying installation", R.drawable.ic_install_circle_check, STATE_SUCCESS);
            return;
        }

        if (containsAny(latest, "error:", "failed", "unsupported", "exit code=137", "exit code=-1")) {
            // Keep the semantic icon for the current stage; only the color changes to red.
            Stage stage = stageFor(latest);
            setStatus(stage.title, stage.iconRes, STATE_FAILED);
            return;
        }

        Stage stage = stageFor(latest);
        setStatus(stage.title, stage.iconRes, STATE_RUNNING);
    }

    private Stage stageFor(String latest) {
        if (containsAny(latest, "running ghostlock", "preparing", "prepare", "starting")) {
            return new Stage("Preparing", R.drawable.ic_install_box_open);
        }
        if (containsAny(latest, "device", "uname", "kernel", "supported kernel")) {
            return new Stage("Checking device", R.drawable.ic_install_mobile);
        }
        if (containsAny(latest, "binary ready", "ksud ready", "manager", "kernelsu", "resukisu")) {
            return new Stage("Detecting manager", R.drawable.ic_install_shield);
        }
        if (containsAny(latest, "offset", "pselect", "kallsyms", "phys", "init_task", "security_hook")) {
            return new Stage("Loading offsets", R.drawable.ic_install_file_code);
        }
        if (containsAny(latest, "applying", "configuration", "config", "writing", "write")) {
            return new Stage("Applying configuration", R.drawable.ic_install_gears);
        }
        if (containsAny(latest, "execution", "exit code=", "verifying", "verify")) {
            return new Stage("Verifying installation", R.drawable.ic_install_circle_check);
        }
        return new Stage("Preparing", R.drawable.ic_install_box_open);
    }

    private void setStatus(String title, int iconRes, int state) {
        boolean changed = iconRes != lastIconRes || state != lastState || !title.equals(statusTitle.getText().toString());
        statusTitle.setText(title);
        setIcon(iconRes, state);

        if (changed && isAttachedToWindow()) {
            animateState();
        }

        if (state == STATE_RUNNING) {
            startBreathing();
        } else {
            stopBreathing();
        }

        lastIconRes = iconRes;
        lastState = state;
    }

    private void setIcon(int iconRes, int state) {
        if (statusIcon.getDrawable() == null || iconRes != lastIconRes) {
            statusIcon.setImageResource(iconRes);
        }

        Drawable drawable = statusIcon.getDrawable();
        if (drawable != null) {
            int color = state == STATE_FAILED
                    ? ERROR_COLOR
                    : getContext().getColor(state == STATE_READY
                    ? R.color.text_secondary
                    : R.color.text_primary);
            drawable.setTint(color);
        }
        statusIcon.setAlpha(state == STATE_RUNNING ? ACTIVE_ALPHA : 1f);
    }

    private void animateState() {
        stopBreathing();
        statusIcon.animate().cancel();
        statusTitle.animate().cancel();

        statusIcon.setAlpha(0.25f);
        statusIcon.setScaleX(0.84f);
        statusIcon.setScaleY(0.84f);
        statusTitle.setAlpha(0.55f);

        statusIcon.animate().alpha(lastState == STATE_RUNNING ? ACTIVE_ALPHA : 1f)
                .scaleX(1f).scaleY(1f).setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        if (lastState == STATE_RUNNING) startBreathing();
                    }
                }).start();
        statusTitle.animate().alpha(1f).setDuration(180).start();
    }

    private void startBreathing() {
        if (!isAttachedToWindow() || breathingAnimator != null && breathingAnimator.isRunning()) {
            return;
        }
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
        setStatus("Ready to run", R.drawable.ic_install_box_open, STATE_READY);
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

    private static final class Stage {
        final String title;
        final int iconRes;

        Stage(String title, int iconRes) {
            this.title = title;
            this.iconRes = iconRes;
        }
    }
}
