package com.ghostlock.app;

import android.content.Context;
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

        // Title
        TextView title = new TextView(getContext());
        title.setText("Statistics");
        title.setTextSize(10);
        title.setTextColor(getResources().getColor(R.color.text_secondary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(8));
        addView(title, titleParams);

        // Stats grid
        LinearLayout statsGrid = new LinearLayout(getContext());
        statsGrid.setOrientation(HORIZONTAL);
        statsGrid.setWeightSum(2);

        // Success rate card
        statsGrid.addView(createStatCard("Success Rate", "0%", R.drawable.ic_analytics, true));

        // Total runs card
        statsGrid.addView(createStatCard("Total Runs", "0", R.drawable.ic_check_circle, false));

        addView(statsGrid);

        // Secondary stats
        LinearLayout secondaryGrid = new LinearLayout(getContext());
        secondaryGrid.setOrientation(HORIZONTAL);
        secondaryGrid.setWeightSum(2);
        LinearLayout.LayoutParams secondaryParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secondaryParams.topMargin = dp(8);

        secondaryGrid.addView(createStatCard("Success", "0", R.drawable.ic_check_circle, false));
        secondaryGrid.addView(createStatCard("Failed", "0", R.drawable.ic_error, false));

        addView(secondaryGrid, secondaryParams);

        refreshStats();
    }

    private View createStatCard(String label, String value, int iconRes, boolean highlight) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setColor(getResources().getColor(highlight ? R.color.accent_container : R.color.surface_container));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cardParams.rightMargin = dp(8);
        card.setLayoutParams(cardParams);

        // Icon + Label row
        LinearLayout headerRow = new LinearLayout(getContext());
        headerRow.setOrientation(HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(R.color.icon_tint));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(16), dp(16));
        iconParams.rightMargin = dp(6);
        icon.setLayoutParams(iconParams);
        headerRow.addView(icon);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(10);
        labelText.setTextColor(getResources().getColor(R.color.text_secondary));
        headerRow.addView(labelText);

        card.addView(headerRow);

        // Value text
        TextView valueText = new TextView(getContext());
        valueText.setText(value);
        valueText.setTextSize(20);
        valueText.setTextColor(getResources().getColor(R.color.text_primary));
        valueText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dp(4);
        card.addView(valueText, valueParams);

        // Store references for updates
        if (label.equals("Success Rate")) successRateText = valueText;
        else if (label.equals("Total Runs")) totalRunsText = valueText;
        else if (label.equals("Success")) successCountText = valueText;
        else if (label.equals("Failed")) failureCountText = valueText;

        return card;
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
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
