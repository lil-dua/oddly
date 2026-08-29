# ODDLY — 1% HUMAN

An offline-first mobile app that turns one small action a day into a habit.
One challenge, a few minutes, a reward, a streak — no account, no backend, no network.

> **Status:** Android and iOS apps both complete and running against sample
> data, fully bilingual (Vietnamese / English) with local share-image export.
> Persistence and notifications are still to come — see
> [Not built yet](#not-built-yet).

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
  core/text/          Strings — every UI string, in both languages
  core/time/          DateFormat — date/weekday formatting per language
  data/seed/          ChallengeSeed (60 bilingual challenges), QuoteSeed, SampleData
  domain/model/       Category, Challenge, Difficulty, ChallengeCompletion,
                      DailyChallenge, UserProfile, StreakInfo, Quote, AppSettings,
                      LocalizedText
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

iosApp/iosApp/
  iOSApp.swift        App entry point
  OddlyRootView.swift Theme + splash/onboarding/main phase dispatch
  Theme/              Colours, gradients, fonts, palette, status-bar scrim
  Components/         Buttons, surfaces, icons, illustrations, challenge widgets
  Navigation/         Destination, MainShell, bottom bar, swipe-back shim
  State/              OddlyAppState — the SwiftUI twin of the Android one,
                      plus the bridging helpers for the Kotlin framework
  Screens/            Onboarding · Home · Challenge · Journey · Statistics ·
                      Quotes · Settings · Share
```

Everything both apps need — challenge selection, streak maths, statistics, date
formatting, seed content — lives in `sharedLogic`, so each UI layer only has to
render and dispatch. The two `OddlyAppState` classes are deliberately parallel:
same properties, same methods, same derived values, one in Compose snapshot
state and one in Swift's `@Observable`.

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
  astronaut, planet, starfield and brand ring are drawn with Compose `Canvas` on
  Android and SwiftUI `Canvas` on iOS, from the same normalised 0..1 path data.
  This keeps both bundles small and lets artwork pick up theme colours.

## Running

Requires a JDK. If `JAVA_HOME` is unset, Android Studio's bundled JBR works:

```bash
export JAVA_HOME="F:/App/Android/jbr"     # adjust to your install
./gradlew :androidApp:installDebug
```

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open [`iosApp/iosApp.xcodeproj`](./iosApp) in Xcode and run. The Xcode
  build invokes `:sharedLogic:embedAndSignAppleFrameworkForXcode`, so the
  framework is produced as part of the app build — no separate Gradle step.

The Xcode project uses file-system-synchronised groups, so Swift files added
under `iosApp/iosApp/` join the target automatically.

### Toolchain note

The Compose BOM is pinned to `2026.06.01` (Compose 1.11.4 / Material3 1.4.0).
That is the newest line that still compiles against `compileSdk 36` with
AGP 9.0.1 — later BOMs require AGP 9.1+ and `compileSdk 37`. Bump all three
together when you upgrade.

## Not built yet

Deliberate gaps, in the order the spec's roadmap tackles them:

1. **Persistence.** `OddlyAppState` is in-memory on both platforms, so state
   resets on relaunch. Replace with Room/SQLite repositories — the domain models
   already match the entity shapes, and screens take plain values, so only the
   state holders change.
2. **Daily challenge locking.** `DailyChallenge` is modelled but not yet
   persisted, so reopening the app re-picks today's challenge.
3. **Local notifications.** Reminder time is stored and editable; nothing is
   scheduled yet on either platform.
4. **Content.** 60 seed challenges of the ~240 the spec targets for beta.
5. **Tests.** `ChallengeSelector`, `StreakCalculator` and `StatsCalculator` are
   pure functions and the obvious first candidates.

## Localization

The app is bilingual, and the language is an **in-app setting** rather than the
system locale — a Vietnamese phone can run the app in English. Platform resource
lookup (`strings.xml`, `Localizable.strings`) follows the device, so it cannot
express that on its own, and duplicating the content library into two resource
formats would let the two apps drift. Everything therefore lives in
`sharedLogic` and both UIs read the same table:

| What | Where |
|---|---|
| UI chrome (~130 strings) | `core/text/Strings.kt` — an interface with a `VietnameseStrings` and an `EnglishStrings` object |
| Content (challenges, quotes, category and difficulty names) | `LocalizedText` fields on the domain models themselves |
| Dates and weekdays | `DateFormat`, which takes the language as an argument |

`Strings` being an interface is deliberate: adding a string breaks both
implementations until each supplies a translation, so a half-translated build
cannot compile.

Screens never thread the language through their own bodies. Android reads
`LocalStrings.current`, iOS reads `@Environment(\.strings)`, and content is
resolved once at the navigation boundary — `challenge.localized(language)`
returns a `LocalizedChallenge` whose every field is already a plain `String`.

The English side of the 60 seed challenges is written, not machine-translated,
and quotes with a known original use that original rather than a round trip.

## Share export

Both platforms generate the card locally and hand it to the system share sheet
(spec §S18) — Android via `GraphicsLayer` + `FileProvider`, iOS via
`ImageRenderer` + `ShareLink`. The image is composited onto an opaque
background before it leaves the app: the card's fill is layered and its corners
are rounded, so an unflattened export picks up whatever the receiving app puts
behind it and washes out.

Android exposes exactly one path through the provider — the cache directory the
card is written to (`res/xml/file_paths.xml`) — and the image carries only the
aggregate achievements the card shows, never notes or dates.

## Settings pickers

Theme, language and reminder time each open a picker — a dialog on Android, a
bottom sheet on iOS — rather than cycling their value on tap. With three theme
modes and a free-form time, a row that changes on every tap makes the user hunt
for the option they wanted.

Reminder time is genuinely free-form (Material 3 `TimePicker` on Android, a
wheel `DatePicker` on iOS). The four preset times remain only in onboarding,
where the spec asks for a single quick choice (§S04).

## Navigation

Each platform uses its own idiom over the same route set.

On Android, `Navigator` is a small hand-rolled back stack rather than
`navigation-compose`. The route set is small and fixed, and screens take plain
callbacks, so swapping in the library later touches only `OddlyApp.kt`.

On iOS, `MainShell` puts one `NavigationStack` behind the four tabs, so pushes
get the native slide and the edge-swipe back. Switching tabs clears the child
routes, matching the Android `selectTab`. The two screens that are modal by
nature — the reward celebration and the share card — are a `fullScreenCover` and
a `sheet` rather than pushes, which is what their own close affordances imply.

`Navigation/SwipeBack.swift` is the one UIKit shim in the iOS app: hiding the
navigation bar makes UIKit disable the interactive pop gesture, and every screen
draws its own header, so the swipe has to be re-enabled explicitly.
