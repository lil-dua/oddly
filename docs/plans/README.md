# Trạng thái & lưu trữ kế hoạch

Điểm vào cho phiên mới: đọc file này + `docs/release-todo.xlsx` (nguồn sự thật về task) trước khi làm tiếp.
Kế hoạch đang chạy luôn ở `docs/PLAN.md`; kế hoạch đã APPROVE được chuyển vào thư mục này.

## Đã làm (cập nhật 24/10/2026, sau đợt T-24)
| Task | Trạng thái | Kế hoạch / báo cáo | Còn lại |
|---|---|---|---|
| T-01 Giờ hoàn thành thật | Xong | (làm cùng Bước 0, trước khi có quy trình PLAN.md) | — |
| T-02 "Hôm nay" theo đổi ngày + banner | Code xong, APPROVE | `T-02.md` | QA tay trên máy thật (Test report trong file) |
| T-03 Chỉ 1 hoàn thành/ngày | Code xong, APPROVE | `T-03.md` | QA tay trên máy thật (Test report trong file) |
| T-04 Giới hạn 3 lượt đổi/ngày | Code xong, APPROVE | `T-04_T-35.md` | QA tay 13 kịch bản (kill app, qua nửa đêm, nâng cấp từ `3244c04`) |
| T-35 Test migration dữ liệu | Code xong, APPROVE | `T-04_T-35.md` | — (fixture `V1_DOCUMENT` + nhánh `migrate`) |
| T-08 Không nhắc khi đã xong + copy giữ chuỗi | Code xong, APPROVE | `T-08_T-09.md` | QA tay (8 Android + 8 iOS, chung với T-09) |
| T-09 Đổi múi giờ/giờ hệ thống, ngày địa phương iOS | Code xong, APPROVE | `T-08_T-09.md` | QA tay: Android #5–6 (TIME_SET/TIMEZONE_CHANGED khi app bị kill), #8 bản release |
| T-05 Quote yêu thích bền vững (v3) | Code xong, APPROVE | `T-05.md` | QA tay 11 kịch bản (#2 đổi setting giữa chừng, #9 nâng cấp từ `b275512`, #11 bản R8) |
| T-07 System bar theo theme + thẻ share luôn tối | Code xong, APPROVE (1 vòng sửa comment) | `T-07_T-29.md` | QA tay 12 Android + 4 iOS (đổi theme/dark mode lúc đang mở, ảnh share theme Sáng) |
| T-29 Splash Android dùng core-splashscreen | Code xong, APPROVE | `T-07_T-29.md` | QA tay: một splash duy nhất, icon crop API 24–25, bản R8 |
| T-13 Nhập dữ liệu + backup hệ thống | Code xong, APPROVE (1 vòng sửa) | `T-13.md` | QA tay Android #1–10 (#9 `bmgr`, #10 R8), iOS #1–9 (#8 alert sau `fileImporter`) |
| T-28 Một nguồn version + script bump | Xong, APPROVE | `T-28_T-18_T-22.md` | — |
| T-18 iOS deployment target 17.0 | Code xong, APPROVE | `T-28_T-18_T-22.md` | QA tay trên iOS 17 (SwipeBack, StatusBarScrim, fileImporter, ShareLink) — máy dev chỉ có iOS 26.5 |
| T-22 Crash reporting (không SDK) | Xong (tài liệu) | `T-28_T-18_T-22.md`, `docs/release-process.md` | — |
| T-06 Rung + âm hệ thống theo toggle | Code xong, APPROVE | `T-06.md` | QA tay 12 kịch bản trên máy thật (haptic khi `fullScreenCover` S07 mở trên iOS, nghe thử `TONE_PROP_ACK` / âm 1025) |
| T-26A Quote 12 → 60 + test bảo vệ id | Code xong, APPROVE (1 vòng sửa) | `T-26A.md` | Người dùng duyệt `docs/content-review.md`; QA tay 7 bước; T-27 xác minh các câu CẦN XÁC MINH |
| T-26B1 +47 thử thách Khó (120 → 167) | Code xong, APPROVE (1 vòng sửa) | `T-26B1.md` | Người dùng duyệt `docs/content-review-challenges.md`; QA tay 4 kịch bản; thêm 47 id vào `PublishedContent.kt` trước khi phát hành |
| T-26B2 +29 Dễ, +20 Vừa (167 → 216) | Code xong, APPROVE (2 vòng sửa) | `T-26B2.md` | Người dùng duyệt mục B2 của `docs/content-review-challenges.md`; QA tay; thêm 96 id (B1 + B2) vào `PublishedContent.kt` trước khi phát hành |
| T-23A Accessibility: nhãn, vai trò, heading, live region, vùng chạm | Code xong, APPROVE (tester 1 vòng + reviewer 1 vòng sửa) | `T-23A.md` | QA tay TalkBack (12 kịch bản) + VoiceOver (11) + Accessibility Inspector; so ảnh layout trước/sau |
| T-23B Cỡ chữ lớn + tương phản | Code xong, APPROVE (1 vòng sửa) | `T-23B.md` | QA tay Android font scale 1.3/1.5/2.0 (có máy API ≤ 33), iOS 5 màn còn lại + iPhone SE + đổi cỡ chữ khi app mở + Large Content Viewer; ảnh share giống nhau ở mọi cỡ chữ; theme Sáng |
| T-24 UI test smoke Android + iOS | Xong, APPROVE (A: 0 vòng sửa; B: 3 vòng sửa — flaky qua nửa đêm + chạm sớm lúc splash cross-fade) | `T-24.md` | QA tay: TalkBack/VoiceOver nhãn không đổi; Archive thiết bị không chứa `UITestHooks`; chạy UI test trên Android API ≤ 33 khi có AVD |
| T-23C Neon thương hiệu/lĩnh vực làm chữ trên theme Sáng (1.35–2.72) | Chưa làm — cần người duyệt bảng màu thương hiệu | — | — |
| T-12 Pháp lý / Hỗ trợ | Một phần: version lấy từ build | `T-28_T-18_T-22.md` | Row policy / liên hệ / đánh giá — chờ URL, email, link store (T-10/T-11) |

`T-01_T-02_T-03.md` là kế hoạch tổng ban đầu — chỉ để tham khảo lịch sử.

## Nền tảng code đã có (đợt T-01…T-03)
- Luật hoàn thành/đổi/chọn thử thách nằm ở `sharedLogic/.../domain/usecase/DailyChallengeRules.kt`
  (`complete`, `reroll`, `adopt`, `availability`, `isDayDone`; `CompletionRejection`: `DAY_CHANGED` →
  `ALREADY_COMPLETED_TODAY` → `DAY_ALREADY_DONE` → `NOT_TODAYS`). Hai `OddlyAppState` chỉ gọi luật này.
- `Today.now()`; `OddlyRepository.rollover()`; `refreshDay()` ở hai AppState; `DayChangeNotice` + banner ở Home.
- Android đã khai báo `androidx.lifecycle:lifecycle-runtime-compose:2.9.4`.
- `PersistedState` `version = 2` (đợt T-04): `DailyChallenge.swapsUsed`, `OddlyRepository.migrate()` chạy từng bước,
  key lưu vẫn `oddly.state.v1`. Fixture `V1_DOCUMENT` ở `commonTest/.../data/local/LegacyDocuments.kt`.
- `DailyChallengeRules.MAX_SWAPS_PER_DAY = 3`, `swapsLeft()`; reroll + adopt dùng chung quỹ lượt.
- Thông báo (đợt T-08/T-09): `domain/usecase/ReminderPlanner.kt` (`streakBefore`, `content` → null = không nhắc;
  ưu tiên đã xong → lapsed → streak ≥ 3 → copy xoay vòng `ReminderSeed`). Android `ReminderReceiver` đọc repository,
  re-arm trước rồi mới bỏ qua; `BootReceiver` nhận thêm `TIME_SET`/`TIMEZONE_CHANGED`. iOS `reschedule(settings:completions:)`
  theo ngày địa phương (`LocalDate.from`), dựng lại khi sang ngày/hoàn thành (`ReminderScheduleKey`).
- `PersistedState` `version = 3` (đợt T-05): `favoriteQuoteIds: Set<String>`; fixture `V1_DOCUMENT` + `V2_DOCUMENT`
  (sinh bằng `OddlyRepository.save`) ở `LegacyDocuments.kt`. `domain/usecase/QuoteFavorites.kt` (`toggle`, `favorites` theo thứ tự seed).
  AppState hai bên: `favoriteQuoteIds`, `favoriteQuotes`, `isFavorite`, `toggleFavorite`; `absorb` KHÔNG nạp favorites.
- Theme (đợt T-07/T-29): `ThemeMode.isDark(systemIsDark)` ở shared, hai nền tảng đều gọi. Android `ui/theme/SystemBars.kt`
  (`SystemBarsEffect` gọi lại `enableEdgeToEdge` khi theme đổi; API 24–25 nav scrim tối ở theme Sáng). Thẻ share luôn palette tối
  (Android `OddlyTheme.darkPalette`, iOS `.dark` cho preview + `renderCard`). Android splash = `core-splashscreen:1.2.0`
  (`Theme.Oddly.Starting` → `postSplashScreenTheme` `Theme.Oddly`); đã xoá `SplashScreen.kt`/`Destination.Splash`, Navigator mở
  thẳng Home/Onboarding. `androidx.core` resolve 1.18.0 — đừng nâng.
- Import (đợt T-13): `OddlyRepository.readBackup` (decode, không ghi), `MAX_BACKUP_BYTES` 5 MB, `restoreFromBackup`
  (ép onboarding, `resolveDailyChallenge`). AppState hai bên `readBackup`/`importBackup` nạp đủ mọi trường (iOS gán `settings`
  sau cùng). Android `screens/settings/DataImport.kt` (IO + giới hạn byte), `res/xml/data_extraction_rules.xml` + `backup_rules.xml`
  (include `oddly.xml`, `reminder.xml`). iOS `ReminderScheduleKey` có `completionCount`. Privacy policy nhắc backup Google/iCloud.
- Version (đợt T-28): nguồn duy nhất `iosApp/Configuration/Version.xcconfig` (`MARKETING_VERSION`, `CURRENT_PROJECT_VERSION`),
  `Config.xcconfig` `#include` nó, Gradle parse qua `providers.fileContents`, `buildConfig = true`. Tăng bằng `tools/bump-version.sh [x.y]`.
  `Strings.settingsFooter(version)`/`aboutBody(version)`; Android `BuildConfig.VERSION_NAME`, iOS `AppVersion.marketing`.
  Quy trình phát hành + giám sát crash không SDK: `docs/release-process.md`. iOS deployment target 17.0.
- Phản hồi (đợt T-06): `domain/usecase/FeedbackRules.kt` (`selection`, `reward`, `preview` → `FeedbackCue`/`HapticCue`).
  Android `ui/feedback/OddlyFeedback.kt` (`performHapticFeedback`, không quyền VIBRATE; `ToneGenerator` STREAM_NOTIFICATION chỉ khi
  ringer NORMAL). iOS `Feedback/OddlyFeedback.swift` (`.sensoryFeedback` trên MainShell, system sound 1025). Phát tại điểm dispatch,
  không trong `LaunchedEffect` S07. Toggle cũng điều khiển âm thông báo (Android `setSilent`).
- Nội dung (đợt T-26A): `QuoteSeed` 60 câu, mỗi câu có `// Nguồn:`; chính sách attribution trong KDoc (tác giả mất trước 1956
  có tác phẩm cụ thể / "Tục ngữ Việt Nam", "Ca dao" / "1% HUMAN"; không thêm "Khuyết danh"). `commonTest/.../data/seed/PublishedContent.kt`
  chụp id đã phát hành (120 thử thách + lĩnh vực, 12 quote) — thêm id mới phát hành vào đây khi ra bản. `SeedContentTest` có
  `APPROVED_QUOTE_AUTHORS`, `LEGACY_QUOTE_AUTHORS`. Bảng duyệt: `docs/content-review.md`.
- Thử thách (đợt T-26B1): 167 (mỗi lĩnh vực 12 Khó; Khó thuộc 1 trong 4 loại Công sức / Kỹ năng / Vùng thoải mái / Kỷ luật cả ngày,
  quy ước phút/bước trong KDoc `ChallengeSeed`). Thêm mới luôn nối CUỐI list (test `published challenges stay ahead...`).
  Đợt T-26B2: 216 thử thách, mỗi ô lĩnh vực × độ khó 12, mỗi lĩnh vực 36; định nghĩa Dễ/Vừa trong KDoc; test
  `every difficulty has twelve...`, `every category outlasts the cooldown` (≥ 31), `minutes match the difficulty`.
  **96 id mới (47 B1 + 49 B2) CHƯA nằm trong `PublishedContent.kt`** — thêm khi người dùng duyệt xong, trước khi phát hành.
  Bảng duyệt: `docs/content-review-challenges.md`.
- Accessibility (đợt T-23A): chuỗi `a11y*` trong `Strings`; `core/text/AccessibilityText.kt` (câu đọc lịch / dải tuần / biểu đồ;
  bỏ qua cột đệm `DayBar.isPadding`, thứ trần chỉ trong 7 ngày gần nhất). Android `ui/components/Modifiers.kt`:
  `clickableNoRipple(onClickLabel, role = Button)`, `selectableNoRipple`, `toggleableNoRipple`, `a11yHeading/Hidden/LiveRegion`;
  `IconCircleButton`; `SettingsRow(toggled, onToggle)` cả hàng là Switch. iOS `Components/Accessibility.swift` (`accessibilityHeading`,
  `accessibilitySelected`, `minimumTapTarget`, `announce`); `CircleIconButton(label:)`, `OddlyToggle(label:)` bắt buộc nhãn.
  Minh hoạ/emoji trang trí ẩn khỏi trình đọc. Banner đổi ngày không tự đóng khi trình đọc bật.
- Màu + cỡ chữ (đợt T-23B): `core/design/PaletteSpec.kt` (`PaletteSpecs.dark/light`) là nguồn màu nền/chữ/semantic duy nhất cho
  cả hai nền tảng; chữ trạng thái dùng `palette.success/warning/danger/flame` (Sáng: tertiary 666678, success 14713A, warning A14A0A,
  danger B91C1C, flame AA340A; Tối: tertiary 8A8A9C), fill/tint vẫn neon. `commonTest/.../core/design/PaletteContrastTest` chặn
  màu chữ < 4.5. iOS `.oddlyFont(OddlyFont.x)` (`@ScaledMetric`), trần AX2 trong `oddlyPalette`; `FillScrollView`. Android
  `FontScale.kt` (`CappedFontScale`, `FixedFontScale`, `isLargeFontScale`), `Scroll.kt` (`FillHeightScrollColumn`). Thẻ share khoá
  cỡ chữ 1.0/`.large`; tab dưới ≤ 1.3×/xxxLarge; ô đồ hoạ cố định ≤ 1.5×/AX1.
- UI test (đợt T-24): `core/time/Today.pinForTesting(start)` (độ lệch, mặc định 0) + `core/testing/UiTestTags.kt` (id dùng chung).
  Android `androidApp/src/androidTest/kotlin/dev/lildua/oddly/` (`support/OddlyAppRule`, `Flows.kt`; 6 test: smoke, giờ hoàn thành,
  đổi ngày ×2, một hoàn thành/ngày ×2). iOS `iosApp/iosAppUITests/` (5 XCUITest, `OddlyAppLauncher.tap(_:expecting:)`), hook
  `State/UITestHooks.swift` (`#if DEBUG` + env `ODDLY_UI_TEST*`), scheme dùng chung `xcshareddata/xcschemes/iosApp.xcscheme`.
  Test qua nửa đêm iOS chờ giờ thật (~70 s / ~45 s) — mốc `launchedAt` lấy SAU `app.launch()`. Lệnh chạy trong `.claude/CLAUDE.md`.
- Test shared: 274 test mỗi nền tảng (JVM + iosSimulatorArm64).

## Ghi nhận để sau (không thuộc task nào đang mở)
- iOS `ChallengeDetailScreen.swift`: `.dayAlreadyDone` rơi vào `default` (hành vi đúng).
- Android `absorb()` clear + addAll `_completions` mỗi lần — chấp nhận được với dữ liệu nhỏ (liên quan T-31).
- `swapsLeft`/`canReroll` dựng lại `currentState()` mỗi lần đọc — gom vào T-31.
- `Strings.wantAnotherChallengeBody` không còn dùng — xoá khi dọn chuỗi.
- S08 vào lại qua back stack khi hết lượt hiện "Còn 0 lượt" (bấm bị guard về Home) — có thể ẩn caption khi `swapsLeft == 0`.
- iOS chỉ gỡ thông báo đã hiện khi nhắc đang bật + có quyền (sau `guard` trong `reschedule`); Android gỡ vô điều kiện — chuyển `removeDeliveredNotifications` lên trước `guard`.
- Copy xoay vòng cũ "Giữ chuỗi ngày của bạn nhé" trong `ReminderSeed` vẫn có thể hiện với người chưa có chuỗi.
- "Xóa tất cả dữ liệu" đi qua `resetAllData()` (không gọi `repository.clear()`) — xoá favorites chỉ kiểm bằng QA tay; cần test AppState khi có hạ tầng.
- Android S14 dùng `remember` (không `rememberSaveable`) cho `showFavorites`/`favoriteSnapshot`/`index` — xoay màn hình về chip "Tất cả".
- Nhãn trợ năng cho nút tim + chip lọc (cả app chưa có, làm đồng loạt); nút Share ở S14 chia sẻ thử thách chứ không phải quote.
- Android bỏ tagline S01 (splash hệ thống chỉ nền + icon), iOS vẫn giữ — Quyết định #6 của `T-07_T-29.md`; phương án (b) giữ màn Compose nếu muốn.
- iOS theme Sáng: launch screen tối chuyển sang S01 sáng.
- iOS đọc tệp import đồng bộ trên main thread (Android dùng IO) — nếu QA thấy giật với tệp iCloud thì chuyển `Task.detached`.
- iOS `importBackup` ghi store hai lần cùng nội dung (`settings.didSet` rồi `persist()`) — vô hại.
- Privacy policy EN ghi "Reset all data" trong khi app ghi "Erase all data".
- `docs/store-listing.md:17` ghi "currently `1.0`, build `1`" — sẽ lỗi thời sau mỗi lần bump; bỏ hoặc thêm vào checklist.
- Quote trùng ý (nhóm "việc lớn bắt đầu từ nhỏ", nhóm kiên trì); `q_proverb_beginning_hard`/`q_proverb_failure_mother` gốc Hán nhưng ghi "Tục ngữ Việt Nam"; bản en `q_seneca_time` sát bản lưu hành — người dùng quyết khi duyệt content-review / T-27.
- Tiêu đề thử thách vi 55–60 ký tự có thể bị cắt "…" ở dòng 2 trong hàng danh sách trên máy hẹp — rút tiêu đề nếu QA thấy xấu.
- `life_sunrise`, `life_watch_something_end` (đã phát hành) thiếu câu cấm nhìn thẳng mặt trời — sửa chữ không cần migrate.
- `health_dance_three_songs` (Vừa, 12 phút) sát ranh Dễ; vài cặp thử thách gần ý ghi trong Backlog của `docs/content-review-challenges.md`.
- Ô lịch Android ~40dp (trong `aspectRatio`), iOS rộng ~42–45pt — dưới 48dp/44pt, xem lại khi làm lưới ở T-23B.
- M3 `Switch(onCheckedChange = null)` mất `minimumInteractiveComponentSize` riêng → hàng công tắc có thể thấp ~16dp; các chỗ mới thêm vùng chạm làm layout cao lên — so ảnh, bù `heightIn` nếu thiết kế không chấp nhận.
- iOS `StarryBox` ẩn cả `content()` khỏi VoiceOver — chỉ đúng khi bên trong là trang trí; iOS banner Home announce lại khi quay về tab Home.
- Onboarding "Bỏ qua" không tắt `reminderEnabled` (mặc định true) trong khi "Để sau" ở S03 có tắt — hành vi có sẵn, cần quyết định sản phẩm.
- iOS `TabDestination.testId` cho Statistics/Settings trả `tab.<raw>`, Android trả null — thống nhất khi cần test hai tab này.
- UI test: bước chọn sở thích chưa assert ô đã selected (cần `accessibilityAddTraits(.isSelected)` iOS khi có test phụ thuộc sở thích); CI Android phải chờ `sys.boot_completed`.
- Scheme `iosApp` riêng cũ của người dùng giữ ở `xcuserdata/.../xcschemes/iosApp (local backup).xcscheme` (gitignored).
- Fixture `V1_DOCUMENT` viết tay (chỉ 1 completion có `"note": null`); fixture `V2_DOCUMENT` sau này nên sinh bằng `OddlyRepository.save`.

## Gợi ý thứ tự tiếp theo
- **P0 còn lại (chủ yếu việc store, cần thông tin từ người dùng):** T-10, T-11, T-14, T-15, T-16, T-17, T-19, T-20, T-21.
- **P1 code làm được ngay bằng `team-run`:** T-23C nếu người dùng duyệt bảng màu; T-27 (rà nội dung/bản quyền — cần người). Phần còn lại của T-12 chờ T-10/T-11.
