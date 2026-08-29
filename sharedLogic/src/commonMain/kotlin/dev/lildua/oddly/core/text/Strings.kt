package dev.lildua.oddly.core.text

import dev.lildua.oddly.domain.model.AppLanguage

/**
 * Every piece of UI chrome, in one language.
 *
 * Declared as an interface so the compiler refuses a build where a translation
 * is missing: adding a string to [Strings] breaks both [VietnameseStrings] and
 * [EnglishStrings] until each supplies it.
 *
 * Content strings — challenge titles, quotes, category names — live on the
 * domain models themselves as `LocalizedText`, not here.
 */
interface Strings {

    val language: AppLanguage

    // --- Splash & onboarding (S01–S04) ---
    val splashTaglineLine1: String
    val splashTaglineLine2: String
    val onboardingWelcome: String
    val onboardingSubtitle: String
    val onboardingBenefitDaily: String
    val onboardingBenefitMeaningful: String
    val onboardingBenefitChange: String
    val onboardingStart: String
    val skip: String
    val interestsTitle: String
    val interestsSubtitle: String
    val interestsContinue: String
    val interestsUseAll: String
    val reminderPermissionTitle: String
    val reminderPermissionBody: String
    val reminderPermissionPickTime: String
    val reminderPermissionEnable: String
    val later: String

    // --- Bottom navigation ---
    val tabHome: String
    val tabJourney: String
    val tabStatistics: String
    val tabSettings: String

    // --- Home (S05) ---
    val homeHeadline: String
    val todaysChallenge: String
    val homeStartChallenge: String
    val homeAlreadyDoneToday: String
    val streak: String
    val dailyInspiration: String
    val wantAnotherChallenge: String
    val wantAnotherChallengeBody: String
    val challengesCompletedLabel: String

    // --- Challenge detail (S06) ---
    val whyItMatters: String
    val howToDoIt: String
    val reward: String
    val begin: String
    val alreadyCompletedThis: String
    val seeAnotherChallenge: String

    // --- Complete (S07) ---
    val done: String
    val celebrationTitle: String
    val celebrationBody: String
    val share: String
    val levelUp: String

    // --- Another challenge & categories (S08, S09) ---
    val anotherChallengeTitle: String
    val anotherChallengeBody: String
    val surpriseMe: String
    val or: String
    val chooseAnotherCategory: String
    val chooseCategoryTitle: String
    val back: String

    // --- Calendar (S10) ---
    val calendar: String
    val noChallengeThisDay: String
    val recent: String

    // --- Journey (S11) ---
    val journeyTitle: String
    val currentLevel: String
    val quickStats: String
    val consecutiveDays: String
    val categoriesExplored: String
    val completionRate: String
    val categoryBreakdown: String
    val completionCalendar: String
    val streakRow: String
    val allChallenges: String

    // --- Statistics (S12) ---
    val statisticsTitle: String
    val totalCompleted: String
    val noStatsYet: String
    val noStatsYetBody: String
    val noChallengesInRange: String
    val noData: String

    // --- Streak (S13) ---
    val streakTitle: String
    val days: String
    val personalBest: String
    val totalDone: String
    val lastSevenDays: String
    val keepStreakTitle: String
    val keepStreakDoneToday: String
    val keepStreakPending: String
    val reminderLabel: String
    val reminderToggleBody: String
    val streakNoPenalty: String

    // --- Quotes (S14) ---
    val quotesTitle: String

    // --- Settings (S15) ---
    val settingsTitle: String
    val sectionAppearance: String
    val sectionReminders: String
    val sectionData: String
    val theme: String
    val languageRow: String
    val dailyReminder: String
    val reminderTime: String
    val soundAndHaptics: String
    val backupData: String
    val exportData: String
    val about: String
    val eraseAllData: String
    val eraseAllDataConfirm: String
    val eraseAllDataBody: String
    val erase: String
    val cancel: String
    val close: String
    val save: String
    val settingsFooter: String
    val aboutBody: String

    // --- Share card (S18) ---
    val shareCardTitle: String
    val shareLayoutStory: String
    val shareLayoutSquare: String
    val shareNow: String
    val shareExportFailed: String
    val sharePrivacyNote: String
    val shareICompleted: String

