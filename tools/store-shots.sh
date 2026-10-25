#!/usr/bin/env bash
# Generates every store image from one command: raw screenshots on an emulator and a simulator,
# then framed + captioned versions, feature graphics and the Play icon. Nothing leaves the machine.
#   tools/store-shots.sh                 everything
#   tools/store-shots.sh --android-only  raw Android shots, then frames
#   tools/store-shots.sh --ios-only      raw iOS shots, then frames
#   tools/store-shots.sh --frame-only    re-frame the raw shots already in build/store-shots/raw
# Output (git-ignored, not committed): build/store-assets/{play,appstore}/...; raw: build/store-shots/raw/...
# Captions: tools/store-art/captions.tsv. Demo data: sharedLogic .../core/testing/StoreShotScenario.kt.
# Does not commit anything.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
SDK="${ANDROID_SDK_ROOT:-/Users/ryan.hoang21/Development Tools/Android Studio/Android SDK}"
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"
AVD="Medium_Phone_API_36.0"
IOS_DEVICE="iPhone 17 Pro Max"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

ART="$ROOT/tools/store-art"
RAW="$ROOT/build/store-shots/raw"
OUT="$ROOT/build/store-assets"
WORK="$OUT/.work"
SHOTS=(01-home 02-reward 03-calendar 04-stats 05-share 06-settings)
LANGS=(vi en)

DO_ANDROID=1; DO_IOS=1; DO_FRAME=1
case "${1:-}" in
  "") ;;
  --android-only) DO_IOS=0 ;;
  --ios-only) DO_ANDROID=0 ;;
  --frame-only) DO_ANDROID=0; DO_IOS=0 ;;
  *) echo "Unknown option '$1' (use --android-only, --ios-only or --frame-only)" >&2; exit 2 ;;
esac

STARTED_EMULATOR=0
STARTED_SIM=0
SIM_UDID=""
DEMO_ON=0

log() { printf '\n==> %s\n' "$*"; }

cleanup() {
  local status=$?
  set +e
  if [ "$DEMO_ON" = 1 ]; then
    "$ADB" shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null 2>&1
    "$ADB" shell settings put global sysui_demo_allowed 0 >/dev/null 2>&1
  fi
  if [ -n "$SIM_UDID" ]; then
    xcrun simctl status_bar "$SIM_UDID" clear >/dev/null 2>&1
    [ "$STARTED_SIM" = 1 ] && xcrun simctl shutdown "$SIM_UDID" >/dev/null 2>&1
  fi
  [ "$STARTED_EMULATOR" = 1 ] && "$ADB" emu kill >/dev/null 2>&1
  [ -n "${CHROME_PROFILE:-}" ] && rm -rf "$CHROME_PROFILE"
  exit "$status"
}
trap cleanup EXIT

