package com.ghostlock.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
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
        if (getVisibility() != View.VISIBLE) {
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
        android.view.ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) parent;
        row.setVisibility(show ? View.VISIBLE : View.GONE);
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                TextView label = (TextView) child;
                label.setVisibility(show ? View.VISIBLE : View.GONE);
                styleLabel(label);
            }
        }
    }

    private void styleLabel() {
        android.view.ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) parent;
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                styleLabel((TextView) child);
            }
        }
    }

    private void styleLabel(TextView label) {
        label.setBackgroundResource(R.drawable.bg_tool_label);
        label.setPadding(dp(12), 0, dp(12), 0);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setElevation(dp(2));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