    // --- Empty & library (S19, S20) ---
    val emptyJourneyTitle: String
    val emptyJourneyBody: String
    val exploreChallenges: String
    val allChallengesTitle: String
    val filterAll: String
    val noChallengesFound: String
    val noChallengesFoundBody: String
    val seeAll: String

    // --- Parameterised ---
    fun minutes(count: Int): String
    fun challengeCount(count: Int): String
    fun streakDays(count: Int): String
    fun topicsSelected(count: Int): String
    fun doneOutOf(done: Int, total: Int): String
    fun xpProgress(current: Int, total: Int): String
    fun levelWithXp(level: Int, current: Int, total: Int): String
    fun deltaVsPrevious(delta: Int): String
    fun keptStreakFor(count: Int): String

    companion object {
        fun of(language: AppLanguage): Strings = when (language) {
            AppLanguage.VIETNAMESE -> VietnameseStrings
            AppLanguage.ENGLISH -> EnglishStrings
        }
    }
}

object VietnameseStrings : Strings {

    override val language = AppLanguage.VIETNAMESE

    override val splashTaglineLine1 = "Mỗi bước chân nhỏ"
    override val splashTaglineLine2 = "tạo nên thay đổi lớn."
    override val onboardingWelcome = "Chào mừng bạn đến với"
    override val onboardingSubtitle = "Mỗi ngày chúng tôi sẽ giao cho\nbạn một thử thách nhỏ."
    override val onboardingBenefitDaily = "Thử thách\nmỗi ngày"
    override val onboardingBenefitMeaningful = "Dễ thực hiện\nnhưng ý nghĩa"
    override val onboardingBenefitChange = "Thay đổi\ncuộc sống"
    override val onboardingStart = "Bắt đầu"
    override val skip = "Bỏ qua"
    override val interestsTitle = "Bạn muốn tập trung vào\nkhía cạnh nào?"
    override val interestsSubtitle = "(Bạn có thể thay đổi sau)"
    override val interestsContinue = "Tiếp tục"
    override val interestsUseAll = "Dùng tất cả chủ đề"
    override val reminderPermissionTitle = "Nhắc bạn mỗi ngày nhé?"
    override val reminderPermissionBody =
        "Một lời nhắc nhẹ nhàng vào giờ bạn chọn, để thử thách hôm nay không bị bỏ lỡ. Bạn có thể tắt bất cứ lúc nào."
    override val reminderPermissionPickTime = "Chọn giờ nhắc"
    override val reminderPermissionEnable = "Bật nhắc nhở"
    override val later = "Để sau"

    override val tabHome = "Hôm nay"
    override val tabJourney = "Hành trình"
    override val tabStatistics = "Thống kê"
    override val tabSettings = "Cài đặt"

    override val homeHeadline = "Hôm nay của bạn\nsẽ khác biệt như thế nào?"
    override val todaysChallenge = "Thử thách hôm nay"
    override val homeStartChallenge = "Tôi sẽ làm!"
    override val homeAlreadyDoneToday = "Đã hoàn thành hôm nay"
    override val streak = "Streak"
    override val dailyInspiration = "Cảm hứng mỗi ngày"
    override val wantAnotherChallenge = "Muốn thử thách khác?"
    override val wantAnotherChallengeBody = "Để chúng tôi chọn ngẫu nhiên cho bạn."
    override val challengesCompletedLabel = "Thử thách\nđã hoàn thành"

    override val whyItMatters = "Vì sao điều này quan trọng"
    override val howToDoIt = "Gợi ý"
    override val reward = "Phần thưởng"
    override val begin = "Bắt đầu"
    override val alreadyCompletedThis = "Bạn đã hoàn thành thử thách này"
    override val seeAnotherChallenge = "Xem thử thách khác"

    override val done = "Xong"
    override val celebrationTitle = "Tuyệt vời!"
    override val celebrationBody = "Bạn đã hoàn thành\nthử thách hôm nay."
    override val share = "Chia sẻ"
    override val levelUp = "Lên cấp"

    override val anotherChallengeTitle = "Bạn muốn thử thách\nthêm ngay bây giờ?"
    override val anotherChallengeBody = "Để chúng tôi chọn\nngẫu nhiên cho bạn."
    override val surpriseMe = "Cho tôi bất ngờ"
    override val or = "HOẶC"
    override val chooseAnotherCategory = "Chọn chủ đề khác"
    override val chooseCategoryTitle = "Chọn chủ đề bạn muốn\nthử thách"
    override val back = "Quay lại"

