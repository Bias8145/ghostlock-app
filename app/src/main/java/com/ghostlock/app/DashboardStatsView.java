package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

/** Material 3 statistics panel with an expandable body. */
public class DashboardStatsView extends LinearLayout {
    private AnalyticsManager analytics;
    private TextView totalRunsText;
    private TextView successRateText;
    private TextView successCountText;
    private TextView failureCountText;
    private View progressFill;
    private LinearLayout body;
    private TextView chevron;
    private boolean expanded;

    public DashboardStatsView(Context context) { super(context); init(); }
    public DashboardStatsView(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        setOrientation(VERTICAL);
        analytics = new AnalyticsManager(getContext());
        expanded = false;

        LinearLayout header = new LinearLayout(getContext());
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(2), 0, dp(2));
        header.setClickable(true);
        header.setFocusable(true);
        TextView title = text("STATISTICS", 10, R.color.text_secondary, Typeface.BOLD);
        title.setLetterSpacing(0.05f);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(28), 1f));
        TextView summary = text("RUN HISTORY", 9, R.color.text_secondary, Typeface.BOLD);
        summary.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(summary, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(28)));
        chevron = text("+", 16, R.color.icon_tint, Typeface.BOLD);
        chevron.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp(32), dp(28));
        arrowParams.leftMargin = dp(4);
        header.addView(chevron, arrowParams);
        addView(header, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(32)));
        header.setOnClickListener(v -> toggle());

        body = new LinearLayout(getContext());
        body.setOrientation(VERTICAL);
        body.setVisibility(View.GONE);
        addView(body, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        buildBody();
        refreshStats();
    }

    private void buildBody() {
        LinearLayout ratePanel = new LinearLayout(getContext());
        ratePanel.setOrientation(VERTICAL);
        ratePanel.setPadding(dp(12), dp(11), dp(12), dp(11));
        ratePanel.setBackground(roundBackground(R.color.surface_container_low, 20));
        LinearLayout heroRow = new LinearLayout(getContext());
        heroRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout heroText = new LinearLayout(getContext());
        heroText.setOrientation(VERTICAL);
        heroText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        heroText.addView(text("Process exit-0 rate", 11, R.color.text_secondary, Typeface.NORMAL));
        successRateText = text("0.0%", 26, R.color.text_primary, Typeface.BOLD);
        heroText.addView(successRateText);
        heroRow.addView(heroText);
        heroRow.addView(icon("ic_analytics", dp(20)));
        ratePanel.addView(heroRow);
        LinearLayout progressTrack = new LinearLayout(getContext());
        progressTrack.setBackground(roundBackground(R.color.surface_container, 16));
        progressTrack.setPadding(1, 1, 1, 1);
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(7));
        trackParams.topMargin = dp(9);
        ratePanel.addView(progressTrack, trackParams);
        progressFill = new View(getContext());
        progressFill.setBackground(roundBackground(R.color.accent, 999));
        progressTrack.addView(progressFill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        body.addView(ratePanel);

        LinearLayout metricsRow = new LinearLayout(getContext());
        metricsRow.setGravity(Gravity.TOP);
        addMetric(metricsRow, "Total", "0", "ic_dashboard", false, 0);
        addMetric(metricsRow, "Exit 0", "0", "ic_check_circle", false, 1);
        addMetric(metricsRow, "Non-zero", "0", "ic_error", true, 2);
        LayoutParams metricsParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metricsParams.topMargin = dp(8);
        body.addView(metricsRow, metricsParams);
    }

    private void addMetric(LinearLayout row, String label, String value, String iconName, boolean error, int index) {
        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(8), dp(9), dp(8), dp(9));
        panel.setBackground(roundBackground(R.color.surface_container, 16));
        LinearLayout header = new LinearLayout(getContext());
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(icon(iconName, dp(16)), new LinearLayout.LayoutParams(dp(16), dp(16)));
        TextView labelText = text(label, 10, R.color.text_secondary, Typeface.NORMAL);
        labelText.setSingleLine(true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = dp(5);
        header.addView(labelText, labelParams);
        panel.addView(header);
        TextView valueText = text(value, 18, error ? R.color.status_error : R.color.text_primary, Typeface.BOLD);
        LayoutParams valueParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dp(4);
        panel.addView(valueText, valueParams);
        if (index == 0) totalRunsText = valueText;
        else if (index == 1) successCountText = valueText;
        else failureCountText = valueText;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        if (index > 0) params.leftMargin = dp(4);
        if (index < 2) params.rightMargin = dp(4);
        row.addView(panel, params);
    }

    private ImageView icon(String name, int size) {
        ImageView v = new ImageView(getContext());
        int res = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        v.setImageResource(res);
        v.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.icon_tint)));
        v.setScaleType(ImageView.ScaleType.CENTER);
        v.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return v;
    }

    public void toggle() { setExpanded(!expanded); }
    public void setExpanded(boolean value) {
        expanded = value;
        body.setVisibility(expanded ? View.VISIBLE : View.GONE);
        chevron.setText(expanded ? "−" : "+");
    }
    public void expand() { setExpanded(true); }
    public void collapse() { setExpanded(false); }
    public boolean isExpanded() { return expanded; }

    public void refreshStats() {
        int total = analytics.getTotalRuns();
        int success = analytics.getSuccessCount();
        int failure = analytics.getFailureCount();
        float successRate = analytics.getSuccessRate();
        if (totalRunsText != null) totalRunsText.setText(String.valueOf(total));
        if (successCountText != null) successCountText.setText(String.valueOf(success));
        if (failureCountText != null) failureCountText.setText(String.valueOf(failure));
        if (successRateText != null) successRateText.setText(String.format(Locale.ROOT, "%.1f%%", successRate));
        updateProgressFill(successRate);
    }

    private void updateProgressFill(float successRate) {
        if (progressFill == null || getWidth() <= 0) return;
        float ratio = Math.max(0f, Math.min(1f, successRate / 100f));
        ViewGroup.LayoutParams params = progressFill.getLayoutParams();
        int width = Math.max(0, Math.round(getWidth() * ratio));
        params.width = ratio == 0f ? 0 : Math.max(dp(2), width);
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

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0) updateProgressFill(analytics.getSuccessRate());
    }

    private GradientDrawable roundBackground(int colorRes, int radiusDp) { GradientDrawable bg = new GradientDrawable(); bg.setColor(getResources().getColor(colorRes)); bg.setCornerRadius(dp(radiusDp)); return bg; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
