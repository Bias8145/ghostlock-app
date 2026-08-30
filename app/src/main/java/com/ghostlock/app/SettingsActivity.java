package com.ghostlock.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.backButton).setOnClickListener(v -> goHome());
        cpuSpinner = findViewById(R.id.cpuSpinner);
        buildCpuPairs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_right, cpuPairLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cpuSpinner.setAdapter(adapter);
        restoreCpuPair();
        cpuSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < 0 || position >= cpuPairs.size()) return;
                int[] pair = cpuPairs.get(position);
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_CPU_PAIR, pair[0] + "," + pair[1]).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        setupResourcePanels();
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
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception ignored) { }
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
    private void restoreCpuPair(){String saved=getSharedPreferences(PREFS,MODE_PRIVATE).getString(PREF_CPU_PAIR,null);if(saved==null||saved.equals("auto")){cpuSpinner.setSelection(0);return;}String[] p=saved.split(",");if(p.length!=2){cpuSpinner.setSelection(0);return;}try{int a=Integer.parseInt(p[0].trim()),b=Integer.parseInt(p[1].trim());for(int i=0;i<cpuPairs.size();i++){int[] pair=cpuPairs.get(i);if(pair[0]==a&&pair[1]==b){cpuSpinner.setSelection(i);return;}}}catch(NumberFormatException ignored){}cpuSpinner.setSelection(0);}
    private static String readSysFile(String path){File f=new File(path);if(!f.isFile())return"";try(BufferedReader r=new BufferedReader(new FileReader(f))){String s=r.readLine();return s==null?"":s.trim();}catch(IOException ignored){return"";}}
    private static List<Integer> parseCpuList(String s){List<Integer> out=new ArrayList<>();if(s==null||s.isEmpty())return out;for(String part:s.split(",")){String[] r=part.split("-");try{int lo=Integer.parseInt(r[0].trim()),hi=r.length>1?Integer.parseInt(r[1].trim()):lo;for(int c=lo;c<=hi;c++)out.add(c);}catch(NumberFormatException ignored){}}return out;}
    private static long readMaxFreq(int cpu){String s=readSysFile("/sys/devices/system/cpu/cpu"+cpu+"/cpufreq/cpuinfo_max_freq");try{return s.isEmpty()?-1:Long.parseLong(s);}catch(NumberFormatException ignored){return-1;}}
    private static String formatFreq(long khz){if(khz>=1_000_000L)return String.format(Locale.ROOT,"%.2f GHz",khz/1_000_000.0);return String.format(Locale.ROOT,"%.0f MHz",khz/1000.0);}
}
