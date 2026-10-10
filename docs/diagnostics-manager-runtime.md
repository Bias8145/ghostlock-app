# Runtime compatibility diagnostics

This document defines what GhostLock's manager/runtime diagnostics can and cannot establish.

## Status meanings

- **Manager detected**: an installed APK matches a known package/signature identity. This does not prove that its service or kernel backend is operational.
- **Runtime package coverage**: the detected package is present in the app's current runtime lookup list. A mismatch means the two recognition layers disagree; it is a diagnostic finding, not proof of the sole failure cause.
- **ksud artifact found/prepared**: a candidate executable was found and copied/prepared for the app's own runtime. This does not prove that the manager accepts the request or that its protocol is compatible.
- **Process exit 0**: the launched process returned zero. It must not be displayed or recorded as verified root-manager access.
- **Root-manager access verified**: only report this when a separate, explicit, authorized check confirms the intended manager access. Do not infer it from APK presence, an artifact, log phrases such as "ready", or process exit code.

## Diagnostic sequence

1. Record the complete device kernel release (`uname -r`) and Android build identity.
2. Log manager detection result and package name, while distinguishing an absent manager from a failed detection check.
3. Log whether the detected package is covered by the runtime lookup list.
4. Report artifact discovery/preparation independently from manager detection.
5. Preserve the native process exit code and the stage logs. Label this metric as a process result, not a root success rate.
6. When evidence is insufficient, use an explicit **unverified** status rather than upgrading it to success.

## Lessons adapted from YukongA's project structure

YukongA documents exact kernel-release matching, explicit unsupported states, profile validation, and a separation between parsing/configuration and runtime execution. The transferable lesson for this branch is to make capability and validation states explicit and to fail closed when support cannot be established. This document does not import its privilege-escalation implementation, kernel offsets, race routes, or module-loading behavior.

## Current known gap

`ManagerCompatibility` recognizes KernelSU Next (`com.rifsxd.ksunext`), while `MainActivity`'s runtime `KSU_MANAGER_PACKAGES` list does not include that package. The diagnostic should keep exposing this mismatch. This document intentionally does not change the runtime lookup list or its artifact-loading behavior.

## Validation checklist

- [ ] Manager absent is distinguishable from detection failure.
- [ ] A detected package outside runtime coverage is explicitly reported.
- [ ] Artifact presence is not treated as backend readiness.
- [ ] Exit code 0 is reported as process completion only.
- [ ] UI and analytics do not label process exit 0 as verified root success.
- [ ] Build and device tests are recorded separately; neither is implied by a documentation change.