package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.TextView;

/** Keeps the whole hamburger action row hidden/shown with its icon. */
public class ToolActionButton extends ImageButton {
    public ToolActionButton(Context context) { super(context); }
    public ToolActionButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public ToolActionButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    public int getVisibility() {
        ViewParent parent = getParent();
        if (parent instanceof View && ((View) parent).getVisibility() != View.VISIBLE) {
            return View.GONE;
        }
        return super.getVisibility();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) parent;
        boolean show = visibility == View.VISIBLE;
        row.setVisibility(show ? View.VISIBLE : View.GONE);
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                child.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }
}
