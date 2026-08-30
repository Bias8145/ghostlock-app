# GhostLock App V2

A compact Android frontend for GhostLock kernel tooling, with exact kernel matching, offset management, manager compatibility, and an adaptive light/dark interface.

## Supported Kernels

| Kernel | Device |
| --- | --- |
| `6.1.118-android14-11-gca0ef6d17716-ab13624819` | Xiaomi 14 |
| `6.1.138-android14-11-g0c3d559bcd85-ab14529422` | Xiaomi 14 |
| `6.1.145-android14-11-g09f1c0074ad7-ab14226177` | Infinix Note 50s 5G |
| `6.1.162-android14-11-gce140c0e5bf5-ab15450923` | Zenfone 11 Ultra |
| `6.6.30-android15-8-g54dcbfbef792-ab12368803-4k` | Red Magic Tablet 3 Pro |
| `6.6.77-android15-8-g4a507830d890-ab13636293-4k` | Xiaomi Civi 5 Pro, REDMI K90 / 4 Turbo, POCO F7 |
| `6.6.77-android15-8-g63ce7556864c-ab13994517-4k` | Xiaomi 15 |
| `6.6.77-android15-8-gca30f3b4bef6-abogki440974771-4k` | Xiaomi 15 Pro, REDMI K80 Pro / K80 Ultra |
| `6.6.89-android15-8-g096cdb6ecefc-ab14358676-4k` | OPPO Pad 4 Pro |
| `6.6.89-android15-8-g0889fe95bb10-ab14402178-4k` | POCO X8 Pro Max |
| `6.6.89-android15-8-gf4dc45704e54-abogki446052083-4k` | OnePlus 13 |
| `6.6.92-android15-8-g3637f4904cf7-ab13944661-4k` | Red Magic Tablet 3 Pro, Red Magic 10 Pro, Red Magic 11 Air |
| `6.6.102-android15-8-gab8eb70a71b8-ab14350911-4k` | Nothing Phone 3 |
| `6.6.102-android15-8-gb01b41c2647c-ab15574720-4k` | Xiaomi 17T |
| `6.6.102-android15-8-gfe76d1bc97fd-ab14689815-4k` | Xiaomi 17T |
| `6.6.118-android15-8-g2e6b9c3812c5-ab15114928-4k` | OPPO Find N5 |
| `6.6.118-android15-8-g93e223c276e7-abogki500782043-4k` | OPPO Find X8 Ultra, OnePlus 13 / ACE 5 Pro |
| `6.6.118-android15-8-g608a629fedf7-ab15154340-4k` | REDMI K90 Ultra |
| `6.6.118-android15-8-gc44b714366cc-abogki519650608-4k` | REDMI K80 Pro / Turbo 5 Max, POCO X8 Pro Max, Xiaomi Pad 7 Ultra |
| `6.6.118-android15-8-ge56cf6b09cca-ab15511674-ab15511674-4k` | REDMI K90 Ultra, POCO F7 |
| `6.6.118-android15-8-ge58033dc8ea6-abogki498046332-4k` | OPPO Pad 5, OnePlus Pad 2 |
| `6.6.118-android15-8-gebdfad32d749-ab15099304-4k` | OPPO Find X8 / X8 Pro |
| `6.12.23-android16-5-g16e473de48a3-abogki462654244-4k` | REDMI K90 Pro Max |
| `6.12.23-android16-5-g75e9b1c7ae7c-abogki463945075-4k` | Xiaomi 17 / 17 Pro / 17 Pro Max / 17 Ultra |
| `6.12.23-android16-5-g82efd98459a2-ab14457512-4k` | OPPO Find X9 / X9 Pro |
| `6.12.23-android16-5-ga8f88ad96df3-ab13929693-4k` | OnePlus 15 |
| `6.12.23-android16-5-gb2a876903b49-ab14541642-4k` | OnePlus 15 |
| `6.12.23-android16-5-gf1bdb13583da-ab13761046-4k` | Red Magic 11 Pro, Tablet 5 Pro |
| `6.12.30-android16-5-g6e872b4863d6-ab13847919-4k` | REDMI Note 15 4G, POCO M6 Pro 4G |
| `6.12.38-android16-5-g844001fb8721-ab14552068-4k` | OnePlus 15T |

### Unverified

Motorola moto g67 power 5G (`portov`) has been identified with:

`6.6.118-android15-8-gbf8cd367de7a-ab15314822-4k`

Build: `W1VTS36H.22-20-3-2-3` · SPL: `2026-07-01`

Status: **UNVERIFIED**. Do not use offsets from another kernel build.

Kernels are matched by exact `uname -r`. Unsupported builds are rejected. Offsets are stored under `src/kernels/<uname-release>/offsets.h`.

## Usage

Open GhostLock and select **Run**. KernelSU (`me.weishu.kernelsu`) or ReSukiSU (`com.resukisu.resukisu`) provides `ksud` for module loading. Without a compatible manager, W1/W2 can still grant uid 0, but no module is loaded.

CPU selection can be configured from Settings. The Home screen is focused on execution, tools, and logs.

## Offset Extraction

The Rust extractor can process a `boot.img`, optional `xbl_config.img`, full OTA ZIP, or an HTTP(S) OTA/image URL. It can recover kallsyms and derive required arm64 offsets.

```bash
cargo build --release --manifest-path tools/extract_rs/Cargo.toml
tools/extract_rs/target/release/ghostlock-extract boot.img --register
tools/extract_rs/target/release/ghostlock-extract OTA.zip --format json --out offsets.json
```

`--register` stores the result under `src/kernels/<uname-release>/offsets.h`.

## Importing Offsets

New kernels can be added without rebuilding the app. Use **Import offsets.json** or place `offsets.json` in the configured GhostLock data directory. Imported entries are matched against the current `uname -r` before execution.

The app can also generate offsets from **Parse OTA Link** and **Parse Image**.

## Debugging

For quick shell verification:

```bash
make ghostlock
adb push ghostlock /data/local/tmp/ghostlock
adb shell chmod 755 /data/local/tmp/ghostlock
adb shell /data/local/tmp/ghostlock
```

## Credits & License

GhostLock was originally created by **YuKongA**. This version is independently adapted and developed by **Bias8145** for the specific needs of the **Motorola G67 Power 5G**.

Additional upstream references include:

- [NebuSec/CyberMeowfia](https://github.com/NebuSec/CyberMeowfia)
- [JoinChang/ghostlock-oneplus](https://github.com/JoinChang/ghostlock-oneplus)
- [x-spy/CVE-2026-43499-popsicle](https://github.com/x-spy/CVE-2026-43499-popsicle)

Licensed under Apache License 2.0. See [LICENSE](LICENSE).

Chinese documentation: [README_ZH.md](README_ZH.md).