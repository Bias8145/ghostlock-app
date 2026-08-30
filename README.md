# GhostLock App V2

A compact Android frontend for GhostLock kernel tooling, independently adapted by **Bias8145** for the **Motorola moto g67 power 5G (portov)**.

## Features

- Exact kernel identification and offset matching by `uname -r`.
- KernelSU and ReSukiSU manager integration.
- CPU Pair selection from Settings.
- Boot image and OTA offset extraction.
- `offsets.json` import without rebuilding the APK.
- Runtime status, execution logs, and log copying.
- Adaptive light/dark theme.
- Compact expandable Developer & Resources panels.

## Motorola moto g67 power 5G

V2 is specifically adapted for the Motorola moto g67 power 5G (`portov`).

- Kernel: `6.6.118-android15-8-gbf8cd367de7a-ab15314822-4k`
- Build: `W1VTS36H.22-20-3-2-3`
- Security patch: `2026-07-01`
- Status: **Experimental / device verification required**

The kernel-specific offset entry is included. Do not substitute offsets from another kernel build.

## Interface

**Home** — runtime status, device information, Tools, Run, and logs.

**Settings** — CPU Pair and Developer & Resources.

Developer & Resources provides independent expandable panels for:

- **Another Resource** — Google Pixel custom ROM and Android resources.
- **Telegram Channel** — developer mod news, releases, and updates.
- **Developer GitHub** — source code and Android development projects.
- **Original Source** — attribution and thanks to the original GhostLock project.

## Offset Tools

`tools/extract_rs` supports `boot.img`, optional `xbl_config.img`, complete OTA ZIPs, and HTTP(S) image/OTA sources. It can recover kernel symbols and derive GhostLock offsets.

```bash
cargo build --release --manifest-path tools/extract_rs/Cargo.toml

tools/extract_rs/target/release/ghostlock-extract boot.img --register

tools/extract_rs/target/release/ghostlock-extract OTA.zip --format json --out offsets.json
```

`--register` stores the kernel entry under `src/kernels/<uname-release>/offsets.h`.

Compatible `offsets.json` files can also be imported directly from the app, allowing new kernel entries without rebuilding the APK.

## Build

The Android application is built with Gradle and automated through GitHub Actions. V2 release artifacts are published as **GhostLock App V2**.

## Credits

GhostLock was originally created by **YuKongA**.

This version is an independent adaptation and continued development by **Bias8145**, created specifically for the Motorola moto g67 power 5G and its device/kernel requirements.

Special thanks to YuKongA for the original GhostLock project and the foundation used as a reference for this adaptation.

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
