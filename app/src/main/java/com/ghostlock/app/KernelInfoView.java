package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TabStopSpan;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Locale;

public class KernelInfoView extends TextView {
    private static final float SECTION_SCALE = 0.95f;
    private static final float IDENTITY_SCALE = 1.05f;
    private static final float LABEL_SCALE = 0.86f;
    private static final float VALUE_SCALE = 0.86f;
    private static final float DETAIL_SCALE = 0.86f;
    private static final float KERNEL_SCALE = 0.80f;
    private static final int LABEL_COLUMN_DP = 92;

    public KernelInfoView(Context context) { super(context); init(); }
    public KernelInfoView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public KernelInfoView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setClickable(false);
        setFocusable(false);
        setIncludeFontPadding(false);
        setMaxLines(Integer.MAX_VALUE);
        setEllipsize(null);
        setLineSpacing(0, 1.0f);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(buildSnapshot(), BufferType.SPANNABLE);
    }

    private CharSequence buildSnapshot() {
        String deviceName = valid(Build.MODEL);
        String codename = propertyOrFallback("ro.product.device", Build.DEVICE);
        String build = propertyOrFallback("ro.build.display.id", Build.DISPLAY);
        String kernel = safe(System.getProperty("os.version", "unknown"));
        String android = humanAndroidVersion();
        String abi = Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0 ? safe(Build.SUPPORTED_ABIS[0]) : "unknown";
        String pageSize = pageSizeText();
        String soc = firstValidProperty("ro.soc.model", "ro.board.platform");

        SpannableStringBuilder out = new SpannableStringBuilder();
        appendIdentity(out, deviceName, codename);
        out.append("\n\n");

        appendSection(out, "PLATFORM");
        appendRow(out, "Android", android);
        appendRow(out, "SoC", formatSoc(soc));
        appendRow(out, "Architecture", abi);
        appendRow(out, "Page Size", pageSize);
        out.append('\n');

        appendSection(out, "BUILD");
        appendValue(out, build, DETAIL_SCALE, false, false);
        out.append("\n\n");

        appendSection(out, "KERNEL");
        appendKernelValue(out, kernel);
        return out;
    }

    private void appendIdentity(SpannableStringBuilder out, String deviceName, String codename) {
        String name = deviceName == null ? "unknown" : deviceName.trim();
        String code = codename == null ? "unknown" : codename.trim();
        appendValue(out, name, IDENTITY_SCALE, false, false);
        if (!code.isEmpty() && !"unknown".equalsIgnoreCase(code)) {
            out.append(" ");
            int start = out.length();
            out.append(code);
            out.setSpan(new RelativeSizeSpan(IDENTITY_SCALE), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            out.setSpan(new ForegroundColorSpan(colorAccent()), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void appendSection(SpannableStringBuilder out, String title) {
        int start = out.length();
        out.append(title);
        out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new RelativeSizeSpan(SECTION_SCALE), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new ForegroundColorSpan(colorSecondary()), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.append('\n');
    }

    private void appendRow(SpannableStringBuilder out, String label, String value) {
        int lineStart = out.length();
        appendValue(out, label, LABEL_SCALE, false, true);
        out.append('\t');
        appendValue(out, value, VALUE_SCALE, false, false);
        out.append('\n');
        out.setSpan(new TabStopSpan.Standard(dp(LABEL_COLUMN_DP)), lineStart, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void appendKernelValue(SpannableStringBuilder out, String value) {
        int start = out.length();
        out.append(value == null ? "unknown" : value);
        out.setSpan(new RelativeSizeSpan(KERNEL_SCALE), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new TypefaceSpan(Typeface.MONOSPACE), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new ForegroundColorSpan(colorPrimary()), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void appendValue(SpannableStringBuilder out, String value, float scale, boolean bold, boolean secondary) {
        int start = out.length();
        out.append(value == null ? "unknown" : value);
        out.setSpan(new RelativeSizeSpan(scale), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold) out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new ForegroundColorSpan(secondary ? colorSecondary() : colorPrimary()), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private int colorPrimary() { return getResources().getColor(R.color.text_primary, getContext().getTheme()); }
    private int colorSecondary() { return getResources().getColor(R.color.text_secondary, getContext().getTheme()); }
    private int colorAccent() { return getResources().getColor(R.color.accent, getContext().getTheme()); }

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

    private String formatSoc(String value) {
        if (value == null || value.isEmpty() || "unknown".equalsIgnoreCase(value)) return "unknown";
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("zumapro")) return "Google Tensor G4 (" + value + ")";
        if (lower.startsWith("zuma")) return "Google Tensor G3 (" + value + ")";
        if (lower.startsWith("gs201")) return "Google Tensor G2 (" + value + ")";
        if (lower.startsWith("gs101")) return "Google Tensor G1 (" + value + ")";
        return value;
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
}
