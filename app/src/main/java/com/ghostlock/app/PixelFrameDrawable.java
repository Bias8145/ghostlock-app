package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.StateSet;

/**
 * Hard-edged 8-bit frame used by the dashboard panels and action buttons.
 * No blur, rounded corners, or interpolated shadows: the silhouette is built
 * from a small chamfer and a fixed offset shadow.
 */
public final class PixelFrameDrawable extends Drawable {
    public enum Kind { PANEL, BUTTON }

    private final float density;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Path shadowPath = new Path();
    private final Kind kind;
    private final int normalColor;
    private final int pressedColor;
    private final int borderColor;
    private final int shadowColor;
    private final float cut;
    private final float stroke;
    private final float offset;
    private boolean pressed;

    public PixelFrameDrawable(Context context, Kind kind) {
        this.kind = kind;
        density = context.getResources().getDisplayMetrics().density;
        normalColor = context.getResources().getColor(
                kind == Kind.BUTTON ? R.color.accent : R.color.surface_container_low,
                context.getTheme());
        pressedColor = context.getResources().getColor(
                kind == Kind.BUTTON ? R.color.accent_pressed : R.color.surface_container,
                context.getTheme());
        borderColor = context.getResources().getColor(R.color.border, context.getTheme());
        shadowColor = context.getResources().getColor(R.color.pixel_shadow, context.getTheme());
        cut = dp(kind == Kind.BUTTON ? 4 : 5);
        stroke = dp(2);
        offset = dp(kind == Kind.BUTTON ? 4 : 3);

        fill.setStyle(Paint.Style.FILL);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(stroke);
        border.setStrokeJoin(Paint.Join.MITER);
        border.setStrokeCap(Paint.Cap.SQUARE);
        shadow.setStyle(Paint.Style.FILL);
    }

    @Override public void draw(Canvas canvas) {
        RectF b = new RectF(getBounds());
        float inset = stroke / 2f;

        makePath(shadowPath, b.left + offset, b.top + offset, b.right + offset, b.bottom + offset, cut);
        shadow.setColor(shadowColor);
        canvas.drawPath(shadowPath, shadow);

        makePath(path, b.left + inset, b.top + inset,
                b.right - inset - (kind == Kind.BUTTON ? 0 : 0),
                b.bottom - inset - (kind == Kind.BUTTON ? offset : 0), cut);
        fill.setColor(pressed ? pressedColor : normalColor);
        canvas.drawPath(path, fill);

        border.setColor(borderColor);
        canvas.drawPath(path, border);

        if (kind == Kind.BUTTON && !pressed) {
            Paint highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
            highlight.setColor(borderColor);
            highlight.setStyle(Paint.Style.STROKE);
            highlight.setStrokeWidth(dp(1));
            highlight.setStrokeJoin(Paint.Join.MITER);
            Path top = new Path();
            top.moveTo(b.left + cut, b.top + dp(1.5f));
            top.lineTo(b.right - cut - dp(1), b.top + dp(1.5f));
            canvas.drawPath(top, highlight);
        }
    }

    private void makePath(Path out, float left, float top, float right, float bottom, float c) {
        out.reset();
        out.moveTo(left, top + c);
        out.lineTo(left + c, top);
        out.lineTo(right - c, top);
        out.lineTo(right, top + c);
        out.lineTo(right, bottom - c);
        out.lineTo(right - c, bottom);
        out.lineTo(left + c, bottom);
        out.lineTo(left, bottom - c);
        out.close();
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
    @Override public void setAlpha(int alpha) { fill.setAlpha(alpha); border.setAlpha(alpha); shadow.setAlpha(alpha); }
    @Override public void setColorFilter(android.graphics.ColorFilter filter) {
        fill.setColorFilter(filter); border.setColorFilter(filter); shadow.setColorFilter(filter);
    }
    @Override public int getIntrinsicHeight() { return -1; }
    @Override public int getIntrinsicWidth() { return -1; }
}
