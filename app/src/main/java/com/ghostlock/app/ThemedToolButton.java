package com.ghostlock.app;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Method;

/** Tool action button that preserves the original MainActivity handlers while using GhostLock-styled dialogs. */
public class ThemedToolButton extends MaterialButton {
    public ThemedToolButton(Context c) { super(c); }
    public ThemedToolButton(Context c, android.util.AttributeSet a) { super(c, a); }
    public ThemedToolButton(Context c, android.util.AttributeSet a, int s) { super(c, a, s); }

    @Override public boolean performClick() {
        Object tag = getTag();
        if (tag == null) return super.performClick();
        String action = String.valueOf(tag);
        // Import is intentionally delegated to the XML click listener so the original
        // ACTION_OPEN_DOCUMENT flow is preserved exactly.
        if ("import".equals(action)) return super.performClick();
        if ("parse_link".equals(action)) { showUrlDialog(); return true; }
        if ("parse_boot".equals(action)) { showParseChoice(); return true; }
        return super.performClick();
    }

    private void showParseChoice() {
        Dialog d = baseDialog("Parse Boot", "Choose the source used for offset extraction.");
        LinearLayout box = content(d);
        addAction(box, "Boot image", "Parse boot.img only", () -> { d.dismiss(); invokeBySignature(new Class[]{boolean.class}, new Object[]{false}); });
        addAction(box, "Boot + XBL config", "Parse with xbl_config.img", () -> { d.dismiss(); invokeBySignature(new Class[]{boolean.class}, new Object[]{true}); });
        addCancel(box, d);
        d.show(); size(d);
    }

    private void showUrlDialog() {
        Dialog d = baseDialog("Parse Link", "Parse offsets from an OTA or supported URL.");
        LinearLayout box = content(d);
        EditText input = new EditText(getContext());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("https://...");
        input.setTextColor(getColor(R.color.text_primary));
        input.setHintTextColor(getColor(R.color.text_secondary));
        input.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.border)));
        input.setPadding(dp(14), 0, dp(14), 0);
        box.addView(input, margin(-1, dp(54), 0, 4, 0, 12));
        LinearLayout actions = new LinearLayout(getContext());
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        MaterialButton cancel = button("Cancel", false);
        cancel.setOnClickListener(v -> d.dismiss());
        MaterialButton parse = button("Parse", true);
        parse.setOnClickListener(v -> {
            String url = input.getText().toString().trim();
            if (!(url.startsWith("http://") || url.startsWith("https://"))) { input.setError("Enter a valid URL"); return; }
            d.dismiss();
            invokeBySignature(new Class[]{String.class, java.io.File.class}, new Object[]{url, null});
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(48)); p.leftMargin = dp(8); actions.addView(parse, p);
        box.addView(actions, new LinearLayout.LayoutParams(-1, dp(54)));
        d.show(); size(d); input.requestFocus();
    }

    private Dialog baseDialog(String title, String subtitle) {
        Dialog d = new Dialog(getContext());
        LinearLayout root = new LinearLayout(getContext()); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(20), dp(22), dp(16)); root.setBackground(round(getColor(R.color.surface_container), 26));
        root.addView(text(title, 20, true), margin(-1, -2, 0, 0, 0, 4));
        root.addView(text(subtitle, 12, false), margin(-1, -2, 0, 0, 0, 12));
        d.setContentView(root); return d;
    }

    private LinearLayout content(Dialog d) {
        View content = d.findViewById(android.R.id.content);
        if (content instanceof ViewGroup) { ViewGroup group=(ViewGroup)content; if(group.getChildCount()>0&&group.getChildAt(0) instanceof LinearLayout)return(LinearLayout)group.getChildAt(0); }
        throw new IllegalStateException("GhostLock dialog content unavailable");
    }

    private void addAction(LinearLayout box, String title, String sub, Runnable r) {
        LinearLayout row=new LinearLayout(getContext()); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(dp(14),dp(9),dp(14),dp(9));
        row.setBackground(round(getColor(R.color.surface_container_low),16)); row.setClickable(true); row.setFocusable(true); row.setOnClickListener(v->r.run());
        row.addView(text(title,14,true)); row.addView(text(sub,11,false)); box.addView(row,margin(-1,dp(66),0,0,0,8));
    }
    private void addCancel(LinearLayout box,Dialog d){MaterialButton b=button("Cancel",false);b.setOnClickListener(v->d.dismiss());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(48));p.gravity=Gravity.END;box.addView(b,p);}
    private MaterialButton button(String s,boolean primary){MaterialButton b=new MaterialButton(getContext());b.setText(s);b.setAllCaps(false);b.setMinWidth(0);b.setTextSize(13);b.setBackgroundTintList(ColorStateList.valueOf(getColor(primary?R.color.accent:R.color.surface_container_low)));b.setTextColor(getColor(primary?R.color.on_accent:R.color.text_primary));return b;}
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(getContext());v.setText(s);v.setTextSize(size);v.setTextColor(getColor(bold?R.color.text_primary:R.color.text_secondary));if(bold)v.setTypeface(null,android.graphics.Typeface.BOLD);return v;}
    private int getColor(int id){return getResources().getColor(id,getContext().getTheme());}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private GradientDrawable round(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private void size(Dialog d){if(d.getWindow()==null)return;d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);d.getWindow().setDimAmount(.55f);d.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f),ViewGroup.LayoutParams.WRAP_CONTENT);}
    private void invokeBySignature(Class<?>[] types,Object[] args){try{MainActivity host=findMainActivity(getContext());if(host==null)throw new IllegalStateException("Tool host activity unavailable");Method found=null;for(Method m:MainActivity.class.getDeclaredMethods()){Class<?>[] params=m.getParameterTypes();if(params.length!=types.length)continue;boolean match=true;for(int i=0;i<params.length;i++)if(!params[i].equals(types[i])){match=false;break;}if(match){found=m;break;}}if(found==null)throw new NoSuchMethodException("No tool handler for requested signature");found.setAccessible(true);found.invoke(host,args);}catch(Throwable t){Throwable cause=t.getCause()!=null?t.getCause():t;String detail=cause.getMessage();Toast.makeText(getContext(),"Tool action failed: "+(detail==null?cause.getClass().getSimpleName():detail),Toast.LENGTH_SHORT).show();}}
    private MainActivity findMainActivity(Context context){Context current=context;while(current instanceof ContextWrapper){if(current instanceof MainActivity)return(MainActivity)current;Context base=((ContextWrapper)current).getBaseContext();if(base==current)break;current=base;}return current instanceof MainActivity?(MainActivity)current:null;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
