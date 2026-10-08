package com.ghostlock.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Compact, informative manager status component. */
public class RuntimeStatusView extends FrameLayout {
    private final LinearLayout content;
    private final LinearLayout statusHeader;
    private final TextView statusTitle;
    private final TextView statusLabel;
    private final TextView message;
    private final FrameLayout managerCard;
    private final ImageView managerIcon;
    private final TextView managerName;
    private final TextView managerStatus;
    private final TextView packageLabel;
    private final TextView integrityLabel;
    private final TextView signatureValue;
    private final LinearLayout actionButtons;
    private final TextView installButton;

    public RuntimeStatusView(Context context) { this(context, null); }

    public RuntimeStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        setBackground(null);

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(13));
        addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        statusHeader = new LinearLayout(context);
        statusHeader.setOrientation(LinearLayout.VERTICAL);
        statusHeader.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        statusTitle = text(10, true);
        statusTitle.setText("RUNTIME MANAGER");
        statusTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        statusTitle.setGravity(Gravity.START);
        statusHeader.addView(statusTitle, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        statusLabel = text(12, true);
        statusLabel.setGravity(Gravity.START);
        statusLabel.setMaxLines(2);
        statusLabel.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dp(4);
        statusHeader.addView(statusLabel, statusParams);
        content.addView(statusHeader);
        message = text(11, false);
        message.setGravity(Gravity.START);
        message.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        message.setLineSpacing(dp(1), 1.0f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        msgParams.topMargin = dp(5);
        msgParams.bottomMargin = dp(10);
        content.addView(message, msgParams);

        managerCard = new FrameLayout(context);
        managerCard.setBackground(createRoundedBackground(
                ContextCompat.getColor(context, R.color.surface_container_low), 16));
        managerCard.setClipToOutline(true);
        managerCard.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(4));
            }
        });

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(dp(16), dp(14), dp(16), dp(4));
        managerCard.addView(cardContent, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout identityRow = new LinearLayout(context);
        identityRow.setOrientation(LinearLayout.HORIZONTAL);
        identityRow.setGravity(Gravity.CENTER_VERTICAL);
        identityRow.setMinimumHeight(dp(42));

        managerIcon = new ImageView(context);
        managerIcon.setScaleType(ImageView.ScaleType.CENTER);
        managerIcon.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint));
        identityRow.addView(managerIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        LinearLayout identityText = new LinearLayout(context);
        identityText.setOrientation(LinearLayout.VERTICAL);
        identityText.setGravity(Gravity.START);
        identityText.setPadding(dp(10), 0, 0, 0);
        identityRow.addView(identityText, new LinearLayout.LayoutParams(
                0, LayoutParams.WRAP_CONTENT, 1f));

        managerName = text(15, true);
        managerName.setMaxLines(2);
        managerName.setEllipsize(null);
        identityText.addView(managerName, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        managerStatus = text(11, false);
        managerStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        managerStatus.setMaxLines(1);
        managerStatus.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams managerStatusParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        managerStatusParams.topMargin = dp(3);
        identityText.addView(managerStatus, managerStatusParams);

        cardContent.addView(identityRow);

        View divider = new View(context);
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.outline_variant));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.topMargin = dp(12);
        dividerParams.bottomMargin = dp(12);
        cardContent.addView(divider, dividerParams);

        // Compact three-column metadata: Package • Signature • Integrity.
        LinearLayout details = new LinearLayout(context);
        details.setOrientation(LinearLayout.HORIZONTAL);
        details.setGravity(Gravity.TOP);
        details.setPadding(0, 0, 0, 0);

        LinearLayout packageColumn = metadataColumn(context, "PACKAGE");
        packageColumn.setMinimumWidth(dp(0));
        packageLabel = text(11, true);
        packageLabel.setSingleLine(true);
        packageLabel.setHorizontallyScrolling(true);
        packageLabel.setEllipsize(null);
        LinearLayout.LayoutParams packageValueParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        packageValueParams.topMargin = dp(5);
        packageLabel.setMaxLines(1);
        packageLabel.setEllipsize(TextUtils.TruncateAt.END);
        packageColumn.addView(packageLabel, packageValueParams);
        details.addView(packageColumn, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.15f));

        details.addView(metadataSeparator(context), new LinearLayout.LayoutParams(dp(8), LayoutParams.WRAP_CONTENT));

        LinearLayout signatureColumn = metadataColumn(context, "SIGNATURE");
        signatureValue = text(11, true);
        LinearLayout.LayoutParams signatureValueParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        signatureValueParams.topMargin = dp(5);
        signatureValue.setMaxLines(1);
        signatureValue.setEllipsize(TextUtils.TruncateAt.END);
        signatureColumn.addView(signatureValue, signatureValueParams);
        details.addView(signatureColumn, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.95f));

        details.addView(metadataSeparator(context), new LinearLayout.LayoutParams(dp(16), LayoutParams.WRAP_CONTENT));

        LinearLayout integrityColumn = metadataColumn(context, "INTEGRITY");
        integrityLabel = text(11, true);
        LinearLayout.LayoutParams integrityValueParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        integrityValueParams.topMargin = dp(5);
        integrityLabel.setMaxLines(1);
        integrityLabel.setEllipsize(TextUtils.TruncateAt.END);
        integrityColumn.addView(integrityLabel, integrityValueParams);
        details.addView(integrityColumn, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.9f));
        cardContent.addView(details);

        content.addView(managerCard);

        actionButtons = new LinearLayout(context);
        actionButtons.setOrientation(LinearLayout.HORIZONTAL);
        actionButtons.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actionButtons.setVisibility(View.GONE);
        installButton = text(12, true);
        installButton.setText("Install Manager");
        installButton.setGravity(Gravity.CENTER);
        installButton.setPadding(dp(16), dp(9), dp(16), dp(9));
        installButton.setBackground(createRoundedBackground(
                ContextCompat.getColor(context, R.color.accent), 12));
        installButton.setTextColor(ContextCompat.getColor(context, R.color.on_accent));
        actionButtons.addView(installButton);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = dp(9);
        content.addView(actionButtons, btnParams);
        refresh();
    }

    private LinearLayout metadataColumn(Context context, String label) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        TextView title = text(9, false);
        title.setText(label);
        title.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        title.setMaxLines(1);
        column.addView(title, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return column;
    }

    private TextView metadataSeparator(Context context) {
        TextView separator = text(12, false);
        separator.setText("•");
        separator.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        separator.setGravity(Gravity.CENTER);
        separator.setAlpha(0.42f);
        return separator;
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refresh(); }

    public void refresh() {
        ManagerCompatibility.Result result = ManagerCompatibility.evaluate(getContext());
        boolean showInstall = false;
        int statusColor, bgColor;
        String statusText, messageText;
        switch (result.state) {
            case READY:
                statusText = "READY";
                messageText = "Manager verified and ready to use";
                statusColor = R.color.status_success;
                bgColor = R.color.status_success_bg;
                break;
            case MANAGER_REQUIRED:
                statusText = "MANAGER REQUIRED";
                messageText = "Install a supported manager to continue";
                statusColor = R.color.accent;
                bgColor = R.color.accent_container;
                showInstall = true;
                break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:
                statusText = "NOT INSTALLED";
                messageText = "No supported manager detected on device";
                statusColor = R.color.accent;
                bgColor = R.color.accent_container;
                showInstall = true;
                break;
            case KERNEL_UNSUPPORTED:
                statusText = "KERNEL UNSUPPORTED";
                messageText = "Current kernel is not supported by GhostLock";
                statusColor = R.color.status_error;
                bgColor = R.color.status_error_bg;
                break;
            case SPOOFED_MANAGER:
                statusText = "IDENTITY MISMATCH";
                messageText = "Manager identity verification failed";
                statusColor = R.color.status_error;
                bgColor = R.color.status_error_bg;
                break;
            case UNSUPPORTED_MANAGER:
                statusText = "UNSUPPORTED MANAGER";
                messageText = "Installed manager is not registered";
                statusColor = R.color.status_error;
                bgColor = R.color.status_error_bg;
                break;
            default:
                statusText = "STATUS UNAVAILABLE";
                messageText = "Manager information unavailable";
                statusColor = R.color.text_secondary;
                bgColor = R.color.surface_container;
                break;
        }
        statusLabel.setText(statusText);
        statusLabel.setTextColor(ContextCompat.getColor(getContext(), statusColor));
        message.setText(messageText);
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
            managerIcon.setImageResource(result.manager.spoofed
                    ? R.drawable.ic_shield_alert : R.drawable.ic_shield_check);
            packageLabel.setText(result.manager.packageName);
            packageLabel.setSingleLine(true);
            packageLabel.setHorizontallyScrolling(true);
            packageLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            signatureValue.setText(result.manager.identityVerified ? "Verified" : "Not Verified");
            signatureValue.setTextColor(ContextCompat.getColor(getContext(),
                    result.manager.identityVerified ? R.color.status_success : R.color.text_secondary));
            integrityLabel.setText(result.manager.identityVerified ? "✓ Verified" : "Not Verified");
            integrityLabel.setTextColor(ContextCompat.getColor(getContext(),
                    result.manager.identityVerified ? R.color.status_success : R.color.text_secondary));
        } else {
            managerName.setText("No Manager");
            managerStatus.setText("Not installed");
            managerStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            managerIcon.setImageResource(R.drawable.ic_shield_alert);
            packageLabel.setText("—");
            packageLabel.setMaxLines(1);
            packageLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            signatureValue.setText("Not Verified");
            signatureValue.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            integrityLabel.setText("Not available");
            integrityLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        }
        actionButtons.setVisibility(showInstall ? View.VISIBLE : View.GONE);
        if (showInstall) installButton.setOnClickListener(v -> showManagerPicker());
        setSurface(bgColor);
    }

    private void showManagerPicker() {
        final java.util.List<ManagerCompatibility.ManagerInfo> managers =
                ManagerCompatibility.registeredManagers(getContext());
        final android.app.Dialog dialog = new android.app.Dialog(getContext());
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(22), dp(24), dp(18));
        box.setBackground(createRoundedBackground(
                ContextCompat.getColor(getContext(), R.color.surface), 26));
        TextView title = text(20, true);
        title.setText("Install Manager");
        box.addView(title);
        TextView subtitle = text(13, false);
        subtitle.setText("Select a supported manager to continue");
        subtitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(6);
        subtitleParams.bottomMargin = dp(16);
        box.addView(subtitle, subtitleParams);
        for (ManagerCompatibility.ManagerInfo info : managers) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16), dp(14), dp(16), dp(14));
            row.setBackground(createRoundedBackground(
                    ContextCompat.getColor(getContext(), R.color.surface_container_low), 14));
            row.setClickable(true);
            row.setFocusable(true);
            ImageView icon = new ImageView(getContext());
            icon.setImageResource(R.drawable.ic_shield_check);
            icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.icon_tint));
            row.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));
            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setPadding(dp(12), 0, 0, 0);
            row.addView(textLayout, new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, 1f));
            TextView nameText = text(14, true);
            nameText.setText(info.name);
            textLayout.addView(nameText);
            TextView statusText = text(12, false);
            statusText.setText(info.installed ? "Installed" : "Not installed");
            statusText.setTextColor(ContextCompat.getColor(getContext(),
                    info.installed ? R.color.status_success : R.color.text_secondary));
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            statusParams.topMargin = dp(2);
            textLayout.addView(statusText, statusParams);
            row.setOnClickListener(v -> {
                dialog.dismiss();
                ManagerCompatibility.openInstaller(getContext(), info);
            });
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(8);
            box.addView(row, rowParams);
        }
        TextView cancel = text(14, true);
        cancel.setText("Cancel");
        cancel.setGravity(Gravity.CENTER);
        cancel.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
        cancel.setMinHeight(dp(48));
        cancel.setOnClickListener(v -> dialog.dismiss());
        box.addView(cancel, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(48)));
        dialog.setContentView(box);
        GhostLockModal.apply(dialog, false);
        dialog.setOnDismissListener(d -> GhostLockModal.clear(dialog));
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    Math.min((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), dp(390)),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private TextView text(int size, boolean bold) {
        TextView v = new TextView(getContext());
        v.setTextSize(size);
        v.setIncludeFontPadding(false);
        v.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD);
        return v;
    }

    private GradientDrawable createRoundedBackground(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(Math.min(radius, 4)));
        return d;
    }

    private void setSurface(int colorRes) {
        GradientDrawable surface = createRoundedBackground(
                ContextCompat.getColor(getContext(), colorRes), 16);
        if (getParent() instanceof View) ((View) getParent()).setBackground(surface);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}