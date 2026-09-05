package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceInfoHeader extends LinearLayout {
    public DeviceInfoHeader(Context context) { super(context); init(); }
    public DeviceInfoHeader(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public DeviceInfoHeader(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setClickable(false);
        setFocusable(false);
        setBackgroundResource(android.R.color.transparent);

        TextView title = new TextView(getContext());
        title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        title.setText("DEVICE INFO");
        title.setTextColor(getResources().getColor(R.color.text_secondary, getContext().getTheme()));
        title.setTextSize(14);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        if (android.os.Build.VERSION.SDK_INT >= 21) title.setLetterSpacing(0.08f);
        addView(title);
    }
}
