package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

/** Primary Run action with themed confirmation and compatibility gating. */
public class ConfirmRunButton extends MaterialButton {
    private boolean confirmedClick;
    private boolean arranged;
    private final Handler handler = new Handler();
    public ConfirmRunButton(Context context) { super(context); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs) { super(context, attrs); }
    public ConfirmRunButton(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); handler.post(this::finalizeUiLayout); }

    private void finalizeUiLayout() {
        View root = getRootView();
        View logScroll = root.findViewById(R.id.logScroll);
        View tools = root.findViewById(R.id.toolsFabMenu);
        if (logScroll != null) {
            View frame = (View) logScroll.getParent();
            if (frame != null) {
                View cardContent = (View) frame.getParent();
                if (cardContent != null) {
                    View card = (View) cardContent.getParent();
                    if (card != null) {
                        ViewGroup.LayoutParams lp = card.getLayoutParams();
                        lp.height = dp(600);
                        card.setLayoutParams(lp);
                    }
                }
            }
        }
        if (!arranged && tools != null && tools.getParent() instanceof FrameLayout && getParent() instanceof ViewGroup) {
            FrameLayout frame = (FrameLayout) tools.getParent();
            ViewGroup oldParent = (ViewGroup) getParent();
            oldParent.removeView(this);
            frame.removeView(tools);
            LinearLayout stack = new LinearLayout(getContext());
            stack.setOrientation(LinearLayout.VERTICAL);
            stack.setGravity(Gravity.END);
            FrameLayout.LayoutParams stackLp = new FrameLayout.LayoutParams(-2, -2, Gravity.END | Gravity.BOTTOM);
            stackLp.rightMargin = dp(4);
            stackLp.bottomMargin = dp(4);
            frame.addView(stack, stackLp);
            stack.addView(tools, new LinearLayout.LayoutParams(-2, -2));
            LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(dp(148), dp(62));
            runLp.topMargin = dp(8);
            stack.addView(this, runLp);
            setElevation(dp(4));
            arranged = true;
        }
        View theme = root.findViewById(R.id.headerThemeToggle);
        if (theme != null) { ViewGroup.LayoutParams lp = theme.getLayoutParams(); lp.width = dp(58); lp.height = dp(58); theme.setLayoutParams(lp); theme.setTranslationY(-dp(2)); }
        View bottomNav = root.findViewById(R.id.bottomNav);
        if (bottomNav instanceof ViewGroup) {
            bottomNav.setBackgroundResource(R.drawable.bg_bottom_nav);
            ViewGroup nav = (ViewGroup) bottomNav;
            for (int i=0;i<nav.getChildCount();i++) if (nav.getChildAt(i) instanceof MaterialButton) { MaterialButton b=(MaterialButton)nav.getChildAt(i); b.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)); b.setCornerRadius(0); }
        }
        installToolsBlurHook();
        installClearHistory();
    }

    private void installToolsBlurHook() {
        View toolsFab = getRootView().findViewById(R.id.toolsFab);
        if (toolsFab == null || toolsFab.getTag(R.id.toolsFab) != null) return;
        toolsFab.setTag(R.id.toolsFab, Boolean.TRUE);
        toolsFab.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_UP && Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) handler.postDelayed(()->{
                View item=getRootView().findViewById(R.id.toolImport);
                boolean enabled=item!=null && item.getVisibility()==View.VISIBLE;
                RenderEffect effect=enabled?RenderEffect.createBlurEffect(8f,8f,Shader.TileMode.CLAMP):null;
                View log=getRootView().findViewById(R.id.logScroll), device=getRootView().findViewById(R.id.deviceInfo), status=getRootView().findViewById(R.id.runtimeStatus);
                if(log!=null)log.setRenderEffect(effect); if(device!=null)device.setRenderEffect(effect); if(status!=null)status.setRenderEffect(effect);
            },40L);
            return false;
        });
    }

    private void installClearHistory() {
        View root=getRootView(); if(!(root instanceof LinearLayout)||root.findViewById(R.id.clearHistoryButton)!=null)return;
        HistoryClearButton clear=new HistoryClearButton(getContext()); clear.setId(R.id.clearHistoryButton);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(48)); lp.gravity=Gravity.END; lp.rightMargin=dp(20); lp.topMargin=-dp(58); lp.bottomMargin=dp(10); clear.setLayoutParams(lp); ((LinearLayout)root).addView(clear);
    }

    @Override public boolean performClick() { if(confirmedClick){confirmedClick=false;return super.performClick();} ManagerCompatibility.Result result=ManagerCompatibility.evaluate(getContext()); if(!result.canRun()){showBlocked(result);return true;} showConfirmation(result);return true; }
    private void showBlocked(ManagerCompatibility.Result result){Dialog dialog=createDialog();LinearLayout box=box();TextView title=text(20,true);title.setText(blockTitle(result));box.addView(title,margin(-1,-2,0,0,0,7));TextView message=text(13,false);message.setText(blockMessage(result));message.setLineSpacing(0,1.08f);box.addView(message,margin(-1,-2,0,0,0,16));if(result.state==ManagerCompatibility.State.MANAGER_REQUIRED){MaterialButton install=actionButton("Install supported manager",true);install.setOnClickListener(v->{dialog.dismiss();showManagerPicker();});box.addView(install,margin(-1,dp(54),0,0,0,14));}MaterialButton close=actionButton("Close",false);close.setOnClickListener(v->dialog.dismiss());box.addView(close,margin(-1,dp(48),0,0,0,0));show(dialog,box);}
    private void showConfirmation(ManagerCompatibility.Result result){Dialog dialog=createDialog();LinearLayout box=box();TextView title=text(20,true);title.setText("Run GhostLock");box.addView(title,margin(-1,-2,0,0,0,4));TextView subtitle=text(12,false);subtitle.setText("Execute kernel exploit & runtime");box.addView(subtitle,margin(-1,-2,0,0,0,16));LinearLayout info=new LinearLayout(getContext());info.setOrientation(LinearLayout.VERTICAL);info.setBackground(round(color(R.color.surface_container_low),16));info.setPadding(dp(12),dp(10),dp(12),dp(10));info.addView(infoRow("Kernel",result.kernelSupported?"Kernel supported":"Kernel unsupported",result.kernelSupported?R.color.status_success:R.color.status_error),new LinearLayout.LayoutParams(-1,dp(44)));info.addView(infoRow("Manager",result.manager.name,R.color.text_primary),margin(-1,dp(44),0,7,0,0));String identity=result.manager.identityVerified?"Verified":"Recognized";info.addView(infoRow("Identity",identity,result.manager.identityVerified?R.color.status_success:R.color.accent),margin(-1,dp(44),0,7,0,0));box.addView(info,margin(-1,-2,0,0,0,16));TextView message=text(12,false);message.setText("This operation will modify runtime state on the current device.");message.setLineSpacing(0,1.08f);box.addView(message,margin(-1,-2,0,0,0,16));LinearLayout actions=new LinearLayout(getContext());actions.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);MaterialButton cancel=actionButton("Cancel",false);cancel.setOnClickListener(v->dialog.dismiss());MaterialButton run=actionButton("Run",true);run.setOnClickListener(v->{dialog.dismiss();confirmedClick=true;performClick();});actions.addView(cancel,new LinearLayout.LayoutParams(-2,dp(48)));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-2,dp(48));rp.leftMargin=dp(8);actions.addView(run,rp);box.addView(actions,new LinearLayout.LayoutParams(-1,dp(52)));show(dialog,box);}
    private void showManagerPicker(){Dialog dialog=createDialog();LinearLayout box=box();TextView title=text(19,true);title.setText("Install supported manager");box.addView(title);TextView subtitle=text(12,false);subtitle.setText("Select a registered manager to continue.");box.addView(subtitle,margin(-1,-2,0,6,0,14));for(ManagerCompatibility.ManagerInfo manager:ManagerCompatibility.registeredManagers(getContext())){TextView row=text(14,true);row.setText(manager.name+(manager.installed?"  ·  Installed":"  ·  Not installed"));row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),0,dp(14),0);row.setBackground(round(color(R.color.surface_container_low),16));row.setClickable(true);row.setFocusable(true);row.setOnClickListener(v->{dialog.dismiss();ManagerCompatibility.openInstaller(getContext(),manager);});box.addView(row,margin(-1,dp(54),0,0,0,9));}MaterialButton cancel=actionButton("Cancel",false);cancel.setOnClickListener(v->dialog.dismiss());box.addView(cancel,margin(-1,dp(48),0,4,0,0));show(dialog,box);}
    private LinearLayout infoRow(String label,String value,int colorRes){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);TextView labelView=text(12,true);labelView.setText(label);row.addView(labelView,new LinearLayout.LayoutParams(0,dp(44),1));TextView valueView=text(12,true);valueView.setText(value);valueView.setTextColor(color(colorRes));valueView.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);valueView.setMaxLines(1);row.addView(valueView,new LinearLayout.LayoutParams(-2,dp(44)));return row;}
    private String blockTitle(ManagerCompatibility.Result result){switch(result.state){case MANAGER_REQUIRED:return "Manager required";case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:return "Kernel and manager unavailable";case SPOOFED_MANAGER:return "Manager identity mismatch";case UNSUPPORTED_MANAGER:return "Unsupported manager";default:return "Kernel unsupported";}}
    private String blockMessage(ManagerCompatibility.Result result){switch(result.state){case MANAGER_REQUIRED:return "The kernel is supported, but a registered manager is not installed.";case KERNEL_UNSUPPORTED_MANAGER_REQUIRED:return "The current kernel is not supported and no manager is installed. Installing a manager will not make this kernel compatible.";case SPOOFED_MANAGER:return "The detected manager identity is not trusted. Check the package and signing certificate before running GhostLock.";case UNSUPPORTED_MANAGER:return "The installed manager is not registered with GhostLock.";default:return "The current kernel does not provide the capability required by GhostLock.";}}
    private Dialog createDialog(){return new Dialog(getContext());}
    private LinearLayout box(){LinearLayout box=new LinearLayout(getContext());box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(22),dp(20),dp(22),dp(16));box.setBackground(round(color(R.color.surface_container),26));return box;}
    private void show(Dialog dialog,LinearLayout box){dialog.setContentView(box);dialog.setOnShowListener(x->{if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setDimAmount(.42f);if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND);dialog.getWindow().getAttributes().setBlurBehindRadius(dp(28));}dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f),-2);}});dialog.show();}
    private MaterialButton actionButton(String text,boolean primary){MaterialButton b=new MaterialButton(getContext());b.setText(text);b.setAllCaps(false);b.setMinWidth(0);b.setTextSize(13);b.setBackgroundTintList(ColorStateList.valueOf(color(primary?R.color.accent:R.color.surface_container_low)));b.setTextColor(color(primary?R.color.on_accent:R.color.text_primary));return b;}
    private TextView text(int size,boolean bold){TextView v=new TextView(getContext());v.setTextSize(size);v.setTextColor(color(bold?R.color.text_primary:R.color.text_secondary));if(bold)v.setTypeface(null,android.graphics.Typeface.BOLD);return v;}
    private int color(int id){return ContextCompat.getColor(getContext(),id);}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private GradientDrawable round(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
