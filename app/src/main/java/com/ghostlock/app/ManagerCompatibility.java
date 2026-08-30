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

/** Centralized kernel capability, manager registration and identity detection. */
public final class ManagerCompatibility {
    public enum State { READY, MANAGER_REQUIRED, KERNEL_UNSUPPORTED_MANAGER_REQUIRED, KERNEL_UNSUPPORTED, UNSUPPORTED_MANAGER, SPOOFED_MANAGER }

    public static final class ManagerInfo {
        public final String packageName, name, installUrl;
        public final boolean installed, recognized, identityVerified, spoofed;
        ManagerInfo(String p, String n, String u, boolean i, boolean r, boolean v, boolean s) { packageName=p; name=n; installUrl=u; installed=i; recognized=r; identityVerified=v; spoofed=s; }
    }

    public static final class Result {
        public final boolean kernelSupported;
        public final State state;
        public final ManagerInfo manager;
        Result(boolean k, State s, ManagerInfo m) { kernelSupported=k; state=s; manager=m; }
        /** Recognized managers remain usable when their APK signature differs; spoofed is a warning state. */
        public boolean canRun() { return state == State.READY || (state == State.SPOOFED_MANAGER && manager.recognized); }
    }

    private static final class Registered {
        final String pkg, name, url; final String[] certs;
        Registered(String p, String n, String u, String... c) { pkg=p; name=n; url=u; certs=c; }
    }

    /* These managers are currently wired into the native ksud preparation path. */
    private static final Registered[] REGISTERED = {
            new Registered("me.weishu.kernelsu", "KernelSU", "https://github.com/tiann/KernelSU/releases", "1417081413bf7ab1de8e440ecbcb62685037c8f28f048f0f8b79e305b31ab916"),
            new Registered("com.resukisu.resukisu", "ReSukiSU", "https://github.com/ReSukiSU/ReSukiSU/releases"),
            new Registered("com.kowx712.supermanager", "KOWSU", "https://github.com/KOWX712/KernelSU/releases")
    };

    private ManagerCompatibility() {}

    public static Result evaluate(Context context) {
        boolean kernel = isKernelSupported(context);
        ManagerInfo manager = detectManager(context);
        State state;
        if (!kernel) {
            if (!manager.installed) state = State.KERNEL_UNSUPPORTED_MANAGER_REQUIRED;
            else if (manager.recognized && manager.spoofed) state = State.SPOOFED_MANAGER;
            else if (!manager.recognized) state = State.UNSUPPORTED_MANAGER;
            else state = State.KERNEL_UNSUPPORTED;
        } else if (!manager.installed) state = State.MANAGER_REQUIRED;
        else if (manager.recognized && manager.spoofed) state = State.SPOOFED_MANAGER;
        else if (!manager.recognized) state = State.UNSUPPORTED_MANAGER;
        else state = State.READY;
        return new Result(kernel, state, manager);
    }

    public static boolean isKernelSupported(Context context) {
        String version = System.getProperty("os.version", "");
        for (String supported : SupportedKernels.UNAMES) if (supported.equals(version)) return true;
        return importedOffsetsMatch(context, version);
    }

    private static boolean importedOffsetsMatch(Context context, String version) {
        java.io.File file = new java.io.File(context.getFilesDir(), "offsets.json");
        if (!file.isFile()) return false;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int p = line.indexOf("\"release\"");
                if (p < 0) continue;
                int first = line.indexOf('\"', p + 9);
                int second = first < 0 ? -1 : line.indexOf('\"', first + 1);
                if (first >= 0 && second > first && version.equals(line.substring(first + 1, second))) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static ManagerInfo detectManager(Context context) {
        PackageManager pm = context.getPackageManager();
        for (Registered r : REGISTERED) {
            try {
                PackageInfo info = packageInfo(pm, r.pkg);
                boolean verified = r.certs.length > 0 && hasExpectedCertificate(info, r.certs);
                boolean spoofed = r.certs.length > 0 && !verified;
                return new ManagerInfo(r.pkg, r.name, r.url, true, true, verified, spoofed);
            } catch (Throwable ignored) {}
        }
        try {
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                if (app == null || app.packageName == null) continue;
                String libDir = app.nativeLibraryDir == null ? "" : app.nativeLibraryDir;
                if (new java.io.File(libDir, "libksud.so").isFile()) {
                    CharSequence label = app.loadLabel(pm);
                    // A library match is only a diagnostic fallback. It is not a recognized manager
                    // and must never be treated as a spoofed/approved manager.
                    return new ManagerInfo(app.packageName, label == null ? app.packageName : label.toString(), "", true, false, false, false);
                }
            }
        } catch (Throwable ignored) {}
        return new ManagerInfo("", "", "", false, false, false, false);
    }

    private static PackageInfo packageInfo(PackageManager pm, String pkg) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= 33) return pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES));
        return pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES);
    }

    private static boolean hasExpectedCertificate(PackageInfo info, String[] expected) throws Exception {
        Signature[] signatures;
        if (Build.VERSION.SDK_INT >= 28 && info.signingInfo != null) signatures = info.signingInfo.hasMultipleSigners() ? info.signingInfo.getApkContentsSigners() : info.signingInfo.getSigningCertificateHistory();
        else signatures = info.signatures;
        if (signatures == null) return false;
        for (Signature signature : signatures) {
            String digest = sha256(signature.toByteArray());
            for (String value : expected) if (value.equalsIgnoreCase(digest)) return true;
        }
        return false;
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder out = new StringBuilder(digest.length * 2);
        for (byte value : digest) out.append(String.format(Locale.ROOT, "%02x", value));
        return out.toString();
    }

    public static List<ManagerInfo> registeredManagers(Context context) {
        List<ManagerInfo> result = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        for (Registered r : REGISTERED) {
            boolean installed = false, verified = false;
            try { PackageInfo info = packageInfo(pm, r.pkg); installed = true; verified = r.certs.length > 0 && hasExpectedCertificate(info, r.certs); } catch (Throwable ignored) {}
            result.add(new ManagerInfo(r.pkg, r.name, r.url, installed, true, verified, installed && r.certs.length > 0 && !verified));
        }
        return Collections.unmodifiableList(result);
    }

    public static void openInstaller(Context context, ManagerInfo manager) {
        if (manager == null || manager.installUrl == null || manager.installUrl.isEmpty()) return;
        try { context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(manager.installUrl))); } catch (Throwable ignored) {}
    }
}