    override val calendar = "Lịch"
    override val noChallengeThisDay = "Không có thử thách nào trong ngày này."
    override val recent = "Gần đây"

    override val journeyTitle = "Hành trình của bạn"
    override val currentLevel = "Cấp độ hiện tại"
    override val quickStats = "Thống kê nhanh"
    override val consecutiveDays = "Ngày liên tiếp"
    override val categoriesExplored = "Chủ đề\nđã khám phá"
    override val completionRate = "Tỷ lệ hoàn thành"
    override val categoryBreakdown = "Phân bổ chủ đề"
    override val completionCalendar = "Lịch hoàn thành"
    override val streakRow = "Chuỗi ngày liên tiếp"
    override val allChallenges = "Tất cả thử thách"

    override val statisticsTitle = "Thống kê"
    override val totalCompleted = "Tổng thử thách đã hoàn thành"
    override val noStatsYet = "Chưa có dữ liệu để thống kê"
    override val noStatsYetBody = "Hoàn thành thử thách đầu tiên\nđể bắt đầu theo dõi tiến trình."
    override val noChallengesInRange = "Chưa có thử thách nào trong khoảng thời gian này."
    override val noData = "Chưa có dữ liệu"

    override val streakTitle = "Chuỗi ngày liên tiếp"
    override val days = "ngày"
    override val personalBest = "Kỷ lục của bạn"
    override val totalDone = "Tổng đã hoàn thành"
    override val lastSevenDays = "7 ngày gần nhất"
    override val keepStreakTitle = "Đừng để chuỗi ngày bị gián đoạn!"
    override val keepStreakDoneToday = "Hôm nay xong rồi. Hẹn gặp lại bạn ngày mai."
    override val keepStreakPending = "Hoàn thành thử thách hôm nay để duy trì chuỗi ngày."
    override val reminderLabel = "Lời nhắc"
    override val reminderToggleBody = "Bật thông báo hằng ngày"
    override val streakNoPenalty = "Mất chuỗi ngày không làm bạn mất XP hay cấp độ."

    override val quotesTitle = "Cảm hứng mỗi ngày"

    override val settingsTitle = "Cài đặt"
    override val sectionAppearance = "Giao diện"
    override val sectionReminders = "Nhắc nhở"
    override val sectionData = "Dữ liệu"
    override val theme = "Chủ đề"
    override val languageRow = "Ngôn ngữ"
    override val dailyReminder = "Lời nhắc hằng ngày"
    override val reminderTime = "Giờ nhắc"
    override val soundAndHaptics = "Âm thanh & rung"
    override val backupData = "Sao lưu dữ liệu"
    override val exportData = "Xuất dữ liệu (JSON)"
    override val about = "Giới thiệu"
    override val eraseAllData = "Xóa tất cả dữ liệu"
    override val eraseAllDataConfirm = "Xóa tất cả dữ liệu?"
    override val eraseAllDataBody =
        "Toàn bộ lịch sử, streak và cấp độ sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác."
    override val erase = "Xóa"
    override val cancel = "Hủy"
    override val close = "Đóng"
    override val save = "Lưu"
    override val settingsFooter =
        "1% HUMAN · phiên bản 1.0\nDữ liệu của bạn được lưu an toàn trên thiết bị."
    override val aboutBody = "Phiên bản 1.0\n\n" +
        "Mỗi ngày một điều nhỏ. Một phiên bản tốt hơn.\n\n" +
        "Ứng dụng hoạt động hoàn toàn offline. Không tài khoản, " +
        "không thu thập vị trí, không gửi dữ liệu lên máy chủ."

    override val shareCardTitle = "Chia sẻ thành quả"
    override val shareLayoutStory = "Dọc 9:16"
    override val shareLayoutSquare = "Vuông 1:1"
    override val shareNow = "Chia sẻ ngay"
    override val shareExportFailed =
        "Không tạo được ảnh chia sẻ. Hãy kiểm tra dung lượng trống rồi thử lại."
    override val sharePrivacyNote =
        "Ảnh chỉ hiển thị thành tích của bạn, không kèm ghi chú hay dữ liệu cá nhân."
    override val shareICompleted = "Tôi đã hoàn thành"

