# ODDLY — 1% HUMAN

[![CI](https://github.com/lil-dua/oddly/actions/workflows/ci.yml/badge.svg)](https://github.com/lil-dua/oddly/actions/workflows/ci.yml)

An offline-first mobile app that turns one small action a day into a habit.
One challenge, a few minutes, a reward, a streak — no account, no backend, no network.

> **Status:** Android and iOS apps are feature-complete and fully bilingual
> (Vietnamese / English), with 216 seed challenges, daily local reminders,
> share-image export and JSON backup/restore. Everything is stored on the device
> (`PersistedState` v3, migrated step by step); there is no account, backend or
> network permission. What is left before release is tracked in
> [`docs/release-todo.xlsx`](docs/release-todo.xlsx) and
> [`docs/release-process.md`](docs/release-process.md).

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
                      ReminderSeed — rotating reminder copy
                      Brand — links, support address, store ids
                      ExternalLinks, AccessibilityText
  core/time/          Today (the clock, pinnable in tests), DateFormat
  core/design/        PaletteSpec — colours shared by both platforms
  core/testing/       UiTestTags — ids the UI tests find elements by
  data/local/         KeyValueStore, PersistedState, OddlyRepository
  data/seed/          ChallengeSeed (216 bilingual challenges), QuoteSeed
  domain/model/       Category, Challenge, Difficulty, ChallengeCompletion,
                      DailyChallenge, UserProfile, StreakInfo, Quote, AppSettings,
                      LocalizedText, Reward, DayChangeNotice
  domain/usecase/     ChallengeSelector, DailyChallengeRules, StreakCalculator,
                      StatsCalculator, ReminderPlanner, QuoteFavorites,
                      FeedbackRules

androidApp/src/main/kotlin/dev/lildua/oddly/
  MainActivity.kt     Edge-to-edge host activity
  OddlyApp.kt         Theme + navigator + screen dispatch
  ui/theme/           Colors, gradients, typography, shapes, OddlyTheme
  ui/components/      Buttons, surfaces, icons, illustrations, challenge widgets
  ui/navigation/      Destination, Navigator, bottom bar
  ui/feedback/        Sound and haptics
  notifications/      ReminderScheduler (AlarmManager), receivers, ReminderStore
  ui/state/           OddlyAppState — app state, loaded from and saved to
                      OddlyRepository on every mutation
  ui/screens/         onboarding · home · challenge · journey · statistics ·
                      quotes · settings · share

iosApp/iosApp/
  iOSApp.swift        App entry point
  OddlyRootView.swift Theme + splash/onboarding/main phase dispatch
  Theme/              Colours, gradients, fonts, palette, status-bar scrim
  Components/         Buttons, surfaces, icons, illustrations, challenge widgets
  Navigation/         Destination, MainShell, bottom bar, swipe-back shim
  Feedback/           Sound and haptics
  Notifications/      ReminderScheduler (UNUserNotificationCenter)
  State/              OddlyAppState — the SwiftUI twin of the Android one,
                      plus the bridging helpers for the Kotlin framework
  PrivacyInfo.xcprivacy  App Store privacy manifest
  Screens/            Onboarding · Home · Challenge · Journey · Statistics ·
                      Quotes · Settings · Share
iosApp/iosAppUITests/ XCUITest smoke flows
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
| S01 | Splash (Android: the system splash via `core-splashscreen`) | S11 | Journey / Progress |
| S02 | Onboarding | S12 | Statistics |
| S03 | Choose Interests | S13 | Streak |
| S04 | Notification Permission | S14 | Quotes |
| S05 | Home / Today's Challenge | S15 | Settings |
| S06 | Challenge Detail | S18 | Share Card |
| S07 | Challenge Complete | S19 | Empty State |
| S08 | Another Challenge | S20 | All Challenges |
| S09 | Choose Category | | |
| S10 | Calendar | | |

## Persistence

Everything the app remembers is one JSON document in the platform key-value
store — `SharedPreferences` on Android, `NSUserDefaults` on iOS — under the key
`oddly.state.v1`, behind `OddlyRepository`.

```
KeyValueStore          read/write/remove a string. An interface, not
                       expect/actual, so Android's implementation can take a
                       Context and iOS's can take nothing.
PersistedState         profile · settings · completions · today's pick ·
                       skipped count · onboarding flag · favourite quotes,
                       plus a version field (currently 3).
OddlyRepository        encode/decode, migration, export/import, and the two
                       decisions that have to happen before the first frame:
                       freshState() and resolveDailyChallenge().
```

Not a database, deliberately. The whole data set is one profile, one settings
record and a completion list the app already holds in memory to derive the
streak, calendar and statistics from — there is nothing to query, so a schema
and a query engine would buy nothing. Seed content is not persisted either: a
completion stores only the challenge id, so new content in a later release needs
no migration (published ids must never change).

**Versioning.** Changing the shape of `PersistedState` means bumping
`CURRENT_VERSION`, giving new fields defaults, adding a step to
`OddlyRepository`'s migration and a test that reads an old document
(`LegacyDocuments.kt` holds the fixtures). A document from a *newer* version is
refused rather than guessed at.

**Backup.** Settings can export the document as JSON and import one back; import
runs through the same decode and migration, so an old backup still loads.

Writes happen on every mutation rather than on a timer, and Android's store uses
`commit()` rather than `apply()` — a write still in flight when the process is
killed loses the completion the user was just congratulated for.

Today's challenge is resolved and written back during load, so reopening the app
at lunchtime shows the challenge it showed at breakfast. A saved pick from
another day, or one whose challenge no longer exists, is replaced.

Streak, week strip and explored-category count are **derived, not stored**:
they come from the completion list and today's date alone, and each
`OddlyAppState` caches them until one of those changes. "Erase all data" in
Settings clears everything.

## Tests

```bash
./gradlew :sharedLogic:iosSimulatorArm64Test :sharedLogic:testAndroidHostTest
```

303 tests in `sharedLogic/src/commonTest`, run against both Kotlin/Native and the
JVM: streak edges (a run ending yesterday is still current), the selector's
cooldown, interest and difficulty weighting, the statistics windows and the
invariant that chart bars sum to the headline total, repository round-trips,
migration from old documents and the refusal to load one from a future version,
the reminder planner, and integrity checks on the shipped content — unique ids,
both languages present, no category too thin to reroll within.

Test names use backticks; note that Kotlin/Native rejects a comma inside one,
so the JVM-only habit of writing prose titles does not port.

UI smoke tests drive the real app on each platform (onboarding, complete a
challenge, reward, Journey, day change, one completion per day): Compose UI
tests in `androidApp/src/androidTest` on an emulator, XCUITest in
`iosApp/iosAppUITests` on a simulator. Elements are found by the ids in
`UiTestTags`, not by text. The commands are in [`.claude/CLAUDE.md`](.claude/CLAUDE.md).

## CI

GitHub Actions, no secrets needed. `.github/workflows/ci.yml` runs on every push and
pull request (docs-only changes are skipped) with two parallel jobs: `android`
(Ubuntu: shared JVM tests, `lintDebug`, an unsigned R8 `bundleRelease`, and a compile
of the androidTest sources) and `ios` (macOS: Kotlin/Native shared tests, then
`xcodebuild build-for-testing` with signing off). Existing lint findings live in
`androidApp/lint-baseline.xml`; only new errors fail the build.

UI tests are not part of CI. Run them by hand from the Actions tab: UI tests, Run
workflow (`.github/workflows/ui-tests.yml`, platform android, ios or both).

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
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"   # macOS; adjust to your install
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

## Releasing

### Android

Release builds are signed from a `keystore.properties` at the repository root,
which is gitignored along with `*.jks` / `*.keystore` — copy
`keystore.properties.example` and follow the `keytool` line in it. Without that
file the release build still assembles, unsigned, so a clone that has never seen
the keystore is not broken by its absence.

```bash
./gradlew :androidApp:bundleRelease     # the .aab Play wants
```

R8 and resource shrinking are on for release. `proguard-rules.pro` keeps the
kotlinx-serialization `$serializer` classes and the two manifest-declared
receivers, which R8 otherwise cannot see a reference to.

### iOS

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -configuration Release -destination 'generic/platform=iOS' archive
```

`TEAM_ID` in `iosApp/Configuration/Config.xcconfig` is deliberately blank in the
repository and has to be filled in before an App Store build.

Decisions already baked in:

- **iPhone only** (`TARGETED_DEVICE_FAMILY = 1`). The layout is phone-shaped;
  on a 13" iPad it stretched rather than adapted, and shipping it would have
  meant a separate iPad screenshot set and an iPad design review.
- **Portrait only**, matching Android.
- `PrivacyInfo.xcprivacy` declares no tracking, no collected data, and
  `UserDefaults` as the only required-reason API (`CA92.1`).
- `ITSAppUsesNonExemptEncryption = false`, so App Store Connect stops asking
  about export compliance on every upload. Accurate: the app has no networking
  at all.
- `UILaunchScreen` paints the launch window `#07070C`, the same colour Android
  sets as its `windowBackground`. Xcode's generated launch screen flashed white
  before the dark splash painted.

### Store submission

- [`docs/privacy-policy.html`](docs/privacy-policy.html) — a self-contained
  bilingual page. Both stores require a live URL for it; GitHub Pages serving
  `/docs` is the cheapest route. It is hosted at `Brand.PRIVACY_POLICY_URL`
  (`lil-dua.github.io`); after editing the file, republish that site or the live
  page stays out of date.
- [`docs/store-listing.md`](docs/store-listing.md) — paste-ready titles,
  descriptions, keywords and release notes in both languages, within each
  store's character limits, plus the Data Safety and privacy-label answers and
  a screenshot plan.

## What is left

Release tasks (store assets, accounts, QA passes) are tracked in
[`docs/release-todo.xlsx`](docs/release-todo.xlsx); the release routine —
versioning, notes, build checklist, crash monitoring through Play Console and
App Store Connect rather than an SDK — is in
[`docs/release-process.md`](docs/release-process.md). Adding any SDK or network
access means revisiting the privacy policy, the store Data Safety / App Privacy
answers and `PrivacyInfo.xcprivacy`, which all state that nothing is collected.

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

The English side of the 216 seed challenges is written, not machine-translated,
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

The card and the share caption both carry a call to action, because the card is
the app's only growth surface and an image with no attribution leaves the
recipient with nothing to act on. The wording and the landing-page link
(`lil-dua.github.io/projects/oddly`, source in `docs/landing`) live in
`core/text/Brand.kt`; there is deliberately no social handle until the developer
holds a real account.

## App icon

The mark is the app's own wordmark: a geometric **1%** filled with the
pink→purple→blue brand sweep, sitting on the same near-black space tile with a
purple bloom and a scattering of stars that the hero screens use.

It is authored as path data rather than typeset, for the same reason every icon
in the app is: the project ships no font or image assets it did not draw, and
outlines lifted from a system font could not be redistributed inside an APK.

[`tools/IconRenderer.swift`](tools/IconRenderer.swift) is the single source —
one path definition produces every asset, so the platforms cannot drift:

```bash
swift tools/IconRenderer.swift .
```

| Output | Where |
|---|---|
| iOS icon, light / dark / tinted | `AppIcon.appiconset/icon-*.png` (1024²) |
| Android adaptive layers | `mipmap-*/ic_launcher_{foreground,background}.png` |
| Android legacy icons (API 24–25) | `mipmap-*/ic_launcher{,_round}.png` |
| Android themed-icon layer | `drawable/ic_launcher_monochrome.xml` |
| Notification small icon | `drawable/ic_notification.xml` |

The two vectors carry the same mark as a flat silhouette; the script prints
their `pathData` when it runs.

**Why the adaptive layers are bitmaps.** The brand sweep needs a gradient, and a
vector drawable expresses one through an inline `aapt:attr`. AGP 9's resource
pipeline silently drops such a file — it compiles cleanly under standalone
`aapt2` but never reaches the merged resources, and the build then fails with
`resource drawable/ic_launcher_foreground not found`. Bitmaps sidestep that and
have the side benefit of being pixel-identical to the iOS tile.

## Daily reminder

Local notifications (spec §S16), scheduled from `reminderEnabled` and
`reminderTime` and re-derived whenever either changes. `ReminderPlanner` decides
what each fire date shows: nothing once that day's challenge is done, a
"come back" message on the last scheduled day, streak copy once the run before
that day is at least three, otherwise the rotating `ReminderSeed` copy, which
cycles by day so the reminder does not read as the same sentence forever. It
never names today's challenge: reminders are scheduled days ahead and the pick
can be swapped afterwards, so a named one would often be wrong. Completing the
day also removes a reminder already showing in the notification shade.

| | Android | iOS |
|---|---|---|
| Scheduling | `AlarmManager.setWindow`, re-armed by the receiver each day | 14 one-shot `UNCalendarNotificationTrigger`s, topped up on launch |
| Why not the obvious API | `setInexactRepeating` lets the system slide a daily alarm by most of a day; exact alarms would need `SCHEDULE_EXACT_ALARM` | a single repeating trigger cannot vary its content |
| Permission | `POST_NOTIFICATIONS`, requested at the point the user opts in | `UNUserNotificationCenter`, same |
| Surviving a reboot | `BootReceiver` re-arms from `ReminderStore` | nothing to do; iOS keeps pending requests |

The status-bar icon is the same **1%** mark as a white silhouette, so a reminder
is recognisably from this app at a glance.

`ReminderStore` is a small `SharedPreferences` mirror of the reminder time.
It exists because an Android alarm does not survive a reboot, and `BootReceiver`
can re-arm from three small values without decoding the whole saved document at
boot; it is scheduler state, not the app's data layer.

If the OS permission is refused while the in-app toggle is on, Settings shows a
notice that links straight to the system settings page — otherwise the toggle
would claim to be on while nothing ever appeared.

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
