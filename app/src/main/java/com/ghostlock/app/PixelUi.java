package com.ghostlock.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * GhostLock Material 3 visual adapter.
 *
 * This class intentionally contains no pixel/retro rendering. It keeps the
 * existing XML screen compatible while moving shared components onto the
 * Material 3 expressive visual language.
 */
public final class PixelUi {
    private PixelUi() {}

    public static void apply(Activity activity, View root) {
        if (root == null) return;
        walk(activity, root);
    }

    private static void walk(Context context, View view) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            boolean bold = text.getTypeface() != null && text.getTypeface().isBold();
            text.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            text.getPaint().setAntiAlias(true);
        }

        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            card.setCardElevation(dp(context, 1));
            card.setRadius(dp(context, 24));
            card.setStrokeWidth(dp(context, 1));
            card.setStrokeColor(context.getColor(R.color.outline_variant));
            card.setCardBackgroundColor(context.getColor(R.color.surface_container_low));
            card.setUseCompatPadding(false);
        }

        if (view instanceof MaterialButton) {
            MaterialButton button = (MaterialButton) view;
            button.setAllCaps(false);
            button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            button.setTextSize(14);
            button.setCornerRadius(dp(context, 18));
            button.setStrokeWidth(0);
            button.setElevation(dp(context, 1));
            button.setInsetTop(0);
            button.setInsetBottom(0);
            button.setBackgroundTintList(ColorStateList.valueOf(
                    button.isEnabled()
                            ? context.getColor(R.color.primary)
                            : context.getColor(R.color.surface_container_high)));
            button.setTextColor(button.isEnabled()
                    ? context.getColor(R.color.on_primary)
                    : context.getColor(R.color.text_disabled));
            button.setRippleColor(ColorStateList.valueOf(context.getColor(R.color.primary_pressed)));
        }

        if (view instanceof ImageView) {
            ImageView image = (ImageView) view;
            image.setAlpha(1f);
            image.clearColorFilter();
            if (image.getDrawable() != null) {
                image.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.icon_tint)));
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                walk(context, group.getChildAt(i));
            }
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
