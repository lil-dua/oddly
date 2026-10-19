# Trạng thái & lưu trữ kế hoạch

Điểm vào cho phiên mới: đọc file này + `docs/release-todo.xlsx` (nguồn sự thật về task) trước khi làm tiếp.
Kế hoạch đang chạy luôn ở `docs/PLAN.md`; kế hoạch đã APPROVE được chuyển vào thư mục này.

## Đã làm (cập nhật 19/10/2026, sau đợt T-26A)
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
| T-26B Thử thách 120 → 216 | Chưa làm (phác thảo trong `T-26A.md`) | — | — |
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
- Test shared: 220 test mỗi nền tảng (JVM + iosSimulatorArm64).

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
- Fixture `V1_DOCUMENT` viết tay (chỉ 1 completion có `"note": null`); fixture `V2_DOCUMENT` sau này nên sinh bằng `OddlyRepository.save`.

## Gợi ý thứ tự tiếp theo
- **P0 còn lại (chủ yếu việc store, cần thông tin từ người dùng):** T-10, T-11, T-14, T-15, T-16, T-17, T-19, T-20, T-21.
- **P1 code làm được ngay bằng `team-run`:** T-26B (96 thử thách mới, phác thảo ở `T-26A.md`; có thể tách B1 Khó / B2 Dễ+Vừa), T-23 (accessibility, 2 ngày), T-24 (UI test, 2 ngày), T-27 (rà nội dung/bản quyền — cần người). Phần còn lại của T-12 chờ T-10/T-11.
