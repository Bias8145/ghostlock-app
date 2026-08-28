package com.ghostlock.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.google.android.material.button.MaterialButton;

/** Fixed, compact clear-history action; visible only on the History page. */
public class HistoryClearButton extends MaterialButton {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable sync = new Runnable() {
        @Override public void run() {
            View root = getRootView();
            View history = root.findViewById(R.id.navHistory);
            boolean onHistory = history != null && history.getAlpha() > .95f;
            setVisibility(onHistory ? View.VISIBLE : View.GONE);
            handler.postDelayed(this, 180L);
        }
    };

    public HistoryClearButton(Context context) { super(context); init(); }
    public HistoryClearButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public HistoryClearButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setText("");
        setAllCaps(false);
        setIconResource(R.drawable.ic_delete_outline);
        setIconSize(dp(22));
        setContentDescription("Clear history");
        setMinWidth(0);
        setMinimumWidth(0);
        setPadding(dp(10), dp(10), dp(10), dp(10));
        setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(getContext(), R.color.surface_container_low)));
        setIconTint(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(getContext(), R.color.icon_tint)));
        setOnClickListener(v -> {
            getContext().getSharedPreferences("ghostlock_prefs", Context.MODE_PRIVATE)
                    .edit().remove("run_history").apply();
            View history = getRootView().findViewById(R.id.navHistory);
            if (history != null) history.performClick();
        });
        handler.post(sync);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(sync);
        super.onDetachedFromWindow();
    }
}
