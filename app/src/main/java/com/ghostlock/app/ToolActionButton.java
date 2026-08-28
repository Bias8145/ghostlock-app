package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Keeps a hamburger action's complete row (label + icon) synchronized with
 * the icon button visibility. The row is hidden until the button is made
 * visible by the hamburger controller.
 */
public class ToolActionButton extends ImageButton {
    public ToolActionButton(Context context) { super(context); }
    public ToolActionButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public ToolActionButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // XML inflation can temporarily expose child views while their parent
        // row is being attached. Re-assert the initial collapsed state.
        if (super.getVisibility() != View.VISIBLE) {
            syncRow(false);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        syncRow(visibility == View.VISIBLE);
    }

    private void syncRow(boolean show) {
        if (!(getParent() instanceof ViewGroup)) {
            return;
        }
        ViewGroup row = (ViewGroup) getParent();
        row.setVisibility(show ? View.VISIBLE : View.GONE);
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                child.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }
}
