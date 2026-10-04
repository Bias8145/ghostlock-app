# GhostLock：魅族 20 Pro

[English](README.md)

为 **Flyme 10.2.0.0A 的魅族 20 Pro** 提供临时 root 配置。

## 支持配置

| 项目 | 支持配置 |
|---|---|
| 设备 | 魅族 20 Pro |
| 芯片 | 高通 SM8550 |
| 固件 | Flyme 10.2.0.0A |
| Android | 13 |
| 安全补丁 | 2023-06-01 |
| 内核 | `5.15.41-android13-8-g8dc4c75ab7d8-ab1673212412` |
| 路线 | `multicast_waiter` |
| 执行环境 | Shizuku，ADB shell UID 2000，Seccomp 0 |
| CPU 对 | 主线程 3，消费者 4 |
| Root 管理器 | KernelSU v3.3.0，versionCode 32601 |
| Shizuku | v13.6.0 |

必须匹配上面的完整内核版本。其他固件或内核需要单独验证。

这是临时 root，重启后失效。重启后先启动 Shizuku，再执行 GhostLock。错误的配置可能导致内核崩溃或手机重启。

## 兼容性

内核配置依据相同版本的官方固件布局及镜像内的 BTF 类型信息。支持范围限定为上面的固件和完整内核版本，重复成功率及长期稳定性尚未确定。

## 使用步骤

1. 执行 `adb shell uname -r`，确认完整内核版本。
2. 安装 GhostLock、[KernelSU](https://github.com/tiann/KernelSU/releases/tag/v3.3.0) 和 [Shizuku](https://github.com/RikkaApps/Shizuku/releases/tag/v13.6.0)。
3. 在 Shizuku 的电脑/ADB 启动选项下打开“查看指令”，在电脑执行其显示的命令。
4. 打开 GhostLock，允许其 Shizuku 授权请求，开启“通过 Shizuku 执行”。
5. 选择 CPU 对 **3,4**，保持“强制攻击测试”关闭。
6. 执行一次，再核查 KernelSU 和 `adb shell su -c id`。

本分支已将配置加入内置索引。如果使用[上游预发布 APK](https://github.com/YuKongA/ghostlock-app/releases/tag/pre-release)，请在“高级选项 → 参数配置 → 加载配置 → 导入 offsets.conf”中导入[机型 HOCON 配置](app/src/main/assets/kernel_profiles/5.15.41-android13-8-g8dc4c75ab7d8-ab1673212412.conf)，再选择“加载此配置”。

本分支提供源码和内核配置，尚未构建独立的 APK 发布版本。

## 构建

使用 Gradle wrapper 与上游构建工作流声明的工具链：JDK 21、Android SDK、ONDK/NDK、Rust 和 Android arm64 目标。

```sh
./gradlew :profile-core:exportKernelProfiles
./gradlew :profile-core:test
./gradlew :app:assembleDebug
```

现有 GitHub “Build” 工作流支持在本分支手动执行。内置配置位于 `app/src/main/assets/kernel_profiles/`，沿用上游运行时和构建系统。

## 上游与许可证

基于 [YuKongA/ghostlock-app](https://github.com/YuKongA/ghostlock-app) 的 `123a469cdde94e0913d57c46427ce0c3bc16b96c`。

本分支新增魅族 20 Pro 配置，登记完整内核版本，并提供机型专用的中英文文档。

原始工作与致谢：[NebuSec/CyberMeowfia](https://github.com/NebuSec/CyberMeowfia)、[JoinChang/ghostlock-oneplus](https://github.com/JoinChang/ghostlock-oneplus)、[x-spy/CVE-2026-43499-popsicle](https://github.com/x-spy/CVE-2026-43499-popsicle)。

[Apache License 2.0](LICENSE)。