# ---------------------------------------------------------------- Android
android_shots() {
  log "Android: raw screenshots on $AVD"
  [ -x "$ADB" ] || { echo "adb not found at $ADB" >&2; exit 1; }
  if ! "$ADB" devices | awk 'NR>1 && $2=="device"' | grep -q .; then
    "$EMULATOR" -avd "$AVD" -no-window -no-audio -no-boot-anim -no-snapshot-save >/dev/null 2>&1 &
    STARTED_EMULATOR=1
    "$ADB" wait-for-device
    until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; do sleep 2; done
  fi

  # Demo mode: fixed 9:41 clock, full battery, no notification icons.
  "$ADB" shell settings put global sysui_demo_allowed 1
  DEMO_ON=1
  demo() { "$ADB" shell am broadcast -a com.android.systemui.demo -e command "$@" >/dev/null; }
  demo enter
  demo clock -e hhmm 0941
  demo battery -e level 100 -e plugged false
  # One broadcast on purpose: each "network" command replaces the whole state, so a second one would drop the wifi.
  # Mobile is hidden because the emulator's SIM otherwise shows a "3G" label beside the wifi icon.
  demo network -e wifi show -e level 4 -e fully true -e mobile hide
  demo notifications -e visible false

  rm -rf androidApp/build/outputs/connected_android_test_additional_output
  ./gradlew :androidApp:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=dev.lildua.oddly.screenshots.StoreScreenshotTest \
    -Pandroid.testInstrumentationRunnerArguments.storeShots=true

  local found=0 lang name f
  for lang in "${LANGS[@]}"; do
    mkdir -p "$RAW/android/$lang"
    rm -f "$RAW/android/$lang"/*.png
  done
  while IFS= read -r f; do
    name="$(basename "$f")"          # vi-01-home.png
    lang="${name%%-*}"
    cp "$f" "$RAW/android/$lang/${name#*-}"
    found=$((found + 1))
  done < <(find androidApp/build/outputs/connected_android_test_additional_output -name '*.png' -path '*store-shots*')
  [ "$found" -ge 12 ] || { echo "Expected 12 Android shots, found $found" >&2; exit 1; }
}

# ---------------------------------------------------------------- iOS
ios_shots() {
  log "iOS: raw screenshots on $IOS_DEVICE"
  SIM_UDID="$(xcrun simctl list devices available | grep -F "$IOS_DEVICE (" | head -1 | sed -E 's/.*\(([0-9A-F-]{36})\).*/\1/')"
  [ -n "$SIM_UDID" ] || { echo "Simulator '$IOS_DEVICE' not found" >&2; exit 1; }
  if ! xcrun simctl list devices | grep "$SIM_UDID" | grep -q Booted; then
    xcrun simctl boot "$SIM_UDID"
    STARTED_SIM=1
  fi
  # The status bar clock follows the simulator's region: a 24-hour one (e.g. en_VN) prints "09:41" whatever
  # --time says. Store shots want the App Store look "9:41", so switch the simulator to en_US once (needs a reboot).
  if [ "$(xcrun simctl spawn "$SIM_UDID" defaults read -g AppleLocale 2>/dev/null)" != en_US ]; then
    xcrun simctl spawn "$SIM_UDID" defaults write -g AppleLocale en_US
    xcrun simctl spawn "$SIM_UDID" defaults write -g AppleLanguages -array en-US
    xcrun simctl shutdown "$SIM_UDID"
    xcrun simctl boot "$SIM_UDID"
    STARTED_SIM=1
  fi
  # Waits for the full boot; a plain test run against a half-booted simulator can hang.
  xcrun simctl bootstatus "$SIM_UDID" -b >/dev/null
  xcrun simctl status_bar "$SIM_UDID" override --time "9:41" --batteryState discharging --batteryLevel 100 \
    --wifiBars 3 --cellularBars 4

  rm -rf "$RAW/ios" "$ROOT/build/store-shots/ios.xcresult"
  mkdir -p "$RAW/ios"
  TEST_RUNNER_ODDLY_SHOTS_DIR="$RAW/ios" xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iosApp \
    -destination "id=$SIM_UDID" -only-testing:iosAppUITests/StoreScreenshotTests \
    -resultBundlePath "$ROOT/build/store-shots/ios.xcresult" > "$ROOT/build/store-shots/ios-test.log" 2>&1 \
    || { tail -40 "$ROOT/build/store-shots/ios-test.log" >&2; echo "iOS screenshot test failed (log: build/store-shots/ios-test.log)" >&2; exit 1; }
}

# ---------------------------------------------------------------- framing
caption() { awk -F'\t' -v id="$1" -v col="$2" '$1 == id { print $col }' "$ART/captions.tsv"; }

# Renders an HTML file to a PNG of exactly $3 x $4 pixels with headless Chrome (no network use).
render() {
  local html="$1" png="$2" w="$3" h="$4"
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
    --window-size="$w,$h" --virtual-time-budget=2000 --user-data-dir="$CHROME_PROFILE" \
    --disable-background-networking --disable-component-update --no-first-run --no-default-browser-check \
    --disable-features=Translate,OptimizationHints --allow-file-access-from-files \
    --screenshot="$png" "file://$html" >/dev/null 2>&1
}

fill() { # fill <template> <out> key=value...
  local content out="$2" kv
  content="$(<"$1")"; shift 2
  for kv in "$@"; do content="${content//"{{${kv%%=*}}}"/${kv#*=}}"; done
  printf '%s' "$content" > "$out"
}

