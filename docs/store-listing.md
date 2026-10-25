# Store listing — 1% HUMAN

Paste-ready copy for Google Play and the App Store, in Vietnamese and English.
Character counts are given where the store enforces a limit; every string below
is inside its limit as written.

Placeholders in `[SQUARE BRACKETS]` are the only things that still need filling in.

---

## 1. Identity

| Field | Value |
| --- | --- |
| Android application ID | `dev.lildua.oddly` |
| iOS bundle identifier | `dev.lildua.oddly` |
| Version | See `iosApp/Configuration/Version.xcconfig` (`MARKETING_VERSION` / `CURRENT_PROJECT_VERSION`; bump with `tools/bump-version.sh`) |
| Minimum OS | Android 7.0 (API 24), iOS 17.0 |
| Default / primary language | Vietnamese (`vi-VN`) |
| Additional language | English (`en-US`) |
| Devices | Phone only. iOS ships as iPhone-only; the layout is not designed for iPad. |
| Price | Free, no in-app purchases, no ads |

**Primary language matters.** Set Vietnamese as the default listing language on
both stores — it is the app's default in-app language and the primary market.
English is added as a second localisation, not a replacement.

---

## 2. Google Play

### App name — 30 characters max

| Locale | String | Count |
| --- | --- | --- |
| vi-VN | `1% HUMAN — Thử thách mỗi ngày` | 29 |
| en-US | `1% HUMAN — Daily Challenge` | 26 |

### Short description — 80 characters max

| Locale | String | Count |
| --- | --- | --- |
| vi-VN | `Mỗi ngày một thử thách nhỏ để sống trọn vẹn hơn 1%. Offline, miễn phí.` | 70 |
| en-US | `One small challenge a day to become 1% more human. Fully offline, free.` | 70 |

### Full description — 4000 characters max

**vi-VN**

```
Bạn không cần thay đổi cả cuộc đời trong một ngày. Chỉ cần tốt hơn 1%.

1% HUMAN giao cho bạn một thử thách nhỏ mỗi ngày — đủ nhỏ để làm được ngay hôm nay, đủ ý nghĩa để bạn nhớ mãi. Gọi cho một người bạn lâu ngày không gặp. Viết một dòng cho chính mình của một năm sau. Đi một con đường khác về nhà.

MỖI NGÀY MỘT THỬ THÁCH
Mở ứng dụng, bạn có đúng một việc để làm. Không danh sách dài, không cảm giác tội lỗi vì chưa xong. Nếu thử thách hôm nay chưa hợp, đổi lấy một thử thách khác chỉ bằng một chạm.

SÁU LĨNH VỰC CỦA CUỘC SỐNG
💪 Sức khỏe · 💗 Mối quan hệ · 🌱 Phát triển bản thân
🎨 Sáng tạo · 💰 Tài chính · 🌍 Trải nghiệm cuộc sống

Chọn những lĩnh vực bạn quan tâm, ứng dụng sẽ ưu tiên gợi ý theo đó.

THẤY MÌNH ĐANG ĐI TỚI
Chuỗi ngày liên tiếp, cấp độ, điểm XP và lịch hành trình cho bạn thấy mình đã đi được bao xa — không phải để so sánh với ai, mà để nhớ rằng bạn có đang đi.

CHIA SẺ KHI BẠN MUỐN
Hoàn thành một thử thách, tạo một tấm ảnh đẹp về nó và gửi cho người bạn nghĩ tới. Ảnh chỉ hiện thành tích của bạn — không ghi chú, không dữ liệu cá nhân.

LỜI NHẮC ĐÚNG GIỜ CỦA BẠN
Đặt giờ nhắc theo nhịp sống của bạn, không phải giờ mặc định của ai khác. Tắt bất cứ lúc nào.

HOÀN TOÀN OFFLINE
Không tài khoản. Không đăng nhập. Không máy chủ. Ứng dụng thậm chí không xin quyền truy cập Internet. Mọi thứ bạn làm đều nằm trên máy bạn, và bạn có thể xuất ra tệp JSON, nhập lại khi đổi máy, hoặc xóa sạch bất cứ lúc nào.

MIỄN PHÍ, KHÔNG QUẢNG CÁO
Không quảng cáo, không mua trong ứng dụng, không thu thập dữ liệu để bán.

SONG NGỮ
Tiếng Việt và English, đổi ngay trong ứng dụng.

---

Một năm có 365 ngày. Tốt hơn 1% mỗi ngày là một con người khác hẳn vào cuối năm.
Bắt đầu từ hôm nay.
```

