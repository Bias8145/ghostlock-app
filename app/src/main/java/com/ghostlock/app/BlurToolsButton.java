package com.ghostlock.app;

import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/** Tools FAB that blurs the complete app surface while floating actions are expanded. */
public class BlurToolsButton extends ImageButton {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean blurred;

    public BlurToolsButton(Context context) { super(context); }
    public BlurToolsButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); }
    public BlurToolsButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override public boolean performClick() {
        boolean result = super.performClick();
        handler.postDelayed(() -> applyBlur(!blurred), 24L);
        return result;
    }

    private void applyBlur(boolean enabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        View root = getRootView();
        if (!(root instanceof ViewGroup)) return;
        RenderEffect effect = enabled ? RenderEffect.createBlurEffect(9f, 9f, Shader.TileMode.CLAMP) : null;
        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            // Keep the floating action stack crisp; blur every other top-level surface,
            // including the log, header, content, history/settings pages and navbar.
            if (child.getId() == R.id.toolsFabMenu || child == this) continue;
            child.setRenderEffect(effect);
        }
        blurred = enabled;
    }

    @Override protected void onDetachedFromWindow() {
        applyBlur(false);
        handler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
