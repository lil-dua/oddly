#!/usr/bin/env bash
# Bump the shared build number (and optionally the marketing version).
#   tools/bump-version.sh        -> CURRENT_PROJECT_VERSION + 1
#   tools/bump-version.sh 1.1    -> MARKETING_VERSION=1.1 and build + 1
# Does not commit or tag.
set -euo pipefail

FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/iosApp/Configuration/Version.xcconfig"
[ -f "$FILE" ] || { echo "Missing $FILE" >&2; exit 1; }

NEW_MARKETING="${1:-}"
if [ -n "$NEW_MARKETING" ] && ! [[ "$NEW_MARKETING" =~ ^[0-9]+(\.[0-9]+){1,2}$ ]]; then
  echo "Invalid marketing version '$NEW_MARKETING' (expected 1.0 or 1.0.1)" >&2
  exit 1
fi

OLD_MARKETING="$(sed -n 's/^MARKETING_VERSION=\(.*\)$/\1/p' "$FILE")"
OLD_BUILD="$(sed -n 's/^CURRENT_PROJECT_VERSION=\(.*\)$/\1/p' "$FILE")"
[[ "$OLD_MARKETING" =~ ^[0-9]+(\.[0-9]+){1,2}$ ]] || { echo "$FILE: bad MARKETING_VERSION '$OLD_MARKETING'" >&2; exit 1; }
[[ "$OLD_BUILD" =~ ^[0-9]+$ ]] || { echo "$FILE: bad CURRENT_PROJECT_VERSION '$OLD_BUILD'" >&2; exit 1; }

NEW_BUILD=$((OLD_BUILD + 1))
NEW_MARKETING="${NEW_MARKETING:-$OLD_MARKETING}"

TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
sed -e "s/^MARKETING_VERSION=.*/MARKETING_VERSION=$NEW_MARKETING/" \
    -e "s/^CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=$NEW_BUILD/" "$FILE" > "$TMP"
cat "$TMP" > "$FILE"

echo "MARKETING_VERSION: $OLD_MARKETING -> $NEW_MARKETING"
echo "CURRENT_PROJECT_VERSION: $OLD_BUILD -> $NEW_BUILD"
