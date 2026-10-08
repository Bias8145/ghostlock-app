package com.ghostlock.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/** Central visual pass for the GhostLock 8-bit interface. */
public final class PixelUi {
    private PixelUi() {}

    public static void apply(Activity activity, View root) {
        if (root == null) return;
        walk(activity, root, Typeface.MONOSPACE);
    }

    private static void walk(Context context, View view, Typeface pixelTypeface) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            boolean bold = text.getTypeface() != null && text.getTypeface().isBold();
            text.setTypeface(pixelTypeface, bold ? Typeface.BOLD : Typeface.NORMAL);
            text.getPaint().setAntiAlias(false);
            text.getPaint().setSubpixelText(false);
        }

        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            card.setCardElevation(0);
            card.setRadius(0);
            card.setStrokeWidth(0);
            card.setCardBackgroundColor(Color.TRANSPARENT);
            card.setBackground(new PixelFrameDrawable(context, PixelFrameDrawable.Kind.PANEL));
            card.setUseCompatPadding(false);
        }

        if (view instanceof MaterialButton) {
            MaterialButton button = (MaterialButton) view;
            button.setCornerRadius(0);
            button.setStrokeWidth(0);
            button.setElevation(0);
            button.setInsetTop(0);
            button.setInsetBottom(0);
            button.setBackgroundTintList(null);
            Drawable background = new PixelFrameDrawable(context, PixelFrameDrawable.Kind.BUTTON);
            button.setBackground(background);
            button.setPadding(dp(context, 10), dp(context, 2), dp(context, 10), dp(context, 2));
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                walk(context, group.getChildAt(i), pixelTypeface);
            }
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
