package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Modern manager status card with improved visual hierarchy and proper icons. */
public class RuntimeStatusView extends FrameLayout {
    private final LinearLayout content;
    private final LinearLayout statusBadge;
    private final ImageView statusIcon;
    private final TextView statusLabel;
    private final TextView message;
    private final LinearLayout managerCard;
    private final ImageView managerIcon;
    private final TextView managerName;
    private final TextView managerStatus;
    private final LinearLayout actionButtons;
    private final TextView installButton;

    public RuntimeStatusView(Context context) {
        this(context, null);
    }

    public RuntimeStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        setBackground(null);

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));
        addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Status badge with icon
        statusBadge = new LinearLayout(context);
        statusBadge.setOrientation(LinearLayout.HORIZONTAL);
        statusBadge.setGravity(Gravity.CENTER_VERTICAL);
        statusBadge.setPadding(dp(10), dp(6), dp(10), dp(6));
        statusBadge.setBackground(createRoundedBackground(ContextCompat.getColor(context, R.color.accent_container), 12));

        statusIcon = new ImageView(context);
        statusIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(16), dp(16)));
        statusBadge.addView(statusIcon);

        statusLabel = text(11, true);
        statusLabel.setPadding(dp(6), 0, 0, 0);
        statusBadge.addView(statusLabel, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        content.addView(statusBadge, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Message text
        message = text(12, false);
        message.setLineSpacing(dp(2), 1.0f);
        message.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        msgParams.topMargin = dp(10);
        content.addView(message, msgParams);

        // Manager info card
        managerCard = new LinearLayout(context);
        managerCard.setOrientation(LinearLayout.HORIZONTAL);
        managerCard.setGravity(Gravity.CENTER_VERTICAL);
        managerCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        managerCard.setBackground(createRoundedBackground(ContextCompat.getColor(context, R.color.surface_container), 14));

        managerIcon = new ImageView(context);
        managerIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        managerIcon.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint));
        managerCard.addView(managerIcon);

        LinearLayout managerText = new LinearLayout(context);
        managerText.setOrientation(LinearLayout.VERTICAL);
        managerText.setPadding(dp(10), 0, 0, 0);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        managerCard.addView(managerText, textParams);

        managerName = text(13, true);
        managerText.addView(managerName);

        managerStatus = text(11, false);
        managerStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dp(2);
        managerText.addView(managerStatus, statusParams);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(12);
        content.addView(managerCard, cardParams);

        // Action buttons
        actionButtons = new LinearLayout(context);
        actionButtons.setOrientation(LinearLayout.HORIZONTAL);
        actionButtons.setGravity(Gravity.CENTER);
        actionButtons.setVisibility(View.GONE);

        installButton = text(13, true);
        installButton.setText("Install Manager");
        installButton.setGravity(Gravity.CENTER);
        installButton.setPadding(dp(16), dp(12), dp(16), dp(12));
        installButton.setBackground(createRoundedBackground(ContextCompat.getColor(context, R.color.accent), 14));
        installButton.setTextColor(ContextCompat.getColor(context, R.color.on_accent));
        actionButtons.addView(installButton);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = dp(14);
        content.addView(actionButtons, btnParams);

        refresh();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refresh();
    }

    public void refresh() {
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        boolean showInstall = false;
        int statusColor, bgColor, iconRes;
        String statusText, messageText;

        switch (result.state) {
            case READY:
                statusText = "READY";
                messageText = "Manager verified and ready to use";
                statusColor = R.color.status_success;
                bgColor = R.color.status_success_bg;
                iconRes = R.drawable.ic_shield_check;
                break;

            case MANAGER_REQUIRED:
                statusText = "MANAGER REQUIRED";
                messageText = "Install a supported manager to continue";
                statusColor = R.color.accent;
                bgColor = R.color.accent_container;
                iconRes = R.drawable.ic_shield_alert;
                showInstall = true;
                break;

            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:
                statusText = "NOT INSTALLED";
                messageText = "No supported manager detected on device";
                statusColor = R.color.accent;
                bgColor = R.color.accent_container;
                iconRes = R.drawable.ic_shield_alert;
                showInstall = true;
                break;

            case KERNEL_UNSUPPORTED:
                statusText = "KERNEL UNSUPPORTED";
                messageText = "Current kernel is not supported by GhostLock";
                statusColor = R.color.status_error;
                bgColor = R.color.status_error_bg;
                iconRes = R.drawable.ic_shield_alert;
                break;

            case SPOOFED_MANAGER:
                statusText = "IDENTITY MISMATCH";
                messageText = "Manager identity verification failed";
                statusColor = R.color.status_error;
                bgColor = R.color.status_error_bg;
                iconRes = R.drawable.ic_shield_alert;
                break;

            case UNSUPPORTED_MANAGER:
                statusText = "UNSUPPORTED MANAGER";
                messageText = "Installed manager is not registered";
                statusColor = R.color.status_error;
                bgColor = R.color.status_error_bg;
                iconRes = R.drawable.ic_shield_half;
                break;

            default:
                statusText = "STATUS UNAVAILABLE";
                messageText = "Manager information unavailable";
                statusColor = R.color.text_secondary;
                bgColor = R.color.surface_container;
                iconRes = R.drawable.ic_shield_half;
                break;
        }

        // Update status badge
        statusLabel.setText(statusText);
        statusLabel.setTextColor(ContextCompat.getColor(getContext(), statusColor));
        statusIcon.setImageResource(iconRes);
        statusIcon.setColorFilter(ContextCompat.getColor(getContext(), statusColor));
        statusBadge.setBackground(createRoundedBackground(ContextCompat.getColor(getContext(), bgColor), 12));

        // Update message
        message.setText(messageText);

        // Update manager info
        if (result.manager.installed) {
            managerName.setText(result.manager.name);

            String status;
            int statusColorRes;
            if (result.manager.spoofed) {
                status = "Identity Mismatch";
                statusColorRes = R.color.status_error;
            } else if (result.manager.identityVerified) {
                status = "Verified";
                statusColorRes = R.color.status_success;
            } else if (result.manager.recognized) {
                status = "Recognized";
                statusColorRes = R.color.accent;
            } else {
                status = "Unknown";
                statusColorRes = R.color.text_secondary;
            }

            managerStatus.setText(status);
            managerStatus.setTextColor(ContextCompat.getColor(getContext(), statusColorRes));
            managerIcon.setImageResource(R.drawable.ic_shield_check);
        } else {
            managerName.setText("No Manager");
            managerStatus.setText("Not installed");
            managerStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            managerIcon.setImageResource(R.drawable.ic_shield_alert);
        }

        // Update action buttons
        actionButtons.setVisibility(showInstall ? View.VISIBLE : View.GONE);
        if (showInstall) {
            installButton.setOnClickListener(v -> showManagerPicker());
        }

        // Update background
        setSurface(bgColor);
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers = ManagerCompatibility.registeredManagers(getContext());
        final android.app.Dialog dialog = new android.app.Dialog(getContext());

        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(22), dp(24), dp(18));
        box.setBackground(createRoundedBackground(ContextCompat.getColor(getContext(), R.color.surface), 26));

        TextView title = text(20, true);
        title.setText("Install Manager");
        box.addView(title);

        TextView subtitle = text(13, false);
        subtitle.setText("Select a supported manager to continue");
        subtitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        subtitle.setLineSpacing(0f, 1.08f);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(6);
        subtitleParams.bottomMargin = dp(16);
        box.addView(subtitle, subtitleParams);

        for (ManagerCompatibility.ManagerInfo info : managers) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16), dp(14), dp(16), dp(14));
            row.setBackground(createRoundedBackground(ContextCompat.getColor(getContext(), R.color.surface_container_low), 14));
            row.setClickable(true);
            row.setFocusable(true);

            ImageView icon = new ImageView(getContext());
            icon.setImageResource(R.drawable.ic_shield_check);
            icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.icon_tint));
            icon.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
            row.addView(icon);

            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setPadding(dp(12), 0, 0, 0);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
            row.addView(textLayout, textParams);

            TextView nameText = text(14, true);
            nameText.setText(info.name);
            textLayout.addView(nameText);

            TextView statusText = text(12, false);
            statusText.setText(info.installed ? "Installed" : "Not installed");
            statusText.setTextColor(ContextCompat.getColor(getContext(), info.installed ? R.color.status_success : R.color.text_secondary));
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            statusParams.topMargin = dp(2);
            textLayout.addView(statusText, statusParams);

            row.setOnClickListener(v -> {
                dialog.dismiss();
                ManagerCompatibility.openInstaller(getContext(), info);
            });

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(8);
            box.addView(row, rowParams);
        }

        TextView cancel = text(14, true);
        cancel.setText("Cancel");
        cancel.setGravity(Gravity.CENTER);
        cancel.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
        cancel.setMinHeight(dp(48));
        cancel.setOnClickListener(v -> dialog.dismiss());
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(48));
        cancelParams.topMargin = dp(8);
        box.addView(cancel, cancelParams);

        dialog.setContentView(box);
        GhostLockModal.apply(dialog, false);
        dialog.setOnDismissListener(d -> GhostLockModal.clear(dialog));
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            int width = Math.min((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), dp(390));
            window.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private TextView text(int size, boolean bold) {
        TextView v = new TextView(getContext());
        v.setTextSize(size);
        v.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD);
        return v;
    }

    private GradientDrawable createRoundedBackground(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private void setSurface(int colorRes) {
        GradientDrawable surface = createRoundedBackground(ContextCompat.getColor(getContext(), colorRes), 20);
        if (getParent() instanceof View) {
            ((View) getParent()).setBackground(surface);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
