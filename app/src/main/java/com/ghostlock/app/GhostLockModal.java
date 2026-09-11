package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/** Shared modal window treatment for GhostLock dialogs. */
public final class GhostLockModal {
    private static final float DEFAULT_DIM = 0.46f;
    private static final float WARNING_DIM = 0.50f;
    private static final float DEFAULT_BLUR = 14f;
    private static final float WARNING_BLUR = 16f;

    private GhostLockModal() {}

    public static void apply(Dialog dialog) {
        apply(dialog, false);
    }

    public static void apply(Dialog dialog, boolean warning) {
        if (dialog == null) return;
        dialog.setOnShowListener(d -> configure(dialog, warning));
        if (dialog.isShowing()) configure(dialog, warning);
    }

    private static void configure(Dialog dialog, boolean warning) {
        Window window = dialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = warning ? WARNING_DIM : DEFAULT_DIM;
        window.setAttributes(lp);

        applyBackgroundBlur(dialog.getContext(), warning);
    }

    private static void applyBackgroundBlur(Context context, boolean warning) {
        if (Build.VERSION.SDK_INT < 31) return;
        if (!(context instanceof android.app.Activity)) return;

        View decor = ((android.app.Activity) context).getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 31) {
            float radius = warning ? WARNING_BLUR : DEFAULT_BLUR;
            decor.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
        }
    }

    /** Remove the background blur after a modal is dismissed. */
    public static void clear(Dialog dialog) {
        if (dialog == null) return;
        Context context = dialog.getContext();
        if (context instanceof android.app.Activity && Build.VERSION.SDK_INT >= 31) {
            ((android.app.Activity) context).getWindow().getDecorView().setRenderEffect(null);
        }
    }
}
