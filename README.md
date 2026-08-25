# ODDLY — 1% HUMAN

An offline-first mobile app that turns one small action a day into a habit.
One challenge, a few minutes, a reward, a streak — no account, no backend, no network.

> **Status:** Android app complete and running against sample data.
> Persistence, notifications, share-image export and the iOS app are still to
> come — see [Not built yet](#not-built-yet).

## Architecture

**Shared logic, native UI.** The domain layer is Kotlin Multiplatform and is
shared; the UI is written natively per platform — Jetpack Compose on Android,
SwiftUI on iOS. There is no Compose Multiplatform in this project.

| Module | Contains |
|---|---|
| `sharedLogic` | KMP domain layer: models, seed content, use cases. Pure Kotlin, no UI. |
| `androidApp` | The Android app: Jetpack Compose UI, navigation, state, host activity. |
| `iosApp` | SwiftUI app. Consumes `sharedLogic` via the `SharedLogic` framework. |

```
sharedLogic/src/commonMain/kotlin/dev/lildua/oddly/
  core/time/          DateFormat — Vietnamese date/weekday formatting
  data/seed/          ChallengeSeed (60 challenges), QuoteSeed, SampleData
  domain/model/       Category, Challenge, Difficulty, ChallengeCompletion,
                      DailyChallenge, UserProfile, StreakInfo, Quote, AppSettings
  domain/usecase/     ChallengeSelector, StreakCalculator, StatsCalculator

androidApp/src/main/kotlin/dev/lildua/oddly/
  MainActivity.kt     Edge-to-edge host activity
  OddlyApp.kt         Theme + navigator + screen dispatch
  ui/theme/           Colors, gradients, typography, shapes, OddlyTheme
  ui/components/      Buttons, surfaces, icons, illustrations, challenge widgets
  ui/navigation/      Destination, Navigator, bottom bar
  ui/state/           OddlyAppState — in-memory app state seeded from SampleData
  ui/screens/         onboarding · home · challenge · journey · statistics ·
                      quotes · settings · share
```

Everything the iOS app will need — challenge selection, streak maths,
statistics, date formatting, seed content — already lives in `sharedLogic`, so
the SwiftUI layer only has to render and dispatch.

## Screens

All screens from the product spec's inventory, except the two that are pure
platform surfaces (S16 notification, S17 widget).

| | Screen | | Screen |
|---|---|---|---|
| S01 | Splash | S11 | Journey / Progress |
| S02 | Onboarding | S12 | Statistics |
| S03 | Choose Interests | S13 | Streak |
| S04 | Notification Permission | S14 | Quotes |
| S05 | Home / Today's Challenge | S15 | Settings |
| S06 | Challenge Detail | S18 | Share Card |
| S07 | Challenge Complete | S19 | Empty State |
| S08 | Another Challenge | S20 | All Challenges |
| S09 | Choose Category | | |
| S10 | Calendar | | |

## Sample data

`SampleData` generates state relative to the real "today", so the calendar,
streak and statistics look live rather than frozen at a fixed date:

- 32 completions — a 12-day run ending yesterday (current streak), an earlier
  18-day run (personal best), and two stragglers.
- Today is deliberately left unfinished, because that is the state the daily
  loop is designed around.
- Level 7, 260/500 XP, 96% completion rate, six categories explored.

Everything else is **derived, not hardcoded**: completing a challenge really does
advance XP, level, streak, calendar and statistics. Reset in Settings clears it.

## Design system

Dark-first, per the spec: near-black space backdrop with sparing neon accents.

- Colours and gradients live in `ui/theme` — the pink→purple→blue sweep is
  defined once and reused by the wordmark, CTAs, progress bars and share card.
- **No image or font assets.** Category icons are emoji; UI-chrome icons and the
  astronaut, planet, starfield and brand ring are drawn with Compose `Canvas`.
  This keeps the APK small and lets artwork pick up theme colours.

## Running

Requires a JDK. If `JAVA_HOME` is unset, Android Studio's bundled JBR works:

```bash
export JAVA_HOME="F:/App/Android/jbr"     # adjust to your install
./gradlew :androidApp:installDebug
```

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open [`/iosApp`](./iosApp) in Xcode.

### Toolchain note

The Compose BOM is pinned to `2026.06.01` (Compose 1.11.4 / Material3 1.4.0).
That is the newest line that still compiles against `compileSdk 36` with
AGP 9.0.1 — later BOMs require AGP 9.1+ and `compileSdk 37`. Bump all three
together when you upgrade.

## Not built yet

Deliberate gaps, in the order the spec's roadmap tackles them:

1. **Persistence.** `OddlyAppState` is in-memory, so state resets on relaunch.
   Replace with Room/SQLite repositories — the domain models already match the
   entity shapes, and screens take plain values, so only the state holder changes.
2. **Daily challenge locking.** `DailyChallenge` is modelled but not yet
   persisted, so reopening the app re-picks today's challenge.
3. **Local notifications.** Reminder time is stored and editable; nothing is
   scheduled yet.
4. **Share export.** The share card composition is final; rendering it to a
   bitmap and handing it to the OS share sheet is outstanding.
5. **iOS app.** `iosApp` is still the project template. `sharedLogic` is ready
   for it; the SwiftUI screens are the work.
6. **Content.** 60 seed challenges of the ~240 the spec targets for beta.
7. **Tests.** `ChallengeSelector`, `StreakCalculator` and `StatsCalculator` are
   pure functions and the obvious first candidates.

## Navigation

`Navigator` is a small hand-rolled back stack rather than `navigation-compose`.
The route set is small and fixed, and screens take plain callbacks, so swapping
in the library later touches only `OddlyApp.kt`.
