package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

/** Polished expandable Tools section; collapsed by default. */
public class CollapsibleToolsLayout extends LinearLayout {
    private View header;
    private View content;
    private TextView title;
    private ImageView arrow;
    private boolean expanded = false;
    private int scrollYBeforeExpand = -1;

    public CollapsibleToolsLayout(Context context) { super(context); setOrientation(VERTICAL); }
    public CollapsibleToolsLayout(Context context, AttributeSet attrs) { super(context, attrs); setOrientation(VERTICAL); }
    public CollapsibleToolsLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); setOrientation(VERTICAL); }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) return;
        header = getChildAt(0);
        content = getChildAt(1);
        header.setClickable(true);
        header.setFocusable(true);
        title = findFirstTextView(header);
        if (header instanceof LinearLayout) {
            LinearLayout row = (LinearLayout) header;
            arrow = new ImageView(getContext());
            arrow.setImageResource(com.ghostlock.app.R.drawable.ic_chevron_down);
            arrow.setScaleType(ImageView.ScaleType.CENTER);
            arrow.setContentDescription("Expand tools");
            row.addView(arrow, new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
        header.setOnClickListener(v -> toggle());
        content.setVisibility(GONE);
        content.setAlpha(1f);
        updateHeader();
    }

    private TextView findFirstTextView(View v) {
        if (v instanceof TextView) return (TextView) v;
        if (v instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                TextView found = findFirstTextView(((ViewGroup) v).getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    private void toggle() {
        expanded = !expanded;
        if (expanded) {
            NestedScrollView scrollView = findParentScrollView();
            if (scrollView != null) {
                scrollYBeforeExpand = scrollView.getScrollY();
            }
            animateExpand();
        } else {
            animateCollapse();
        }
    }

    private void animateCollapse() {
        final int start = content.getHeight();
        ValueAnimator a = ValueAnimator.ofInt(start, 0);
        a.addUpdateListener(x -> {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            p.height = (int) x.getAnimatedValue();
            content.setLayoutParams(p);
            content.setAlpha(1f - x.getAnimatedFraction());
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator x) {
                content.setVisibility(GONE);
                ViewGroup.LayoutParams p = content.getLayoutParams();
                p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                content.setLayoutParams(p);
                content.setAlpha(1f);
                restoreScrollPosition();
            }
        });
        a.setDuration(180).start();
        updateHeader();
    }

    private void animateExpand() {
        content.setVisibility(VISIBLE);
        content.measure(
                MeasureSpec.makeMeasureSpec(Math.max(getMeasuredWidth(), 1), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        final int target = content.getMeasuredHeight();
        ViewGroup.LayoutParams p = content.getLayoutParams();
        p.height = 0;
        content.setLayoutParams(p);
        content.setAlpha(0f);
        ValueAnimator a = ValueAnimator.ofInt(0, target);
        a.addUpdateListener(x -> {
            p.height = (int) x.getAnimatedValue();
            content.setLayoutParams(p);
            content.setAlpha(x.getAnimatedFraction());
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator x) {
                p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                content.setLayoutParams(p);
                content.setAlpha(1f);
                scrollExpandedToolsIntoView();
            }
        });
        a.setDuration(220).start();
        updateHeader();
    }

    /** Scrolls only as much as needed to reveal the expanded Tools section. */
    private void scrollExpandedToolsIntoView() {
        final NestedScrollView scrollView = findParentScrollView();
        if (scrollView == null) return;

        post(() -> {
            int viewportBottom = scrollView.getScrollY() + scrollView.getHeight()
                    - scrollView.getPaddingBottom();
            int toolsBottom = getBottom() + getPaddingBottom();
            int delta = toolsBottom - viewportBottom;
            if (delta > 0) {
                scrollView.smoothScrollBy(0, delta + dp(12));
            }
        });
    }

    /** Returns the user to exactly the scroll position from before expansion. */
    private void restoreScrollPosition() {
        final NestedScrollView scrollView = findParentScrollView();
        if (scrollView == null || scrollYBeforeExpand < 0) return;
        final int target = scrollYBeforeExpand;
        scrollYBeforeExpand = -1;
        scrollView.post(() -> scrollView.smoothScrollTo(0, target));
    }

    private NestedScrollView findParentScrollView() {
        ViewParentWalker:
        {
            android.view.ViewParent parent = getParent();
            while (parent != null) {
                if (parent instanceof NestedScrollView) {
                    return (NestedScrollView) parent;
                }
                parent = parent.getParent();
            }
        }
        return null;
    }

    private void updateHeader() {
        if (title != null) {
            title.setText("Tools");
            title.setTextColor(0xFFE7E8EB);
        }
        if (arrow != null) {
            arrow.animate().rotation(expanded ? 180f : 0f).setDuration(200).start();
            arrow.setAlpha(expanded ? 1f : 0.82f);
            arrow.setContentDescription(expanded ? "Collapse tools" : "Expand tools");
        }
        header.setContentDescription(expanded ? "Collapse tools" : "Expand tools");
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