    override val emptyJourneyTitle = "Bạn chưa có thử thách nào"
    override val emptyJourneyBody = "Hãy bắt đầu hành trình\n1% tốt hơn mỗi ngày."
    override val exploreChallenges = "Khám phá thử thách"
    override val allChallengesTitle = "Tất cả thử thách"
    override val filterAll = "Tất cả"
    override val noChallengesFound = "Không tìm thấy thử thách nào"
    override val noChallengesFoundBody = "Thử chọn một chủ đề khác."
    override val seeAll = "Xem tất cả"

    override fun minutes(count: Int) = "$count phút"
    override fun challengeCount(count: Int) = "$count thử thách"
    override fun streakDays(count: Int) = "$count ngày"
    override fun topicsSelected(count: Int) = "$count chủ đề đã chọn"
    override fun doneOutOf(done: Int, total: Int) = "$done / $total đã làm"
    override fun xpProgress(current: Int, total: Int) = "$current / $total XP"
    override fun levelWithXp(level: Int, current: Int, total: Int) =
        "Level $level · $current/$total XP"
    override fun deltaVsPrevious(delta: Int) =
        if (delta >= 0) "+$delta so với kỳ trước" else "$delta so với kỳ trước"
    override fun keptStreakFor(count: Int) = "và duy trì chuỗi ngày\n$count ngày liên tiếp!"
}

object EnglishStrings : Strings {

    override val language = AppLanguage.ENGLISH

    override val splashTaglineLine1 = "Every tiny step"
    override val splashTaglineLine2 = "makes a huge change."
    override val onboardingWelcome = "Welcome to"
    override val onboardingSubtitle = "Every day we hand you\none small challenge."
    override val onboardingBenefitDaily = "A challenge\nevery day"
    override val onboardingBenefitMeaningful = "Easy to do,\nstill meaningful"
    override val onboardingBenefitChange = "Change\nyour life"
    override val onboardingStart = "Get started"
    override val skip = "Skip"
    override val interestsTitle = "What would you like\nto focus on?"
    override val interestsSubtitle = "(You can change this later)"
    override val interestsContinue = "Continue"
    override val interestsUseAll = "Use every topic"
    override val reminderPermissionTitle = "Shall we remind you daily?"
    override val reminderPermissionBody =
        "A gentle nudge at a time you pick, so today's challenge doesn't slip by. You can turn it off whenever you like."
    override val reminderPermissionPickTime = "Pick a reminder time"
    override val reminderPermissionEnable = "Turn on reminders"
    override val later = "Maybe later"

    override val tabHome = "Today"
    override val tabJourney = "Journey"
    override val tabStatistics = "Stats"
    override val tabSettings = "Settings"

    override val homeHeadline = "How will today\nbe different for you?"
    override val todaysChallenge = "Today's challenge"
    override val homeStartChallenge = "I'm in!"
    override val homeAlreadyDoneToday = "Done for today"
    override val streak = "Streak"
    override val dailyInspiration = "Daily inspiration"
    override val wantAnotherChallenge = "Want a different challenge?"
    override val wantAnotherChallengeBody = "Let us pick one at random for you."
    override val challengesCompletedLabel = "Challenges\ncompleted"

    override val whyItMatters = "Why it matters"
    override val howToDoIt = "How to do it"
    override val reward = "Reward"
    override val begin = "Start"
    override val alreadyCompletedThis = "You've already done this one"
    override val seeAnotherChallenge = "See another challenge"

    override val done = "Done"
    override val celebrationTitle = "Brilliant!"
    override val celebrationBody = "You finished\ntoday's challenge."
    override val share = "Share"
    override val levelUp = "Level up to"

    override val anotherChallengeTitle = "Fancy another\nchallenge right now?"
    override val anotherChallengeBody = "Let us pick one\nat random for you."
    override val surpriseMe = "Surprise me"
    override val or = "OR"
    override val chooseAnotherCategory = "Pick a different topic"
    override val chooseCategoryTitle = "Pick the topic you\nwant to be challenged on"
    override val back = "Back"

    override val calendar = "Calendar"
    override val noChallengeThisDay = "No challenges on this day."
    override val recent = "Recent"

