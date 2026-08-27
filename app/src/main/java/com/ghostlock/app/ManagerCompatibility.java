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
    public enum State {
        READY,
        MANAGER_REQUIRED,
        KERNEL_UNSUPPORTED,
        UNSUPPORTED_MANAGER,
        SPOOFED_MANAGER
    }

    public static final class ManagerInfo {
        public final String packageName;
        public final String name;
        public final String installUrl;
        public final boolean installed;
        public final boolean recognized;
        public final boolean identityVerified;
        public final boolean spoofed;

        ManagerInfo(String packageName, String name, String installUrl, boolean installed,
                    boolean recognized, boolean identityVerified, boolean spoofed) {
            this.packageName = packageName;
            this.name = name;
            this.installUrl = installUrl;
            this.installed = installed;
            this.recognized = recognized;
            this.identityVerified = identityVerified;
            this.spoofed = spoofed;
        }
    }

    public static final class Result {
        public final boolean kernelSupported;
        public final State state;
        public final ManagerInfo manager;

        Result(boolean kernelSupported, State state, ManagerInfo manager) {
            this.kernelSupported = kernelSupported;
            this.state = state;
            this.manager = manager;
        }

        public boolean canRun() {
            return state == State.READY;
        }
    }

    private static final class Registered {
        final String pkg;
        final String name;
        final String url;
        final String[] certificateSha256;

        Registered(String pkg, String name, String url, String... certificateSha256) {
            this.pkg = pkg;
            this.name = name;
            this.url = url;
            this.certificateSha256 = certificateSha256;
        }
    }

    /* Keep this table synchronized with the manager list accepted by GhostLock. */
    private static final Registered[] REGISTERED = {
            new Registered("me.weishu.kernelsu", "KernelSU", "https://github.com/tiann/KernelSU/releases",
                    // Current official KernelSU manager certificate SHA-256.
                    "1417081413bf7ab1de8e440ecbcb62685037c8f28f048f0f8b79e305b31ab916"),
            new Registered("com.resukisu.resukisu", "ReSukiSU", "https://github.com/ReSukiSU/ReSukiSU/releases"),
            new Registered("com.kowx712.supermanager", "KOWSU", "https://github.com/KOWX712/KernelSU/releases")
    };

    private ManagerCompatibility() {}

    public static Result evaluate(Context context) {
        boolean kernel = isKernelSupported(context);
        ManagerInfo manager = detectManager(context);
        State state;
        if (!kernel) {
            state = manager.spoofed ? State.SPOOFED_MANAGER
                    : (manager.installed && !manager.recognized ? State.UNSUPPORTED_MANAGER : State.KERNEL_UNSUPPORTED);
        } else if (!manager.installed) {
            state = State.MANAGER_REQUIRED;
        } else if (manager.spoofed) {
            state = State.SPOOFED_MANAGER;
        } else if (!manager.recognized) {
            state = State.UNSUPPORTED_MANAGER;
        } else {
            state = State.READY;
        }
        return new Result(kernel, state, manager);
    }

    public static boolean isKernelSupported(Context context) {
        String version = System.getProperty("os.version", "");
        for (String supported : SupportedKernels.UNAMES) {
            if (supported.equals(version)) return true;
        }
        return importedOffsetsMatch(context, version);
    }

    private static boolean importedOffsetsMatch(Context context, String version) {
        java.io.File offsets = new java.io.File(context.getFilesDir(), "offsets.json");
        if (!offsets.isFile()) return false;
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(offsets))) {
            String line;
            while ((line = r.readLine()) != null) {
                int p = line.indexOf("\"release\"");
                if (p >= 0 && line.indexOf('\"', p + 9) >= 0) {
                    int first = line.indexOf('"', p + 9);
                    int second = line.indexOf('"', first + 1);
                    if (first >= 0 && second > first && version.equals(line.substring(first + 1, second))) return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static ManagerInfo detectManager(Context context) {
        PackageManager pm = context.getPackageManager();
        for (Registered r : REGISTERED) {
            try {
                PackageInfo pi = packageInfo(pm, r.pkg);
                if (pi == null) continue;
                boolean verified = r.certificateSha256.length == 0 || hasExpectedCertificate(pi, r.certificateSha256);
                return new ManagerInfo(r.pkg, r.name, r.url, true, true, verified, !verified && r.certificateSha256.length > 0);
            } catch (Throwable ignored) {}
        }

        // A manager can be renamed/spoofed. A package carrying ksud is a useful
        // signal, but without a registered package/signature we deliberately do
        // not treat it as a supported manager.
        try {
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                if (app == null || app.packageName == null) continue;
                java.io.File lib = new java.io.File(app.nativeLibraryDir == null ? "" : app.nativeLibraryDir, "libksud.so");
                if (lib.isFile()) {
                    return new ManagerInfo(app.packageName, app.loadLabel(pm).toString(), "", true, false, false, true);
                }
            }
        } catch (Throwable ignored) {}
        return new ManagerInfo("", "", "", false, false, false, false);
    }

    private static PackageInfo packageInfo(PackageManager pm, String pkg) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= 33) {
            return pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES));
        }
        return pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES);
    }

    private static boolean hasExpectedCertificate(PackageInfo pi, String[] expected) throws Exception {
        Signature[] signatures;
        if (Build.VERSION.SDK_INT >= 28 && pi.signingInfo != null) {
            signatures = pi.signingInfo.hasMultipleSigners()
                    ? pi.signingInfo.getApkContentsSigners()
                    : pi.signingInfo.getSigningCertificateHistory();
        } else {
            signatures = pi.signatures;
        }
        if (signatures == null) return false;
        for (Signature signature : signatures) {
            String digest = sha256(signature.toByteArray());
            for (String e : expected) if (e.equalsIgnoreCase(digest)) return true;
        }
        return false;
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder out = new StringBuilder(digest.length * 2);
        for (byte b : digest) out.append(String.format(Locale.ROOT, "%02x", b));
        return out.toString();
    }

    public static List<ManagerInfo> registeredManagers(Context context) {
        List<ManagerInfo> out = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        for (Registered r : REGISTERED) {
            boolean installed = false;
            boolean verified = false;
            try {
                PackageInfo pi = packageInfo(pm, r.pkg);
                installed = true;
                verified = r.certificateSha256.length == 0 || hasExpectedCertificate(pi, r.certificateSha256);
            } catch (Throwable ignored) {}
            out.add(new ManagerInfo(r.pkg, r.name, r.url, installed, true, verified,
                    installed && r.certificateSha256.length > 0 && !verified));
        }
        return Collections.unmodifiableList(out);
    }

    public static void openInstaller(Context context, ManagerInfo manager) {
        if (manager == null || manager.installUrl == null || manager.installUrl.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(manager.installUrl));
        context.startActivity(intent);
    }
}
