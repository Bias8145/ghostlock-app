package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogFilterView extends LinearLayout {
    public interface FilterListener {
        void onFilterChanged(String query, LogLevel level);
    }

    public enum LogLevel {
        ALL("All", 0),
        INFO("Info", 1),
        WARNING("Warning", 2),
        ERROR("Error", 3),
        DEBUG("Debug", 4);

        public final String label;
        public final int value;

        LogLevel(String label, int value) {
            this.label = label;
            this.value = value;
        }
    }

    private EditText searchInput;
    private LinearLayout filterChips;
    private FilterListener listener;
    private LogLevel currentLevel = LogLevel.ALL;

    public LogFilterView(Context context) {
        super(context);
        init();
    }

    public LogFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setPadding(0, 0, 0, dp(12));

        // Search bar
        LinearLayout searchBar = new LinearLayout(getContext());
        searchBar.setOrientation(HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setBackground(createSearchBackground());
        searchBar.setPadding(dp(12), dp(8), dp(12), dp(8));

        ImageView searchIcon = new ImageView(getContext());
        searchIcon.setImageResource(R.drawable.ic_search);
        searchIcon.setColorFilter(getResources().getColor(R.color.text_secondary));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        searchIcon.setLayoutParams(iconParams);
        searchBar.addView(searchIcon);

        searchInput = new EditText(getContext());
        searchInput.setHint("Search logs...");
        searchInput.setTextSize(12);
        searchInput.setTextColor(getResources().getColor(R.color.log_text));
        searchInput.setHintTextColor(getResources().getColor(R.color.text_secondary));
        searchInput.setBackground(null);
        searchInput.setPadding(dp(8), 0, 0, 0);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchInput.setLayoutParams(inputParams);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listener != null) {
                    listener.onFilterChanged(s.toString(), currentLevel);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        searchBar.addView(searchInput);

        addView(searchBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Filter chips
        filterChips = new LinearLayout(getContext());
        filterChips.setOrientation(HORIZONTAL);
        filterChips.setGravity(Gravity.START);
        LinearLayout.LayoutParams chipsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipsParams.topMargin = dp(8);
        filterChips.setLayoutParams(chipsParams);

        for (LogLevel level : LogLevel.values()) {
            filterChips.addView(createFilterChip(level));
        }

        addView(filterChips);
    }

    private View createFilterChip(LogLevel level) {
        LinearLayout chip = new LinearLayout(getContext());
        chip.setOrientation(HORIZONTAL);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), dp(6), dp(12), dp(6));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp(6);
        chip.setLayoutParams(params);
        chip.setClickable(true);
        chip.setFocusable(true);

        TextView label = new TextView(getContext());
        label.setText(level.label);
        label.setTextSize(11);
        label.setGravity(Gravity.CENTER);
        chip.addView(label);

        updateChipStyle(chip, label, level == currentLevel);

        chip.setOnClickListener(v -> {
            if (currentLevel != level) {
                // Update all chips
                for (int i = 0; i < filterChips.getChildCount(); i++) {
                    View chipView = filterChips.getChildAt(i);
                    if (chipView instanceof LinearLayout) {
                        LinearLayout chipLayout = (LinearLayout) chipView;
                        TextView chipLabel = (TextView) chipLayout.getChildAt(0);
                        updateChipStyle(chipLayout, chipLabel, false);
                    }
                }

                currentLevel = level;
                updateChipStyle(chip, label, true);

                if (listener != null) {
                    listener.onFilterChanged(searchInput.getText().toString(), currentLevel);
                }
            }
        });

        return chip;
    }

    private void updateChipStyle(LinearLayout chip, TextView label, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));

        if (selected) {
            bg.setColor(getResources().getColor(R.color.accent_container));
            label.setTextColor(getResources().getColor(R.color.text_primary));
        } else {
            bg.setColor(getResources().getColor(R.color.surface_container));
            label.setTextColor(getResources().getColor(R.color.text_secondary));
        }

        chip.setBackground(bg);
    }

    private GradientDrawable createSearchBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(getResources().getColor(R.color.surface_container));
        return bg;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    public LogLevel getCurrentLevel() {
        return currentLevel;
    }

    public String getSearchQuery() {
        return searchInput.getText().toString();
    }
}
