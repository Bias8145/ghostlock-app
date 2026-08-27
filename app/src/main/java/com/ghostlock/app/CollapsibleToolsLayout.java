package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A small self-contained expandable section used by the Tools area. */
public class CollapsibleToolsLayout extends LinearLayout {
    private View header;
    private View content;
    private boolean expanded = true;

    public CollapsibleToolsLayout(Context context) { super(context); setOrientation(VERTICAL); }
    public CollapsibleToolsLayout(Context context, AttributeSet attrs) { super(context, attrs); setOrientation(VERTICAL); }
    public CollapsibleToolsLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); setOrientation(VERTICAL); }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) return;
        header = getChildAt(0);
        content = getChildAt(1);
        header.setClickable(true);
        header.setOnClickListener(v -> toggle());
        updateHeader();
    }

    private void toggle() {
        expanded = !expanded;
        if (expanded) animateExpand(); else animateCollapse();
    }

    private void animateCollapse() {
        final int start = content.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(start, 0);
        animator.addUpdateListener(a -> {
            ViewGroup.LayoutParams lp = content.getLayoutParams();
            lp.height = (int) a.getAnimatedValue();
            content.setLayoutParams(lp);
            content.setAlpha(1f - a.getAnimatedFraction());
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                content.setVisibility(GONE);
                ViewGroup.LayoutParams lp = content.getLayoutParams();
                lp.height = WRAP_CONTENT;
                content.setLayoutParams(lp);
                content.setAlpha(1f);
            }
        });
        animator.setDuration(180).start();
        updateHeader();
    }

    private void animateExpand() {
        content.setVisibility(VISIBLE);
        content.measure(MeasureSpec.makeMeasureSpec(Math.max(getMeasuredWidth(), 1), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        final int target = content.getMeasuredHeight();
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        lp.height = 0;
        content.setLayoutParams(lp);
        content.setAlpha(0f);
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.addUpdateListener(a -> {
            lp.height = (int) a.getAnimatedValue();
            content.setLayoutParams(lp);
            content.setAlpha(a.getAnimatedFraction());
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                lp.height = WRAP_CONTENT;
                content.setLayoutParams(lp);
                content.setAlpha(1f);
            }
        });
        animator.setDuration(200).start();
        updateHeader();
    }

    private void updateHeader() {
        if (header instanceof TextView) {
            ((TextView) header).setText(expanded ? "Tools  ˅" : "Tools  ˃");
        }
        header.setContentDescription(expanded ? "Collapse tools" : "Expand tools");
    }
}
