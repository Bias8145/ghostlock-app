package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KernelInfoView extends TextView {
    private static final int COLLAPSED_LINES = 2;
    private static final int EXPANDED_LINES = 12;
    private static final long EXPAND_DURATION = 220L;
    private static final long COLLAPSE_DURATION = 180L;
    private static final int ICON_SIZE_DP = 14;
    private static final int ICON_GAP_DP = 8;
    private static final int SECTION_TEXT_SP = 10;
    private static final int IDENTITY_TEXT_SP = 14;
    private static final int CODENAME_TEXT_SP = 11;
    private static final int VALUE_TEXT_SP = 12;
    private static final int DETAIL_TEXT_SP = 11;

    private boolean expanded;
    private boolean animating;
    private CharSequence compactText = "";
    private CharSequence snapshotText = "";
    private ValueAnimator heightAnimator;

    public KernelInfoView(Context context) { super(context); init(); }
    public KernelInfoView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public KernelInfoView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setEllipsize(TextUtils.TruncateAt.END);
        setMaxLines(COLLAPSED_LINES);
        setClickable(true);
        setFocusable(true);
        setCompoundDrawablePadding(dp(ICON_GAP_DP));
        setOnClickListener(v -> toggleExpanded());
        updateExpandIcon();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String base = text == null ? "" : text.toString();
        compactText = buildCompactText(base);
        snapshotText = buildSnapshot(base);
        applyDisplayedText();
    }

    private void applyDisplayedText() {
        if (expanded) {
            setMaxLines(EXPANDED_LINES);
            setEllipsize(null);
            setContentDescription("Collapse device information");
            super.setText(snapshotText, BufferType.SPANNABLE);
        } else {
            setMaxLines(COLLAPSED_LINES);
            setEllipsize(TextUtils.TruncateAt.END);
            setContentDescription("Expand device information");
            super.setText(compactText, BufferType.NORMAL);
        }
        updateExpandIcon();
    }

    private void updateExpandIcon() {
        try {
            String iconKey = expanded ? "faw-chevron-up" : "faw-chevron-down";
            IconicsDrawable icon = new IconicsDrawable(getContext(), iconKey);
            icon.setColorList(ColorStateList.valueOf(getCurrentTextColor()));
            int size = dp(ICON_SIZE_DP);
            icon.setSizeXPx(size);
            icon.setSizeYPx(size);
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null);
        } catch (Throwable ignored) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        }
    }

    private String buildCompactText(String base) {
        String[] lines = base.split("\\n", -1);
        String device = lines.length > 0 && !lines[0].trim().isEmpty() ? lines[0].trim() : "Device: unknown";
        String kernel = lines.length > 1 && !lines[1].trim().isEmpty() ? lines[1].trim() : "Kernel: " + safe(System.getProperty("os.version", "unknown"));
        return device + "\n" + kernel;
    }

    private CharSequence buildSnapshot(String base) {
        String deviceName = resolveDeviceNameFromBase(base);
        String codename = propertyOrFallback("ro.product.device", Build.DEVICE);
        String build = propertyOrFallback("ro.build.display.id", Build.DISPLAY);
        String kernel = safe(System.getProperty("os.version", "unknown"));
        String android = humanAndroidVersion();
        String abi = Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0 ? safe(Build.SUPPORTED_ABIS[0]) : "unknown";
        String pageSize = pageSizeText();
        String soc = firstValidProperty("ro.soc.model", "ro.board.platform");

        SpannableStringBuilder out = new SpannableStringBuilder();
        appendIdentity(out, deviceName, codename);
        out.append('\n');
        appendSection(out, "PLATFORM");
        appendRow(out, "Android", android, VALUE_TEXT_SP, true);
        appendRow(out, "SoC", formatSoc(soc), VALUE_TEXT_SP, false);
        appendRow(out, "Architecture", abi, VALUE_TEXT_SP, false);
        appendRow(out, "Page Size", pageSize, VALUE_TEXT_SP, false);
        out.append('\n');
        appendSection(out, "BUILD");
        appendValue(out, build, DETAIL_TEXT_SP, false);
        out.append('\n');
        appendSection(out, "KERNEL");
        appendValue(out, kernel, DETAIL_TEXT_SP, false);
        return out;
    }

    private void appendIdentity(SpannableStringBuilder out, String device, String codename) {
        appendValue(out, device, IDENTITY_TEXT_SP, true);
        out.append('\n');
        appendValue(out, codename, CODENAME_TEXT_SP, false);
    }

    private void appendSection(SpannableStringBuilder out, String title) {
        int start = out.length();
        out.append(title);
        out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.append('\n');
    }

    private void appendRow(SpannableStringBuilder out, String label, String value, int valueSizeSp, boolean emphasizeValue) {
        appendValue(out, String.format(Locale.ROOT, "%-18s", label), DETAIL_TEXT_SP, false);
        appendValue(out, value, valueSizeSp, emphasizeValue);
        out.append('\n');
    }

    private void appendValue(SpannableStringBuilder out, String value, int sizeSp, boolean bold) {
        int start = out.length();
        out.append(value == null ? "unknown" : value);
        out.setSpan(new android.text.style.AbsoluteSizeSpan(sizeSp, true), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold) {
            out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private String humanAndroidVersion() {
        String release = valid(Build.VERSION.RELEASE);
        if (!release.isEmpty() && !"unknown".equalsIgnoreCase(release)) return release;
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 36) return "16";
        if (sdk == 35) return "15";
        if (sdk == 34) return "14";
        if (sdk == 33) return "13";
        if (sdk == 32) return "12L";
        if (sdk == 31) return "12";
        return String.valueOf(sdk);
    }

    private String resolveDeviceNameFromBase(String base) {
        if (base != null) {
            int colon = base.indexOf(':');
            if (colon > 0) {
                String value = base.substring(colon + 1).trim();
                if (!value.isEmpty()) return value;
            }
        }
        String model = valid(Build.MODEL);
        return model.isEmpty() ? "unknown" : model;
    }

    private String formatSoc(String value) {
        if (value == null || value.isEmpty() || "unknown".equalsIgnoreCase(value)) return "unknown";
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("zumapro")) return "Google Tensor G4 (" + value + ")";
        if (lower.startsWith("zuma")) return "Google Tensor G3 (" + value + ")";
        if (lower.startsWith("gs201")) return "Google Tensor G2 (" + value + ")";
        if (lower.startsWith("gs101")) return "Google Tensor G1 (" + value + ")";
        return value;
    }

    private String extractKmi(String kernel) {
        Matcher matcher = Pattern.compile("(android\\d+-\\d+\\.\\d+)").matcher(kernel == null ? "" : kernel);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    private String pageSizeText() {
        try {
            long page = android.system.Os.sysconf(android.system.OsConstants._SC_PAGESIZE);
            if (page > 0) return page >= 1024 && page % 1024 == 0 ? (page / 1024) + " KB" : page + " B";
        } catch (Throwable ignored) { }
        return "unknown";
    }

    private String firstValidProperty(String... keys) {
        for (String key : keys) {
            try {
                Class<?> props = Class.forName("android.os.SystemProperties");
                Object raw = props.getMethod("get", String.class).invoke(null, key);
                String value = raw instanceof String ? ((String) raw).trim() : "";
                if (!value.isEmpty() && !"unknown".equalsIgnoreCase(value) && !"null".equalsIgnoreCase(value)) return value;
            } catch (Throwable ignored) { }
        }
        return "unknown";
    }

    private String propertyOrFallback(String key, String fallback) {
        String value = firstValidProperty(key);
        return !"unknown".equals(value) ? value : valid(fallback);
    }

    private String valid(String value) { return value == null || value.trim().isEmpty() ? "unknown" : value.trim(); }
    private String safe(String value) { return value == null || value.trim().isEmpty() ? "unknown" : value.trim(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private void toggleExpanded() {
        if (animating) return;
        expanded = !expanded;
        animateSnapshot();
    }

    private void animateSnapshot() {
        if (heightAnimator != null) heightAnimator.cancel();
        int start = getHeight();
        if (start <= 0) { applyDisplayedText(); return; }

        if (expanded) {
            setMaxLines(EXPANDED_LINES);
            setEllipsize(null);
            super.setText(snapshotText, BufferType.SPANNABLE);
        } else {
            setMaxLines(COLLAPSED_LINES);
            setEllipsize(TextUtils.TruncateAt.END);
            super.setText(compactText, BufferType.NORMAL);
        }
        updateExpandIcon();

        ViewGroup.LayoutParams lp = getLayoutParams();
        int width = getWidth();
        if (width <= 0) { applyDisplayedText(); return; }
        measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int target = getMeasuredHeight();
        lp.height = start;
        setLayoutParams(lp);

        animating = true;
        heightAnimator = ValueAnimator.ofInt(start, Math.max(target, 1));
        heightAnimator.setDuration(expanded ? EXPAND_DURATION : COLLAPSE_DURATION);
        heightAnimator.addUpdateListener(animation -> { lp.height = (Integer) animation.getAnimatedValue(); requestLayout(); });
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                setLayoutParams(lp);
                animating = false;
                heightAnimator = null;
                applyDisplayedText();
            }
            @Override public void onAnimationCancel(Animator animation) { animating = false; heightAnimator = null; }
        });
        heightAnimator.start();
    }
}
