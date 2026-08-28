package com.ghostlock.app;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

/** Fixed clear-history action; visibility is controlled by the history nav state. */
public class HistoryClearButton extends MaterialButton {
    public HistoryClearButton(Context context) { super(context); init(); }
    public HistoryClearButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
    public HistoryClearButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setText("Clear history");
        setAllCaps(false);
        setTextSize(12f);
        setOnClickListener(v -> {
            getContext().getSharedPreferences("ghostlock_prefs", Context.MODE_PRIVATE).edit().remove("run_history").apply();
            View history = getRootView().findViewById(R.id.navHistory);
            if (history != null) history.performClick();
            Toast.makeText(getContext(), "History cleared", Toast.LENGTH_SHORT).show();
        });
    }
}
