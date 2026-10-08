package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.StateSet;

/** Hard-edged 8-bit frame with stepped corners and an offset pixel shadow. */
public final class PixelFrameDrawable extends Drawable {
    public enum Kind { PANEL, BUTTON }
    private final float density;
    private final Paint fill = new Paint();
    private final Paint border = new Paint();
    private final Paint shadow = new Paint();
    private final Path path = new Path();
    private final Path shadowPath = new Path();
    private final Kind kind;
    private final int normalColor, pressedColor, borderColor, shadowColor;
    private final float step, stroke, offset;
    private boolean pressed;

    public PixelFrameDrawable(Context context, Kind kind) {
        this.kind = kind;
        density = context.getResources().getDisplayMetrics().density;
        normalColor = context.getResources().getColor(kind == Kind.BUTTON ? R.color.accent : R.color.surface_container_low, context.getTheme());
        pressedColor = context.getResources().getColor(kind == Kind.BUTTON ? R.color.accent_pressed : R.color.surface_container, context.getTheme());
        borderColor = context.getResources().getColor(R.color.border, context.getTheme());
        shadowColor = context.getResources().getColor(R.color.pixel_shadow, context.getTheme());
        step = dp(kind == Kind.BUTTON ? 3 : 4);
        stroke = dp(2);
        offset = dp(kind == Kind.BUTTON ? 4 : 3);
        fill.setStyle(Paint.Style.FILL);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(stroke);
        border.setStrokeJoin(Paint.Join.MITER);
        border.setStrokeCap(Paint.Cap.SQUARE);
        shadow.setStyle(Paint.Style.FILL);
        fill.setAntiAlias(false);
        border.setAntiAlias(false);
        shadow.setAntiAlias(false);
    }

    @Override public void draw(Canvas canvas) {
        RectF b = new RectF(getBounds());
        float inset = stroke / 2f;
        makeSteppedPath(shadowPath, b.left, b.top, b.right, b.bottom, step);
        shadow.setColor(shadowColor);
        canvas.drawPath(shadowPath, shadow);

        // Leave a hard 3-4dp depth strip visible on the lower/right edges.
        makeSteppedPath(path, b.left + inset, b.top + inset,
                b.right - inset - offset, b.bottom - inset - offset, step);
        fill.setColor(pressed ? pressedColor : normalColor);
        canvas.drawPath(path, fill);
        border.setColor(borderColor);
        canvas.drawPath(path, border);
    }

    private void makeSteppedPath(Path out, float left, float top, float right, float bottom, float s) {
        float x1 = left + s, x2 = left + s * 2f;
        float r1 = right - s, r2 = right - s * 2f;
        float y1 = top + s, y2 = top + s * 2f;
        float b1 = bottom - s, b2 = bottom - s * 2f;
        out.reset();
        out.moveTo(left, y2);
        out.lineTo(x1, y2); out.lineTo(x1, y1); out.lineTo(x2, y1); out.lineTo(x2, top);
        out.lineTo(r2, top); out.lineTo(r2, y1); out.lineTo(r1, y1); out.lineTo(r1, y2); out.lineTo(right, y2);
        out.lineTo(right, b2); out.lineTo(r1, b2); out.lineTo(r1, b1); out.lineTo(r2, b1); out.lineTo(r2, bottom);
        out.lineTo(x2, bottom); out.lineTo(x2, b1); out.lineTo(x1, b1); out.lineTo(x1, b2); out.lineTo(left, b2);
        out.close();
    }

    private float dp(float value) { return value * density; }
    @Override protected boolean onStateChange(int[] stateSet) {
        boolean next = StateSet.stateSetMatches(new int[]{android.R.attr.state_pressed}, stateSet);
        if (next != pressed) { pressed = next; invalidateSelf(); return true; }
        return false;
    }
    @Override public boolean isStateful() { return true; }
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    @Override public void setAlpha(int alpha) { fill.setAlpha(alpha); border.setAlpha(alpha); shadow.setAlpha(alpha); }
    @Override public void setColorFilter(android.graphics.ColorFilter filter) { fill.setColorFilter(filter); border.setColorFilter(filter); shadow.setColorFilter(filter); }
    @Override public int getIntrinsicHeight() { return -1; }
    @Override public int getIntrinsicWidth() { return -1; }
}
