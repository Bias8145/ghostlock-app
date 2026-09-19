package com.ghostlock.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Modern device/kernel info card with chip-based layout and proper icons. */
public class KernelInfoView extends LinearLayout {
    private boolean kernelVisible = true;
    private String deviceName = "";
    private String kernelVersion = "";
    private TextView deviceText;
    private LinearLayout chipsContainer;

    public KernelInfoView(Context context) {
        super(context);
        init();
    }

    public KernelInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KernelInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);

        // Header with device icon and toggle button
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        ImageView deviceIcon = new ImageView(getContext());
        deviceIcon.setImageResource(R.drawable.ic_device);
        deviceIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.icon_tint));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        iconParams.rightMargin = dp(8);
        header.addView(deviceIcon, iconParams);

        deviceText = new TextView(getContext());
        deviceText.setTextSize(13);
        deviceText.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        deviceText.setTypeface(null, android.graphics.Typeface.BOLD);
        deviceText.setEllipsize(TextUtils.TruncateAt.END);
        deviceText.setMaxLines(1);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        header.addView(deviceText, textParams);

        ImageView toggleIcon = new ImageView(getContext());
        toggleIcon.setImageResource(R.drawable.ic_visibility);
        toggleIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.icon_tint));
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        toggleParams.leftMargin = dp(8);
        toggleIcon.setLayoutParams(toggleParams);
        toggleIcon.setClickable(true);
        toggleIcon.setFocusable(true);
        toggleIcon.setOnClickListener(v -> {
            setKernelVisible(!kernelVisible);
            updateToggleIcon(toggleIcon);
        });
        header.addView(toggleIcon);

        // Chips container
        chipsContainer = new LinearLayout(getContext());
        chipsContainer.setOrientation(HORIZONTAL);
        chipsContainer.setGravity(Gravity.START);
        LinearLayout.LayoutParams chipsParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        chipsParams.topMargin = dp(8);
        addView(chipsContainer, chipsParams);
    }

    public void setText(CharSequence text) {
        setText(text, TextView.BufferType.NORMAL);
    }

    public void setText(CharSequence text, TextView.BufferType type) {
        String value = text == null ? "" : text.toString();
        String[] lines = value.split("\n", 2);

        deviceName = lines.length > 0 ? lines[0].trim() : "";
        kernelVersion = lines.length > 1 ? lines[1].replace("Kernel:", "").trim() : "";

        updateDisplay();
    }

    private void updateDisplay() {
        deviceText.setText(deviceName);

        chipsContainer.removeAllViews();

        if (kernelVisible && !kernelVersion.isEmpty()) {
            // Parse kernel version
            String[] parts = kernelVersion.split("-");
            String version = parts.length > 0 ? parts[0] : kernelVersion;

            // Version chip
            addChip("v" + version, R.drawable.ic_chip, R.color.accent_container, R.color.text_primary);

            // Architecture chip (assume ARM64 for Android)
            addChip("ARM64", R.drawable.ic_cpu, R.color.surface_container, R.color.text_secondary);

            // API level chip
            addChip("API " + android.os.Build.VERSION.SDK_INT, R.drawable.ic_info, R.color.surface_container, R.color.text_secondary);
        } else if (!kernelVisible) {
            // Hidden chip
            addChip("Hidden", R.drawable.ic_visibility_off, R.color.surface_container, R.color.text_secondary);
        }
    }

    private void addChip(String text, int iconRes, int bgColorRes, int textColorRes) {
        LinearLayout chip = new LinearLayout(getContext());
        chip.setOrientation(HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setPadding(dp(8), dp(5), dp(8), dp(5));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ContextCompat.getColor(getContext(), bgColorRes));
        bg.setCornerRadius(dp(10));
        chip.setBackground(bg);

        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        chipParams.rightMargin = dp(6);
        chip.setLayoutParams(chipParams);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(ContextCompat.getColor(getContext(), textColorRes));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(14), dp(14));
        iconParams.rightMargin = dp(4);
        chip.addView(icon, iconParams);

        TextView label = new TextView(getContext());
        label.setText(text);
        label.setTextSize(10);
        label.setTextColor(ContextCompat.getColor(getContext(), textColorRes));
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        chip.addView(label);

        chipsContainer.addView(chip);
    }

    private void setKernelVisible(boolean visible) {
        kernelVisible = visible;
        updateDisplay();
    }

    private void updateToggleIcon(ImageView icon) {
        Drawable drawable = ContextCompat.getDrawable(getContext(),
            kernelVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        if (drawable != null) {
            drawable.setTint(ContextCompat.getColor(getContext(), R.color.icon_tint));
        }
        icon.setImageDrawable(drawable);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidate();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