**en-US**

```
You don't have to change your whole life today. Just be 1% better.

1% HUMAN gives you one small challenge each day — small enough to actually do before bed, meaningful enough that you remember it. Call the friend you keep meaning to call. Write a line to yourself one year from now. Walk home a different way.

ONE CHALLENGE A DAY
Open the app and there is exactly one thing to do. No endless list, no guilt about what you didn't finish. If today's challenge doesn't fit, swap it for another one with a single tap.

SIX AREAS OF LIFE
💪 Health · 💗 Relationships · 🌱 Self Growth
🎨 Creativity · 💰 Finance · 🌍 Life Experience

Pick the areas you care about and the app leans toward them when it chooses.

SEE YOURSELF MOVING
Streaks, levels, XP and a calendar of everything you've done show you how far you've come — not to compare yourself with anyone, but to remember that you're going somewhere.

SHARE WHEN YOU WANT TO
Finish a challenge, turn it into a card worth looking at, and send it to whoever came to mind. The image shows your achievements only — no notes, no personal data.

A REMINDER AT YOUR HOUR
Set the reminder to your own rhythm, not somebody's default. Turn it off any time.

COMPLETELY OFFLINE
No account. No sign-in. No server. The app doesn't even ask for Internet access. Everything stays on your device, and you can export it as JSON, import it again on a new phone, or erase all of it whenever you like.

FREE, NO ADS
No ads, no in-app purchases, no data harvested to sell.

BILINGUAL
Vietnamese and English, switchable inside the app.

---

There are 365 days in a year. 1% better each day is a different person by the end of it.
Start today.
```

### Category and tags

- **Category:** Health & Fitness — where habit and streak apps sit on Play, and
  where the intent traffic is. Lifestyle is the honest alternative if you would
  rather not compete in Health & Fitness.
- **Tags (choose up to 5):** Habit tracker · Self-improvement · Daily planner ·
  Motivation · Journal

### Content rating (IARC questionnaire)

Every question answers **No**. The app has no violence, no sexual content, no
profanity, no gambling, no drug references, no user-generated content shown to
other users, no location sharing, no purchases, and no ads. It does allow the
user to share an image through the system share sheet — that is *not* in-app
UGC and does not raise the rating.

Expected result: **Everyone / PEGI 3 / 3+**.

### Data safety form

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | N/A — nothing is transmitted |
| Do you provide a way for users to request data deletion? | N/A — but note in the description that Settings → Erase all data erases everything locally |

This is verifiable from the manifest: the app declares only `POST_NOTIFICATIONS`
and `RECEIVE_BOOT_COMPLETED`, and has no `INTERNET` permission at all. Do not
add an analytics or crash-reporting SDK without coming back and changing this
answer — Play cross-checks the declaration against the uploaded bundle.

### Graphics

| Asset | Spec | Required |
| --- | --- | --- |
| App icon | 512 × 512 PNG, 32-bit, no transparency | Yes |
| Feature graphic | 1024 × 500 PNG or JPEG, no transparency | Yes |
| Phone screenshots | 2–8, 9:16, 1080 × 1920 or larger | Yes (min 2, use 6) |

The feature graphic is shown behind the install button — keep the "1%" mark and
the wordmark inside the middle 800 × 400, because the edges get cropped on some
surfaces.

