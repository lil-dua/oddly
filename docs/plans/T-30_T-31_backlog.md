# Kế hoạch: Dọn module chết + README, memo hoá giá trị dẫn xuất, dọn backlog (T-30 + T-31 + backlog)

Đợt sau commit `a675129` (working tree sạch). Yêu cầu người dùng: "Đã commit, hãy tiếp tục với T30, T31 và dọn backlog."
**Không sửa `docs/release-todo.xlsx`, không sửa `docs/plans/README.md`** (phiên chính cập nhật khi kết thúc đợt). Không commit.
Không đổi `PersistedState` (không tăng `CURRENT_VERSION`). Không đổi id thử thách/quote. Không thêm thư viện/quyền/SDK.

## Mục tiêu
1. **T-30:** xoá module `sharedUI/` (chết), gỡ alias catalog không còn ai dùng, sửa README + comment lỗi thời
   ("persistence là gap", "daily pick chưa persist", "Room repositories", "120 thử thách", "67 test", placeholder Brand…)
   để phản ánh đúng hiện trạng.
2. **T-31:** giá trị dẫn xuất (streak, weekActivity, completedToday, exploredCategoryCount, swapsLeft, thống kê) không tính lại
   mỗi lần đọc/recompose; tra cứu thử thách theo id O(1) (Map) dùng chung cho selector, `StatsCalculator`, AppState. Cache phải
   vô hiệu đúng khi completions đổi, khi `today` đổi (`refreshDay`, `importBackup`), khi lượt đổi đổi. Hai AppState giữ cùng API.
3. **Backlog (10 mục người dùng đã chọn):** cảnh báo mặt trời ở 2 thử thách; xoá chuỗi chết; caption S08 khi hết lượt; iOS gỡ
   thông báo đã hiện trước `guard`; Android S14 giữ chip lọc; copy nhắc trung tính; privacy policy/store listing khớp
   "Erase all data"; store listing bỏ số version cứng; tab test id thống nhất; iOS `StarryBox` không ẩn content.

## Hiện trạng (file:dòng)
**T-30**
- `settings.gradle.kts:31-33` chỉ `include(":androidApp")`, `include(":sharedLogic")`. `sharedUI/` có 3 file tracked
  (`git ls-files sharedUI`): `build.gradle.kts`, `src/androidMain/.../ui/navigation/SystemBackHandler.android.kt`,
  `src/commonMain/composeResources/drawable/compose-multiplatform.xml`. `sharedUI/build.gradle.kts:6` dùng
  `libs.plugins.composeMultiplatform`, `:37-50` dùng `libs.compose.uiToolingPreview`, `compose.components.resources`,
  `androidx.lifecycle.viewmodelCompose` — **không alias nào tồn tại** trong `gradle/libs.versions.toml`.
- grep `sharedUI|SystemBackHandler` ngoài thư mục `sharedUI/` (gradle, `iosApp.xcodeproj/project.pbxproj`, kt, swift, md, toml,
  properties): **0 kết quả**. An toàn để xoá.
- Catalog `gradle/libs.versions.toml`: không có alias nào chỉ dùng bởi sharedUI. Alias **không ai dùng** (grep
  `androidApp/build.gradle.kts`, `sharedLogic/build.gradle.kts`, `build.gradle.kts`): `kotlin-testJunit` (`:27`),
  `androidx-core-ktx` (`:32`, version `androidx-core = "1.19.0"` `:7` — chính là bản không dùng được với toolchain ghim),
  `androidx-espresso-core` (`:37`, version `androidx-espresso` `:8`). `androidApp/build.gradle.kts:30-32` comment nhắc
  "the catalogue's core-ktx 1.19".
- Comment lỗi thời: `androidApp/.../notifications/ReminderStore.kt:11-13` ("When the Room repositories land…"),
  `ReminderScheduler.kt:25-27` ("app data is still in memory, per the roadmap"), `sharedLogic/.../core/text/ReminderSeed.kt:15-17`
  ("the daily pick is not persisted yet"). `ReminderStore` vẫn được dùng (`ReminderScheduler.kt:46,59,66` cho `BootReceiver`) —
  giữ lại, chỉ sửa comment.
- `README.md`: `:6-9` "Persistence is the main gap"; `:30` "ChallengeSeed (120…)"; `:34` usecase thiếu `DailyChallengeRules`,
  `ReminderPlanner`, `QuoteFavorites`, `FeedbackRules`; thiếu `core/design`, `core/testing`, Android `notifications/`, `ui/feedback/`,
  iOS `Feedback/`, `Notifications/`; `:94-95` PersistedState không nói version/migrate (hiện v3, key `oddly.state.v1`);
  `:118` "Reset in Settings"; `:120-135` "67 tests" (hiện 295 mỗi nền tảng) và không nhắc UI test; `:153` `JAVA_HOME="F:/App/..."`;
  `:217-224` "Fill in the developer name…" (đã điền, đã host); `:226-241` "Not built yet" (content 216, share link xong ở T-10,
  crash = `docs/release-process.md`, import xong ở T-13) — toàn bộ lỗi thời; `:267` "120 seed challenges"; `:283-287` "SHORT_LINK
  and HANDLE are placeholders" (T-10 đã thay); `:328-330` "daily pick is not persisted yet"; `:342-345` "folds into the Room
  repositories"; `:68-69` S01 Android nay là splash hệ thống (`core-splashscreen`).

**T-31**
- Android `ui/state/OddlyAppState.kt:65-69` `_completions = mutableStateListOf` → `absorb()` `:377-378` `clear()` + `addAll()` mỗi lần;
  `currentState()` `:362` `_completions.toList()` (copy O(n)). Dẫn xuất là getter thuần: `streak` `:122-123`, `weekActivity` `:125-126`,
  `completedToday` `:128-129`, `swapsLeft` `:132` (dựng `currentState()` mỗi lần đọc), `canReroll` `:138`, `exploredCategoryCount`
  `:148-152` (`ChallengeSeed.byId` mỗi completion). `dailyChallengeDate` `:105` là `var` thường (không phải snapshot state).
- Đọc nhiều lần mỗi frame: `HomeScreen.kt:205-206,220-221` (`canReroll` ×2, `swapsLeft` ×2), `OddlyApp.kt:243,288,292,310`.
- Android màn tính trong composition: `StatisticsScreen.kt:71` `StatsCalculator.summarize(...)` mỗi recompose; `JourneyScreen.kt:61`
  `distribution`; `ChooseCategoryScreen.kt:59-64`. `CalendarScreen.kt:70` `remember(state.completions)` — key là cùng instance
  `SnapshotStateList` nên **không bao giờ tính lại** (lỗi tiềm ẩn; hết lỗi khi completions thành `List` bất biến).
- iOS `State/OddlyAppState.swift:87-117` các computed property tương tự; `swapsLeft` `:100-102` dựng `currentState()` (bridge cả
  mảng completions sang Kotlin mỗi lần đọc); `exploredCategoryCount` `:115-117` gọi `ChallengeSeed.shared.byId` qua bridge.
