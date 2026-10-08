package com.ghostlock.app;
import android.app.Activity;import android.content.Context;import android.content.res.ColorStateList;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.View;import android.view.ViewGroup;import android.widget.TextView;
import com.google.android.material.button.MaterialButton;import com.google.android.material.card.MaterialCardView;
public final class PixelUi{
 private PixelUi(){}
 public static void apply(Activity a,View root){if(root==null)return;Typeface p=Typeface.MONOSPACE;walk(a,root,p);}
 private static void walk(Context c,View v,Typeface p){
  if(v instanceof TextView){TextView t=(TextView)v;boolean b=t.getTypeface()!=null&&t.getTypeface().isBold();t.setTypeface(p,b?Typeface.BOLD:Typeface.NORMAL);t.getPaint().setAntiAlias(false);t.getPaint().setSubpixelText(false);}
  if(v instanceof MaterialCardView){MaterialCardView card=(MaterialCardView)v;card.setCardElevation(0);card.setRadius(0);card.setStrokeWidth(dp(c,2));card.setStrokeColor(ColorStateList.valueOf(c.getResources().getColor(R.color.border)));}
  if(v instanceof MaterialButton){MaterialButton b=(MaterialButton)v;b.setCornerRadius(0);b.setStrokeWidth(dp(c,2));b.setStrokeColor(ColorStateList.valueOf(c.getResources().getColor(R.color.border)));b.setElevation(0);}
  if(v.getBackground() instanceof GradientDrawable)((GradientDrawable)v.getBackground().mutate()).setCornerRadius(0);
  if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)walk(c,g.getChildAt(i),p);}
 }
 private static int dp(Context c,int v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
}