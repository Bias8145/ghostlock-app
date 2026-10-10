# GhostLock V2.3.0

## Changelog

## Manager compatibility
- Added an explicit post-run log warning that a zero process exit code does not verify root-manager access.
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
