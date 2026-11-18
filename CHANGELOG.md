# GhostLock V2.3.0

## Changelog

## Manager compatibility
- Added an explicit post-run log warning that a zero process exit code does not verify root-manager access.
- Renamed run statistics to show process exit codes, not verified root success.
- Updated analytics records to store `processExitZero` instead of the ambiguous `success` field.
- Kept legacy history readable and retained existing preference keys so installed users keep their statistics.
- Renamed internal run outcomes to `COMPLETED_UNVERIFIED` and `NON_ZERO_EXIT`.
- Hardened the manager readiness predicate so a recognized or spoofed manager cannot override an unsupported-kernel state.
- Added explicit KernelSU Next package recognition (`com.rifsxd.ksunext`).
- Kept manager APK detection separate from claims about kernel activation or root readiness; APK presence alone does not prove that root is operational.

- Added BakaSU manager detection while preserving ReSukiSU compatibility.
- Expanded BakaSU package discovery to cover renamed, development, and pull-request package variants.
- Added ksud fingerprint detection to recognize spoofed manager packages.
- Improved manager compatibility detection across renamed and spoofed installations.
- Refined manager status handling without reintroducing the removed status ornament.

## Recommendation

For the best compatibility and experience, use a current ReSukiSU or BakaSU manager build with GhostLock V2.3.0.

## Credits

Special thanks to YuKongA for the original GhostLock project and its foundation.

GhostLock V2 is independently adapted and developed by Khaliq.
