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
        public boolean canRun() { return state == State.READY || (state == State.SPOOFED_MANAGER && manager.recognized); }
    }

    private static final class Registered {
        final String pkg, name, url; final String[] certs;
        Registered(String p, String n, String u, String... c) { pkg=p; name=n; url=u; certs=c; }
    }

    /*
     * BakaSU is the current rebrand of ReSukiSU. Its current upstream build
     * deliberately keeps com.resukisu.resukisu as the default package name.
     * Keep the legacy package registered and resolve the display name from the
     * installed application label so both ReSukiSU and BakaSU remain supported.
     */
    private static final String RESUKISU_PACKAGE = "com.resukisu.resukisu";
    private static final Registered[] REGISTERED = {
            new Registered(RESUKISU_PACKAGE, "ReSukiSU / BakaSU", "https://github.com/Baka-SU/BakaSU"),
            new Registered("me.weishu.kernelsu.pr", "KernelSU PR", "https://github.com/tiann/KernelSU/releases"),
            new Registered("me.weishu.kernelsu", "KernelSU", "https://github.com/tiann/KernelSU/releases", "1417081413bf7ab1de8e440ecbcb62685037c8f28f048f0f8b79e305b31ab916"),
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
                int first = line.indexOf('"', p + 9);
                int second = first < 0 ? -1 : line.indexOf('"', first + 1);
                if (first >= 0 && second > first && version.equals(line.substring(first + 1, second))) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static ManagerInfo detectManager(Context context) {
        PackageManager pm = context.getPackageManager();

        // Detect by package identity first. BakaSU and legacy ReSukiSU share
        // the default package, so use the installed application label to keep
        // the two names distinct without weakening package recognition.
        for (Registered r : REGISTERED) {
            ApplicationInfo app = findApplication(pm, r.pkg);
            if (app == null) continue;

            boolean verified = false;
            if (r.certs.length > 0) {
                try {
                    verified = hasExpectedCertificate(packageInfo(pm, r.pkg), r.certs);
                } catch (Throwable ignored) {
                    // Package is still recognized even when signing metadata is unavailable.
                }
            }

            boolean spoofed = r.certs.length > 0 && !verified;
            String displayName = resolveManagerName(pm, app, r);
            String installUrl = r.pkg.equals(RESUKISU_PACKAGE)
                    ? "https://github.com/Baka-SU/BakaSU"
                    : r.url;
            return new ManagerInfo(r.pkg, displayName, installUrl, true, true, verified, spoofed);
        }

        // Fallback for renamed/spoofed managers: locate an installed package
        // that actually ships ksud. This mirrors GhostLock's runtime dependency
        // instead of treating arbitrary package labels as managers.
        try {
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                if (app == null || app.packageName == null) continue;
                if (!hasKsud(app)) continue;
                CharSequence label = app.loadLabel(pm);
                return new ManagerInfo(
                        app.packageName,
                        label == null ? app.packageName : label.toString(),
                        "",
                        true,
                        false,
                        false,
                        false);
            }
        } catch (Throwable ignored) {}

        return new ManagerInfo("", "", "", false, false, false, false);
    }

    private static String resolveManagerName(PackageManager pm, ApplicationInfo app, Registered registered) {
        if (!RESUKISU_PACKAGE.equals(registered.pkg)) return registered.name;
        try {
            CharSequence label = app.loadLabel(pm);
            if (label != null) {
                String value = label.toString().trim();
                if (value.toLowerCase(Locale.ROOT).contains("bakasu")) return "BakaSU";
                if (value.toLowerCase(Locale.ROOT).contains("resukisu")) return "ReSukiSU";
            }
        } catch (Throwable ignored) {}
        return registered.name;
    }

    private static ApplicationInfo findApplication(PackageManager pm, String pkg) {
        try {
            return pm.getApplicationInfo(pkg, 0);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean hasKsud(ApplicationInfo app) {
        String libDir = app.nativeLibraryDir;
        if (libDir == null || libDir.isEmpty()) return false;
        java.io.File base = new java.io.File(libDir);
        return new java.io.File(base, "libksud.so").isFile()
                || new java.io.File(new java.io.File(base, "arm64"), "libksud.so").isFile()
                || new java.io.File(new java.io.File(base, "arm"), "libksud.so").isFile();
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
            ApplicationInfo app = findApplication(pm, r.pkg);
            boolean installed = app != null;
            boolean verified = false;
            if (installed && r.certs.length > 0) {
                try {
                    verified = hasExpectedCertificate(packageInfo(pm, r.pkg), r.certs);
                } catch (Throwable ignored) {}
            }
            String displayName = installed ? resolveManagerName(pm, app, r) : r.name;
            String installUrl = r.pkg.equals(RESUKISU_PACKAGE)
                    ? "https://github.com/Baka-SU/BakaSU"
                    : r.url;
            result.add(new ManagerInfo(r.pkg, displayName, installUrl, installed, true, verified, installed && r.certs.length > 0 && !verified));
        }
        return Collections.unmodifiableList(result);
    }

    public static void openInstaller(Context context, ManagerInfo manager) {
        if (manager == null || manager.installUrl == null || manager.installUrl.isEmpty()) return;
        try { context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(manager.installUrl))); } catch (Throwable ignored) {}
    }
}
