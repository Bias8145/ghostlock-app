# Manager Readiness Design

GhostLock manager detection must not equate APK visibility with root readiness.

## States

- DETECTED: manager identity inferred from package/native/kernel evidence.
- KERNEL_ACTIVE: KernelSU-family kernel support is active.
- KSUD_AVAILABLE: ksud is available from the privileged/root context.
- ROOT_OBTAINED: the GhostLock privileged execution context reached uid 0.
- ROOT_READY: root context plus active KernelSU-family userspace service have been verified.
- UNKNOWN: app sandbox cannot yet verify privileged state.

## Manager identities

- Official KernelSU: `me.weishu.kernelsu`
- KernelSU Next: `com.rifsxd.ksunext`
- BakaSU/ReSukiSU: `com.resukisu.resukisu`
- KOWSU: `com.kowx712.supermanager`

## ksud verification

Verification must run from the privileged/root context. Candidate paths include:

- `/data/adb/ksu/bin/ksud`
- `/data/adb/ksud`

Symlinks must be accepted. A pre-root app-context failure to access `/data/adb` must not be reported as "manager not installed".

## KSU Next reference

KernelSU Next exposes `com.rifsxd.ksunext` as its default manager package and has a userspace `ksud` component. GhostLock should therefore identify KSU Next independently from generic APK-only detection.

## BakaSU reference

BakaSU documents multi-manager support and compatibility with Official KernelSU/KOWSU managers. GhostLock should consequently model the kernel implementation and manager application as separate capabilities.

## Security boundary

This document intentionally covers detection, state reporting, and post-root readiness only. It does not modify or improve any privilege-escalation primitive, SELinux bypass, credential manipulation, or seccomp bypass.
