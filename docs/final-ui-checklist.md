# GhostLock UI Final Checklist

Branch: `Test`

- Tools FAB: icon + label; menu opens upward; no Run overlap.
- Floating rounded navbar: floating container, rounded shape, active pill, theme-aware.
- Theme: Light / Dark / System behavior must be verified on-device.
- App Scale: scale the application UI, not the system `fontScale`.
- Log: sufficient height and bottom inset; no navbar/FAB obstruction; readable in dark mode.
- Legacy cleanup: no obsolete UI path left active.
- History: current history behavior and empty state verified.
- Settings: CPU, theme, and app-scale controls remain readable in both themes.
- Parse Boot: both existing options remain available and functional.
- Regression: build, install, launch, and core actions verified.
- Final Build: debug APK workflow completes successfully.

This checklist deliberately records verification requirements without modifying runtime behavior.
