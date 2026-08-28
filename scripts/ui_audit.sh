#!/usr/bin/env bash
set -euo pipefail

fail=0
check_file() { test -f "$1" || { echo "MISSING: $1"; fail=1; }; }
check_file app/src/main/java/com/ghostlock/app/MainActivity.java
check_file app/src/main/java/com/ghostlock/app/ThemeToggleButton.java
check_file app/src/main/res/layout/activity_main.xml
check_file app/src/main/res/values/themes.xml
check_file app/src/main/res/values-night/themes.xml

if grep -R -nE 'configuration\.fontScale[[:space:]]*=|config\.fontScale[[:space:]]*=' app/src/main/java 2>/dev/null; then
  echo 'FAIL: runtime fontScale mutation remains in source'
  fail=1
fi

if ! grep -q 'theme_mode' app/src/main/java/com/ghostlock/app/ThemeToggleButton.java; then
  echo 'FAIL: theme preference missing'
  fail=1
fi

if ! grep -q 'paddingBottom="150dp"' app/src/main/res/layout/activity_main.xml; then
  echo 'FAIL: log bottom inset missing'
  fail=1
fi

if ! grep -q 'Parse Boot' app/src/main/res/layout/activity_main.xml; then
  echo 'FAIL: Parse Boot action missing'
  fail=1
fi

if [ "$fail" -ne 0 ]; then exit 1; fi
echo 'UI audit passed.'