- `sharedLogic/.../data/seed/ChallengeSeed.kt:41` `byId = all.firstOrNull { it.id == id }` (O(n), 216 phần tử).
- `ChallengeSelector.kt:43` `library.firstOrNull { it.id == completion.challengeId }` trong vòng lặp completions (O(n×m)).
  `select` nhận `library` tham số (test dùng library riêng) → map phải dựng từ `library`, không dùng `ChallengeSeed`.
- `StatsCalculator.kt:89` dùng `ChallengeSeed.byId`; `:60-61` gọi `distribution(windowed)` **hai lần**.
- `DailyChallengeRules.kt:91-97` `swapsLeft(state, today)` = `isDayDone` + `swapsUsedOn` (chỉ đọc `completions`, `dailyChallenge`).

**Backlog**
1. `ChallengeSeed.kt:2618` `life_sunrise` (how `:2627-2629`), `:2794` `life_watch_something_end` (how `:2801-2804`): không có câu cấm
   nhìn thẳng mặt trời (mẫu có sẵn `:2041` "đừng nhìn thẳng vào mặt trời" / "never straight at the sun"). Giới hạn 120 ký tự/bước,
   2–4 bước (`SeedContentTest.kt:224,249`).
2. `Strings.kt:53` (interface), `:291` (vi), `:528` (en) `wantAnotherChallengeBody` — grep `androidApp/src`, `iosApp`: 0 chỗ dùng.
3. S08: Android `AnotherChallengeScreen.kt:132-137` luôn `swapsLeftToday(swapsLeft)`; iOS `AnotherChallengeScreen.swift:63`. Gọi từ
   `OddlyApp.kt:287-288`, `MainShell.swift:159-160`. Home đã có logic `completedToday → wantAnotherChallengeDoneBody; swapsLeft == 0 →
   noSwapsLeftBody` (`HomeScreen.kt:218-222`, `HomeScreen.swift:130-133`). Chuỗi có sẵn `noSwapsLeftToday` (`Strings.kt:304/541`).
4. iOS `Notifications/ReminderScheduler.swift:51-66`: `cancelAll()` → `guard settings.reminderEnabled, await isAuthorized()` → mới
   `removeDeliveredNotifications` khi `isDayDone`. Android gỡ vô điều kiện: `OddlyApp.kt:95-97` → `ReminderScheduler.dismissDelivered`.
5. Android `screens/quotes/QuotesScreen.kt:75-77` `remember` cho `showFavorites`/`favoriteSnapshot`/`index`. Lưu ý: Activity khoá
   portrait + `configChanges` gồm `orientation|screenSize|uiMode` (`AndroidManifest.xml:25-26`) nên xoay/dark mode không recreate;
   `Navigator` là `remember` (`Navigator.kt:49-50`) và `OddlyApp.kt:156` `AnimatedContent` chỉ compose route hiện tại, **không có
   `SaveableStateHolder`** → S14 → Share (`OddlyApp.kt:345-346` push `ShareCard`) → Back: S14 compose lại từ đầu, mất chip
   "Yêu thích" + vị trí. iOS Share là `sheet` nên S14 giữ state. `rememberSaveable` đơn thuần **không** sửa được lỗi này.
6. `ReminderSeed.kt:33-36` (vi "Giữ chuỗi ngày của bạn nhé" / "…để không bị gián đoạn"), `:60-63` (en "Keep your streak going" /
   "…stay unbroken"). `ReminderPlanner.kt:53-57` đã có copy streak riêng khi streak ≥ 3; rơi về `ReminderSeed` khi streak < 3.
   Test `ReminderPlannerTest.kt:68` so khớp `ReminderSeed.content(...)`.
7. `docs/privacy-policy.html:209,220` "Reset all data"; app: `Strings.kt:624` `eraseAllData = "Erase all data"` (vi `:386`
   "Xóa tất cả dữ liệu" — bản vi `:132,143` đã khớp). Thêm: `docs/store-listing.md:152` "Settings → Reset all data", `README.md:118`.
8. `docs/store-listing.md:17` "See `iosApp/Configuration/Version.xcconfig` (currently `1.0`, build `1`)".
9. Android `navigation/BottomBar.kt:44-51` `testTag` null cho STATISTICS; iOS `Navigation/Destination.swift:50-59` `default: "tab.\(rawValue)"`.
   `UiTestTags.kt:33-35` chỉ có HOME/JOURNEY/SETTINGS; mirror Swift `iosAppUITests/UITestIds.swift:29-31`.
10. iOS `Components/Illustrations.swift:247-261` `StarryBox.body = drawing.accessibilityHidden(true)` (ẩn cả `content()`); chỗ gọi duy
    nhất `ChallengeComponents.swift:260-264` (EmptyState, astronaut trang trí, đã tự `.accessibilityHidden(true)`). Android `StarryBox`
    (`Illustrations.kt:262-273`) **còn dùng** ở `ChallengeComponents.kt:376` (EmptyState, `a11yHidden()` ở chỗ gọi) → giữ.

