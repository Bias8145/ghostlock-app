package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

/** Shared hard-edged modal treatment for the retro UI. */
public final class GhostLockModal {
    private static final float DEFAULT_DIM = 0.58f;
    private static final float WARNING_DIM = 0.64f;

    private GhostLockModal() {}

    public static void apply(Dialog dialog) { apply(dialog, false); }

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
    }

    public static void clear(Dialog dialog) {
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = DEFAULT_DIM;
        window.setAttributes(lp);
    }
}
