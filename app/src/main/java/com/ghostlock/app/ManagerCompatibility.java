package com.ghostlock.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Centralized compatibility and manager identity detection for the UI/run gate. */
public final class ManagerCompatibility {
    public enum State { READY, MANAGER_REQUIRED, KERNEL_UNSUPPORTED, UNSUPPORTED_MANAGER, SPOOFED_MANAGER }

    public static final class ManagerInfo {
        public final String packageName, name, installUrl;
        public final boolean installed, recognized, identityVerified, spoofed;
        ManagerInfo(String packageName, String name, String installUrl, boolean installed, boolean recognized, boolean identityVerified, boolean spoofed) {
            this.packageName=packageName; this.name=name; this.installUrl=installUrl; this.installed=installed; this.recognized=recognized; this.identityVerified=identityVerified; this.spoofed=spoofed;
        }
    }
    public static final class Result {
        public final boolean kernelSupported; public final State state; public final ManagerInfo manager;
        Result(boolean kernelSupported, State state, ManagerInfo manager) { this.kernelSupported=kernelSupported; this.state=state; this.manager=manager; }
        public boolean canRun() { return state == State.READY; }
    }
    private static final class Registered {
        final String pkg,name,url; final String[] certs;
        Registered(String pkg,String name,String url,String... certs){this.pkg=pkg;this.name=name;this.url=url;this.certs=certs;}
    }

    /* Keep this table synchronized with the manager list accepted by GhostLock. */
    private static final Registered[] REGISTERED = {
            new Registered("me.weishu.kernelsu","KernelSU","https://github.com/tiann/KernelSU/releases","1417081413bf7ab1de8e440ecbcb62685037c8f28f048f0f8b79e305b31ab916"),
            new Registered("com.resukisu.resukisu","ReSukiSU","https://github.com/ReSukiSU/ReSukiSU/releases"),
            new Registered("com.kowx712.supermanager","KOWSU","https://github.com/KOWX712/KernelSU/releases")
    };

    private ManagerCompatibility() {}

    public static Result evaluate(Context context) {
        boolean kernel=isKernelSupported(context); ManagerInfo manager=detectManager(context); State state;
        if (!kernel) state=manager.spoofed?State.SPOOFED_MANAGER:(manager.installed&&!manager.recognized?State.UNSUPPORTED_MANAGER:State.KERNEL_UNSUPPORTED);
        else if (!manager.installed) state=State.MANAGER_REQUIRED;
        else if (manager.spoofed) state=State.SPOOFED_MANAGER;
        else if (!manager.recognized) state=State.UNSUPPORTED_MANAGER;
        else state=State.READY;
        return new Result(kernel,state,manager);
    }

    public static boolean isKernelSupported(Context context) {
        String version=System.getProperty("os.version","");
        for(String supported:SupportedKernels.UNAMES) if(supported.equals(version)) return true;
        return importedOffsetsMatch(context,version);
    }

    private static boolean importedOffsetsMatch(Context context,String version) {
        java.io.File offsets=new java.io.File(context.getFilesDir(),"offsets.json"); if(!offsets.isFile()) return false;
        try(java.io.BufferedReader r=new java.io.BufferedReader(new java.io.FileReader(offsets))){String line;while((line=r.readLine())!=null){int p=line.indexOf("\"release\"");if(p>=0){int first=line.indexOf('"',p+9),second=first<0?-1:line.indexOf('"',first+1);if(first>=0&&second>first&&version.equals(line.substring(first+1,second)))return true;}}}catch(Throwable ignored){}
        return false;
    }

    public static ManagerInfo detectManager(Context context) {
        PackageManager pm=context.getPackageManager();
        for(Registered r:REGISTERED){try{PackageInfo pi=packageInfo(pm,r.pkg);boolean verified=r.certs.length>0&&hasExpectedCertificate(pi,r.certs);return new ManagerInfo(r.pkg,r.name,r.url,true,true,verified,r.certs.length>0&&!verified);}catch(Throwable ignored){}}
        try{for(ApplicationInfo app:pm.getInstalledApplications(PackageManager.GET_META_DATA)){if(app==null||app.packageName==null)continue;java.io.File lib=new java.io.File(app.nativeLibraryDir==null?"":app.nativeLibraryDir,"libksud.so");if(lib.isFile())return new ManagerInfo(app.packageName,app.loadLabel(pm).toString(),"",true,false,false,true);}}catch(Throwable ignored){}
        return new ManagerInfo("","","",false,false,false,false);
    }

    private static PackageInfo packageInfo(PackageManager pm,String pkg)throws PackageManager.NameNotFoundException{
        if(Build.VERSION.SDK_INT>=33)return pm.getPackageInfo(pkg,PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES));
        return pm.getPackageInfo(pkg,PackageManager.GET_SIGNING_CERTIFICATES);
    }
    private static boolean hasExpectedCertificate(PackageInfo pi,String[] expected)throws Exception{
        Signature[] signatures;
        if(Build.VERSION.SDK_INT>=28&&pi.signingInfo!=null) signatures=pi.signingInfo.hasMultipleSigners()?pi.signingInfo.getApkContentsSigners():pi.signingInfo.getSigningCertificateHistory();
        else signatures=pi.signatures;
        if(signatures==null)return false;
        for(Signature signature:signatures){String digest=sha256(signature.toByteArray());for(String e:expected)if(e.equalsIgnoreCase(digest))return true;}
        return false;
    }
    private static String sha256(byte[] bytes)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder s=new StringBuilder(d.length*2);for(byte b:d)s.append(String.format(Locale.ROOT,"%02x",b));return s.toString();}

    public static List<ManagerInfo> registeredManagers(Context context){
        List<ManagerInfo> out=new ArrayList<>();PackageManager pm=context.getPackageManager();
        for(Registered r:REGISTERED){boolean installed=false,verified=false;try{PackageInfo pi=packageInfo(pm,r.pkg);installed=true;verified=r.certs.length>0&&hasExpectedCertificate(pi,r.certs);}catch(Throwable ignored){}out.add(new ManagerInfo(r.pkg,r.name,r.url,installed,true,verified,installed&&r.certs.length>0&&!verified));}
        return Collections.unmodifiableList(out);
    }
    public static void openInstaller(Context context,ManagerInfo manager){if(manager==null||manager.installUrl==null||manager.installUrl.isEmpty())return;context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(manager.installUrl)));}
}