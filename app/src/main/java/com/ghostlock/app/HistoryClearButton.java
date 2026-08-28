package com.ghostlock.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

/** Fixed clear-history action; visibility follows the selected history page. */
public class HistoryClearButton extends MaterialButton {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable sync = new Runnable() {
        @Override public void run() {
            View root=getRootView();
            View history=root.findViewById(R.id.navHistory);
            if(history!=null)setVisibility(history.getAlpha()>0.95f?View.VISIBLE:View.GONE);
            View tools=root.findViewById(R.id.toolsFab); if(tools!=null)tools.setElevation(dp(4));
            int[] ids={R.id.toolImport,R.id.toolParseLink,R.id.toolParseBoot,R.id.toolExport};
            for(int id:ids){View v=root.findViewById(id);if(v!=null)v.setElevation(dp(3));}
            handler.postDelayed(this,120L);
        }
    };
    public HistoryClearButton(Context context){super(context);init();}
    public HistoryClearButton(Context context,android.util.AttributeSet attrs){super(context,attrs);init();}
    public HistoryClearButton(Context context,android.util.AttributeSet attrs,int defStyleAttr){super(context,attrs,defStyleAttr);init();}
    private void init(){setText("Clear history");setAllCaps(false);setTextSize(12f);setOnClickListener(v->{getContext().getSharedPreferences("ghostlock_prefs",Context.MODE_PRIVATE).edit().remove("run_history").apply();View history=getRootView().findViewById(R.id.navHistory);if(history!=null)history.performClick();Toast.makeText(getContext(),"History cleared",Toast.LENGTH_SHORT).show();});handler.post(sync);}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    @Override protected void onDetachedFromWindow(){handler.removeCallbacks(sync);super.onDetachedFromWindow();}
}
