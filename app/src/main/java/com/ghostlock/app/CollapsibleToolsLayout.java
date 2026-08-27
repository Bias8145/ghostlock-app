package com.ghostlock.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

/** Expandable Tools section with theme-aware Material colors. */
public class CollapsibleToolsLayout extends LinearLayout {
    private View header, content;
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
            arrow.setImageResource(R.drawable.ic_chevron_down);
            arrow.setScaleType(ImageView.ScaleType.CENTER);
            arrow.setContentDescription("Expand tools");
            row.addView(arrow, new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
        header.setOnClickListener(v -> toggle());
        content.setVisibility(GONE);
        updateHeader();
    }

    private TextView findFirstTextView(View v) {
        if (v instanceof TextView) return (TextView) v;
        if (v instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
            TextView found = findFirstTextView(((ViewGroup) v).getChildAt(i));
            if (found != null) return found;
        }
        return null;
    }

    private void toggle() { expanded = !expanded; if (expanded) { NestedScrollView s = findParentScrollView(); if (s != null) scrollYBeforeExpand = s.getScrollY(); animateExpand(); } else animateCollapse(); }

    private void animateCollapse() {
        final int start = Math.max(content.getHeight(), 0);
        ValueAnimator a = ValueAnimator.ofInt(start, 0);
        a.addUpdateListener(x -> { ViewGroup.LayoutParams p = content.getLayoutParams(); p.height = (int)x.getAnimatedValue(); content.setLayoutParams(p); content.setAlpha(1f-x.getAnimatedFraction()); });
        a.addListener(new AnimatorListenerAdapter() { @Override public void onAnimationEnd(Animator x) { content.setVisibility(GONE); ViewGroup.LayoutParams p=content.getLayoutParams(); p.height=WRAP_CONTENT; content.setLayoutParams(p); content.setAlpha(1f); restoreScrollPosition(); }});
        a.setDuration(180).start(); updateHeader();
    }

    private void animateExpand() {
        content.setVisibility(VISIBLE);
        content.measure(MeasureSpec.makeMeasureSpec(Math.max(getMeasuredWidth(),1),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
        final int target=content.getMeasuredHeight(); final ViewGroup.LayoutParams p=content.getLayoutParams(); p.height=0; content.setLayoutParams(p); content.setAlpha(0f);
        ValueAnimator a=ValueAnimator.ofInt(0,target);
        a.addUpdateListener(x->{p.height=(int)x.getAnimatedValue();content.setLayoutParams(p);content.setAlpha(x.getAnimatedFraction());});
        a.addListener(new AnimatorListenerAdapter(){@Override public void onAnimationEnd(Animator x){p.height=WRAP_CONTENT;content.setLayoutParams(p);content.setAlpha(1f);scrollExpandedToolsIntoView();}});
        a.setDuration(220).start(); updateHeader();
    }

    private void scrollExpandedToolsIntoView(){final NestedScrollView s=findParentScrollView();if(s==null)return;post(()->{Rect r=new Rect(0,0,getWidth(),getHeight());s.offsetDescendantRectToMyCoords(this,r);int top=s.getScrollY()+s.getPaddingTop(),bottom=s.getScrollY()+s.getHeight()-s.getPaddingBottom(),desired=s.getScrollY();if(r.bottom>bottom)desired+=r.bottom-bottom+dp(12);else if(r.top<top)desired-=top-r.top+dp(12);desired=Math.max(0,Math.min(desired,s.getChildAt(0).getHeight()));if(desired!=s.getScrollY())s.smoothScrollTo(0,desired);});}
    private void restoreScrollPosition(){final NestedScrollView s=findParentScrollView();if(s==null||scrollYBeforeExpand<0)return;final int target=scrollYBeforeExpand;scrollYBeforeExpand=-1;s.post(()->s.smoothScrollTo(0,target));}
    private NestedScrollView findParentScrollView(){android.view.ViewParent p=getParent();while(p!=null){if(p instanceof NestedScrollView)return(NestedScrollView)p;p=p.getParent();}return null;}

    private void updateHeader(){
        int textColor=ContextCompat.getColor(getContext(),R.color.text_primary);
        int iconColor=ContextCompat.getColor(getContext(),R.color.icon_tint);
        if(title!=null) title.setTextColor(textColor);
        if(arrow!=null){arrow.setColorFilter(iconColor);arrow.animate().rotation(expanded?180f:0f).setDuration(200).start();arrow.setAlpha(expanded?1f:.82f);arrow.setContentDescription(expanded?"Collapse tools":"Expand tools");}
        header.setContentDescription(expanded?"Collapse tools":"Expand tools");
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