## Quyết định
| # | Câu hỏi | Chọn | Nhãn (ĐÃ CHỐT / KHUYẾN NGHỊ / CHẶN) | Lý do |
|---|---|---|---|---|
| 1 | Phạm vi đợt | T-30 + T-31 + đúng 10 mục backlog trong prompt; các mục "KHÔNG làm" giữ nguyên | ĐÃ CHỐT | Người dùng/điều phối chọn |
| 2 | Catalog: gỡ gì? | Gỡ `kotlin-testJunit`, `androidx-core-ktx` + version `androidx-core`, `androidx-espresso-core` + version `androidx-espresso` (0 tham chiếu); sửa comment `androidApp/build.gradle.kts:30-32` cho khớp. Không đụng alias đang dùng | KHUYẾN NGHỊ (tự áp dụng) | Alias chết, `core-ktx 1.19` còn là cái bẫy với toolchain ghim. Phương án khác: giữ nguyên catalog (an toàn nhưng để lại bẫy) |
| 3 | `ReminderStore` | Giữ class, chỉ sửa KDoc (lý do tồn tại: `BootReceiver` re-arm không cần decode cả document); không đổi `apply()` | KHUYẾN NGHỊ (tự áp dụng) | T-30 là dọn tài liệu; gộp vào repository là refactor hành vi, ngoài phạm vi |
| 4 | Android memo hoá | `completions` thành `var completions: List<ChallengeCompletion> by mutableStateOf(...)` (`private set`, bỏ `_completions`); `streak`, `weekActivity`, `completedToday`, `exploredCategoryCount` = `by derivedStateOf { }`; `swapsLeft` getter rẻ qua overload shared mới (QĐ #6) đọc `completedToday` đã cache | KHUYẾN NGHỊ (tự áp dụng) | List bất biến: `absorb` 1 phép gán, `currentState()` hết copy, `remember(state.completions)` ở màn hoạt động đúng. `derivedStateOf` tự vô hiệu khi `completions`/`today` (đều snapshot state) đổi. Phương án khác: giữ `SnapshotStateList` + chỉ `derivedStateOf` (vẫn copy trong `currentState`, vẫn lỗi `remember` ở Calendar) |
| 5 | iOS memo hoá | `streak`, `weekActivity`, `completedToday`, `exploredCategoryCount` thành stored `private(set) var`, tính lại trong `recomputeDerived()` gọi từ `didSet` của `completions` và `today` + cuối `init` (didSet không chạy trong init). `swapsLeft` computed rẻ qua overload shared | KHUYẾN NGHỊ (tự áp dụng) | Gợi ý xlsx "cache theo `completions.count`" **sai** với import cùng số lượng khác nội dung và với đổi ngày; `didSet` vô hiệu đúng mọi đường ghi. Phương án khác: cache key `(today, version counter)` — phức tạp hơn, cùng kết quả |
| 6 | `swapsLeft` không dựng `PersistedState` | Thêm overload shared `DailyChallengeRules.swapsLeft(dayDone: Boolean, pickDate: LocalDate?, swapsUsed: Int, today: LocalDate): Int`; overload cũ `swapsLeft(state, today)` uỷ quyền sang nó (hành vi không đổi) | KHUYẾN NGHỊ (tự áp dụng) | Luật vẫn ở shared; hai AppState gọi cùng hàm. Swift: `swapsLeft(dayDone:pickDate:swapsUsed:today:)`, `Int32` |
| 7 | Map id → Challenge | `ChallengeSeed`: `private val byIdIndex: Map<String, Challenge>` khai báo SAU `all`; `byId` tra map. `ChallengeSelector.select`: dựng map một lần từ tham số `library` (giữ phần tử ĐẦU khi trùng id, khớp `firstOrNull`). `StatsCalculator.summarize` tính `distribution(windowed)` một lần | KHUYẾN NGHỊ (tự áp dụng) | Hành vi không đổi, O(n+m). Không thêm API public mới ngoài cái đã có |
| 8 | Thống kê trên màn Android | `remember(state.completions, state.today, range, strings.language) { summarize }` ở S12; `remember(state.completions)` cho `distribution` S11 và `completedPerCategory` S09. iOS giữ nguyên (SwiftUI chỉ chạy lại `body` khi dependency quan sát đổi) | KHUYẾN NGHỊ (tự áp dụng) | Thống kê phụ thuộc `range` cục bộ của màn — không đưa vào AppState (giữ AppState song song) |
| 9 | S08 khi `swapsLeft == 0` | Hiện trạng thái thay vì ẩn: `completedToday → wantAnotherChallengeDoneBody` ("Hẹn mai nhé" / "See you tomorrow"); `swapsLeft == 0 → noSwapsLeftToday` ("Bạn đã dùng hết lượt đổi hôm nay" / "You've used all of today's swaps"); còn lại `swapsLeftToday(n)`. S08 nhận thêm tham số `completedToday`. Nút giữ hành vi cũ (bấm → về Home) | KHUYẾN NGHỊ (tự áp dụng) | Dùng chuỗi có sẵn, không thêm chuỗi; báo người dùng vì sao bấm sẽ về Home. Phương án khác: ẩn caption (mất thông tin) |
| 10 | S14 giữ chip lọc | Android: `rememberSaveable` cho `showFavorites`, `index`, và snapshot yêu thích lưu dạng **danh sách id** (map lại qua `QuoteSeed`); thêm `rememberSaveableStateHolder()` trong `OddlyApp` bọc nội dung mỗi route bằng `SaveableStateProvider(key)` (key chuỗi ổn định từ `Destination`), và `removeState(key)` khi route rời back stack | KHUYẾN NGHỊ (tự áp dụng) | Chỉ `rememberSaveable` là no-op (portrait + configChanges, Navigator không khôi phục); lỗi thật là S14 → Share → Back mất chip, iOS không mất (sheet). Không có màn nào khác dùng `rememberSaveable` nên holder không đổi hành vi màn khác. Mở lại S14 từ Home vẫn về "Tất cả" + quote hôm nay (khớp iOS pop) |
| 11 | Copy nhắc "Giữ chuỗi…" | Viết lại thành copy trung tính (giữ nguyên vị trí index 2 để không xê dịch vòng xoay): vi title "Hôm nay vẫn còn chỗ cho 1%", body "Một hành động nhỏ là đủ để thêm một ngày vào hành trình."; en title "There's still room for today's 1%", body "One small action is enough to add a day to your journey." + test chặn "chuỗi"/"streak" trong `ReminderSeed` | KHUYẾN NGHỊ (tự áp dụng) | Copy streak đã có nhánh riêng (streak ≥ 3) trong `ReminderPlanner`. Phương án khác: lọc theo streak trong planner — thêm luật cho một câu, khó test vòng xoay |
| 12 | Câu mặt trời | Sửa bước có sẵn (không thêm bước, không đổi id): `life_sunrise` bước 3 → "Cất điện thoại đi, và đừng nhìn thẳng vào mặt trời." / "Put your phone away, and never look straight at the sun."; `life_watch_something_end` bước 2 → "Tới trước khi mặt trời lặn khoảng 20 phút; đừng nhìn thẳng vào mặt trời." / "Arrive about twenty minutes before sunset, and never look straight at the sun." | KHUYẾN NGHỊ (tự áp dụng) | Giữ 3 bước, dưới 120 ký tự, cùng cách viết với `:2041` |
| 13 | Tab test id | Thêm `UiTestTags.TAB_STATISTICS = "tab.statistics"` + mirror `UITestIds.tabStatistics`; Android `testTag` thành `String` không null (when đủ 4 nhánh), iOS bỏ `default` | KHUYẾN NGHỊ (tự áp dụng) | Giá trị trùng chuỗi iOS đang phát ra (`tab.statistics`) → không ảnh hưởng gì ngoài thống nhất |
| 14 | `StarryBox` | iOS: chỉ `StarField` `.accessibilityHidden(true)`, `content()` để nguyên; EmptyState giữ `.accessibilityHidden(true)` ở chỗ gọi (astronaut trang trí) → VoiceOver không đổi. Android: giữ (còn dùng), không sửa | KHUYẾN NGHỊ (tự áp dụng) | Đúng ngữ nghĩa wrapper, không đổi trải nghiệm hiện tại |
| 15 | Privacy policy / store listing | EN "Reset all data" → "Erase all data" (`privacy-policy.html:209,220`, `store-listing.md:152`, README); `store-listing.md:17` → "See `iosApp/Configuration/Version.xcconfig` (`MARKETING_VERSION` / `CURRENT_PROJECT_VERSION`; bump with `tools/bump-version.sh`)" | KHUYẾN NGHỊ (tự áp dụng) | Khớp `Strings.kt:624`. Người dùng phải đăng lại `lil-dua.github.io` |

Không có mục CHẶN.

## Phạm vi (làm / không làm)
**Làm:** các task T1–T12 dưới đây.
**Không làm:** Onboarding "Bỏ qua" vs `reminderEnabled`; tagline splash Android; nội dung/bản quyền quote; thêm 96 id vào
`PublishedContent.kt`; T-23C bảng màu; rút tiêu đề dài; ô lịch < 48dp; Switch `heightIn`; iOS import trên main thread; gộp
`ReminderStore` vào repository; khôi phục back stack qua process death (Navigator vẫn `remember`); làm mờ nút S08 khi hết lượt;
nhãn trợ năng tim/chip; đổi `PersistedState`.

## Các task
Thứ tự: T1 → (T2, T3 song song sau T1) → T4 → T5…T11 độc lập → T12 (docs, cuối, để README phản ánh mọi thay đổi) → T13 (tester).
Sau mỗi task: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` rồi
`./gradlew :androidApp:compileDebugKotlin` (+ `:sharedLogic:compileKotlinIosSimulatorArm64` nếu sửa shared; `xcodebuild … build` nếu sửa Swift).

- [x] T1: [shared] Hiệu năng tra cứu + overload `swapsLeft` (file: `data/seed/ChallengeSeed.kt`, `domain/usecase/ChallengeSelector.kt`,
  `domain/usecase/StatsCalculator.kt`, `domain/usecase/DailyChallengeRules.kt`).
  - `ChallengeSeed`: `private val byIdIndex: Map<String, Challenge> = all.associateBy { it.id }` (đặt sau `all` — thứ tự khởi tạo object);
    `fun byId(id: String): Challenge? = byIdIndex[id]`. KDoc ngắn vì sao.
  - `ChallengeSelector.select`: một map `id → Challenge` từ `library` (giữ phần tử đầu khi trùng, VD `buildMap { library.forEach { getOrPut… } }`
    hoặc tương đương), dùng ở `recentCategoryCounts`. Chữ ký `select` không đổi.
  - `StatsCalculator.summarize`: `val distribution = distribution(windowed)` dùng cho cả hai trường.
  - `DailyChallengeRules.swapsLeft(dayDone: Boolean, pickDate: LocalDate?, swapsUsed: Int, today: LocalDate): Int`; overload
    `swapsLeft(state, today)` gọi lại nó. Không tham số mặc định (Swift).
  - Xong khi: compile Android + iOS sim; `:sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test` xanh, số test = 295.
- [x] T2: [android] Memo hoá `ui/state/OddlyAppState.kt` (QĐ #4, #6).
  - `var completions: List<ChallengeCompletion> by mutableStateOf(initial.completions)` + `private set`; xoá `_completions`; `absorb` gán 1 lần;
    `resetAllData` gán `emptyList()`; `currentState()` dùng thẳng `completions`. Thay mọi `_completions` trong file.
  - `val streak by derivedStateOf { StreakCalculator.calculate(completions, today) }`, tương tự `weekActivity`, `completedToday`,
    `exploredCategoryCount` (dùng `ChallengeSeed.byId`). `swapsLeft` getter = `DailyChallengeRules.swapsLeft(completedToday, dailyChallengeDate, swapsUsed, today)`.
    Cập nhật KDoc lớp (`:44-46`) cho đúng: dẫn xuất vẫn không lưu, nhưng được memo theo snapshot state.
  - Không đổi tên/kiểu public nào (`completions` vẫn `List<ChallengeCompletion>`).
  - Xong khi: compile; `assembleDebug`.
- [x] T3: [ios] Memo hoá `State/OddlyAppState.swift` song song T2 (QĐ #5, #6).
  - `today` và `completions` thêm `didSet { recomputeDerived() }`; `private(set) var streak: StreakInfo`, `weekActivity: [DayActivity]`,
    `completedToday: Bool`, `exploredCategoryCount: Int` stored; `private func recomputeDerived()`; gọi ở cuối `init` (sau khi gán mọi stored
    property — cần giá trị khởi tạo tạm cho 4 property hoặc tính trước từ `resolution.state`).
  - `swapsLeft` = `Int(DailyChallengeRules.shared.swapsLeft(dayDone: completedToday, pickDate: dailyChallengeDate, swapsUsed: Int32(swapsUsed), today: today))`.
  - Sửa KDoc lớp `:10-12` như Android. Nếu `@Observable` + `didSet` gây lỗi biên dịch, dùng phương án gọi `recomputeDerived()` tường minh
    ở mọi nơi gán `completions`/`today` (init, `absorb`, `resetAllData`, `refreshDay`, `importBackup`) và ghi vào Dev notes.
  - Xong khi: `xcodebuild … build` thành công.
- [x] T4: [android] Memo thống kê trên màn (QĐ #8): `StatisticsScreen.kt:71`, `JourneyScreen.kt:61`, `ChooseCategoryScreen.kt:59-64` bọc `remember(keys)`.
  Phụ thuộc T2 (key `state.completions` phải là instance mới khi đổi). Xong khi: compile.
- [x] T5: [shared] Nội dung + chuỗi (QĐ #11, #12, mục 2): sửa 2 bước thử thách (vi + en, không đổi id/bước), viết lại `ReminderSeed` index 2 (vi + en) và
  KDoc `ReminderSeed.kt:15-17` (lý do thật: copy không nêu tên thử thách vì có thể đã đổi sau khi lên lịch / lên lịch 14 ngày trước — ghi đúng hiện
  trạng, không còn "not persisted"); xoá `wantAnotherChallengeBody` khỏi interface + `VietnameseStrings` + `EnglishStrings`. Xong khi: compile Android + iOS sim
  (đảm bảo iOS không tham chiếu), test shared xanh.
- [x] T6: [shared][android][ios] Tab test id (QĐ #13): `UiTestTags.TAB_STATISTICS`; `BottomBar.kt` `testTag` non-null, `Modifier.testTag(tab.testTag)`;
  `Destination.swift` đủ 4 case, bỏ `default`, sửa comment; `UITestIds.swift` thêm `tabStatistics`. Xong khi: compile cả hai.
- [x] T7: [android][ios] S08 caption (QĐ #9): thêm tham số `completedToday: Boolean` cho `AnotherChallengeScreen` hai bên, truyền từ `OddlyApp.kt:287`
  và `MainShell.swift:159`; caption theo 3 nhánh. Xong khi: compile cả hai.
- [x] T8: [ios] `ReminderScheduler.reschedule`: chuyển khối tính `todayLocal` + `isDayDone` + `removeDeliveredNotifications` lên trước `guard`
  (sau `cancelAll()`), cập nhật comment cho khớp Android ("gỡ vô điều kiện"). Xong khi: build iOS.
- [x] T9: [android] S14 giữ chip lọc (QĐ #10): `OddlyApp.kt` `rememberSaveableStateHolder()` + `SaveableStateProvider(key)` quanh nội dung trong
  `AnimatedContent`; key = chuỗi ổn định cho mỗi `Destination` (VD `toString()` của data object/data class); dọn `removeState` cho key không còn
  trong back stack (Navigator lộ thêm `val entries: List<Destination>` chỉ đọc nếu cần). `QuotesScreen.kt:75-77` → `rememberSaveable`; snapshot
  lưu `List<String>` id (saver phù hợp Bundle), dẫn ra `List<Quote>` qua `QuoteSeed.all`. Cập nhật KDoc `QuotesScreen.kt:60-63` nếu cần.
  Xong khi: compile, `assembleDebug`; tự kiểm nhanh không crash "Key … was used multiple times" khi chuyển route.
- [x] T10: [ios] `StarryBox` (QĐ #14): `body` = `ZStack { StarField(...).accessibilityHidden(true); content() }`; giữ `.accessibilityHidden(true)`
  ở EmptyState. Xong khi: build iOS.
- [x] T11: [cleanup] T-30 code: xoá thư mục `sharedUI/` (cả file untracked/build bên trong); gỡ 3 alias + 2 version catalog (QĐ #2), sửa comment
  `androidApp/build.gradle.kts:30-32`; sửa KDoc `ReminderStore.kt:7-14`, `ReminderScheduler.kt:25-27`. Grep lại `sharedUI|SystemBackHandler|Room`
  toàn repo (trừ `docs/plans/`) = 0. Xong khi: `./gradlew :androidApp:assembleDebug` + test shared + build iOS (Xcode chạy Gradle) đều xanh.
- [x] T12: [docs] `README.md` viết lại các phần lỗi thời (Hiện trạng T-30): status (Android + iOS đầy đủ, offline-first, dữ liệu lưu trong
  `PersistedState` v3 + migrate từng bước, key `oddly.state.v1`); cây thư mục (thêm usecase, `core/design`, `core/testing`, `notifications/`,
  `ui/feedback/`, iOS `Feedback/`, `Notifications/`, `iosAppUITests`); Persistence (version + migrate + backup/import + fixture `LegacyDocuments.kt`);
  Tests (295 test shared mỗi nền tảng — dev đọc số thật từ `sharedLogic/build/test-results/*/*.xml` sau T11; UI test Android/iOS + lệnh, trỏ
  `.claude/CLAUDE.md`); `JAVA_HOME` macOS; Store submission (policy đã host tại URL trong `Brand.PRIVACY_POLICY_URL`, sửa xong phải đăng lại
  `lil-dua.github.io`); thay "Not built yet" bằng trỏ `docs/release-todo.xlsx` + `docs/release-process.md`; 216 thử thách; đoạn Brand (link landing
  thật); Daily reminder (ReminderPlanner: không nhắc khi đã xong, copy lapsed/streak/xoay vòng; `ReminderStore` vì sao); S01 Android splash hệ thống;
  "Erase all data". Không bịa tính năng — mỗi khẳng định phải kiểm được trong code. `docs/privacy-policy.html:209,220` và `docs/store-listing.md:17,152` (QĐ #15).
- [x] T13: [test] (tester làm) unit test shared + chạy toàn bộ kiểm tra + UI test hai nền tảng + kịch bản QA tay (mục dưới).

## Tiêu chí nghiệm thu
1. Không còn thư mục `sharedUI/`; grep `sharedUI|SystemBackHandler|composeMultiplatform|Room repositor|not persisted|Persistence is the main gap`
   ngoài `docs/plans/` = 0; catalog không còn alias 0-tham-chiếu nêu ở QĐ #2; mọi build xanh.
2. README mô tả đúng: 216 thử thách, `PersistedState` v3 + migrate, số test thật, UI test, offline-first, link policy/landing thật; không còn mục "Not built yet" lỗi thời.
3. `ChallengeSeed.byId` dùng Map; selector không còn `library.firstOrNull` trong vòng lặp; kết quả selector/stats y hệt trước (test cũ xanh).
4. Hai AppState: cùng tên/kiểu public (`streak`, `weekActivity`, `completedToday`, `swapsLeft`, `canReroll`, `exploredCategoryCount`, `completions`);
   giá trị đúng ngay sau: hoàn thành, đổi thử thách, adopt, đổi ngày (`refreshDay`), xoá dữ liệu, nhập backup, onboarding xong.
5. Android `absorb` không còn `clear()+addAll()`; `currentState()` không copy list.
6. `life_sunrise`/`life_watch_something_end` có câu cấm nhìn thẳng mặt trời cả vi + en, id và số bước không đổi.
7. `wantAnotherChallengeBody` không còn trong code. Không còn copy xoay vòng nào nói "chuỗi"/"streak".
8. S08 khi đã xong hôm nay hiện "Hẹn mai nhé"; khi hết lượt (chưa xong) hiện "Bạn đã dùng hết lượt đổi hôm nay"; không bao giờ "Còn 0 lượt". Hai nền tảng.
9. iOS gỡ thông báo đã hiện của hôm nay khi đã xong kể cả khi nhắc tắt / không có quyền.
10. Android S14: chọn "Yêu thích", lướt tới quote thứ 2, bấm Share → Back: vẫn "Yêu thích" + đúng quote. Rời S14 về Home rồi mở lại: về "Tất cả" + quote hôm nay.
11. Tab Statistics có id `tab.statistics` cả hai nền tảng.
12. VoiceOver EmptyState không đổi (astronaut vẫn ẩn); `StarryBox` không còn ẩn `content()`.
13. Privacy policy EN + store listing dùng "Erase all data"; `store-listing.md:17` không còn số version cứng.
14. Test shared tăng (≥ 295 + test mới), JVM và iOS cùng số; UI test Android 7 / iOS 6 PASS.

## Kế hoạch test (cho tester)
**Unit test shared (`sharedLogic/src/commonTest`, tên backtick không dấu phẩy):**
- `SeedContentTest` hoặc file mới: `byId finds every challenge in the library` (mọi `all[i]` → `byId(id) === all[i]`); `byId returns null for an unknown id`.
- `ChallengeSelectorTest`: `recent categories count completions of library challenges only` (completion có id không trong library bị bỏ qua, không crash);
  `a duplicated id in the library resolves to its first entry` nếu khả thi qua trọng số (nếu không quan sát được qua API công khai thì bỏ, ghi lý do).
- `DailyChallengeRulesTest`: `swapsLeft from parts matches swapsLeft from state` trên các fixture có sẵn (`withSwaps(0..3)`, pick hôm qua, `dailyChallenge = null`,
  đã xong hôm nay); `swapsLeft from parts is zero once the day is done`.
- `StatsCalculatorTest`: một test `summarize distribution matches distribution of the window` (bảo vệ refactor).
- `ReminderPlannerTest` / seed test: `rotating reminder copy never mentions a streak` (duyệt `0 until 6` × 2 ngôn ngữ, không chứa "chuỗi", "streak", "unbroken", "gián đoạn").
- `SeedContentTest`: `sunrise and sunset challenges warn against looking at the sun` (2 id, vi chứa "nhìn thẳng vào mặt trời", en chứa "look straight at the sun").
- Chạy: `./gradlew :sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test`, đọc số test từ XML (JVM = iOS).

**Build/UI test:** `:androidApp:assembleDebug`, `:androidApp:bundleRelease` (R8, vì đổi catalog/xoá module), `xcodebuild … build`,
`:androidApp:connectedDebugAndroidTest` (7) và XCUITest `iosAppUITests` (6) — bắt buộc vì đổi AppState, `OddlyApp` (SaveableStateHolder) và testTag.

**QA tay — Android (emulator/máy thật):**
1. Hoàn thành thử thách → Home: streak, dải tuần, thẻ "Muốn thử thách khác?" mờ + "Hẹn mai nhé" cập nhật ngay; Journey: streak + "lĩnh vực đã khám phá" đúng.
2. Đổi thử thách 3 lần → Home "Thử thách này là của bạn…"; vào lại S08 qua back stack (Home → chi tiết → Back… hoặc giữ S08 dưới) → caption "Bạn đã dùng hết lượt đổi hôm nay", bấm → về Home.
3. Sau khi hoàn thành, mở lại S08 qua back stack → "Hẹn mai nhé".
4. Thống kê: đổi tab range Tuần/Tháng/… số liệu đúng; hoàn thành mới → S12 cập nhật.
5. Lịch (S10): hoàn thành rồi mở lịch → ô hôm nay sáng.
6. Đổi ngày (đổi giờ hệ thống qua nửa đêm, resume): streak/dải tuần/swapsLeft về giá trị ngày mới.
7. Xoá tất cả dữ liệu → mọi dẫn xuất về 0; Nhập backup → dẫn xuất khớp backup.
8. S14: chip "Yêu thích" + lướt → Share → Back giữ nguyên; Back về Home rồi mở lại → "Tất cả"; chuyển nhanh giữa các route không crash.
9. Tab Statistics: Layout Inspector thấy testTag `tab.statistics`.
10. Bản R8 (`bundleRelease` → cài qua bundletool hoặc `assembleRelease` nếu có): mở app, hoàn thành, không crash.

**QA tay — iOS (simulator iPhone 17 Pro):**
1–4, 6–7 như Android (S08 caption, Home, Journey, Statistics, đổi ngày, xoá/nhập).
5. Tắt nhắc (hoặc từ chối quyền), có một thông báo hôm nay đã hiện trong Notification Center, hoàn thành thử thách → thông báo bị gỡ.
8. S14: Share (sheet) → đóng → giữ chip (hành vi cũ, kiểm không hồi quy).
9. Accessibility Inspector: tab Statistics id `tab.statistics`; EmptyState (Journey/Statistics khi chưa có dữ liệu) VoiceOver đọc tiêu đề/nút như trước, không đọc astronaut.
10. Ghi rõ trong Test report những kịch bản chỉ QA tay.

## Rủi ro & giả định
- `@Observable` + `didSet`: Observation hỗ trợ property observer, nhưng nếu biên dịch không cho, dùng gọi tường minh (đã ghi ở T3). Bất kỳ đường gán
  `completions`/`today` mới sau này phải giữ cache đúng — ghi chú trong KDoc.
- `derivedStateOf` đọc ngoài composition (trong callback `OddlyApp`) vẫn trả giá trị đúng (tính lại khi dependency đổi) — giả định theo tài liệu Compose; UI test sẽ bắt hồi quy.
- `SaveableStateProvider` trong `AnimatedContent`: hai route cùng key compose đồng thời sẽ crash — key theo `Destination` khác nhau giữa route cũ/mới;
  `selectTab` cùng tab đã return sớm (`Navigator.kt:37`). Có thể có trường hợp cùng `ChallengeDetail(id)` xuất hiện hai lần trong stack (hiếm) → dùng chung state, chấp nhận.
- Xoá alias catalog: Gradle không lỗi khi catalog thiếu alias không ai dùng; nếu IDE cache báo lỗi thì Sync lại.
- README: số test phải đọc từ XML thật sau khi tester thêm test — dev ghi số tại T12, tester cập nhật nếu đổi.
- Privacy policy sửa trong repo chưa có hiệu lực tới khi người dùng đăng lại site `lil-dua.github.io`.
- Copy mới (ReminderSeed, 2 bước thử thách) là đề xuất của planner — người dùng có thể chỉnh chữ, không ảnh hưởng code.

## Dev notes (dev điền)
Đã làm T1–T12. Lệnh đã chạy: `:androidApp:compileDebugKotlin` + `:sharedLogic:compileKotlinIosSimulatorArm64` (OK),
`:sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test :androidApp:assembleDebug` (OK; XML: 295/0 fail cả JVM và iOS),
`xcodebuild … build` (BUILD SUCCEEDED). Chưa chạy `bundleRelease` và UI test (T13).

**Shared**
- `ChallengeSeed.kt` `byIdIndex` + `byId` O(1); `ChallengeSelector.kt` map category theo `library` (giữ phần tử đầu khi trùng id);
  `StatsCalculator.summarize` tính `distribution` một lần; `DailyChallengeRules.swapsLeft(dayDone, pickDate, swapsUsed, today)` + overload cũ uỷ quyền.
- Nội dung: 2 bước `life_sunrise` / `life_watch_something_end` (vi+en); `ReminderSeed` index 2 + KDoc (không nêu "14 ngày" vì không kiểm được ở code: ghi "scheduled days ahead");
  xoá `wantAnotherChallengeBody` (interface + vi + en). `UiTestTags.TAB_STATISTICS`.
**Android**
- `OddlyAppState.kt`: `completions` là `var List` + `mutableStateOf` (`private set`); `streak`/`weekActivity`/`completedToday`/`exploredCategoryCount` = `derivedStateOf`;
  `swapsLeft` qua overload mới; `absorb` gán một lần; `currentState()` không copy.
- Màn: `remember` ở `StatisticsScreen`, `JourneyScreen`, `ChooseCategoryScreen`. `BottomBar.kt` `testTag: String` non-null. S08 thêm `completedToday` (`AnotherChallengeScreen.kt`, `OddlyApp.kt`).
- S14: `OddlyApp.kt` `rememberSaveableStateHolder` + `SaveableStateProvider(destination.toString())` quanh `when` (đã thụt lề thêm 4 space); `LaunchedEffect(current)` chờ 500ms rồi `removeState`
  cho key không còn trong `Navigator.entries` (mới) — chờ để không xoá key đang chạy animation thoát. `QuotesScreen.kt` `rememberSaveable` với snapshot là `ArrayList<String>` id.
- Comment: `ReminderStore.kt`, `ReminderScheduler.kt`, `androidApp/build.gradle.kts`.
**iOS**
- `OddlyAppState.swift`: `didSet` trên `today`/`completions` gọi `recomputeDerived()`; 4 giá trị dẫn xuất stored `private(set)`; `static derive(...)` dùng chung cho init và recompute; build OK với `@Observable` + `didSet` (không cần phương án gọi tường minh). `resetAllData` dùng `removeAll()` nên cũng kích `didSet`.
- `AnotherChallengeScreen.swift` + `MainShell.swift` (caption 3 nhánh); `ReminderScheduler.swift` gỡ thông báo trước `guard`; `Illustrations.swift` `StarryBox` chỉ ẩn `StarField`; `Destination.swift` bỏ `default`; `UITestIds.swift` `tabStatistics`.
**Cleanup/docs**
- Xoá `sharedUI/` (3 file tracked, đang là " D" chưa stage); gỡ 3 alias + 2 version khỏi `libs.versions.toml`. Grep `sharedUI|SystemBackHandler|composeMultiplatform|Room repositor|not persisted|Persistence is the main gap` ngoài `docs/plans/` và PLAN.md = 0.
- `README.md` viết lại các phần lỗi thời (status, cây thư mục, Persistence, Tests = 295 + UI test, JAVA_HOME macOS, Store submission, "What is left", Brand, Daily reminder, ReminderStore). `privacy-policy.html:209,220`, `store-listing.md:17,152` ("Erase all data", bỏ version cứng). Người dùng cần đăng lại site `lil-dua.github.io`.

**Chú ý cho tester/reviewer**
- Kiểm S14 Android: Share → Back giữ chip/vị trí; Home → mở lại S14 về "Tất cả" (phụ thuộc cleanup sau 500ms; mở lại S14 trong <500ms sau khi rời có thể còn state cũ).
- `derivedStateOf` đọc ngoài composition trong callback `OddlyApp` (đã giả định đúng; UI test sẽ bắt).
- `swapsLeft` Android đọc `dailyChallengeDate` là var thường (như cũ), `swapsUsed` là snapshot state.
- Số test 295 là trước khi tester thêm test; README ghi 295, tester cập nhật nếu đổi.
- Không tạo file backup sed; không đụng `release-todo.xlsx`, `docs/plans/README.md`.


**Vòng sửa 1**
- Warning 1 (holder khôi phục state route cũ sau recreate): chọn phương án (a). `OddlyApp.kt` ngay trước `SaveableStateProvider`: `remember(routeKey) { if (savedKeys.add(routeKey)) saveableStates.removeState(routeKey) }`.
  Key lần đầu compose trong phiên thì bỏ slot đã khôi phục từ Bundle; key còn sống trong phiên (Share -> Back) không bị xoá vì `savedKeys` đã chứa nó.
- Test: `QuotesStateTest.favoritesFilterDoesNotSurviveActivityRecreation` (chọn "Đã thích" -> `app.recreate()` -> mở S14 -> chip "Tất cả" được chọn); thêm `OddlyAppRule.recreate()`.
- Đã chạy: `compileDebugKotlin` OK; `connectedDebugAndroidTest` 9/9 pass (emulator đã tắt). Chưa kiểm test mới có đỏ khi bỏ fix.

## Test report (tester điền)
PASS

**Lệnh đã chạy và kết quả**
- `:sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test`: XML đọc thật = **303 test, 0 fail** cả JVM và iOS (295 + 8 mới).
- `:androidApp:assembleDebug :androidApp:bundleRelease` (R8): BUILD SUCCESSFUL.
- `xcodebuild … build` (iPhone 17 Pro): BUILD SUCCEEDED.
- `:androidApp:connectedDebugAndroidTest` (emulator Medium_Phone_API_36.0, đã tắt sau khi chạy): **8/8 pass** (7 cũ + 1 mới `QuotesStateTest`).
- `xcodebuild test … -only-testing:iosAppUITests`: **6/6 pass**, TEST SUCCEEDED.

**Test đã thêm**
- `DailyChallengeRulesTest`: `swapsLeft from parts matches swapsLeft from state`, `swapsLeft from parts is zero once the day is done`, `swapsLeft from parts ignores swaps used on another day`.
- `StatsCalculatorTest`: `summarize distribution matches distribution of the window`, `distribution skips completions whose challenge id is unknown`.
- `ChallengeSelectorTest`: `recent categories count completions of library challenges only` (completion id ngoài library bị bỏ qua, không crash). Bỏ test trùng id trong library: không quan sát được qua API công khai.
- `ReminderPlannerTest`: `rotating reminder copy never mentions a streak` (6 ngày x 2 ngôn ngữ).
- `SeedContentTest`: `sunrise and sunset challenges warn against looking at the sun` (+ 2..4 bước, <=120 ký tự).
- `ChallengeSeed.byId` đã có test sẵn (`every challenge is reachable by id`, `an unknown id resolves to nothing...`).
- Android `androidTest/.../QuotesStateTest.kt` (`favoritesFilterSurvivesShareAndResetsOnReopen`): Home -> S14 -> thích 1 quote -> chip "Đã thích" -> Share -> Back: chip vẫn được chọn; Back về Home, tiến đồng hồ test 1000ms, mở lại S14: chip "Tất cả" được chọn. Tìm bằng text (S14 chưa có test tag).
- README: số test shared 295 -> 303.

**S14 Android (câu hỏi của dev)**
- Share -> Back giữ chip lọc: xác nhận bằng UI test (pass).
- Mở lại sau khi rời reset đúng: pass. Lưu ý khi viết test: `LaunchedEffect` + `delay(500)` chạy trên đồng hồ ảo của Compose test, `Thread.sleep` không làm nó chạy (lần đầu test fail vì vậy, không phải lỗi sản phẩm); đã dùng `mainClock.advanceTimeBy`. Trên máy thật là thời gian thực.
- Phân tích: mở lại S14 trong vòng <500ms sau khi rời vẫn thấy state cũ (đã ghi ở Dev notes) — gần như không xảy ra bằng tay, không coi là bug.

**Bug tìm thấy:** không.

**QA tay còn lại (chưa coi là pass)**
- Android: kịch bản 1-3 (Home/Journey/S08 caption "Hẹn mai nhé" / "Bạn đã dùng hết lượt đổi hôm nay" qua back stack), 4 (Statistics đổi range), 5 (Calendar ô hôm nay), 6 (qua nửa đêm), 7 (xoá/nhập backup), 9 (testTag `tab.statistics` bằng Layout Inspector), 10 (cài và chạy bản R8; mới chỉ build `bundleRelease`).
- iOS: kịch bản 1-4, 6-7 tương tự; 5 (gỡ thông báo đã hiện khi nhắc tắt/không có quyền, cần Notification Center); 8 (S14 sheet Share giữ chip); 9 (VoiceOver EmptyState không đọc astronaut, Accessibility Inspector `tab.statistics`).
- Đọc copy 2 bước mặt trời và copy nhắc mới bằng mắt (nội dung do planner đề xuất).
- Privacy policy sửa trong repo cần được đăng lại lên `lil-dua.github.io`.

### Test report - Vòng sửa 1
PASS

- Kiểm test bắt được lỗi: tạm đổi dòng fix thành `savedKeys.add(routeKey)` (bỏ `removeState`), chạy riêng `QuotesStateTest.favoritesFilterDoesNotSurviveActivityRecreation` -> **FAIL** (`AssertionError: Failed to assert ... (Selected = 'true')`, 1 test 1 fail). Khôi phục fix, `diff` với bản trước khi sửa trống.
- `connectedDebugAndroidTest` (emulator Medium_Phone_API_36.0, đã tắt): **9/9 pass** (XML `tests="9" failures="0"`).
- `:androidApp:bundleRelease` + `:sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test` (`--rerun-tasks`): BUILD SUCCESSFUL; XML = **303 JVM + 303 iOS, 0 fail**.
- iOS không đổi trong vòng này nên không chạy lại UI test iOS.
- Bug mới: không. QA tay còn lại: như báo cáo trên (đổi cỡ chữ/ngôn ngữ hệ thống hoặc process death thật trên máy thật chưa thử; recreate đã được test tự động).

## Review (reviewer điền)
**CHANGES REQUESTED**

Đã kiểm: diff toàn bộ + `QuotesStateTest.kt`; XML test shared = 303 (JVM) / 303 (iOS); grep tiêu chí 1 = 0 (`README.md:118`
"Seed content is not persisted" là câu đúng, không lỗi thời); catalog: 3 alias gỡ không còn tham chiếu `libs.*`; id thử thách
không đổi (chỉ sửa text `how`). Memo hoá Android (`mutableStateOf` List + `derivedStateOf`) và iOS (`didSet` trên `today`/`completions`,
`removeAll()` cũng kích `didSet`, init seed bằng `derive`) vô hiệu đúng ở mọi đường ghi (`absorb`, `resetAllData`, `refreshDay`,
`importBackup`); overload `swapsLeft` khớp `swapsUsedOn`. S08 caption, iOS ReminderScheduler, StarryBox, tab id, docs đều khớp plan.

### Cần sửa
**Warning**
1. `androidApp/src/main/kotlin/dev/lildua/oddly/OddlyApp.kt:134-147` (+ `SaveableStateProvider` ở `:182`): `rememberSaveableStateHolder()`
   được lưu/khôi phục qua Bundle của Activity, nhưng `Navigator` là `remember` (`Navigator.kt:51-52`) và `savedKeys` cũng là `remember` —
   sau khi Activity bị tạo lại, holder mang về state của các route cũ mà cleanup không bao giờ biết để xoá.
   Kịch bản: ở S14 chọn "Đã thích" (hoặc lướt tới quote thứ 5) → đổi cỡ chữ / ngôn ngữ hệ thống (`fontScale`, `locale` không có trong
   `configChanges`, `AndroidManifest.xml:26`) hoặc app bị kill khi ở nền rồi mở lại → app về Home (đúng), `savedKeys` rỗng → mở S14:
   `SaveableStateProvider("Quotes")` khôi phục chip "Đã thích" + snapshot + `index` cũ (sau process death qua đêm còn là vị trí của
   hôm qua thay vì quote hôm nay). Trái tiêu chí 10 ("mở lại từ Home → Tất cả + quote hôm nay"). Cùng cơ chế làm Home/Settings… khôi phục
   scroll cũ.
   Gợi ý (chọn một): (a) xoá state khi một key được compose lần đầu trong phiên này — trong content lambda, trước `SaveableStateProvider`:
   `remember(key) { if (savedKeys.add(key)) saveableStates.removeState(key) }` (key chưa compose nên `removeState` chỉ bỏ entry đã khôi
   phục), giữ cleanup 500ms như hiện tại; hoặc (b) cho holder không sống qua recreate, khớp `Navigator`: tạo holder trong
   `CompositionLocalProvider(LocalSaveableStateRegistry provides null) { rememberSaveableStateHolder() }` (holder hỗ trợ parent null).
   Thêm kiểm chứng: `StateRestorationTester` hoặc `ActivityScenario.recreate()` trong `QuotesStateTest` (chọn "Đã thích" → recreate →
   mở S14 → chip "Tất cả"), hoặc ghi QA tay nếu harness không hỗ trợ.

### Ngoài phạm vi
- `docs/landing/index.html` và `docs/privacy-policy.html:6` có thêm dòng favicon data-URI — PLAN.md không nhắc, có vẻ người dùng tự sửa
  song song; không đánh giá là thay đổi của đợt (nội dung chỉ là SVG inline, không gọi mạng).
- QĐ #10 ghi "không màn nào khác dùng `rememberSaveable`" — thực tế `rememberScrollState()` (Home, Journey, Statistics, Settings, Detail…)
  là saveable, nên nay quay lại route trong back stack sẽ giữ vị trí cuộn (VD Home → chi tiết → Back). Hành vi chuẩn, vô hại; chỉ ghi nhận.
- iOS `refreshDay`/`absorb` gán `today` rồi `completions` → `recomputeDerived()` chạy 2 lần mỗi lần đổi ngày/nhập; rẻ, không đáng sửa.
- Mở lại S14 trong < 500ms sau khi rời vẫn thấy state cũ (dev/tester đã ghi) — chấp nhận.

### Vòng 1
**APPROVE**

Đã kiểm Warning 1 (`OddlyApp.kt:180-189`, `OddlyAppRule.recreate()`, Test report vòng sửa 1):
- Thứ tự: `remember(routeKey)` chạy trong composition, trước `SaveableStateProvider` của cùng lambda, nên key chưa được đăng ký
  với holder → `removeState` chỉ bỏ entry đã khôi phục từ Bundle. `LaunchedEffect(current)` chạy sau khi apply composition, nên lúc
  đó key đã có trong `savedKeys` (`+=` là no-op); không có đường nào `LaunchedEffect` thêm key trước `remember` rồi làm fix bị bỏ qua.
- Share → Back: "Quotes" đã vào `savedKeys` từ lần compose đầu; cleanup 500ms không xoá vì "Quotes" vẫn trong `navigator.entries`;
  khi Back, `remember` mới chạy nhưng `add` trả `false` → không `removeState`, state được khôi phục. Test cũ
  `favoritesFilterSurvivesShareAndResetsOnReopen` vẫn pass.
- Rời S14 → mở lại: cleanup xoá key khỏi cả holder và `savedKeys`, lần mở sau `add` trả `true` → `removeState` (no-op). Đúng tiêu chí 10.
- Sau recreate: mọi route (kể cả Home) bỏ state khôi phục — khớp với `Navigator` không sống qua recreate (QĐ "Không làm": khôi phục back stack).
- Test mới đỏ khi bỏ fix (tester đã mutation check), 9/9 Android, 303+303 shared, `bundleRelease` OK. Diff vòng này không đụng iOS/shared.

### Cần sửa
(trống)

### Ngoài phạm vi
- Như vòng trước; không có mục mới.
