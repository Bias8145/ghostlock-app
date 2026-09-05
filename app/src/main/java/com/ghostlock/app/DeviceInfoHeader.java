package com.ghostlock.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

public class DeviceInfoHeader extends LinearLayout {
    private static final int ICON_SIZE_DP = 16;
    private static final int ICON_TOUCH_DP = 40;
    private ImageView chevron;

    public DeviceInfoHeader(Context context) { super(context); init(); }
    public DeviceInfoHeader(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public DeviceInfoHeader(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setClickable(true);
        setFocusable(true);
        setBackgroundResource(android.R.color.transparent);

        TextView title = new TextView(getContext());
        title.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        title.setText("DEVICE INFO");
        title.setTextColor(getResources().getColor(R.color.text_secondary, getContext().getTheme()));
        title.setTextSize(11);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        if (android.os.Build.VERSION.SDK_INT >= 21) title.setLetterSpacing(0.08f);
        addView(title);

        chevron = new ImageView(getContext());
        LayoutParams iconParams = new LayoutParams(dp(ICON_TOUCH_DP), dp(ICON_TOUCH_DP));
        chevron.setLayoutParams(iconParams);
        chevron.setPadding(dp(12), dp(12), dp(12), dp(12));
        chevron.setScaleType(ImageView.ScaleType.CENTER);
        chevron.setBackgroundResource(com.google.android.material.R.drawable.mtrl_btn_transparent_bg);
        chevron.setClickable(false);
        addView(chevron);

        setContentDescription("Expand device information");
        setOnClickListener(v -> {
            View root = getRootView();
            View target = root.findViewById(R.id.deviceInfo);
            if (target instanceof KernelInfoView) {
                KernelInfoView info = (KernelInfoView) target;
                info.toggleExpanded();
                updateIcon(info.isExpanded());
            }
        });
        post(() -> refreshFromInfo());
    }

    private void refreshFromInfo() {
        View target = getRootView().findViewById(R.id.deviceInfo);
        if (target instanceof KernelInfoView) updateIcon(((KernelInfoView) target).isExpanded());
        else updateIcon(false);
    }

    private void updateIcon(boolean expanded) {
        try {
            String key = expanded ? "faw-chevron-up" : "faw-chevron-down";
            IconicsDrawable icon = new IconicsDrawable(getContext(), key);
            icon.setColorList(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary, getContext().getTheme())));
            int size = dp(ICON_SIZE_DP);
            icon.setSizeXPx(size);
            icon.setSizeYPx(size);
            chevron.setImageDrawable(icon);
            setContentDescription(expanded ? "Collapse device information" : "Expand device information");
        } catch (Throwable ignored) {
            chevron.setImageDrawable(null);
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