frame_all() {
  log "Framing"
  [ -x "$CHROME" ] || { echo "Google Chrome not found at $CHROME" >&2; exit 1; }
  CHROME_PROFILE="$(mktemp -d)"
  rm -rf "$OUT"
  mkdir -p "$WORK" "$OUT/play" "$OUT/appstore"

  local lang col shot cap
  for lang in "${LANGS[@]}"; do
    [ "$lang" = vi ] && col=2 || col=3
    mkdir -p "$OUT/play/$lang" "$OUT/appstore/$lang"
    for shot in "${SHOTS[@]}"; do
      cap="$(caption "$shot" "$col")"
      frame_one "$RAW/android/$lang/$shot.png" "$OUT/play/$lang/$shot.png" 1080 1920 "$lang" "$cap" 0.672
      frame_one "$RAW/ios/$lang/$shot.png" "$OUT/appstore/$lang/$shot.png" 1320 2868 "$lang" "$cap" 0.80
    done
    fill "$ART/feature-graphic.html" "$WORK/feature-$lang.html" "LANG=$lang" "TAGLINE=$(caption tagline "$col")"
    render "$WORK/feature-$lang.html" "$WORK/feature-$lang.png" 1024 500
    swift "$ART/Flatten.swift" "$WORK/feature-$lang.png" "$OUT/play/feature-graphic-$lang.png" 1024 500
  done

  # The icon is the one the app ships (IconRenderer), only scaled; Flatten drops its alpha channel.
  sips -z 512 512 iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon-light.png --out "$WORK/icon.png" >/dev/null
  swift "$ART/Flatten.swift" "$WORK/icon.png" "$OUT/play/icon-512.png" 512 512
  rm -rf "$WORK"
}

# <phone> is the phone width as a share of the canvas: Android raw shots are taller (20:9) than the canvas has room
# for, so they get a narrower phone to keep the bottom buttons in view instead of cropping them.
frame_one() { # frame_one <raw> <out> <w> <h> <lang> <caption> <phone>
  local raw="$1" out="$2" w="$3" h="$4" lang="$5" cap="$6" phone="$7"
  [ -f "$raw" ] || { echo "Missing raw shot $raw" >&2; exit 1; }
  local base; base="$(basename "$out" .png)-$lang-$w"
  fill "$ART/frame.html" "$WORK/$base.html" "WIDTH=$w" "HEIGHT=$h" "LANG=$lang" "CAPTION=$cap" "PHONE=$phone" "IMAGE=file://$raw"
  render "$WORK/$base.html" "$WORK/$base.png" "$w" "$h"
  swift "$ART/Flatten.swift" "$WORK/$base.png" "$out" "$w" "$h"
}

# ---------------------------------------------------------------- check
verify() {
  log "Checking sizes and alpha"
  local bad=0 f w h alpha lang shot
  check() { # check <file> <w> <h>
    f="$1"
    if [ ! -f "$f" ]; then echo "MISSING $f"; bad=1; return; fi
    w="$(sips -g pixelWidth "$f" | awk '/pixelWidth/ {print $2}')"
    h="$(sips -g pixelHeight "$f" | awk '/pixelHeight/ {print $2}')"
    alpha="$(sips -g hasAlpha "$f" | awk '/hasAlpha/ {print $2}')"
    if [ "$w" != "$2" ] || [ "$h" != "$3" ] || [ "$alpha" != no ]; then
      echo "BAD $f: ${w}x${h} hasAlpha=$alpha (want $2x$3, no alpha)"; bad=1
    fi
  }
  for lang in "${LANGS[@]}"; do
    for shot in "${SHOTS[@]}"; do
      check "$OUT/play/$lang/$shot.png" 1080 1920
      check "$OUT/appstore/$lang/$shot.png" 1320 2868
    done
    check "$OUT/play/feature-graphic-$lang.png" 1024 500
  done
  check "$OUT/play/icon-512.png" 512 512
  [ "$bad" = 0 ] || { echo "Some store assets are missing or wrong." >&2; exit 1; }
  echo "All 27 store assets are the right size with no alpha."
  (cd "$ROOT" && find build/store-assets -name '*.png' | sort)
}

[ "$DO_ANDROID" = 1 ] && android_shots
[ "$DO_IOS" = 1 ] && ios_shots
frame_all
verify
