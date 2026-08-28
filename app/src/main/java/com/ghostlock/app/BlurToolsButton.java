package com.ghostlock.app;

import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;

/** Tools FAB that blurs the page behind the expanded floating actions. */
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
        View log = getRootView().findViewById(R.id.logScroll);
        View device = getRootView().findViewById(R.id.deviceInfo);
        View status = getRootView().findViewById(R.id.runtimeStatus);
        RenderEffect effect = enabled ? RenderEffect.createBlurEffect(8f, 8f, Shader.TileMode.CLAMP) : null;
        if (log != null) log.setRenderEffect(effect);
        if (device != null) device.setRenderEffect(effect);
        if (status != null) status.setRenderEffect(effect);
        blurred = enabled;
    }
}
