package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public class DashboardStatsView extends LinearLayout {
    private AnalyticsManager analytics;
    private TextView totalRunsText;
    private TextView successRateText;
    private TextView successCountText;
    private TextView failureCountText;
    private View progressFill;

    public DashboardStatsView(Context context) {
        super(context);
        init();
    }

    public DashboardStatsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        analytics = new AnalyticsManager(getContext());

        TextView title = text("Statistics", 10, R.color.text_secondary, Typeface.BOLD);
        title.setLetterSpacing(0.04f);
        LayoutParams titleParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(8));
        addView(title, titleParams);

        // Row 1: Success rate panel.
        LinearLayout ratePanel = new LinearLayout(getContext());
        ratePanel.setOrientation(VERTICAL);
        ratePanel.setPadding(dp(12), dp(11), dp(12), dp(11));
        ratePanel.setBackground(roundBackground(R.color.surface_container, 14));

        LinearLayout heroRow = new LinearLayout(getContext());
        heroRow.setOrientation(HORIZONTAL);
        heroRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout heroText = new LinearLayout(getContext());
        heroText.setOrientation(VERTICAL);
        heroText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView heroLabel = text("Success rate", 11, R.color.text_secondary, Typeface.NORMAL);
        heroLabel.setIncludeFontPadding(false);
        heroText.addView(heroLabel);

        successRateText = text("0.0%", 26, R.color.text_primary, Typeface.BOLD);
        successRateText.setIncludeFontPadding(false);
        LayoutParams rateParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rateParams.topMargin = dp(2);
        heroText.addView(successRateText, rateParams);

        heroRow.addView(heroText);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(R.drawable.ic_analytics);
        icon.setColorFilter(getResources().getColor(R.color.icon_tint));
        icon.setAlpha(0.8f);
        heroRow.addView(icon, new LinearLayout.LayoutParams(dp(20), dp(20)));

        ratePanel.addView(heroRow);

        LinearLayout progressTrack = new LinearLayout(getContext());
        progressTrack.setClipToOutline(true);
        progressTrack.setBackground(roundBackground(R.color.outline_variant, 3));
        LayoutParams trackParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(5));
        trackParams.topMargin = dp(9);
        ratePanel.addView(progressTrack, trackParams);

        progressFill = new View(getContext());
        progressFill.setBackground(roundBackground(R.color.accent, 3));
        progressTrack.addView(progressFill,
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));

        LayoutParams ratePanelParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(ratePanel, ratePanelParams);

        // Row 2: Three equal metric panels.
        LinearLayout metricsRow = new LinearLayout(getContext());
        metricsRow.setOrientation(HORIZONTAL);
        metricsRow.setGravity(Gravity.TOP);

        LinearLayout.LayoutParams metricParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        LinearLayout totalPanel = createMetricPanel(
                "Total", "0", R.drawable.ic_dashboard, false);
        LinearLayout successPanel = createMetricPanel(
                "Success", "0", R.drawable.ic_check_circle, false);
        LinearLayout failurePanel = createMetricPanel(
                "Failed", "0", R.drawable.ic_error, true);

        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        totalParams.topMargin = dp(8);
        totalParams.rightMargin = dp(4);
        metricsRow.addView(totalPanel, totalParams);

        LinearLayout.LayoutParams successParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        successParams.topMargin = dp(8);
        successParams.leftMargin = dp(4);
        successParams.rightMargin = dp(4);
        metricsRow.addView(successPanel, successParams);

        LinearLayout.LayoutParams failureParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        failureParams.topMargin = dp(8);
        failureParams.leftMargin = dp(4);
        metricsRow.addView(failurePanel, failureParams);

        LayoutParams metricsParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metricsParams.topMargin = 0;
        metricsParams.bottomMargin = dp(2);
        addView(metricsRow, metricsParams);
        refreshStats();
    }

    private LinearLayout createMetricPanel(String label, String value, int iconRes, boolean isError) {
        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(8), dp(9), dp(8), dp(9));
        panel.setBackground(roundBackground(R.color.surface_container, 12));

        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(isError ? R.color.status_error : R.color.icon_tint));
        icon.setAlpha(0.75f);
        header.addView(icon, new LinearLayout.LayoutParams(dp(16), dp(16)));

        TextView labelText = text(label, 10, R.color.text_secondary, Typeface.NORMAL);
        labelText.setMaxLines(1);
        labelText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        labelText.setIncludeFontPadding(false);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = dp(5);
        header.addView(labelText, labelParams);

        panel.addView(header);

        TextView valueText = text(value, 18,
                isError ? R.color.status_error : R.color.text_primary, Typeface.BOLD);
        valueText.setIncludeFontPadding(false);
        LayoutParams valueParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dp(4);
        panel.addView(valueText, valueParams);

        if ("Total".equals(label)) totalRunsText = valueText;
        else if ("Success".equals(label)) successCountText = valueText;
        else if ("Failed".equals(label)) failureCountText = valueText;

        return panel;
    }

    public void refreshStats() {
        int total = analytics.getTotalRuns();
        int success = analytics.getSuccessCount();
        int failure = analytics.getFailureCount();
        float successRate = analytics.getSuccessRate();

        if (totalRunsText != null) totalRunsText.setText(String.valueOf(total));
        if (successCountText != null) successCountText.setText(String.valueOf(success));
        if (failureCountText != null) failureCountText.setText(String.valueOf(failure));
        if (successRateText != null) {
            successRateText.setText(String.format(Locale.ROOT, "%.1f%%", successRate));
        }

        updateProgressFill(successRate);
    }

    private void updateProgressFill(float successRate) {
        if (progressFill == null || getWidth() <= 0) return;

        float ratio = Math.max(0f, Math.min(1f, successRate / 100f));
        ViewGroup.LayoutParams params = progressFill.getLayoutParams();
        params.width = ratio == 0f ? 0 : Math.max(dp(2), Math.round(getWidth() * ratio));
        progressFill.setLayoutParams(params);
    }

    private TextView text(String value, float size, int colorRes, int style) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(getResources().getColor(colorRes));
        view.setTypeface(null, style);
        view.setIncludeFontPadding(false);
        return view;
    }

    private GradientDrawable roundBackground(int colorRes, int radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(getResources().getColor(colorRes));
        bg.setCornerRadius(dp(radiusDp));
        return bg;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0) {
            updateProgressFill(analytics.getSuccessRate());
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
