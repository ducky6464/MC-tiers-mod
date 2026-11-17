#!/usr/bin/env bash
# Collect exact Minecraft/Fabric crash logs without copying mods, saves, or credentials.
set -Eeuo pipefail

INSTANCE="${1:-$HOME/.minecraft}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUT="${2:-$PWD/mod-diagnostics-$STAMP}"
mkdir -p "$OUT/crash-reports"

if [[ ! -d "$INSTANCE" ]]; then
  echo "Minecraft instance directory not found: $INSTANCE" >&2
  echo "Usage: $0 [minecraft-instance-directory] [output-directory]" >&2
  exit 2
fi

{
  echo "Collected UTC: $(date -u --iso-8601=seconds 2>/dev/null || date -u)"
  echo "Instance directory: $INSTANCE"
  echo "OS: $(uname -a)"
  echo
  echo "Java:"
  java -version 2>&1 || true
  echo
  echo "Minecraft log files:"
  find "$INSTANCE" -maxdepth 2 -type f \( -name 'latest.log' -o -name 'debug.log' -o -name 'hs_err_pid*.log' \) -printf '%TY-%Tm-%Td %TH:%TM %p\n' 2>/dev/null | sort -r | head -30 || true
  echo
  echo "Installed mod filenames (no JAR contents copied):"
  find "$INSTANCE/mods" -maxdepth 1 -type f -name '*.jar' -printf '%f\n' 2>/dev/null | sort || true
} > "$OUT/diagnostic-summary.txt"

for name in latest.log debug.log; do
  file="$INSTANCE/logs/$name"
  if [[ -f "$file" ]]; then cp "$file" "$OUT/$name"; fi
done

if [[ -d "$INSTANCE/crash-reports" ]]; then
  find "$INSTANCE/crash-reports" -maxdepth 1 -type f -name '*.txt' -printf '%T@ %p\n' 2>/dev/null \
    | sort -rn | head -5 | cut -d' ' -f2- \
    | while IFS= read -r file; do [[ -f "$file" ]] && cp "$file" "$OUT/crash-reports/"; done
fi

find "$INSTANCE" -maxdepth 3 -type f -name 'hs_err_pid*.log' -print0 2>/dev/null \
  | while IFS= read -r -d '' file; do cp "$file" "$OUT/"; done

if [[ ! -f "$OUT/latest.log" && -z "$(find "$OUT/crash-reports" -type f -print -quit)" ]]; then
  echo "No latest.log or crash report was found. Check the instance path: $INSTANCE" >&2
  exit 3
fi

ARCHIVE="$OUT.tar.gz"
tar -czf "$ARCHIVE" -C "$(dirname "$OUT")" "$(basename "$OUT")"
echo "Diagnostics saved to: $ARCHIVE"
echo "Review the archive before sharing it. Logs can contain usernames, server addresses, and chat."
