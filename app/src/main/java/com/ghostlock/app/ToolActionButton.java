package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageButton;

/**
 * FAB-menu action button whose row follows its visibility. This lets the
 * existing MainActivity animation control the button while keeping the
 * complete icon+label row hidden until Tools is opened.
 */
public class ToolActionButton extends ImageButton {
    public ToolActionButton(Context context) {
        super(context);
    }

    public ToolActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        ViewParent parent = getParent();
        if (parent instanceof View) {
            ((View) parent).setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
        }
    }
}
