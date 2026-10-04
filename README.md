# GhostLock for MEIZU 20 Pro

[简体中文](README_ZH.md)

Temporary root support for **MEIZU 20 Pro running Flyme 10.2.0.0A**.

## Supported configuration

| Item | Supported configuration |
|---|---|
| Device | MEIZU 20 Pro |
| SoC | Qualcomm SM8550 |
| Firmware | Flyme 10.2.0.0A |
| Android | 13 |
| Security patch | 2023-06-01 |
| Kernel | `5.15.41-android13-8-g8dc4c75ab7d8-ab1673212412` |
| Route | `multicast_waiter` |
| Execution | Shizuku, ADB shell UID 2000, Seccomp 0 |
| CPU pair | Main 3, consumer 4 |
| Root manager | KernelSU v3.3.0, versionCode 32601 |
| Shizuku | v13.6.0 |

Support requires the complete kernel version above. Another firmware or kernel needs its own validation.

Root is temporary and is lost after a reboot. Restart Shizuku and run GhostLock again to regain it. An incorrect profile can cause a kernel crash or reboot.

## Compatibility

The kernel profile uses the matching official firmware layout and embedded BTF type information. Support is limited to the exact firmware and kernel listed above. Repeatability and long-term stability are not established.

## Quick start

1. Check the kernel with `adb shell uname -r`.
2. Install GhostLock, [KernelSU](https://github.com/tiann/KernelSU/releases/tag/v3.3.0), and [Shizuku](https://github.com/RikkaApps/Shizuku/releases/tag/v13.6.0).
3. In Shizuku, open **View command** under the computer/ADB startup option and run the displayed command from the computer.
4. Open GhostLock and grant its Shizuku request. Enable **Run via Shizuku**.
5. Select CPU pair **3,4** and keep force-attack testing disabled.
6. Run once and verify KernelSU and `adb shell su -c id`.

This branch registers the profile in the built-in index. When using the [upstream pre-release APK](https://github.com/YuKongA/ghostlock-app/releases/tag/pre-release), import [the supplied HOCON profile](app/src/main/assets/kernel_profiles/5.15.41-android13-8-g8dc4c75ab7d8-ab1673212412.conf) through **Advanced options → Profile configuration → Load configuration → Import offsets.conf**, then select **Load this configuration**.

This branch supplies source and a kernel profile. A separate APK release has not been built.

## Build

Use the toolchain declared by the Gradle wrapper and the upstream build workflow: JDK 21, Android SDK, ONDK/NDK, Rust, and the Android arm64 target.

```sh
./gradlew :profile-core:exportKernelProfiles
./gradlew :profile-core:test
./gradlew :app:assembleDebug
```

The existing GitHub **Build** workflow supports manual execution for this branch. Built-in profile sources are under `app/src/main/assets/kernel_profiles/`. The upstream runtime and build system are retained.

## Upstream and license

Based on [YuKongA/ghostlock-app](https://github.com/YuKongA/ghostlock-app) at `123a469cdde94e0913d57c46427ce0c3bc16b96c`.

This branch adds the MEIZU 20 Pro profile, registers its exact kernel, and provides device-specific English and Chinese documentation.

Original work and acknowledgements: [NebuSec/CyberMeowfia](https://github.com/NebuSec/CyberMeowfia), [JoinChang/ghostlock-oneplus](https://github.com/JoinChang/ghostlock-oneplus), and [x-spy/CVE-2026-43499-popsicle](https://github.com/x-spy/CVE-2026-43499-popsicle).

[Apache License 2.0](LICENSE).
