package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Large, low-contrast manager-state glyph used as a decorative overlay. */
public class ManagerWatermarkView extends View {
    private static final float ALPHA = 0.10f;
    private static final float SIZE_DP = 132f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int state = -1;
    private int color;

    public ManagerWatermarkView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setState(int state, int color) {
        this.state = state;
        this.color = color;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (color == 0) return;

        float density = getResources().getDisplayMetrics().density;
        float size = SIZE_DP * density;
        paint.setColor(color);
        paint.setAlpha(Math.round(ALPHA * 255f));
        paint.setStrokeWidth(Math.max(2f * density, size * 0.038f));

        // Large and right-shifted so the emblem naturally clips at the card edge.
        float cx = getWidth() - size * 0.20f;
        float cy = getHeight() - size * 0.18f;
        canvas.save();
        canvas.translate(cx - size / 2f, cy - size / 2f);
        drawGlyph(canvas, size);
        canvas.restore();
    }

    private void drawGlyph(Canvas canvas, float size) {
        switch (state) {
            case 0: drawShield(canvas, size); drawCheck(canvas, size); break;
            case 1:
            case 2: drawPackage(canvas, size); drawPlus(canvas, size); break;
            case 3: drawShield(canvas, size); drawSlash(canvas, size); break;
            case 4: drawPuzzle(canvas, size); break;
            case 5: drawShield(canvas, size); drawExclamation(canvas, size); break;
            default: drawQuestion(canvas, size); break;
        }
    }

    private void drawShield(Canvas c, float s) {
        path.reset();
        path.moveTo(s * .50f, s * .10f);
        path.lineTo(s * .82f, s * .22f);
        path.lineTo(s * .78f, s * .57f);
        path.cubicTo(s * .74f, s * .76f, s * .62f, s * .88f, s * .50f, s * .94f);
        path.cubicTo(s * .38f, s * .88f, s * .26f, s * .76f, s * .22f, s * .57f);
        path.lineTo(s * .18f, s * .22f);
        path.close();
        c.drawPath(path, paint);
    }

    private void drawCheck(Canvas c, float s) {
        path.reset();
        path.moveTo(s * .34f, s * .51f);
        path.lineTo(s * .46f, s * .63f);
        path.lineTo(s * .68f, s * .38f);
        c.drawPath(path, paint);
    }

    private void drawSlash(Canvas c, float s) { c.drawLine(s * .22f, s * .78f, s * .78f, s * .22f, paint); }

    private void drawExclamation(Canvas c, float s) {
        c.drawLine(s * .50f, s * .31f, s * .50f, s * .62f, paint);
        c.drawCircle(s * .50f, s * .74f, s * .025f, paint);
    }

    private void drawPlus(Canvas c, float s) {
        c.drawLine(s * .50f, s * .30f, s * .50f, s * .70f, paint);
        c.drawLine(s * .30f, s * .50f, s * .70f, s * .50f, paint);
    }

    private void drawPackage(Canvas c, float s) {
        RectF box = new RectF(s * .22f, s * .25f, s * .78f, s * .76f);
        c.drawRoundRect(box, s * .07f, s * .07f, paint);
        path.reset();
        path.moveTo(s * .22f, s * .40f);
        path.lineTo(s * .50f, s * .53f);
        path.lineTo(s * .78f, s * .40f);
        c.drawPath(path, paint);
        c.drawLine(s * .50f, s * .53f, s * .50f, s * .76f, paint);
    }

    private void drawPuzzle(Canvas c, float s) {
        path.reset();
        path.moveTo(s * .30f, s * .30f);
        path.lineTo(s * .43f, s * .30f);
        path.cubicTo(s * .42f, s * .22f, s * .47f, s * .17f, s * .54f, s * .17f);
        path.cubicTo(s * .62f, s * .17f, s * .67f, s * .23f, s * .66f, s * .30f);
        path.lineTo(s * .78f, s * .30f);
        path.lineTo(s * .78f, s * .43f);
        path.cubicTo(s * .86f, s * .42f, s * .91f, s * .47f, s * .91f, s * .54f);
        path.cubicTo(s * .91f, s * .62f, s * .85f, s * .67f, s * .78f, s * .66f);
        path.lineTo(s * .78f, s * .78f);
        path.lineTo(s * .30f, s * .78f);
        path.close();
        c.drawPath(path, paint);
    }

    private void drawQuestion(Canvas c, float s) {
        RectF arc = new RectF(s * .31f, s * .18f, s * .69f, s * .58f);
        c.drawArc(arc, 210f, 300f, false, paint);
        c.drawLine(s * .50f, s * .58f, s * .50f, s * .70f, paint);
        c.drawCircle(s * .50f, s * .80f, s * .025f, paint);
    }
}
