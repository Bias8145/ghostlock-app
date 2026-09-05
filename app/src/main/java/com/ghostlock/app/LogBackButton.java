package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/** Back control for the Logs page; returns to the existing Home view. */
public class LogBackButton extends ImageButton {
    public LogBackButton(Context context) {
        super(context);
        init();
    }

    public LogBackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LogBackButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(v -> {
            View navigation = getRootView().findViewById(R.id.bottomNavigation);
            if (navigation instanceof GhostLockBottomNavigation) {
                ((GhostLockBottomNavigation) navigation).showHome();
            }
        });
    }
}
