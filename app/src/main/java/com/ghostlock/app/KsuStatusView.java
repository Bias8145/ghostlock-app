package com.ghostlock.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** Compact manager identity indicator kept in the device card. */
public class KsuStatusView extends TextView {
    public KsuStatusView(Context context){super(context);init();}
    public KsuStatusView(Context context,android.util.AttributeSet attrs){super(context,attrs);init();}
    public KsuStatusView(Context context,android.util.AttributeSet attrs,int defStyleAttr){super(context,attrs,defStyleAttr);init();}
    private void init(){setGravity(Gravity.CENTER_VERTICAL);setTypeface(Typeface.DEFAULT);setTextSize(11);refreshStatus();}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();refreshStatus();}
    public void refreshStatus(){
        ManagerCompatibility.ManagerInfo m=ManagerCompatibility.detectManager(getContext());
        if(!m.installed){setText("Manager  ·  Not installed");setTextColor(ContextCompat.getColor(getContext(),R.color.accent));}
        else if(m.spoofed){setText("Manager  ·  "+m.name+"  ·  Identity mismatch");setTextColor(ContextCompat.getColor(getContext(),R.color.status_error));}
        else if(m.identityVerified){setText("Manager  ·  "+m.name+"  ·  Verified");setTextColor(ContextCompat.getColor(getContext(),R.color.status_success));}
        else {setText("Manager  ·  "+m.name+"  ·  Recognized");setTextColor(ContextCompat.getColor(getContext(),R.color.text_secondary));}
    }
}