    override val journeyTitle = "Your journey"
    override val currentLevel = "Current level"
    override val quickStats = "At a glance"
    override val consecutiveDays = "Day streak"
    override val categoriesExplored = "Topics\nexplored"
    override val completionRate = "Completion rate"
    override val categoryBreakdown = "Topic breakdown"
    override val completionCalendar = "Completion calendar"
    override val streakRow = "Day streak"
    override val allChallenges = "All challenges"

    override val statisticsTitle = "Statistics"
    override val totalCompleted = "Total challenges completed"
    override val noStatsYet = "Nothing to report yet"
    override val noStatsYetBody = "Finish your first challenge\nto start tracking progress."
    override val noChallengesInRange = "No challenges in this period yet."
    override val noData = "No data yet"

    override val streakTitle = "Day streak"
    override val days = "days"
    override val personalBest = "Your record"
    override val totalDone = "Total completed"
    override val lastSevenDays = "Last 7 days"
    override val keepStreakTitle = "Don't break the chain!"
    override val keepStreakDoneToday = "Today's done. See you tomorrow."
    override val keepStreakPending = "Finish today's challenge to keep your streak."
    override val reminderLabel = "Reminder"
    override val reminderToggleBody = "Send a daily notification"
    override val streakNoPenalty = "Losing a streak never costs you XP or levels."

    override val quotesTitle = "Daily inspiration"

    override val settingsTitle = "Settings"
    override val sectionAppearance = "Appearance"
    override val sectionReminders = "Reminders"
    override val sectionData = "Data"
    override val theme = "Theme"
    override val languageRow = "Language"
    override val dailyReminder = "Daily reminder"
    override val reminderTime = "Reminder time"
    override val soundAndHaptics = "Sound & haptics"
    override val backupData = "Back up data"
    override val exportData = "Export data (JSON)"
    override val about = "About"
    override val eraseAllData = "Erase all data"
    override val eraseAllDataConfirm = "Erase all data?"
    override val eraseAllDataBody =
        "Your entire history, streak and level will be permanently deleted. This cannot be undone."
    override val erase = "Erase"
    override val cancel = "Cancel"
    override val close = "Close"
    override val save = "Save"
    override val settingsFooter =
        "1% HUMAN · version 1.0\nYour data is stored safely on this device."
    override val aboutBody = "Version 1.0\n\n" +
        "One small thing a day. A slightly better version of you.\n\n" +
        "The app works entirely offline. No account, " +
        "no location tracking, nothing sent to a server."

    override val shareCardTitle = "Share your progress"
    override val shareLayoutStory = "Portrait 9:16"
    override val shareLayoutSquare = "Square 1:1"
    override val shareNow = "Share now"
    override val shareExportFailed =
        "Couldn't create the share image. Check your free storage and try again."
    override val sharePrivacyNote =
        "The image shows your achievements only — no notes, no personal data."
    override val shareICompleted = "I have completed"

    override val emptyJourneyTitle = "No challenges yet"
    override val emptyJourneyBody = "Start the journey to\n1% better every day."
    override val exploreChallenges = "Explore challenges"
    override val allChallengesTitle = "All challenges"
    override val filterAll = "All"
    override val noChallengesFound = "No challenges found"
    override val noChallengesFoundBody = "Try picking a different topic."
    override val seeAll = "See all"

    override fun minutes(count: Int) = if (count == 1) "1 min" else "$count min"
    override fun challengeCount(count: Int) =
        if (count == 1) "1 challenge" else "$count challenges"
    override fun streakDays(count: Int) = if (count == 1) "1 day" else "$count days"
    override fun topicsSelected(count: Int) =
        if (count == 1) "1 topic selected" else "$count topics selected"
    override fun doneOutOf(done: Int, total: Int) = "$done / $total done"
    override fun xpProgress(current: Int, total: Int) = "$current / $total XP"
    override fun levelWithXp(level: Int, current: Int, total: Int) =
        "Level $level · $current/$total XP"
    override fun deltaVsPrevious(delta: Int) =
        if (delta >= 0) "+$delta vs. previous period" else "$delta vs. previous period"
    override fun keptStreakFor(count: Int) =
        if (count == 1) "and kept a streak going\nfor 1 day!" else "and kept a streak going\nfor $count days!"
}
