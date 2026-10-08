package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.StateSet;

/** Retro 3D pixel frame with rounded pixel-friendly corners and hard depth. */
public final class PixelFrameDrawable extends Drawable {
    public enum Kind { PANEL, BUTTON }

    private final float density;
    private final Paint fill = new Paint();
    private final Paint border = new Paint();
    private final Paint shadow = new Paint();
    private final Paint bevel = new Paint();
    private final Kind kind;
    private final int normalColor, pressedColor, borderColor, shadowColor, highlightColor;
    private final float radius, stroke, offset;
    private boolean pressed;

    public PixelFrameDrawable(Context context, Kind kind) {
        this.kind = kind;
        density = context.getResources().getDisplayMetrics().density;
        normalColor = context.getResources().getColor(kind == Kind.BUTTON ? R.color.accent : R.color.surface_container_low, context.getTheme());
        pressedColor = context.getResources().getColor(kind == Kind.BUTTON ? R.color.accent_pressed : R.color.surface_container, context.getTheme());
        borderColor = context.getResources().getColor(R.color.border, context.getTheme());
        shadowColor = context.getResources().getColor(R.color.pixel_shadow, context.getTheme());
        highlightColor = context.getResources().getColor(R.color.pixel_highlight, context.getTheme());

        // Rounded ends remain visible, but the small radius keeps the shape chunky and retro.
        radius = dp(kind == Kind.BUTTON ? 6 : 7);
        stroke = dp(2);
        offset = dp(kind == Kind.BUTTON ? 4 : 3);

        fill.setStyle(Paint.Style.FILL);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(stroke);
        border.setStrokeJoin(Paint.Join.ROUND);
        border.setStrokeCap(Paint.Cap.SQUARE);
        shadow.setStyle(Paint.Style.FILL);
        bevel.setStyle(Paint.Style.FILL);
        fill.setAntiAlias(false);
        border.setAntiAlias(false);
        shadow.setAntiAlias(false);
        bevel.setAntiAlias(false);
    }

    @Override public void draw(Canvas canvas) {
        RectF b = new RectF(getBounds());
        float inset = stroke / 2f;
        float depth = pressed ? dp(2) : offset;

        // Dark backing provides the chunky lower/right 3D extrusion.
        shadow.setColor(shadowColor);
        RectF shadowRect = new RectF(b.left + dp(1), b.top + dp(1), b.right - dp(1), b.bottom - dp(1));
        canvas.drawRoundRect(shadowRect, radius, radius, shadow);

        // Face is pulled up/left, leaving a hard pixel-like depth strip exposed.
        RectF face = new RectF(b.left + inset, b.top + inset, b.right - inset - depth, b.bottom - inset - depth);
        fill.setColor(pressed ? pressedColor : normalColor);
        canvas.drawRoundRect(face, radius, radius, fill);

        border.setColor(borderColor);
        canvas.drawRoundRect(face, radius, radius, border);

        // Block highlights instead of gradients or glow.
        bevel.setColor(highlightColor);
        float p = dp(2);
        canvas.drawRect(face.left + radius, face.top + p, face.right - radius, face.top + p + p, bevel);
        canvas.drawRect(face.left + p, face.top + radius, face.left + p + p, face.bottom - radius, bevel);

        // Reinforce the extrusion with square pixel blocks on the lower/right edge.
        if (!pressed) {
            float d = dp(2);
            canvas.drawRect(face.right - d, face.top + radius, face.right, face.bottom - radius, shadow);
            canvas.drawRect(face.left + radius, face.bottom - d, face.right - radius, face.bottom, shadow);
        }
    }

    private float dp(float value) { return value * density; }

    @Override protected boolean onStateChange(int[] stateSet) {
        boolean next = StateSet.stateSetMatches(new int[]{android.R.attr.state_pressed}, stateSet);
        if (next != pressed) {
            pressed = next;
            invalidateSelf();
            return true;
        }
        return false;
    }

    @Override public boolean isStateful() { return true; }
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    @Override public void setAlpha(int alpha) {
        fill.setAlpha(alpha); border.setAlpha(alpha); shadow.setAlpha(alpha); bevel.setAlpha(alpha);
    }
    @Override public void setColorFilter(android.graphics.ColorFilter filter) {
        fill.setColorFilter(filter); border.setColorFilter(filter); shadow.setColorFilter(filter); bevel.setColorFilter(filter);
    }
    @Override public int getIntrinsicHeight() { return -1; }
    @Override public int getIntrinsicWidth() { return -1; }
}
