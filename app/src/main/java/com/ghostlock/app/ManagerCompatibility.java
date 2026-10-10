package com.ghostlock.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
     * BakaSU is the current rebrand of ReSukiSU.
     * Official builds keep com.resukisu.resukisu, while development/PR builds
     * may use com.resukisu.resukisu.dev or com.resukisu.resukisu.pr<number>.
     *
     * Package names and labels are not reliable for spoofed builds.
     * The manager APK embeds libksud.so, so an unknown package is accepted
     * when that native fingerprint is present.
     */
    private static final String RESUKISU_PACKAGE = "com.resukisu.resukisu";
    private static final String RESUKISU_PREFIX = "com.resukisu.resukisu.";
    private static final String BAKASU_URL = "https://github.com/Baka-SU/BakaSU";
    private static final Registered[] REGISTERED = {
            new Registered(RESUKISU_PACKAGE, "ReSukiSU / BakaSU", BAKASU_URL),
            new Registered("me.weishu.kernelsu.pr", "KernelSU PR", "https://github.com/tiann/KernelSU/releases"),
            new Registered("com.rifsxd.ksunext", "KernelSU Next", "https://github.com/KernelSU-Next/KernelSU-Next"),
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

        // First check known package IDs.
        for (Registered r : REGISTERED) {
            ApplicationInfo app = findApplication(pm, r.pkg);
            if (app == null) continue;
            return buildManagerInfo(pm, r.pkg, app, r);
        }

        // Inspect visible launcher applications. Android 11+ filters package
        // enumeration, while launcher queries can still expose user-facing managers.
        try {
            Intent launcher = new Intent(Intent.ACTION_MAIN);
            launcher.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launchers = queryLauncherActivities(pm, launcher);
            for (ResolveInfo info : launchers) {
                ApplicationInfo app = info.activityInfo == null ? null : info.activityInfo.applicationInfo;
                ManagerInfo detected = inspectCandidate(pm, app);
                if (detected != null) return detected;
            }
        } catch (Throwable ignored) {}

        // Fallback to installed applications for environments where launcher
        // visibility is unavailable.
        try {
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                ManagerInfo detected = inspectCandidate(pm, app);
                if (detected != null) return detected;
            }
        } catch (Throwable ignored) {}

        return new ManagerInfo("", "", "", false, false, false, false);
    }

    private static List<ResolveInfo> queryLauncherActivities(PackageManager pm, Intent launcher) {
        if (Build.VERSION.SDK_INT >= 33) {
            return pm.queryIntentActivities(launcher, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL));
        }
        return pm.queryIntentActivities(launcher, PackageManager.MATCH_ALL);
    }

    private static ManagerInfo inspectCandidate(PackageManager pm, ApplicationInfo app) {
        if (app == null || app.packageName == null) return null;

        String pkg = app.packageName;
        String label = "";
        try {
            CharSequence value = app.loadLabel(pm);
            if (value != null) label = value.toString().trim();
        } catch (Throwable ignored) {}
        String lowerLabel = label.toLowerCase(Locale.ROOT);

        boolean resukisuFamily = pkg.equals(RESUKISU_PACKAGE) || pkg.startsWith(RESUKISU_PREFIX);
        boolean bakaLabel = lowerLabel.contains("bakasu") || lowerLabel.contains("resukisu");
        boolean ksud = hasKsud(app);

        // Package/label identity is preferred, but libksud.so is the
        // authoritative fingerprint for randomized/spoofed manager APKs.
        if (!resukisuFamily && !bakaLabel && !ksud) return null;

        if (resukisuFamily || bakaLabel) {
            String name = lowerLabel.contains("bakasu") ? "BakaSU"
                    : lowerLabel.contains("resukisu") ? "ReSukiSU"
                    : "BakaSU / ReSukiSU";
            return new ManagerInfo(pkg, name, BAKASU_URL, true, true, false, false);
        }

        // Unknown package + libksud.so = spoofed/repackaged BakaSU/ReSukiSU.
        return new ManagerInfo(
                pkg,
                label.isEmpty() ? "BakaSU / ReSukiSU (Spoofed)" : label,
                BAKASU_URL,
                true,
                true,
                false,
                true);
    }

    private static ManagerInfo buildManagerInfo(PackageManager pm, String pkg, ApplicationInfo app, Registered r) {
        boolean verified = false;
        if (r.certs.length > 0) {
            try {
                verified = hasExpectedCertificate(packageInfo(pm, pkg), r.certs);
            } catch (Throwable ignored) {}
        }

        boolean spoofed = r.certs.length > 0 && !verified;
        String displayName = resolveManagerName(pm, app, r);
        return new ManagerInfo(
                pkg,
                displayName,
                r.pkg.equals(RESUKISU_PACKAGE) ? BAKASU_URL : r.url,
                true,
                true,
                verified,
                spoofed);
    }

    private static String resolveManagerName(PackageManager pm, ApplicationInfo app, Registered registered) {
        if (!registered.pkg.equals(RESUKISU_PACKAGE)) return registered.name;
        try {
            CharSequence label = app.loadLabel(pm);
            if (label != null) {
                String value = label.toString().trim().toLowerCase(Locale.ROOT);
                if (value.contains("bakasu")) return "BakaSU";
                if (value.contains("resukisu")) return "ReSukiSU";
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
            String installUrl = r.pkg.equals(RESUKISU_PACKAGE) ? BAKASU_URL : r.url;
            result.add(new ManagerInfo(r.pkg, displayName, installUrl, installed, true, verified, installed && r.certs.length > 0 && !verified));
        }

        // Expose randomized/spoofed managers too.
        try {
            Intent launcher = new Intent(Intent.ACTION_MAIN);
            launcher.addCategory(Intent.CATEGORY_LAUNCHER);
            for (ResolveInfo info : queryLauncherActivities(pm, launcher)) {
                ApplicationInfo app = info.activityInfo == null ? null : info.activityInfo.applicationInfo;
                if (app == null || !hasKsud(app)) continue;
                boolean alreadyKnown = false;
                for (ManagerInfo existing : result) {
                    if (existing.packageName.equals(app.packageName)) {
                        alreadyKnown = true;
                        break;
                    }
                }
                if (!alreadyKnown) {
                    CharSequence label = app.loadLabel(pm);
                    result.add(new ManagerInfo(
                            app.packageName,
                            label == null ? "BakaSU / ReSukiSU (Spoofed)" : label.toString(),
                            BAKASU_URL,
                            true,
                            true,
                            false,
                            true));
                }
            }
        } catch (Throwable ignored) {}

        return Collections.unmodifiableList(result);
    }

    public static void openInstaller(Context context, ManagerInfo manager) {
        if (manager == null || manager.installUrl == null || manager.installUrl.isEmpty()) return;
        try { context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(manager.installUrl))); } catch (Throwable ignored) {}
    }
}