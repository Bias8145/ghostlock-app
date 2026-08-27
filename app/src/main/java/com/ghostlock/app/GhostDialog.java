package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

/** Small, reusable GhostLock-themed dialog surface. */
public final class GhostDialog {
    private GhostDialog() {}

    public static Dialog base(Context c, String title, String message) {
        Dialog d = new Dialog(c);
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(c, 22), dp(c, 20), dp(c, 22), dp(c, 14));
        box.setBackground(round(0xFF202226, 26, c));
        TextView t = text(c, title, 19, 0xFFF1F2F4, true);
        box.addView(t, lp(-1, -2, 0, 0, 0, 0));
        if (message != null && !message.isEmpty()) {
            TextView m = text(c, message, 13, 0xFFB8BBC2, false);
            box.addView(m, lp(-1, -2, 0, 7, 0, 8));
        }
        d.setContentView(box);
        d.setOnShowListener(x -> size(d, c));
        return d;
    }

    public static void showChoice(Context c, String title, String message, String[] choices, int[] icons, ChoiceListener listener) {
        Dialog d = base(c, title, message);
        LinearLayout box = (LinearLayout) d.findViewById(android.R.id.content);
        // Dialog content is the supplied root; retrieve it directly when possible.
        View root = d.getWindow() == null ? null : d.getWindow().getDecorView().findViewWithTag("ghost_root");
        if (root == null) root = d.getWindow() == null ? null : d.getWindow().getDecorView().findViewById(android.R.id.content);
        LinearLayout content = (LinearLayout) d.getWindow().getDecorView().findViewById(android.R.id.content);
        for (int i = 0; i < choices.length; i++) {
            final int index = i;
            MaterialButton b = new MaterialButton(c);
            b.setText(choices[i]); b.setAllCaps(false); b.setTextSize(13); b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            b.setMinWidth(0); b.setMinimumHeight(dp(c, 50));
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2B2D32));
            b.setTextColor(0xFFE8E9EC); b.setCornerRadius(dp(c, 15));
            if (icons != null && i < icons.length && icons[i] != 0) b.setIcon(c.getDrawable(icons[i]));
            b.setOnClickListener(v -> { d.dismiss(); listener.onChoice(index); });
            content.addView(b, lp(-1, 50, 0, i == 0 ? 0 : 7, 0, 0));
        }
        MaterialButton cancel = button(c, "Cancel", false);
        cancel.setOnClickListener(v -> d.dismiss());
        content.addView(cancel, lp(-2, 46, 0, 10, 0, 0));
        show(d, c);
    }

    public static void showInput(Context c, String title, String message, String hint, InputListener listener) {
        Dialog d = base(c, title, message);
        LinearLayout content = (LinearLayout) d.getWindow().getDecorView().findViewById(android.R.id.content);
        EditText input = new EditText(c);
        input.setSingleLine(true); input.setHint(hint); input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setTextColor(0xFFE8E9EC); input.setHintTextColor(0xFF858992);
        input.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF8F96A3));
        content.addView(input, lp(-1, 56, 0, 8, 0, 0));
        LinearLayout actions = new LinearLayout(c); actions.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        MaterialButton cancel = button(c, "Cancel", false); cancel.setOnClickListener(v -> d.dismiss());
        MaterialButton ok = button(c, "Parse  →", true); ok.setOnClickListener(v -> { String s=input.getText().toString().trim(); d.dismiss(); listener.onInput(s); });
        actions.addView(cancel, lp(-2, 46, 0, 0, 6, 0)); actions.addView(ok, lp(-2, 46, 6, 0, 0, 0));
        content.addView(actions, lp(-1, 52, 0, 10, 0, 0));
        show(d, c);
        input.requestFocus();
    }

    private static MaterialButton button(Context c, String text, boolean primary) {
        MaterialButton b = new MaterialButton(c); b.setText(text); b.setAllCaps(false); b.setMinWidth(0); b.setCornerRadius(dp(c, 15));
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? 0xFFE8E9EC : 0xFF2B2D32));
        b.setTextColor(primary ? 0xFF17181A : 0xFFE8E9EC); return b;
    }
    private static TextView text(Context c,String s,int size,int color,boolean bold){TextView v=new TextView(c);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private static LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(l,t,r,b);return p;}
    private static GradientDrawable round(int color,int radius,Context c){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));return d;}
    private static int dp(Context c,int v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
    private static void size(Dialog d,Context c){if(d.getWindow()!=null){d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);d.getWindow().setDimAmount(.68f);d.getWindow().setLayout((int)(c.getResources().getDisplayMetrics().widthPixels*.88f),-2);}}
    private static void show(Dialog d,Context c){d.show();size(d,c);}
    public interface ChoiceListener{void onChoice(int index);}
    public interface InputListener{void onInput(String input);}
}
