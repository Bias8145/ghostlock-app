package com.ghostlock.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

/** Manager status surface with light/dark theme-aware Material colors. */
public class RuntimeStatusView extends LinearLayout {
    private final TextView state, message, manager, action;
    private boolean navigationInstalled;
    private TextView historyLog;
    private View historyPage;
    private View settingsPage;

    public RuntimeStatusView(Context context) { this(context, null); }
    public RuntimeStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs); setOrientation(VERTICAL); setPadding(dp(18),dp(16),dp(18),dp(16));
        state=text(15,true); addView(state); message=text(12,false); addView(message,margin(-1,-2,0,7,0,0)); manager=text(11,true); addView(manager,margin(-1,-2,0,13,0,0));
        action=text(12,true); action.setGravity(Gravity.CENTER); action.setPadding(dp(12),0,dp(12),0); action.setMinHeight(dp(56)); action.setVisibility(View.GONE); addView(action,margin(-1,dp(56),0,18,0,0)); refresh();
    }
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();refresh();post(this::installNavigation);}
    @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus)post(this::refresh);}

    public void refresh(){
        ManagerCompatibility.Result result=ManagerCompatibility.evaluate(getContext()); boolean showInstall=false;
        switch(result.state){
            case READY: state.setText("READY");state.setTextColor(ContextCompat.getColor(getContext(),R.color.status_success));message.setText("Compatible manager detected and verified");setSurface(R.color.status_success_bg);break;
            case MANAGER_REQUIRED: state.setText("MANAGER REQUIRED");state.setTextColor(ContextCompat.getColor(getContext(),R.color.accent));message.setText("Install a registered manager before running GhostLock");setSurface(R.color.accent_container);showInstall=true;break;
            case KERNEL_UNSUPPORTED_MANAGER_REQUIRED: state.setText("MANAGER NOT INSTALLED");state.setTextColor(ContextCompat.getColor(getContext(),R.color.accent));message.setText("No registered manager is installed");setSurface(R.color.accent_container);showInstall=true;break;
            case SPOOFED_MANAGER: state.setText("IDENTITY MISMATCH");state.setTextColor(ContextCompat.getColor(getContext(),R.color.status_error));message.setText("The detected manager identity could not be verified");setSurface(R.color.status_error_bg);break;
            case UNSUPPORTED_MANAGER: state.setText("UNSUPPORTED MANAGER");state.setTextColor(ContextCompat.getColor(getContext(),R.color.status_error));message.setText("The installed manager is not registered with GhostLock");setSurface(R.color.status_error_bg);break;
            default: state.setText("MANAGER STATUS UNAVAILABLE");state.setTextColor(ContextCompat.getColor(getContext(),R.color.text_secondary));message.setText("Manager information could not be determined");setSurface(R.color.surface_container);break;
        }
        String managerText=!result.manager.installed?"Manager  ·   Not installed":result.manager.spoofed?"Manager  ·   "+result.manager.name+"  ·  Identity mismatch":result.manager.identityVerified?"Manager  ·   "+result.manager.name+"  ·  Verified":"Manager  ·   "+result.manager.name+"  ·  Recognized";
        manager.setText(managerText);manager.setTextColor(ContextCompat.getColor(getContext(),result.manager.spoofed?R.color.status_error:result.manager.installed?R.color.text_primary:R.color.accent));
        action.setVisibility(showInstall?View.VISIBLE:View.GONE);
        if(showInstall){action.setText("Install supported manager");action.setTextColor(ContextCompat.getColor(getContext(),R.color.on_accent));action.setBackground(round(ContextCompat.getColor(getContext(),R.color.accent),18));action.setOnClickListener(v->showManagerPicker());}else action.setOnClickListener(null);
    }

    private void installNavigation(){
        if(navigationInstalled) return;
        final View root=findViewById(R.id.root);
        if(!(root instanceof ViewGroup)) return;
        final ViewGroup rootGroup=(ViewGroup)root;
        final View content=root.findViewById(R.id.contentScroll);
        final View bottom=root.findViewById(R.id.bottomNav);
        final MaterialButton navHome=root.findViewById(R.id.navHome);
        final MaterialButton navHistory=root.findViewById(R.id.navHistory);
        final MaterialButton navSettings=root.findViewById(R.id.navSettings);
        if(content==null||bottom==null||navHome==null||navHistory==null||navSettings==null) return;
        View advanced=root.findViewById(R.id.advancedButton);
        if(advanced!=null) advanced.setVisibility(GONE);
        hideHeaderTheme(root);
        int index=rootGroup.indexOfChild(content);
        ViewGroup.LayoutParams originalLp=content.getLayoutParams();
        rootGroup.removeView(content);
        FrameLayout pages=new FrameLayout(getContext());
        pages.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.bg));
        originalLp.width=ViewGroup.LayoutParams.MATCH_PARENT; originalLp.height=0;
        if(originalLp instanceof LinearLayout.LayoutParams) ((LinearLayout.LayoutParams)originalLp).weight=1f;
        pages.setLayoutParams(originalLp);
        content.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        pages.addView(content);
        historyPage=buildHistoryPage(root); settingsPage=buildSettingsPage(root);
        pages.addView(historyPage,matchParent()); pages.addView(settingsPage,matchParent());
        historyPage.setVisibility(GONE); settingsPage.setVisibility(GONE); rootGroup.addView(pages,index);
        configureNavButton(navHome,R.drawable.fa_house,"Home"); configureNavButton(navHistory,R.drawable.fa_history,"History"); configureNavButton(navSettings,R.drawable.fa_gear,"Settings");
        navHome.setOnClickListener(v->showPage(content,historyPage,settingsPage,0,navHome,navHistory,navSettings));
        navHistory.setOnClickListener(v->showPage(content,historyPage,settingsPage,1,navHome,navHistory,navSettings));
        navSettings.setOnClickListener(v->showPage(content,historyPage,settingsPage,2,navHome,navHistory,navSettings));
        showPage(content,historyPage,settingsPage,0,navHome,navHistory,navSettings); navigationInstalled=true;
    }

    private void hideHeaderTheme(View root){
        if(!(root instanceof ViewGroup)) return; ViewGroup group=(ViewGroup)root;
        for(int i=0;i<group.getChildCount();i++){View child=group.getChildAt(i);if(child instanceof ThemeToggleButton){child.setVisibility(GONE);continue;}if(child instanceof ViewGroup) hideHeaderTheme(child);}
    }

    private View buildHistoryPage(View root){
        LinearLayout outer=new LinearLayout(getContext());outer.setOrientation(VERTICAL);outer.setPadding(dp(16),dp(20),dp(16),dp(16));
        TextView title=text(24,true);title.setText("History");outer.addView(title);
        TextView subtitle=text(12,false);subtitle.setText("Execution output and recent GhostLock activity");outer.addView(subtitle,margin(-1,-2,0,3,0,14));
        LinearLayout card=new LinearLayout(getContext());card.setOrientation(VERTICAL);card.setPadding(dp(16),dp(14),dp(16),dp(14));card.setBackground(round(ContextCompat.getColor(getContext(),R.color.log_bg),20));
        LinearLayout header=new LinearLayout(getContext());header.setGravity(Gravity.CENTER_VERTICAL);
        TextView label=text(14,true);label.setText("Execution log");header.addView(label,new LinearLayout.LayoutParams(0,dp(44),1));
        MaterialButton copy=new MaterialButton(getContext(),null,com.google.android.material.R.attr.materialButtonOutlinedStyle);copy.setText("Copy");copy.setAllCaps(false);copy.setMinHeight(dp(40));copy.setIconResource(R.drawable.fa_history);copy.setIconSize(dp(16));header.addView(copy,new LinearLayout.LayoutParams(dp(100),dp(44)));
        card.addView(header);
        ScrollView scroll=new ScrollView(getContext());scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.setPadding(0,dp(8),0,0);
        historyLog=new TextView(getContext());historyLog.setTextSize(12);historyLog.setTextColor(ContextCompat.getColor(getContext(),R.color.log_text));historyLog.setTypeface(android.graphics.Typeface.MONOSPACE);historyLog.setLineSpacing(dp(3),1f);scroll.addView(historyLog,new ScrollView.LayoutParams(-1,-2));card.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        copy.setOnClickListener(v->{ClipboardManager cm=(ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("ghostlock-log",historyLog.getText()));});
        outer.addView(card,new LinearLayout.LayoutParams(-1,0,1)); return outer;
    }

    private View buildSettingsPage(View root){
        LinearLayout outer=new LinearLayout(getContext());outer.setOrientation(VERTICAL);outer.setPadding(dp(16),dp(20),dp(16),dp(16));
        TextView title=text(24,true);title.setText("Settings");outer.addView(title);
        TextView subtitle=text(12,false);subtitle.setText("Appearance and GhostLock execution options");outer.addView(subtitle,margin(-1,-2,0,3,0,14));
        LinearLayout appearance=new LinearLayout(getContext());appearance.setGravity(Gravity.CENTER_VERTICAL);appearance.setPadding(dp(16),dp(8),dp(8),dp(8));appearance.setBackground(round(ContextCompat.getColor(getContext(),R.color.surface_container_low),18));
        LinearLayout labels=new LinearLayout(getContext());labels.setOrientation(VERTICAL);TextView a=text(14,true);a.setText("Appearance");labels.addView(a);TextView b=text(11,false);b.setText("Switch between light and dark theme");labels.addView(b,margin(-1,-2,0,2,0,0));appearance.addView(labels,new LinearLayout.LayoutParams(0,dp(64),1));
        ThemeToggleButton theme=new ThemeToggleButton(getContext());appearance.addView(theme,new LinearLayout.LayoutParams(dp(52),dp(52)));outer.addView(appearance);
        TextView section=text(13,true);section.setText("Advanced");outer.addView(section,margin(-1,-2,0,18,0,8));
        View advanced=root.findViewById(R.id.advancedPanel);
        if(advanced!=null){ViewGroup old=(ViewGroup)advanced.getParent();if(old!=null)old.removeView(advanced);advanced.setVisibility(VISIBLE);outer.addView(advanced,new LinearLayout.LayoutParams(-1,-2));}
        return outer;
    }

    private void showPage(View home,View history,View settings,int page,MaterialButton h,MaterialButton hi,MaterialButton s){home.setVisibility(page==0?VISIBLE:GONE);history.setVisibility(page==1?VISIBLE:GONE);settings.setVisibility(page==2?VISIBLE:GONE);tintNav(h,page==0);tintNav(hi,page==1);tintNav(s,page==2);if(page==1) updateHistoryLog();}
    private void updateHistoryLog(){if(historyLog==null)return;View root=findViewById(R.id.root);View source=root.findViewById(R.id.logView);if(source instanceof TextView)historyLog.setText(((TextView)source).getText());historyLog.postDelayed(this::updateHistoryLog,500);}
    private void configureNavButton(MaterialButton button,int icon,String label){button.setText(label);button.setAllCaps(false);button.setIconResource(icon);button.setIconSize(dp(20));button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);button.setIconPadding(dp(4));button.setPadding(dp(8),dp(5),dp(8),dp(5));button.setContentDescription(label);button.setTextSize(11);}
    private void tintNav(MaterialButton button,boolean selected){int fg=ContextCompat.getColor(getContext(),selected?R.color.accent:R.color.text_secondary);button.setTextColor(fg);button.setIconTint(ColorStateList.valueOf(fg));button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),selected?R.color.accent_container:R.color.surface_container_low)));}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private FrameLayout.LayoutParams matchParent(){return new FrameLayout.LayoutParams(-1,-1);}
    private void showManagerPicker(){final java.util.List<ManagerCompatibility.ManagerInfo> managers=ManagerCompatibility.registeredManagers(getContext());final android.app.Dialog dialog=new android.app.Dialog(getContext());LinearLayout box=new LinearLayout(getContext());box.setOrientation(VERTICAL);box.setPadding(dp(22),dp(20),dp(22),dp(16));box.setBackground(round(ContextCompat.getColor(getContext(),R.color.surface),26));TextView title=text(19,true);title.setText("Install supported manager");box.addView(title);TextView subtitle=text(12,false);subtitle.setText("Select a registered manager to continue.");box.addView(subtitle,margin(-1,-2,0,6,0,14));for(ManagerCompatibility.ManagerInfo info:managers){TextView row=text(14,true);row.setText(info.name+(info.installed?"  ·  Installed":"  ·  Not installed"));row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),0,dp(14),0);row.setTextColor(ContextCompat.getColor(getContext(),R.color.text_primary));row.setBackground(round(ContextCompat.getColor(getContext(),R.color.surface_container_low),16));row.setClickable(true);row.setFocusable(true);row.setOnClickListener(v->{dialog.dismiss();ManagerCompatibility.openInstaller(getContext(),info);});box.addView(row,margin(-1,54,0,0,0,9));}TextView cancel=text(13,true);cancel.setText("Cancel");cancel.setGravity(Gravity.CENTER);cancel.setTextColor(ContextCompat.getColor(getContext(),R.color.accent));cancel.setOnClickListener(v->dialog.dismiss());box.addView(cancel,margin(-1,44,0,4,0,0));dialog.setContentView(box);dialog.setOnShowListener(x->{if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setDimAmount(.68f);dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f),-2);}});dialog.show();}
    private TextView text(int size,boolean bold){TextView v=new TextView(getContext());v.setTextSize(size);v.setTextColor(ContextCompat.getColor(getContext(),R.color.text_primary));if(bold)v.setTypeface(null,android.graphics.Typeface.BOLD);return v;}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private void setSurface(int colorRes){setBackground(round(ContextCompat.getColor(getContext(),colorRes),22));}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
