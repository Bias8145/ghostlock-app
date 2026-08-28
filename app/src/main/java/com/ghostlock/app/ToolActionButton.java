package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/** Keeps a hamburger action's complete row and themed label synchronized. */
public class ToolActionButton extends ImageButton {
    public ToolActionButton(Context context) { super(context); }
    public ToolActionButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public ToolActionButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (super.getVisibility() != View.VISIBLE) {
            syncRow(false);
        } else {
            styleLabel();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        syncRow(visibility == View.VISIBLE);
    }

    private void syncRow(boolean show) {
        if (!(getParent() instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) getParent();
        row.setVisibility(show ? View.VISIBLE : View.GONE);
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                child.setVisibility(show ? View.VISIBLE : View.GONE);
                child.setBackgroundResource(R.drawable.bg_tool_label);
                child.setPadding(dp(12), 0, dp(12), 0);
                child.setGravity(android.view.Gravity.CENTER);
                child.setSingleLine(true);
                child.setElevation(dp(2));
            }
        }
    }

    private void styleLabel() {
        if (!(getParent() instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) getParent();
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                child.setBackgroundResource(R.drawable.bg_tool_label);
                child.setPadding(dp(12), 0, dp(12), 0);
                child.setGravity(android.view.Gravity.CENTER);
                child.setSingleLine(true);
                child.setElevation(dp(2));
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
