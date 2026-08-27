package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Method;

/** Tool action button that replaces legacy platform dialogs with GhostLock-styled surfaces. */
public class ThemedToolButton extends MaterialButton {
    public ThemedToolButton(Context c) { super(c); }
    public ThemedToolButton(Context c, android.util.AttributeSet a) { super(c, a); }
    public ThemedToolButton(Context c, android.util.AttributeSet a, int s) { super(c, a, s); }

    @Override public boolean performClick() {
        Object tag = getTag();
        if (tag == null) return super.performClick();
        String action = String.valueOf(tag);
        if ("import".equals(action)) { invokeBySignature(new Class[]{int.class}, new Object[]{1001}); return true; }
        if ("parse_link".equals(action)) { showUrlDialog(); return true; }
        if ("parse_boot".equals(action)) { showParseChoice(); return true; }
        return super.performClick();
    }

    private void showParseChoice() {
        Dialog d = baseDialog("Parse Boot", "Choose the source used for offset extraction.");
        LinearLayout box = content(d);
        addAction(box, "Boot image", "Parse boot.img only", () -> { d.dismiss(); invokeBySignature(new Class[]{boolean.class}, new Object[]{false}); });
        addAction(box, "Boot + XBL config", "Parse with xbl_config.img", () -> { d.dismiss(); invokeBySignature(new Class[]{boolean.class}, new Object[]{true}); });
        addCancel(box, d);
        d.show(); size(d);
    }

    private void showUrlDialog() {
        Dialog d = baseDialog("Parse Link", "Parse offsets from an OTA or supported URL.");
        LinearLayout box = content(d);
        EditText input = new EditText(getContext());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("https://...");
        input.setTextColor(0xFFF0F1F3);
        input.setHintTextColor(0xFF8D9199);
        input.setPadding(dp(14), 0, dp(14), 0);
        box.addView(input, margin(-1, dp(54), 0, 4, 0, 12));
        LinearLayout actions = new LinearLayout(getContext());
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        MaterialButton cancel = button("Cancel", false);
        cancel.setOnClickListener(v -> d.dismiss());
        MaterialButton parse = button("Parse", true);
        parse.setOnClickListener(v -> {
            String url = input.getText().toString().trim();
            if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                input.setError("Enter a valid URL");
                return;
            }
            d.dismiss();
            invokeBySignature(new Class[]{String.class, java.io.File.class}, new Object[]{url, null});
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(48));
        p.leftMargin = dp(8);
        actions.addView(parse, p);
        box.addView(actions, new LinearLayout.LayoutParams(-1, dp(54)));
        d.show();
        size(d);
        input.requestFocus();
    }

    private Dialog baseDialog(String title, String subtitle) {
        Dialog d = new Dialog(getContext());
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(20), dp(22), dp(16));
        root.setBackground(round(0xFF202124, 26));
        TextView t = text(title, 20, true);
        root.addView(t, margin(-1, -2, 0, 0, 0, 4));
        TextView s = text(subtitle, 12, false);
        root.addView(s, margin(-1, -2, 0, 0, 0, 12));
        d.setContentView(root);
        return d;
    }

    private LinearLayout content(Dialog d) {
        View content = d.findViewById(android.R.id.content);
        if (content instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) content;
            if (group.getChildCount() > 0 && group.getChildAt(0) instanceof LinearLayout) {
                return (LinearLayout) group.getChildAt(0);
            }
        }
        throw new IllegalStateException("GhostLock dialog content unavailable");
    }

    private void addAction(LinearLayout box, String title, String sub, Runnable r) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(9), dp(14), dp(9));
        row.setBackground(round(0xFF2A2D32, 16));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> r.run());
        TextView a = text(title, 14, true);
        TextView b = text(sub, 11, false);
        row.addView(a);
        row.addView(b);
        LinearLayout.LayoutParams p = margin(-1, dp(66), 0, 0, 0, 8);
        box.addView(row, p);
    }

    private void addCancel(LinearLayout box, Dialog d) {
        MaterialButton b = button("Cancel", false);
        b.setOnClickListener(v -> d.dismiss());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(48));
        p.gravity = Gravity.END;
        box.addView(b, p);
    }

    private MaterialButton button(String s, boolean primary) {
        MaterialButton b = new MaterialButton(getContext());
        b.setText(s);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setTextSize(13);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? 0xFFE7E8EB : 0xFF303238));
        b.setTextColor(primary ? 0xFF17181A : 0xFFE7E8EB);
        return b;
    }

    private TextView text(String s, int size, boolean bold) {
        TextView v = new TextView(getContext());
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(bold ? 0xFFF1F2F4 : 0xFFB8BBC2);
        if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD);
        return v;
    }

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

    private void size(Dialog d) {
        if (d.getWindow() == null) return;
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        d.getWindow().setDimAmount(.68f);
        d.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * .88f),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Resolve private MainActivity tool handlers by their parameter signature.
     * Release builds may obfuscate method names, which made name-based
     * reflection fail with messages such as h6.runExtract.  These handlers
     * have unique signatures in MainActivity, so resolving by parameter types
     * keeps the themed UI decoupled from R8-renamed method names.
     */
    private void invokeBySignature(Class<?>[] types, Object[] args) {
        try {
            if (!(getContext() instanceof MainActivity)) {
                throw new IllegalStateException("Tool host is not MainActivity");
            }
            Class<?> host = getContext().getClass();
            Method found = null;
            for (Method m : host.getDeclaredMethods()) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != types.length) continue;
                boolean match = true;
                for (int i = 0; i < params.length; i++) {
                    if (!params[i].equals(types[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    found = m;
                    break;
                }
            }
            if (found == null) {
                throw new NoSuchMethodException("No tool handler for signature");
            }
            found.setAccessible(true);
            found.invoke(getContext(), args);
        } catch (Throwable t) {
            String detail = t.getCause() != null && t.getCause().getMessage() != null
                    ? t.getCause().getMessage() : t.getMessage();
            Toast.makeText(getContext(), "Tool action failed: " + (detail == null ? "unknown error" : detail), Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
