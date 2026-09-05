package com.ghostlock.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

/** Compact three-page navigation for Home, Logs and Settings. */
public class GhostLockBottomNavigation extends LinearLayout {
    private static final int PAGE_HOME = 0;
    private static final int PAGE_LOGS = 1;
    private static final int PAGE_SETTINGS = 2;
    private static final int SETTINGS_REQUEST = 4107;
    private static final long SELECTION_ANIMATION_MS = 220L;

    private final Item[] items = new Item[3];
    private int selectedPage = PAGE_HOME;

    public GhostLockBottomNavigation(Context context) { super(context); init(); }
    public GhostLockBottomNavigation(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public GhostLockBottomNavigation(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setClipChildren(false);
        setClipToPadding(false);
        GradientDrawable background = new GradientDrawable();
        background.setColor(color(R.color.surface));
        background.setCornerRadius(dp(20));
        setBackground(background);
        setPadding(0, dp(10), 0, dp(4));
        setElevation(0f);
        setTranslationZ(0f);
        setContentDescription("Page navigation");

        items[PAGE_HOME] = addItem("faw-home", "Home", PAGE_HOME);
        items[PAGE_LOGS] = addItem("faw-list-alt", "Logs", PAGE_LOGS);
        items[PAGE_SETTINGS] = addItem("faw-cog", "Settings", PAGE_SETTINGS);
        selectedPage = getContext() instanceof SettingsActivity ? PAGE_SETTINGS : PAGE_HOME;
        updateSelection(false);

        if (Build.VERSION.SDK_INT >= 33 && getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> {
                        if (activity instanceof SettingsActivity) activity.finish();
                        else if (selectedPage == PAGE_LOGS) showHome();
                        else activity.finish();
                    });
        }
    }

    private Item addItem(String iconKey, String label, int page) {
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setPadding(dp(3), dp(2), dp(3), dp(2));
        item.setMinimumHeight(dp(48));
        item.setClickable(true);
        item.setFocusable(true);
        item.setClipChildren(false);
        item.setClipToPadding(false);
        item.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        item.setContentDescription(label);

        FrameLayout iconContainer = new FrameLayout(getContext());
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(dp(48), dp(28));
        iconContainerParams.gravity = Gravity.CENTER_HORIZONTAL;
        iconContainer.setLayoutParams(iconContainerParams);
        iconContainer.setClipChildren(false);
        iconContainer.setClipToPadding(false);

        ImageView icon = new ImageView(getContext());
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER);
        icon.setLayoutParams(iconParams);
        icon.setScaleType(ImageView.ScaleType.CENTER);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        iconContainer.addView(icon);
        item.addView(iconContainer);

        TextView text = new TextView(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textParams.topMargin = dp(1);
        text.setLayoutParams(textParams);
        text.setGravity(Gravity.CENTER);
        text.setIncludeFontPadding(false);
        text.setText(label);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        text.setSingleLine(true);
        text.setEllipsize(TextUtils.TruncateAt.END);
        text.setAutoSizeTextTypeUniformWithConfiguration(9, 11, 1, TypedValue.COMPLEX_UNIT_SP);
        text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(text);

        item.setOnClickListener(v -> selectPage(page));
        addView(item);
        return new Item(item, iconContainer, icon, text, iconKey);
    }

    private void selectPage(int page) {
        Activity activity = getContext() instanceof Activity ? (Activity) getContext() : null;
        if (activity instanceof SettingsActivity) {
            if (page == PAGE_SETTINGS) return;
            if (page == PAGE_HOME) { activity.finish(); return; }
            Intent result = new Intent();
            result.putExtra("navigate_to", "logs");
            activity.setResult(Activity.RESULT_OK, result);
            activity.finish();
            return;
        }
        if (page == PAGE_SETTINGS) {
            if (activity != null) activity.startActivityForResult(new Intent(activity, SettingsActivity.class), SETTINGS_REQUEST);
            return;
        }
        selectedPage = page;
        View homeBody = getRootView().findViewById(R.id.homeBody);
        View logPanel = getRootView().findViewById(R.id.logPanel);
        if (homeBody == null || logPanel == null) { updateSelection(true); return; }
        if (page == PAGE_HOME) transition(homeBody, logPanel, false);
        else transition(logPanel, homeBody, true);
        updateSelection(true);
    }

    private void transition(View show, View hide, boolean enteringLogs) {
        if (show.getVisibility() == VISIBLE && hide.getVisibility() == GONE) return;
        show.animate().cancel();
        hide.animate().cancel();
        hide.setAlpha(0f);
        hide.setVisibility(GONE);
        show.setVisibility(VISIBLE);
        show.setAlpha(0f);
        show.setTranslationX(enteringLogs ? dp(18) : -dp(18));
        show.post(() -> show.animate().alpha(1f).translationX(0f).setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> { show.setAlpha(1f); show.setTranslationX(0f); }).start());
    }

    public void showHome() { selectPage(PAGE_HOME); }
    public void showLogs() { selectPage(PAGE_LOGS); }

    private void updateSelection(boolean animate) {
        for (Item item : items) {
            boolean selected = item == items[selectedPage];
            int tint = color(selected ? R.color.icon_tint : R.color.text_secondary);
            try {
                IconicsDrawable drawable = new IconicsDrawable(getContext(), item.iconKey);
                drawable.setColorList(ColorStateList.valueOf(tint));
                int size = dp(20);
                drawable.setSizeXPx(size);
                drawable.setSizeYPx(size);
                item.icon.setImageDrawable(drawable);
            } catch (Throwable ignored) { item.icon.setImageDrawable(null); }
            item.label.setTextColor(tint);
            item.label.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
            item.view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            item.view.setElevation(0f);
            item.view.setTranslationZ(0f);
            item.view.setContentDescription(item.label.getText() + (selected ? ", selected" : ""));
            updateIconShape(item, selected);
            animateItem(item, selected, animate);
        }
    }

    private void updateIconShape(Item item, boolean selected) {
        if (!selected) {
            item.iconContainer.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            return;
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color(R.color.accent_container));
        shape.setCornerRadius(dp(14));
        item.iconContainer.setBackground(shape);
    }

    private void animateItem(Item item, boolean selected, boolean animate) {
        item.iconContainer.animate().cancel();
        float targetY = selected ? -dp(2) : 0f;
        float targetScale = selected ? 1.03f : 1f;
        if (!animate) {
            item.iconContainer.setTranslationY(targetY);
            item.iconContainer.setScaleX(targetScale);
            item.iconContainer.setScaleY(targetScale);
            return;
        }
        item.iconContainer.animate().translationY(targetY).scaleX(targetScale).scaleY(targetScale)
                .setDuration(SELECTION_ANIMATION_MS).setInterpolator(new DecelerateInterpolator()).start();
    }

    private int color(int resId) { return getResources().getColor(resId, getContext().getTheme()); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private static final class Item {
        final View view;
        final FrameLayout iconContainer;
        final ImageView icon;
        final TextView label;
        final String iconKey;
        Item(View view, FrameLayout iconContainer, ImageView icon, TextView label, String iconKey) {
            this.view = view;
            this.iconContainer = iconContainer;
            this.icon = icon;
            this.label = label;
            this.iconKey = iconKey;
        }
    }
}
