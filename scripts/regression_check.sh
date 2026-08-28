#!/usr/bin/env bash
set -euo pipefail

# Static regression gate for the Test branch. Keep this intentionally non-destructive.
required=(
  'app/src/main/java/com/ghostlock/app/MainActivity.java'
  'app/src/main/res/layout/activity_main.xml'
  'app/src/main/res/values/strings.xml'
)
for f in "${required[@]}"; do
  test -f "$f" || { echo "Missing required file: $f"; exit 1; }
done

# Reject accidental hard-coded system font scaling in the main activity.
if grep -nE 'fontScale[[:space:]]*=|config\.fontScale' app/src/main/java/com/ghostlock/app/MainActivity.java; then
  echo 'Regression: MainActivity still contains direct fontScale mutation.'
  exit 1
fi

echo 'Regression static checks passed.'