All three are generated by `tools/store-shots.sh` into `build/store-assets/play/`
(`icon-512.png`, `feature-graphic-{vi,en}.png`, `{vi,en}/NN-<name>.png`); see
"How to regenerate" in section 4.

---

## 3. App Store

### Name — 30 characters max

| Locale | String | Count |
| --- | --- | --- |
| vi | `1% HUMAN — Thử thách mỗi ngày` | 29 |
| en-US | `1% HUMAN — Daily Challenge` | 26 |

### Subtitle — 30 characters max

| Locale | String | Count |
| --- | --- | --- |
| vi | `Mỗi ngày một thử thách nhỏ` | 26 |
| en-US | `One small challenge a day` | 25 |

### Promotional text — 170 characters max, editable without a new build

| Locale | String | Count |
| --- | --- | --- |
| vi | `Hôm nay của bạn sẽ khác biệt như thế nào? Một thử thách nhỏ mỗi ngày, hoàn toàn offline, không tài khoản, không quảng cáo.` | 121 |
| en-US | `How will today be different? One small challenge a day — fully offline, no account, no ads.` | 90 |

### Description — 4000 characters max

Use the Play full description above verbatim. It fits both stores, and keeping
one source of truth means the two listings can't drift apart.

### Keywords — 100 characters max, comma-separated, no spaces after commas

| Locale | String | Count |
| --- | --- | --- |
| vi | `thói quen,thử thách,mỗi ngày,streak,phát triển bản thân,nhật ký,mục tiêu,kỷ luật,offline` | 88 |
| en-US | `habit,challenge,daily,streak,self growth,routine,goals,mindfulness,journal,offline` | 82 |

Words already in the app name and subtitle are indexed automatically — don't
spend keyword characters repeating them.

### Categories

- **Primary:** Health & Fitness
- **Secondary:** Lifestyle

### Age rating

All questions answer **None**. Expected result: **4+**.

### App privacy (privacy labels)

Select **Data Not Collected**. This must match `iosApp/iosApp/PrivacyInfo.xcprivacy`,
which declares no tracking, no collected data types, and UserDefaults as the only
required-reason API. Changing one without the other causes a review rejection.

### Export compliance

Already answered in `iosApp/iosApp/Info.plist` via
`ITSAppUsesNonExemptEncryption = false`, so App Store Connect will stop asking on
every upload. Accurate: the app performs no encryption and makes no network calls.

### App Review notes

```
The app is completely offline and requires no account or sign-in.
There is nothing to log into and no demo credentials are needed.

On first launch you are asked to choose interest categories and a reminder
time. You can skip both.

The only permission requested is notifications, used for a local daily
reminder at the time set in Settings. Declining it does not block any
feature.

"Export data (JSON)" in Settings writes the local save file and opens the
system share sheet. Nothing is uploaded anywhere. "Import data (JSON)" only
reads the one file the user picks, on the device; it adds no permission.
```

### Screenshots — 6.9" iPhone, 1290 × 2796 or 1320 × 2868

Since the app is iPhone-only, this is the only size App Store Connect requires;
it scales the set down for smaller devices. Up to 10 per locale, minimum 3.

Generated at 1320 × 2868 from the iPhone 17 Pro Max simulator by
`tools/store-shots.sh` into `build/store-assets/appstore/{vi,en}/` (no alpha channel).

---

## 4. Screenshot plan (both stores, both languages)

Six shots, in this order. The first two are what most people ever see, so they
carry the pitch.

| # | Screen | Caption (vi) | Caption (en) |
| --- | --- | --- | --- |
| 1 | Home with today's challenge | Mỗi ngày một thử thách nhỏ | One small challenge a day |
| 2 | Reward screen after completing | Cảm giác hoàn thành, mỗi ngày | The feeling of done, daily |
| 3 | Journey calendar with a filled streak | Nhìn lại hành trình của bạn | See how far you've come |
| 4 | Stats screen | Sáu lĩnh vực, một cuộc sống | Six areas, one life |
| 5 | Share card | Chia sẻ khi bạn muốn | Share it when you want to |
| 6 | Settings | Offline. Không tài khoản. | Offline. No account. |

