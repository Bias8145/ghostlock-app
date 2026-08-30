# GhostLock App

GhostLock App is an Android frontend for GhostLock kernel tooling. This version is independently adapted and developed by **Bias8145**, primarily for the **Motorola moto g67 power 5G (portov)**.

## What it does

- Detects the running kernel using exact `uname -r` matching.
- Loads kernel-specific GhostLock offsets.
- Integrates with supported root managers, including KernelSU and ReSukiSU.
- Provides CPU Pair configuration through Settings.
- Parses `boot.img`, `boot.img + xbl_config.img`, OTA ZIPs, and supported HTTP(S) sources.
- Imports `offsets.json` without rebuilding the APK.
- Provides runtime status, execution logs, and log copying.
- Supports adaptive light/dark themes.

## Motorola moto g67 power 5G

V2 includes a kernel-specific entry for `portov`:

`6.6.118-android15-8-gbf8cd367de7a-ab15314822-4k`

Build: `W1VTS36H.22-20-3-2-3`  
Security patch: `2026-07-01`  
Status: **Experimental — device verification required**

The offsets are tied to this exact kernel release. Do not use offsets from a different kernel build.

## Interface

**Home** provides runtime status, device information, Tools, Run, and logs.

**Settings** contains CPU Pair configuration and the Developer & Resources section. Resource panels remain compact until expanded and provide independent links for developer resources and attribution.

## Offset Extraction

The Rust extractor is located in `tools/extract_rs` and can register kernel-specific offsets under `src/kernels/<uname-release>/offsets.h`.

```bash
cargo build --release --manifest-path tools/extract_rs/Cargo.toml
tools/extract_rs/target/release/ghostlock-extract boot.img --register
tools/extract_rs/target/release/ghostlock-extract OTA.zip --format json --out offsets.json
```

Compatible `offsets.json` files can also be imported directly from the application.

## Build

The Android app is built with Gradle through GitHub Actions. Release artifacts are named **GhostLock App**.

## Credits

Original project: **YuKongA / GhostLock App**

This branch is an independent adaptation and continued development by **Bias8145**, created to address the specific requirements of the Motorola moto g67 power 5G and its kernel environment.

Special thanks to YuKongA for the original GhostLock project and its foundation.

## License

Apache License 2.0. See [LICENSE](LICENSE).
