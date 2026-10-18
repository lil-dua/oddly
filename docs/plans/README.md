# Trạng thái & lưu trữ kế hoạch

Điểm vào cho phiên mới: đọc file này + `docs/release-todo.xlsx` (nguồn sự thật về task) trước khi làm tiếp.
Kế hoạch đang chạy luôn ở `docs/PLAN.md`; kế hoạch đã APPROVE được chuyển vào thư mục này.

## Đã làm (cập nhật 18/10/2026)
| Task | Trạng thái | Kế hoạch / báo cáo | Còn lại |
|---|---|---|---|
| T-01 Giờ hoàn thành thật | Xong | (làm cùng Bước 0, trước khi có quy trình PLAN.md) | — |
| T-02 "Hôm nay" theo đổi ngày + banner | Code xong, APPROVE | `T-02.md` | QA tay trên máy thật (Test report trong file) |
| T-03 Chỉ 1 hoàn thành/ngày | Code xong, APPROVE | `T-03.md` | QA tay trên máy thật (Test report trong file) |

`T-01_T-02_T-03.md` là kế hoạch tổng ban đầu — chỉ để tham khảo lịch sử.

## Nền tảng code đã có (đợt T-01…T-03)
- Luật hoàn thành/đổi/chọn thử thách nằm ở `sharedLogic/.../domain/usecase/DailyChallengeRules.kt`
  (`complete`, `reroll`, `adopt`, `availability`, `isDayDone`; `CompletionRejection`: `DAY_CHANGED` →
  `ALREADY_COMPLETED_TODAY` → `DAY_ALREADY_DONE` → `NOT_TODAYS`). Hai `OddlyAppState` chỉ gọi luật này.
- `Today.now()`; `OddlyRepository.rollover()`; `refreshDay()` ở hai AppState; `DayChangeNotice` + banner ở Home.
- Android đã khai báo `androidx.lifecycle:lifecycle-runtime-compose:2.9.4`.
- `PersistedState` vẫn `version = 1`.
- Test shared: 117 test mỗi nền tảng (JVM + iosSimulatorArm64).

## Ghi nhận để sau (không thuộc task nào đang mở)
- Android `ChallengeDetailScreen.kt` dùng if/else cho 4 trạng thái availability — đổi sang `when` exhaustive khi động lại màn này.
- iOS `ChallengeDetailScreen.swift`: `.dayAlreadyDone` rơi vào `default` (hành vi đúng).
- Android `absorb()` clear + addAll `_completions` mỗi lần — chấp nhận được với dữ liệu nhỏ (liên quan T-31).

## Gợi ý thứ tự tiếp theo
- **P0 còn lại (chủ yếu việc store, cần thông tin từ người dùng):** T-10, T-11, T-14, T-15, T-16, T-17, T-19, T-20, T-21.
- **P1 code làm được ngay bằng `team-run`:** T-04 (giới hạn đổi thử thách — xây trên `DailyChallengeRules.reroll`), T-08, T-09, T-05 (đổi `PersistedState` → v2, làm cùng T-35), T-07, T-29, T-12, T-13.