The caption source is `tools/store-art/captions.tsv` — change a caption there and
in the table above.

### How to regenerate

```bash
tools/store-shots.sh                  # everything: emulator + simulator + frames (about 5 min)
tools/store-shots.sh --android-only   # raw Android shots, then frames
tools/store-shots.sh --ios-only       # raw iOS shots, then frames
tools/store-shots.sh --frame-only     # re-frame the raw shots already taken (after a caption change)
```

- Android shots come from the `Medium_Phone_API_36.0` emulator (1080 × 2400) and are
  framed onto 1080 × 1920 canvases (Play rejects a long side over twice the short side);
  iOS shots come from the iPhone 17 Pro Max simulator. Both start and stop themselves.
- The iOS step switches the simulator's `AppleLocale`/`AppleLanguages` to `en_US`/`en-US` (once, with a
  reboot) so the status bar shows "9:41" rather than "09:41". The change persists on that simulator; to
  revert, boot it and run `xcrun simctl spawn <udid> defaults delete -g AppleLocale` and
  `... defaults delete -g AppleLanguages`, or erase it with `xcrun simctl erase <udid>`.
- Output: raw shots in `build/store-shots/raw/{android,ios}/{vi,en}/`, store-ready files in
  `build/store-assets/{play,appstore}/` (feature graphics and the 512 icon under `play/`).
  `build/` is git-ignored, so the images are never committed; copy the approved set elsewhere.
- Data is the fixed demo in `StoreShotScenario` (sharedLogic, `core/testing`): clock pinned to
  19/11/2026 09:41, 17 completed days (streak 14, 15 after today's), all six areas, language
  seeded per run, so reruns look the same.
- The screenshot tests are skipped by the normal UI test commands; the script enables them
  (Android arg `storeShots=true`, iOS env `ODDLY_SHOTS_DIR`).

---

## 5. URLs the stores require

| URL | Store | Status |
| --- | --- | --- |
| Privacy policy | Both (required) | https://lil-dua.github.io/projects/oddly/privacy/privacy-policy.html (source: `docs/privacy-policy.html` — re-upload after every edit) |
| Support URL | App Store (required) | https://lil-dua.github.io/projects/oddly/ — publish `docs/landing/index.html` first; App Store does not accept `mailto:` |
| Marketing URL | App Store (optional) | https://lil-dua.github.io/projects/oddly/ (same page) |
| Landing page (link on share card + caption) | In-app | Published 25/10/2026 at https://lil-dua.github.io/projects/oddly/ (source `docs/landing/index.html`) |
| Contact email | Play (required, shown publicly) | huuthom.it@gmail.com |

The policy is hosted on GitHub Pages at the URL above (site repo `lil-dua.github.io`,
path `projects/oddly/privacy/`). `docs/privacy-policy.html` in this repo is the source:
after editing it, copy it to the site repo so the live page and the app stay in sync
(the stores review the live page). The same applies to the landing page: copy
`docs/landing/index.html` to `projects/oddly/index.html` and re-upload after every edit.

---

## 6. Release notes

### 1.0

**vi**

```
Phiên bản đầu tiên của 1% HUMAN.

• Một thử thách mới mỗi ngày, sáu lĩnh vực của cuộc sống
• Chuỗi ngày, cấp độ và lịch hành trình
• Ảnh chia sẻ thành tích
• Xuất và nhập dữ liệu (JSON) để sao lưu
• Lời nhắc hằng ngày vào giờ bạn chọn
• Tiếng Việt và English
• Hoàn toàn offline — không tài khoản, không quảng cáo
```

**en-US**

```
The first release of 1% HUMAN.

• A new challenge every day across six areas of life
• Streaks, levels and a calendar of your journey
• Shareable achievement cards
• Export and import your data (JSON) as a backup
• A daily reminder at the hour you choose
• Vietnamese and English
• Completely offline — no account, no ads
```
