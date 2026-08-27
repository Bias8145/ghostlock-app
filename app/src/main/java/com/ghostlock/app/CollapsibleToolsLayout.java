package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Polished expandable Tools section; collapsed by default. */
public class CollapsibleToolsLayout extends LinearLayout {
    private View header;
    private View content;
    private TextView title;
    private TextView arrow;
    private boolean expanded = false;

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
            arrow = new TextView(getContext());
            arrow.setText("›");
            arrow.setTextSize(26);
            arrow.setTextColor(0xFFB8BBC2);
            arrow.setGravity(Gravity.CENTER);
            row.addView(arrow, new LinearLayout.LayoutParams(dp(40), dp(48)));
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

    private void toggle() { expanded = !expanded; if (expanded) animateExpand(); else animateCollapse(); }

    private void animateCollapse() {
        final int start = content.getHeight();
        ValueAnimator a = ValueAnimator.ofInt(start, 0);
        a.addUpdateListener(x -> { ViewGroup.LayoutParams p=content.getLayoutParams(); p.height=(int)x.getAnimatedValue(); content.setLayoutParams(p); content.setAlpha(1f-x.getAnimatedFraction()); });
        a.addListener(new AnimatorListenerAdapter(){ @Override public void onAnimationEnd(Animator x){ content.setVisibility(GONE); ViewGroup.LayoutParams p=content.getLayoutParams(); p.height=ViewGroup.LayoutParams.WRAP_CONTENT; content.setLayoutParams(p); content.setAlpha(1f); }});
        a.setDuration(180).start(); updateHeader();
    }

    private void animateExpand() {
        content.setVisibility(VISIBLE);
        content.measure(MeasureSpec.makeMeasureSpec(Math.max(getMeasuredWidth(),1), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        final int target=content.getMeasuredHeight();
        ViewGroup.LayoutParams p=content.getLayoutParams(); p.height=0; content.setLayoutParams(p); content.setAlpha(0f);
        ValueAnimator a=ValueAnimator.ofInt(0,target);
        a.addUpdateListener(x->{p.height=(int)x.getAnimatedValue();content.setLayoutParams(p);content.setAlpha(x.getAnimatedFraction());});
        a.addListener(new AnimatorListenerAdapter(){@Override public void onAnimationEnd(Animator x){p.height=ViewGroup.LayoutParams.WRAP_CONTENT;content.setLayoutParams(p);content.setAlpha(1f);}});
        a.setDuration(220).start(); updateHeader();
    }

    private void updateHeader() {
        if (title != null) { title.setText("Tools"); title.setTextColor(0xFFE7E8EB); }
        if (arrow != null) { arrow.setText(expanded ? "⌄" : "›"); arrow.setTextColor(expanded ? 0xFFE7E8EB : 0xFFB8BBC2); }
        header.setContentDescription(expanded ? "Collapse tools" : "Expand tools");
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
