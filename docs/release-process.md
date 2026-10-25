# Quy trình phát hành

## 1. Version
- Nguồn duy nhất: `iosApp/Configuration/Version.xcconfig` (`MARKETING_VERSION`, `CURRENT_PROJECT_VERSION`).
  Nằm trong `iosApp/` vì Xcode phải `#include` nó; Gradle tự đọc cùng tệp.
- Android: `versionName` = `MARKETING_VERSION`, `versionCode` = `CURRENT_PROJECT_VERSION` (`androidApp/build.gradle.kts`).
  iOS: `CFBundleShortVersionString` / `CFBundleVersion`. Settings (footer, Giới thiệu) hiển thị `MARKETING_VERSION`
  (Android `BuildConfig.VERSION_NAME`, iOS `AppVersion.marketing`).
- Build number dùng chung hai store và phải tăng sau mỗi lần upload. Nếu một store đã dùng số lớn hơn, nhảy lên số lớn nhất.
- Lệnh: `tools/bump-version.sh` (build + 1) hoặc `tools/bump-version.sh 1.1` (đổi marketing version, build + 1).
  Script không commit/tag; người dùng tự commit.

## 2. Release notes
Cập nhật `docs/store-listing.md` mục 6 trước mỗi bản (mục mới nhất ở trên), cả VI và EN.
Giới hạn: Play ≤ 500 ký tự, App Store ≤ 4000 ký tự.

## 3. Checklist build
0. CI (`ci.yml`) xanh trên commit sẽ phát hành; chạy thủ công workflow "UI tests" (Actions → UI tests → Run workflow).
1. `tools/bump-version.sh [x.y]`.
2. Android: `./gradlew :androidApp:bundleRelease` (AAB đã kèm mapping R8 tại `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`).
3. iOS: Xcode Archive (Release có dSYM); giữ "Upload symbols" bật khi export/upload.
4. Kiểm tra version trên bản build trước khi upload.
5. Landing `lil-dua.github.io/projects/oddly/` trả 200; sau T-17 điền `Brand.APP_STORE_ID` và link App Store trên landing.

## 4. Giám sát crash (không SDK)
- Android: Play Console, Android vitals, Crashes & ANRs. Chỉ gồm người dùng bật chia sẻ chẩn đoán; stack trace được giải mã bằng mapping trong AAB.
- iOS: App Store Connect hoặc Xcode, Organizer, Crashes; TestFlight feedback. dSYM của bản archive phải được giữ/upload.
- App không gửi gì; dữ liệu do Google/Apple thu theo lựa chọn của người dùng (đã nêu trong privacy policy mục 2).

## 5. Nếu sau này cần SDK crash
Phải opt-in, và sửa `docs/privacy-policy.html`, Data safety / App Privacy trong `docs/store-listing.md`,
`iosApp/iosApp/PrivacyInfo.xcprivacy`, thêm quyền INTERNET (xem `.claude/CLAUDE.md`).
