package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

/** Parse launcher using the same compact panel language as the redesign dialogs. */
public class ParseActionButton extends MaterialButton {
    public ParseActionButton(Context context) { super(context); }
    public ParseActionButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); }
    public ParseActionButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override public boolean performClick() {
        if (getId() == R.id.otaButton) {
            showUrlDialog();
        } else {
            showParseDialog();
        }
        return true;
    }

    private void showParseDialog() {
        Dialog dialog = createDialog();
        LinearLayout box = box();
        TextView title = text(20, true);
        title.setText(R.string.parse_title);
        box.addView(title, margin(-1, -2, 0, 0, 0, 5));
        TextView subtitle = text(12, false);
        subtitle.setText("Choose the image source to parse.");
        box.addView(subtitle, margin(-1, -2, 0, 0, 0, 16));
        addChoice(box, getString(R.string.parse_option_boot), "Parse boot.img", () -> {
            dialog.dismiss();
            invoke("pickParseBoot", new Class<?>[]{boolean.class}, new Object[]{false});
        });
        addChoice(box, getString(R.string.parse_option_boot_xbl), "Parse boot.img with XBL config", () -> {
            dialog.dismiss();
            invoke("pickParseBoot", new Class<?>[]{boolean.class}, new Object[]{true});
        });
        MaterialButton cancel = actionButton(getString(R.string.cancel), false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        box.addView(cancel, margin(-1, dp(48), 0, 7, 0, 0));
        show(dialog, box);
    }

    private void showUrlDialog() {
        Dialog dialog = createDialog();
        LinearLayout box = box();
        TextView title = text(20, true);
        title.setText(R.string.parse_url_title);
        box.addView(title, margin(-1, -2, 0, 0, 0, 5));
        TextView subtitle = text(12, false);
        subtitle.setText("Enter the OTA URL to parse.");
        box.addView(subtitle, margin(-1, -2, 0, 0, 0, 14));
        EditText input = new EditText(getContext());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint(R.string.parse_url_hint);
        input.setTextColor(color(R.color.text_primary));
        input.setHintTextColor(color(R.color.text_secondary));
        box.addView(input, margin(-1, dp(54), 0, 0, 0, 16));
        LinearLayout actions = new LinearLayout(getContext());
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        MaterialButton cancel = actionButton(getString(R.string.cancel), false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        MaterialButton start = actionButton(getString(R.string.parse_start), true);
        start.setOnClickListener(v -> {
            String url = input.getText().toString().trim();
            if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
                Toast.makeText(getContext(), R.string.parse_failed_url, Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            invoke("runExtract", new Class<?>[]{String.class, String.class}, new Object[]{url, null});
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-2, dp(48));
        sp.leftMargin = dp(8);
        actions.addView(start, sp);
        box.addView(actions, new LinearLayout.LayoutParams(-1, dp(52)));
        show(dialog, box);
    }

    private void addChoice(LinearLayout box, String title, String subtitle, Runnable action) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setBackground(round(color(R.color.surface_container_low), 16));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> action.run());
        TextView t = text(14, true);
        t.setText(title);
        row.addView(t);
        TextView s = text(11, false);
        s.setText(subtitle);
        row.addView(s);
        box.addView(row, margin(-1, dp(64), 0, 0, 0, 9));
    }

    private void invoke(String name, Class<?>[] types, Object[] args) {
        try {
            java.lang.reflect.Method method = MainActivity.class.getDeclaredMethod(name, types);
            method.setAccessible(true);
            method.invoke(getContext(), args);
        } catch (Throwable t) {
            Toast.makeText(getContext(), t.getMessage() == null ? "Parse failed" : t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Dialog createDialog() { return new Dialog(getContext()); }
    private LinearLayout box() {
        LinearLayout b = new LinearLayout(getContext());
        b.setOrientation(LinearLayout.VERTICAL);
        b.setPadding(dp(22), dp(20), dp(22), dp(16));
        b.setBackground(round(color(R.color.surface_container), 26));
        return b;
    }
    private void show(Dialog dialog, LinearLayout box) {
        dialog.setContentView(box);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(.60f);
            dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * .88f), -2);
        }
    }
    private MaterialButton actionButton(String value, boolean primary) {
        MaterialButton b = new MaterialButton(getContext());
        b.setText(value);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setTextSize(13);
        b.setBackgroundTintList(ColorStateList.valueOf(color(primary ? R.color.accent : R.color.surface_container_low)));
        b.setTextColor(color(primary ? R.color.on_accent : R.color.text_primary));
        return b;
    }
    private TextView text(int size, boolean bold) {
        TextView v = new TextView(getContext());
        v.setTextSize(size);
        v.setTextColor(color(bold ? R.color.text_primary : R.color.text_secondary));
        if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD);
        return v;
    }
    private int color(int id) { return ContextCompat.getColor(getContext(), id); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }
    private GradientDrawable round(int c, int r) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(c);
        d.setCornerRadius(dp(r));
        return d;
    }
}
