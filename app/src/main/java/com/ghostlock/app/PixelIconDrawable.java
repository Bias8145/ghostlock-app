package com.ghostlock.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

/** Small hard-edged icon renderer. It intentionally uses a coarse pixel grid. */
public final class PixelIconDrawable extends Drawable {
    private final Paint paint = new Paint();
    private final String name;
    private final int tint;
    private final int grid = 7;

    public PixelIconDrawable(Context context, String resourceName) {
        name = resourceName == null ? "" : resourceName.toLowerCase();
        tint = ContextCompat.getColor(context, R.color.icon_tint);
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(tint);
    }

    @Override public void draw(Canvas canvas) {
        Rect b = getBounds();
        float unit = Math.max(1f, Math.min(b.width(), b.height()) / (float) grid);
        float ox = b.centerX() - unit * grid / 2f;
        float oy = b.centerY() - unit * grid / 2f;
        String[] p = pattern();
        for (int y = 0; y < p.length; y++) {
            for (int x = 0; x < p[y].length(); x++) {
                if (p[y].charAt(x) == '#') {
                    canvas.drawRect(ox + x * unit, oy + y * unit,
                            ox + (x + 1) * unit, oy + (y + 1) * unit, paint);
                }
            }
        }
    }

    private String[] pattern() {
        if (name.contains("moon") || name.contains("theme")) return new String[]{
                "  #### ", " ##### ", "###    ", "###    ", "###    ", " ##### ", "  #### "};
        if (name.contains("analytics")) return new String[]{
                "       ", " #     ", " #  ## ", " #  ## ", " ## ## ", " ######", "       "};
        if (name.contains("dashboard")) return new String[]{
                "## ## #", "## ## #", "       ", "## ## #", "## ## #", "       ", "## ## #"};
        if (name.contains("check")) return new String[]{
                "       ", "#      ", "##     ", " ##    ", "  ## # ", "   ### ", "       "};
        if (name.contains("error") || name.contains("alert")) return new String[]{
                "  ###  ", "  ###  ", "  ###  ", "  ###  ", "       ", "  ###  ", "       "};
        if (name.contains("shield")) return new String[]{
                "  ###  ", " ##### ", "#######", " ##### ", "  ###  ", "  ###  ", "   #   "};
        if (name.contains("settings") || name.contains("advanced")) return new String[]{
                " #   # ", "  ###  ", "#######", " ## ## ", "#######", "  ###  ", " #   # "};
        if (name.contains("tools") || name.contains("wrench")) return new String[]{
                "##     ", " ###   ", "  ###  ", "   ### ", "  ###  ", " ###   ", "##     "};
        if (name.contains("copy")) return new String[]{
                "  #### ", " ######", " ##    ", " ## ###", " ######", "  #### ", "       "};
        if (name.contains("delete") || name.contains("clear")) return new String[]{
                " ######", "  #### ", "  ####  ", " ######", " ##### ", " ###   ", "       "};
        if (name.contains("import") || name.contains("parse")) return new String[]{
                "   #   ", "   #   ", "#######", "  ###  ", "  ###  ", " ####  ", "       "};
        return new String[]{
                " ######", "##    #", "# #### ", "# #### ", "#    ##", " ######", "       "};
    }

    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
    @Override public int getIntrinsicWidth() { return -1; }
    @Override public int getIntrinsicHeight() { return -1; }
}
