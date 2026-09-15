package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Large, low-contrast GhostLock emblem used as a decorative manager-state overlay. */
public class ManagerWatermarkView extends View {
    private static final float ALPHA = 0.10f;
    private static final float SIZE_DP = 148f;

    private final Drawable emblem;
    private int color;

    public ManagerWatermarkView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        emblem = context.getDrawable(R.drawable.ic_launcher_foreground);
    }

    public void setState(int state, int color) {
        // The manager state controls the existing panel status color. The watermark
        // deliberately uses the same GhostLock emblem for every state so it reads
        // as branding, while the color still communicates the current status.
        this.color = color;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (emblem == null || color == 0 || getWidth() <= 0 || getHeight() <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        int size = Math.round(SIZE_DP * density);

        int left = getWidth() - Math.round(size * 0.82f);
        int top = getHeight() - Math.round(size * 0.82f);
        emblem.setBounds(left, top, left + size, top + size);
        emblem.setTint(color);
        emblem.setTintMode(PorterDuff.Mode.SRC_IN);
        emblem.setAlpha(Math.round(ALPHA * 255f));

        canvas.save();
        emblem.draw(canvas);
        canvas.restore();
    }
}
