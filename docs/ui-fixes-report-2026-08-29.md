# UI fixes from device report

Applied targets for the next UI correction pass:

1. Restore visible Run icon/action.
2. Add bottom content/inset clearance so the floating navbar cannot cover the log panel.
3. Correct navbar icon/label vertical spacing and minimum height so labels are not clipped.
4. Keep hamburger item labels hidden until the hamburger is opened.
5. Use consistent rounded Material-style labels for hamburger items.
6. Make Parse Boot / Parse Link / Import / Export dialogs use the app surface/background/text resources so they follow Light/Dark theme.
7. Move the theme control back into the header and make it open a Light/Dark/System choice instead of immediately toggling and navigating Home.

This file records the requested correction targets; runtime implementation must be checked against the current Test branch before marking them complete.
