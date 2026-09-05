package com.ghostlock.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

/**
 * Compact three-page navigation for Home, Logs and Settings.
 * Uses the existing Font Awesome/Iconics dependency and keeps the existing
 * Home and log views alive so Run/progress state is not recreated on tab change.
 */
public class GhostLockBottomNavigation extends LinearLayout {
    private static final int PAGE_HOME = 0;
    private static final int PAGE_LOGS = 1;
    private static final int PAGE_SETTINGS = 2;

    private final Item[] items = new Item[3];
    private int selectedPage = PAGE_HOME;

    public GhostLockBottomNavigation(Context context) {
        super(context);
        init();
    }

    public GhostLockBottomNavigation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GhostLockBottomNavigation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(color(R.color.surface));
        setPadding(0, dp(4), 0, dp(4));
        setElevation(dp(2));
        setContentDescription("Page navigation");

        items[PAGE_HOME] = addItem("faw-home", "Home", PAGE_HOME);
        items[PAGE_LOGS] = addItem("faw-list-alt", "Logs", PAGE_LOGS);
        items[PAGE_SETTINGS] = addItem("faw-cog", "Settings", PAGE_SETTINGS);
        updateSelection();
    }

    private Item addItem(String iconKey, String label, int page) {
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4), dp(2), dp(4), dp(2));
        item.setMinimumHeight(dp(48));
        item.setClickable(true);
        item.setFocusable(true);
        item.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        item.setContentDescription(label);

        ImageView icon = new ImageView(getContext());
        LayoutParams iconParams = new LayoutParams(dp(22), dp(22));
        icon.setLayoutParams(iconParams);
        icon.setScaleType(ImageView.ScaleType.CENTER);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(icon);

        TextView text = new TextView(getContext());
        LayoutParams textParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textParams.topMargin = dp(1);
        text.setLayoutParams(textParams);
        text.setGravity(Gravity.CENTER);
        text.setSingleLine(true);
        text.setIncludeFontPadding(false);
        text.setText(label);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(text);

        item.setOnClickListener(v -> selectPage(page));
        addView(item);
        return new Item(item, icon, text, iconKey);
    }

    private void selectPage(int page) {
        if (page == PAGE_SETTINGS) {
            Context context = getContext();
            if (context instanceof Activity) {
                context.startActivity(new Intent(context, SettingsActivity.class));
            }
            return;
        }

        selectedPage = page;
        View homeBody = getRootView().findViewById(R.id.homeBody);
        View logPanel = getRootView().findViewById(R.id.logPanel);
        if (homeBody == null || logPanel == null) {
            updateSelection();
            return;
        }

        if (page == PAGE_HOME) {
            homeBody.setVisibility(VISIBLE);
            logPanel.setVisibility(VISIBLE);
        } else {
            homeBody.setVisibility(GONE);
            logPanel.setVisibility(VISIBLE);
        }
        updateSelection();
    }

    private void updateSelection() {
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            boolean selected = i == selectedPage;
            int tint = color(selected ? R.color.icon_tint : R.color.text_secondary);
            try {
                IconicsDrawable drawable = new IconicsDrawable(getContext(), item.iconKey);
                drawable.setColorList(ColorStateList.valueOf(tint));
                int size = dp(20);
                drawable.setSizeXPx(size);
                drawable.setSizeYPx(size);
                item.icon.setImageDrawable(drawable);
            } catch (Throwable ignored) {
                item.icon.setImageDrawable(null);
            }
            item.label.setTextColor(tint);
            item.label.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
            item.view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            item.view.setContentDescription(item.label.getText() + (selected ? ", selected" : ""));
        }
    }

    private int color(int resId) {
        return getResources().getColor(resId, getContext().getTheme());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class Item {
        final View view;
        final ImageView icon;
        final TextView label;
        final String iconKey;

        Item(View view, ImageView icon, TextView label, String iconKey) {
            this.view = view;
            this.icon = icon;
            this.label = label;
            this.iconKey = iconKey;
        }
    }
}
