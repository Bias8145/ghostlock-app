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
        public final String packageName,name,installUrl; public final boolean installed,recognized,identityVerified,spoofed;
        ManagerInfo(String p,String n,String u,boolean i,boolean r,boolean v,boolean s){packageName=p;name=n;installUrl=u;installed=i;recognized=r;identityVerified=v;spoofed=s;}
    }
    public static final class Result {
        public final boolean kernelSupported; public final State state; public final ManagerInfo manager;
        Result(boolean k,State s,ManagerInfo m){kernelSupported=k;state=s;manager=m;} public boolean canRun(){return state==State.READY;}
    }
    private static final class Registered { final String pkg,name,url; final String[] certs; Registered(String p,String n,String u,String... c){pkg=p;name=n;url=u;certs=c;} }

    /* Package identities accepted by the current GhostLock manager table. */
    private static final Registered[] REGISTERED = {
            new Registered("me.weishu.kernelsu","KernelSU","https://github.com/tiann/KernelSU/releases","1417081413bf7ab1de8e440ecbcb62685037c8f28f048f0f8b79e305b31ab916"),
            new Registered("com.resukisu.resukisu","ReSukiSU","https://github.com/ReSukiSU/ReSukiSU/releases"),
            new Registered("com.kowx712.supermanager","KOWSU","https://github.com/KOWX712/KernelSU/releases"),
            new Registered("com.rifsxd.ksunext","KernelSU-Next","https://github.com/rifsxd/KernelSU-Next/releases"),
            new Registered("com.sukisu.ultra","SukiSU-Ultra","https://github.com/SukiSU-Ultra/SukiSU-Ultra/releases")
    };
    private ManagerCompatibility() {}

    public static Result evaluate(Context c){boolean k=isKernelSupported(c);ManagerInfo m=detectManager(c);State s;if(!k)s=m.spoofed?State.SPOOFED_MANAGER:(m.installed&&!m.recognized?State.UNSUPPORTED_MANAGER:State.KERNEL_UNSUPPORTED);else if(!m.installed)s=State.MANAGER_REQUIRED;else if(m.spoofed)s=State.SPOOFED_MANAGER;else if(!m.recognized)s=State.UNSUPPORTED_MANAGER;else s=State.READY;return new Result(k,s,m);}
    public static boolean isKernelSupported(Context c){String v=System.getProperty("os.version","");for(String s:SupportedKernels.UNAMES)if(s.equals(v))return true;return importedOffsetsMatch(c,v);}
    private static boolean importedOffsetsMatch(Context c,String v){java.io.File f=new java.io.File(c.getFilesDir(),"offsets.json");if(!f.isFile())return false;try(java.io.BufferedReader r=new java.io.BufferedReader(new java.io.FileReader(f))){String l;while((l=r.readLine())!=null){int p=l.indexOf("\"release\"");if(p>=0){int a=l.indexOf('"',p+9),b=a<0?-1:l.indexOf('"',a+1);if(a>=0&&b>a&&v.equals(l.substring(a+1,b)))return true;}}}catch(Throwable ignored){}return false;}

    public static ManagerInfo detectManager(Context c){PackageManager pm=c.getPackageManager();for(Registered r:REGISTERED){try{PackageInfo pi=packageInfo(pm,r.pkg);boolean v=r.certs.length>0&&hasExpectedCertificate(pi,r.certs);return new ManagerInfo(r.pkg,r.name,r.url,true,true,v,r.certs.length>0&&!v);}catch(Throwable ignored){}}try{for(ApplicationInfo a:pm.getInstalledApplications(PackageManager.GET_META_DATA)){if(a==null||a.packageName==null)continue;java.io.File lib=new java.io.File(a.nativeLibraryDir==null?"":a.nativeLibraryDir,"libksud.so");if(lib.isFile())return new ManagerInfo(a.packageName,a.loadLabel(pm).toString(),"",true,false,false,true);}}catch(Throwable ignored){}return new ManagerInfo("","","",false,false,false,false);}
    private static PackageInfo packageInfo(PackageManager pm,String p)throws PackageManager.NameNotFoundException{if(Build.VERSION.SDK_INT>=33)return pm.getPackageInfo(p,PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES));return pm.getPackageInfo(p,PackageManager.GET_SIGNING_CERTIFICATES);}
    private static boolean hasExpectedCertificate(PackageInfo pi,String[] e)throws Exception{Signature[] s;if(Build.VERSION.SDK_INT>=28&&pi.signingInfo!=null)s=pi.signingInfo.hasMultipleSigners()?pi.signingInfo.getApkContentsSigners():pi.signingInfo.getSigningCertificateHistory();else s=pi.signatures;if(s==null)return false;for(Signature x:s){String d=sha256(x.toByteArray());for(String y:e)if(y.equalsIgnoreCase(d))return true;}return false;}
    private static String sha256(byte[] b)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(b);StringBuilder s=new StringBuilder(d.length*2);for(byte x:d)s.append(String.format(Locale.ROOT,"%02x",x));return s.toString();}
    public static List<ManagerInfo> registeredManagers(Context c){List<ManagerInfo> out=new ArrayList<>();PackageManager pm=c.getPackageManager();for(Registered r:REGISTERED){boolean i=false,v=false;try{PackageInfo pi=packageInfo(pm,r.pkg);i=true;v=r.certs.length>0&&hasExpectedCertificate(pi,r.certs);}catch(Throwable ignored){}out.add(new ManagerInfo(r.pkg,r.name,r.url,i,true,v,i&&r.certs.length>0&&!v));}return Collections.unmodifiableList(out);}
    public static void openInstaller(Context c,ManagerInfo m){if(m==null||m.installUrl==null||m.installUrl.isEmpty())return;c.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(m.installUrl)));}
}