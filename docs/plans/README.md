# Trạng thái & lưu trữ kế hoạch

Điểm vào cho phiên mới: đọc file này + `docs/release-todo.xlsx` (nguồn sự thật về task) trước khi làm tiếp.
Kế hoạch đang chạy luôn ở `docs/PLAN.md`; kế hoạch đã APPROVE được chuyển vào thư mục này.

## Đã làm (cập nhật 18/10/2026, sau đợt T-08/T-09)
| Task | Trạng thái | Kế hoạch / báo cáo | Còn lại |
|---|---|---|---|
| T-01 Giờ hoàn thành thật | Xong | (làm cùng Bước 0, trước khi có quy trình PLAN.md) | — |
| T-02 "Hôm nay" theo đổi ngày + banner | Code xong, APPROVE | `T-02.md` | QA tay trên máy thật (Test report trong file) |
| T-03 Chỉ 1 hoàn thành/ngày | Code xong, APPROVE | `T-03.md` | QA tay trên máy thật (Test report trong file) |
| T-04 Giới hạn 3 lượt đổi/ngày | Code xong, APPROVE | `T-04_T-35.md` | QA tay 13 kịch bản (kill app, qua nửa đêm, nâng cấp từ `3244c04`) |
| T-35 Test migration dữ liệu | Code xong, APPROVE | `T-04_T-35.md` | — (fixture `V1_DOCUMENT` + nhánh `migrate`) |
| T-08 Không nhắc khi đã xong + copy giữ chuỗi | Code xong, APPROVE | `T-08_T-09.md` | QA tay (8 Android + 8 iOS, chung với T-09) |
| T-09 Đổi múi giờ/giờ hệ thống, ngày địa phương iOS | Code xong, APPROVE | `T-08_T-09.md` | QA tay: Android #5–6 (TIME_SET/TIMEZONE_CHANGED khi app bị kill), #8 bản release |

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
- Test shared: 151 test mỗi nền tảng (JVM + iosSimulatorArm64).

## Ghi nhận để sau (không thuộc task nào đang mở)
- iOS `ChallengeDetailScreen.swift`: `.dayAlreadyDone` rơi vào `default` (hành vi đúng).
- Android `absorb()` clear + addAll `_completions` mỗi lần — chấp nhận được với dữ liệu nhỏ (liên quan T-31).
- `swapsLeft`/`canReroll` dựng lại `currentState()` mỗi lần đọc — gom vào T-31.
- `Strings.wantAnotherChallengeBody` không còn dùng — xoá khi dọn chuỗi.
- S08 vào lại qua back stack khi hết lượt hiện "Còn 0 lượt" (bấm bị guard về Home) — có thể ẩn caption khi `swapsLeft == 0`.
- iOS chỉ gỡ thông báo đã hiện khi nhắc đang bật + có quyền (sau `guard` trong `reschedule`); Android gỡ vô điều kiện — chuyển `removeDeliveredNotifications` lên trước `guard`.
- Key `.task(id:)` iOS không gồm toàn bộ `completions`: import dữ liệu không dựng lại copy streak tới lần sang ngày/complete kế tiếp.
- Copy xoay vòng cũ "Giữ chuỗi ngày của bạn nhé" trong `ReminderSeed` vẫn có thể hiện với người chưa có chuỗi.
- Fixture `V1_DOCUMENT` viết tay (chỉ 1 completion có `"note": null`); fixture `V2_DOCUMENT` sau này nên sinh bằng `OddlyRepository.save`.

## Gợi ý thứ tự tiếp theo
- **P0 còn lại (chủ yếu việc store, cần thông tin từ người dùng):** T-10, T-11, T-14, T-15, T-16, T-17, T-19, T-20, T-21.
- **P1 code làm được ngay bằng `team-run`:** T-05 (favorites — đổi `PersistedState` → v3, thêm nhánh `migrate` + fixture), T-07, T-29, T-12, T-13.
