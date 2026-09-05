package com.ghostlock.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SettingsActivity extends Activity {
    private static final String PREFS = "ghostlock_prefs";
    private static final String PREF_CPU_PAIR = "cpu_pair";
    private static final String DEVELOPER_TELEGRAM_URL = "https://t.me/VOLD_NAMESPACE";
    private static final String CUSTOM_ROM_CHANNEL_URL = "https://t.me/anocroooot";
    private static final String DEVELOPER_GITHUB_URL = "https://github.com/Bias8145";
    private static final String ORIGINAL_URL = "https://github.com/YuKongA/ghostlock-app";
    private Spinner cpuSpinner;
    private final List<int[]> cpuPairs = new ArrayList<>();
    private final List<String> cpuPairLabels = new ArrayList<>();
    private LinearLayout cpuPairOptions;
    private ImageView cpuPairChevron;
    private View rootView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        rootView = findViewById(R.id.root);
        applyWindowInsetsPadding();
        findViewById(R.id.backButton).setOnClickListener(v -> goHome());
        cpuSpinner = findViewById(R.id.cpuSpinner);
        buildCpuPairs();
        restoreCpuPair();
        setupCpuPairPanel();
        setupResourcePanels();
    }

    private void applyWindowInsetsPadding() {
        rootView.setOnApplyWindowInsetsListener((v, insets) -> {
            Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            int side = dp(20);
            v.setPadding(side, bars.top + dp(12), side, bars.bottom + dp(12));
            return insets;
        });
        rootView.requestApplyInsets();
    }

    private void setupCpuPairPanel() {
        ViewGroup cardContent = (ViewGroup) cpuSpinner.getParent();
        if (cardContent == null || cardContent.getChildCount() < 3) return;
        cpuSpinner.setVisibility(View.GONE);
        View titleView = cardContent.getChildAt(0);
        View descriptionView = cardContent.getChildAt(1);
        cardContent.removeView(titleView);
        cardContent.removeView(descriptionView);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(0), dp(0), dp(0), dp(2));
        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setGravity(Gravity.CENTER_VERTICAL);
        textColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        textColumn.addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textColumn.addView(descriptionView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cpuPairChevron = new ImageView(this);
        cpuPairChevron.setImageResource(R.drawable.ic_chevron_down);
        cpuPairChevron.setColorFilter(getResources().getColor(R.color.icon_tint));
        LinearLayout.LayoutParams chevronLp = new LinearLayout.LayoutParams(dp(20), dp(20));
        chevronLp.setMargins(dp(8), 0, dp(4), 0);
        cpuPairChevron.setLayoutParams(chevronLp);
        header.addView(textColumn);
        header.addView(cpuPairChevron);
        header.setClickable(true);
        header.setFocusable(true);
        header.setOnClickListener(v -> toggleCpuPairPanel());
        cardContent.addView(header, 0);
        cpuPairOptions = new LinearLayout(this);
        cpuPairOptions.setOrientation(LinearLayout.VERTICAL);
        cpuPairOptions.setVisibility(View.GONE);
        cpuPairOptions.setPadding(dp(0), dp(2), dp(0), dp(8));
        cardContent.addView(cpuPairOptions, 1);
        rebuildCpuPairOptions();
        updateCpuPairSummary(getSelectedCpuPairLabel());
    }

    private void toggleCpuPairPanel() {
        boolean expand = cpuPairOptions.getVisibility() != View.VISIBLE;
        cpuPairOptions.setVisibility(expand ? View.VISIBLE : View.GONE);
        if (cpuPairChevron != null) cpuPairChevron.setRotation(expand ? 180f : 0f);
    }

    private void rebuildCpuPairOptions() {
        if (cpuPairOptions == null) return;
        cpuPairOptions.removeAllViews();
        String selected = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_CPU_PAIR, null);
        for (int i = 0; i < cpuPairs.size(); i++) {
            final int position = i;
            int[] pair = cpuPairs.get(i);
            boolean active = selected != null && selected.equals(pair[0] + "," + pair[1]);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(dp(46));
            row.setPadding(dp(12), 0, dp(10), 0);
            row.setBackground(null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
            lp.setMargins(0, dp(3), 0, dp(3));
            row.setLayoutParams(lp);
            TextView label = new TextView(this);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            label.setText(cpuPairLabels.get(i));
            label.setTextColor(getResources().getColor(active ? R.color.text_primary : R.color.text_secondary));
            label.setTextSize(12);
            if (active) label.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
            row.addView(label);
            if (active) {
                TextView check = new TextView(this);
                check.setText("✓");
                check.setTextColor(getResources().getColor(R.color.icon_tint));
                check.setTextSize(16);
                check.setGravity(Gravity.CENTER);
                row.addView(check, new LinearLayout.LayoutParams(dp(24), dp(24)));
            }
            row.setOnClickListener(v -> selectCpuPair(position));
            cpuPairOptions.addView(row);
        }
    }

    private void selectCpuPair(int position) {
        if (position < 0 || position >= cpuPairs.size()) return;
        int[] pair = cpuPairs.get(position);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_CPU_PAIR, pair[0] + "," + pair[1]).apply();
        rebuildCpuPairOptions();
        updateCpuPairSummary(cpuPairLabels.get(position));
        cpuPairOptions.setVisibility(View.GONE);
        if (cpuPairChevron != null) cpuPairChevron.setRotation(0f);
    }

    private String getSelectedCpuPairLabel() {
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_CPU_PAIR, null);
        if (saved != null) {
            for (int i = 0; i < cpuPairs.size(); i++) {
                int[] pair = cpuPairs.get(i);
                if (saved.equals(pair[0] + "," + pair[1])) return cpuPairLabels.get(i);
            }
        }
        return cpuPairLabels.isEmpty() ? "No CPU pair detected" : cpuPairLabels.get(0);
    }

    private void updateCpuPairSummary(String value) {
        ViewGroup cardContent = (ViewGroup) cpuSpinner.getParent();
        if (cardContent == null || cardContent.getChildCount() == 0) return;
        View header = cardContent.getChildAt(0);
        if (!(header instanceof ViewGroup)) return;
        View textColumn = ((ViewGroup) header).getChildAt(0);
        if (!(textColumn instanceof ViewGroup) || ((ViewGroup) textColumn).getChildCount() < 2) return;
        View description = ((ViewGroup) textColumn).getChildAt(1);
        if (description instanceof TextView) ((TextView) description).setText("Selected: " + value + "\nTap to change the CPU pair.");
    }

    private void setupResourcePanels() {
        setupResourcePanel(R.id.anotherResourcePanel, R.id.anotherResourceDetails, R.id.anotherResourceChevron, R.id.anotherResourceAction, CUSTOM_ROM_CHANNEL_URL);
        setupResourcePanel(R.id.telegramChannelPanel, R.id.telegramChannelDetails, R.id.telegramChannelChevron, R.id.telegramChannelAction, DEVELOPER_TELEGRAM_URL);
        setupResourcePanel(R.id.developerGithubPanel, R.id.developerGithubDetails, R.id.developerGithubChevron, R.id.developerGithubAction, DEVELOPER_GITHUB_URL);
        setupResourcePanel(R.id.originalSourcePanel, R.id.originalSourceDetails, R.id.originalSourceChevron, R.id.originalSourceAction, ORIGINAL_URL);
    }

    private void setupResourcePanel(int panelId, int detailsId, int chevronId, int actionId, String url) {
        View panel = findViewById(panelId);
        View details = findViewById(detailsId);
        ImageView chevron = findViewById(chevronId);
        View action = findViewById(actionId);
        panel.setOnClickListener(v -> {
            boolean expand = details.getVisibility() != View.VISIBLE;
            collapseAllResourcePanels(panelId);
            details.setVisibility(expand ? View.VISIBLE : View.GONE);
            chevron.setRotation(expand ? 180f : 0f);
        });
        action.setOnClickListener(v -> openUrl(url));
    }

    private void collapseAllResourcePanels(int exceptId) {
        int[] panels = {R.id.anotherResourcePanel, R.id.telegramChannelPanel, R.id.developerGithubPanel, R.id.originalSourcePanel};
        int[] details = {R.id.anotherResourceDetails, R.id.telegramChannelDetails, R.id.developerGithubDetails, R.id.originalSourceDetails};
        int[] chevrons = {R.id.anotherResourceChevron, R.id.telegramChannelChevron, R.id.developerGithubChevron, R.id.originalSourceChevron};
        for (int i = 0; i < panels.length; i++) {
            if (panels[i] == exceptId) continue;
            findViewById(details[i]).setVisibility(View.GONE);
            findViewById(chevrons[i]).setRotation(0f);
        }
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { }
    }
    private void goHome() { finish(); }
    @Override public void onBackPressed() { goHome(); }

    private void buildCpuPairs() {
        List<Integer> online = parseCpuList(readSysFile("/sys/devices/system/cpu/online"));
        Map<Long,List<Integer>> byFreq = new TreeMap<>(Collections.reverseOrder());
        for (int cpu : online) { long freq=readMaxFreq(cpu); if(freq>0) byFreq.computeIfAbsent(freq,k->new ArrayList<>()).add(cpu); }
        for (Map.Entry<Long,List<Integer>> e:byFreq.entrySet()) {
            List<Integer> cluster=e.getValue(); Collections.sort(cluster); String suffix=" · "+formatFreq(e.getKey());
            for(int i=0;i+1<cluster.size();i+=2){int a=cluster.get(i),b=cluster.get(i+1);cpuPairs.add(new int[]{a,b});cpuPairLabels.add(a+","+b+suffix);}
        }
        boolean safe=false; for(int[] p:cpuPairs)if(p[0]==0&&p[1]==1){safe=true;break;}
        if(!safe){cpuPairs.add(new int[]{0,1});long f=readMaxFreq(0);cpuPairLabels.add("0,1"+(f>0?" · "+formatFreq(f):""));}
    }
    private void restoreCpuPair(){String saved=getSharedPreferences(PREFS,MODE_PRIVATE).getString(PREF_CPU_PAIR,null);if(saved==null||saved.equals("auto")){setDefaultCpuPair();return;}String[] p=saved.split(",");if(p.length!=2){setDefaultCpuPair();return;}try{int a=Integer.parseInt(p[0].trim()),b=Integer.parseInt(p[1].trim());for(int i=0;i<cpuPairs.size();i++){int[] pair=cpuPairs.get(i);if(pair[0]==a&&pair[1]==b){return;}}}catch(NumberFormatException ignored){}setDefaultCpuPair();}
    private void setDefaultCpuPair(){if(cpuPairs.isEmpty())return;int[] pair=cpuPairs.get(0);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PREF_CPU_PAIR,pair[0]+","+pair[1]).apply();}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private static String readSysFile(String path){File f=new File(path);if(!f.isFile())return"";try(BufferedReader r=new BufferedReader(new FileReader(f))){String s=r.readLine();return s==null?"":s.trim();}catch(IOException ignored){return"";}}
    private static List<Integer> parseCpuList(String s){List<Integer> out=new ArrayList<>();if(s==null||s.isEmpty())return out;for(String part:s.split(",")){String[] r=part.split("-");try{int lo=Integer.parseInt(r[0].trim()),hi=r.length>1?Integer.parseInt(r[1].trim()):lo;for(int c=lo;c<=hi;c++)out.add(c);}catch(NumberFormatException ignored){}}return out;}
    private static long readMaxFreq(int cpu){String s=readSysFile("/sys/devices/system/cpu/cpu"+cpu+"/cpufreq/cpuinfo_max_freq");try{return s.isEmpty()?-1:Long.parseLong(s);}catch(NumberFormatException ignored){return-1;}}
    private static String formatFreq(long khz){if(khz>=1_000_000L)return String.format(Locale.ROOT,"%.2f GHz",khz/1_000_000.0);return String.format(Locale.ROOT,"%.0f MHz",khz/1000.0);}
}
