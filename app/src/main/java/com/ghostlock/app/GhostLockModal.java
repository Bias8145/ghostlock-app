package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

/** Shared modal window treatment for GhostLock dialogs. */
public final class GhostLockModal {
    private static final float DEFAULT_DIM = 0.46f;
    private static final float WARNING_DIM = 0.50f;
    private static final int DEFAULT_BLUR = 32;
    private static final int WARNING_BLUR = 36;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WindowManager wm = (WindowManager) window.getContext().getSystemService(Context.WINDOW_SERVICE);
            boolean supported = wm == null || wm.isCrossWindowBlurEnabled();
            if (supported) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                lp.setBlurBehindRadius(warning ? WARNING_BLUR : DEFAULT_BLUR);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                lp.setBlurBehindRadius(0);
            }
        }
        window.setAttributes(lp);
    }

    public static void clear(Dialog dialog) {
        if (dialog == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        Window window = dialog.getWindow();
        if (window == null) return;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.setBlurBehindRadius(0);
        window.setAttributes(lp);
        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }
}
