package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import androidx.core.content.ContextCompat;

/** Compact, subtle decorative state glyph for the manager status card. */
public final class ManagerWatermarkView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private ManagerCompatibility.State state = ManagerCompatibility.State.MANAGER_REQUIRED;
    private int color;

    public ManagerWatermarkView(Context context) {
        super(context);
        color = ContextCompat.getColor(context, R.color.accent);
        setWillNotDraw(false);
        setAlpha(0.13f);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);
        setFocusable(false);
    }

    public void setState(ManagerCompatibility.State state, int color) {
        this.state = state == null ? ManagerCompatibility.State.MANAGER_REQUIRED : state;
        this.color = color;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Keep the glyph proportionate to the compact status card rather than
        // using an oversized watermark. The reserved container provides a
        // consistent visual anchor without competing with the card content.
        float size = Math.min(dp(64), Math.min(w * .90f, h * .90f));
        canvas.save();
        canvas.translate(w * .50f, h * .50f);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(dp(2), size * .045f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        switch (state) {
            case READY: drawShield(canvas, size); drawCheck(canvas, size); break;
            case MANAGER_REQUIRED: drawPackage(canvas, size); drawPlus(canvas, size); break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED: drawWarning(canvas, size); break;
            case KERNEL_UNSUPPORTED: drawShield(canvas, size); drawSlash(canvas, size); break;
            case UNSUPPORTED_MANAGER: drawPuzzle(canvas, size); break;
            case SPOOFED_MANAGER: drawShield(canvas, size); drawExclamation(canvas, size); break;
            default: drawQuestion(canvas, size); break;
        }
        canvas.restore();
    }

    private void drawShield(Canvas c, float s) { path.reset(); path.moveTo(0,-s*.43f); path.cubicTo(s*.22f,-s*.36f,s*.35f,-s*.31f,s*.37f,-s*.26f); path.lineTo(s*.33f,s*.02f); path.cubicTo(s*.29f,s*.24f,s*.13f,s*.38f,0,s*.46f); path.cubicTo(-s*.13f,s*.38f,-s*.29f,s*.24f,-s*.33f,s*.02f); path.lineTo(-s*.37f,-s*.26f); path.cubicTo(-s*.35f,-s*.31f,-s*.22f,-s*.36f,0,-s*.43f); path.close(); c.drawPath(path,paint); }
    private void drawCheck(Canvas c,float s){ path.reset(); path.moveTo(-s*.19f,s*.01f); path.lineTo(-s*.05f,s*.15f); path.lineTo(s*.22f,-s*.15f); c.drawPath(path,paint); }
    private void drawPlus(Canvas c,float s){ c.drawLine(s*.10f,-s*.13f,s*.10f,s*.13f,paint); c.drawLine(-s*.03f,0,s*.23f,0,paint); }
    private void drawPackage(Canvas c,float s){ rect.set(-s*.29f,-s*.24f,s*.29f,s*.24f); c.drawRoundRect(rect,s*.04f,s*.04f,paint); c.drawLine(-s*.29f,-s*.08f,0,s*.08f,paint); c.drawLine(s*.29f,-s*.08f,0,s*.08f,paint); c.drawLine(0,s*.08f,0,s*.24f,paint); }
    private void drawWarning(Canvas c,float s){ path.reset(); path.moveTo(0,-s*.43f); path.lineTo(s*.40f,s*.30f); path.lineTo(-s*.40f,s*.30f); path.close(); c.drawPath(path,paint); c.drawLine(0,-s*.18f,0,s*.07f,paint); c.drawCircle(0,s*.19f,s*.018f,paint); }
    private void drawSlash(Canvas c,float s){ c.drawLine(-s*.29f,s*.34f,s*.30f,-s*.34f,paint); }
    private void drawExclamation(Canvas c,float s){ c.drawLine(0,-s*.20f,0,s*.09f,paint); c.drawCircle(0,s*.22f,s*.018f,paint); }
    private void drawQuestion(Canvas c,float s){ path.reset(); path.moveTo(-s*.13f,-s*.10f); path.cubicTo(-s*.14f,-s*.25f,s*.12f,-s*.28f,s*.18f,-s*.11f); path.cubicTo(s*.23f,.01f,s*.01f,.01f,s*.01f,s*.15f); c.drawPath(path,paint); c.drawCircle(s*.01f,s*.28f,s*.018f,paint); }
    private void drawPuzzle(Canvas c,float s){ rect.set(-s*.30f,-s*.28f,s*.04f,s*.06f); c.drawRoundRect(rect,s*.035f,s*.035f,paint); rect.set(-s*.02f,-s*.06f,s*.31f,s*.28f); c.drawRoundRect(rect,s*.035f,s*.035f,paint); c.drawCircle(s*.10f,-s*.28f,s*.075f,paint); c.drawCircle(-s*.30f,-s*.10f,s*.075f,paint); }
    private float dp(float v){ return v*getResources().getDisplayMetrics().density; }
}
