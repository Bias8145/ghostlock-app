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

        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(12));
        panel.setBackground(roundBackground(R.color.surface_container, 14));

        LinearLayout heroRow = new LinearLayout(getContext());
        heroRow.setOrientation(HORIZONTAL);
        heroRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout heroText = new LinearLayout(getContext());
        heroText.setOrientation(VERTICAL);
        heroText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

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

        panel.addView(heroRow);

        LinearLayout progressTrack = new LinearLayout(getContext());
        progressTrack.setClipToOutline(true);
        progressTrack.setBackground(roundBackground(R.color.outline_variant, 3));
        LayoutParams trackParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(5));
        trackParams.topMargin = dp(9);
        panel.addView(progressTrack, trackParams);

        progressFill = new View(getContext());
        progressFill.setBackground(roundBackground(R.color.accent, 3));
        progressTrack.addView(progressFill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));

        View divider = new View(getContext());
        divider.setBackgroundColor(getResources().getColor(R.color.outline_variant));
        LayoutParams dividerParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.topMargin = dp(12);
        dividerParams.bottomMargin = dp(10);
        panel.addView(divider, dividerParams);

        LinearLayout metrics = new LinearLayout(getContext());
        metrics.setOrientation(HORIZONTAL);
        metrics.setWeightSum(3);

        metrics.addView(createMetric("Total", "0", R.drawable.ic_check_circle, false));
        metrics.addView(createMetric("Success", "0", R.drawable.ic_check_circle, false));
        metrics.addView(createMetric("Failed", "0", R.drawable.ic_error, true));
        panel.addView(metrics);

        addView(panel);
        refreshStats();
    }

    private View createMetric(String label, String value, int iconRes, boolean error) {
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(VERTICAL);
        item.setPadding(0, 0, dp(8), 0);

        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(error ? R.color.error : R.color.icon_tint));
        icon.setAlpha(0.75f);
        header.addView(icon, new LinearLayout.LayoutParams(dp(14), dp(14)));

        TextView labelText = text(label, 10, R.color.text_secondary, Typeface.NORMAL);
        labelText.setIncludeFontPadding(false);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = dp(5);
        header.addView(labelText, labelParams);

        item.addView(header);

        TextView valueText = text(value, 18, error ? R.color.error : R.color.text_primary, Typeface.BOLD);
        valueText.setIncludeFontPadding(false);
        LayoutParams valueParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dp(4);
        item.addView(valueText, valueParams);

        if ("Total".equals(label)) totalRunsText = valueText;
        else if ("Success".equals(label)) successCountText = valueText;
        else if ("Failed".equals(label)) failureCountText = valueText;

        return item;
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

        if (progressFill != null) {
            float ratio = Math.max(0f, Math.min(1f, successRate / 100f));
            ViewGroup.LayoutParams params = progressFill.getLayoutParams();
            params.width = ratio == 0f ? 0 : Math.max(dp(2), Math.round(getWidth() * ratio));
            progressFill.setLayoutParams(params);
        }
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
        refreshStats();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
