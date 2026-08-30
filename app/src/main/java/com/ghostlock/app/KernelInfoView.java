package com.ghostlock.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Device/kernel summary with a compact eye toggle for the kernel release. */
public class KernelInfoView extends TextView {
    private boolean kernelVisible = true;
    private CharSequence fullText = "";
    public KernelInfoView(Context context) { super(context); init(); }
    public KernelInfoView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public KernelInfoView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
    private void init() { setCompoundDrawablePadding(dp(8)); setEllipsize(TextUtils.TruncateAt.END); setOnClickListener(v -> setKernelVisible(!kernelVisible)); setContentDescription("Hide kernel version"); updateEyeIcon(); }
    @Override public void setText(CharSequence text, BufferType type) { fullText = text == null ? "" : text; super.setText(kernelVisible ? fullText : hiddenText(fullText), type); }
    private CharSequence hiddenText(CharSequence text) { String value = text == null ? "" : text.toString(); int newline = value.indexOf('\n'); if (newline < 0) return value; return value.substring(0, newline) + "\nKernel: hidden"; }
    private void setKernelVisible(boolean visible) { kernelVisible = visible; super.setText(kernelVisible ? fullText : hiddenText(fullText), BufferType.NORMAL); updateEyeIcon(); setContentDescription(kernelVisible ? "Hide kernel version" : "Show kernel version"); }
    private void updateEyeIcon() { Drawable icon = ContextCompat.getDrawable(getContext(), kernelVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off); if (icon != null) icon.setTint(ContextCompat.getColor(getContext(), R.color.icon_tint)); setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null); }
    @Override protected void onConfigurationChanged(Configuration newConfig) { super.onConfigurationChanged(newConfig); updateEyeIcon(); invalidate(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
