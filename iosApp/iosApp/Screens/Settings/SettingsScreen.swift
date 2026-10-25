import SwiftUI
import UIKit
import UniformTypeIdentifiers
import SharedLogic

/// S15 — configuration and local-data controls. Reset is confirmation-gated per
/// spec §16.
///
/// Theme, language and reminder time each open a bottom sheet rather than
/// cycling on tap: with three theme modes and a free-form time, a row that
/// changes value on every tap makes the user hunt for the option they wanted.
struct SettingsScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings
    // No canOpenURL: it would need LSApplicationQueriesSchemes. The completion
    // reports whether anything handled the URL.
    @Environment(\.openURL) private var openURL

    let state: OddlyAppState

    @State private var notificationsAllowed = true
    @State private var showResetDialog = false
    @State private var exportFailed = false
    @State private var showAboutDialog = false
    @State private var showImporter = false
    @State private var pendingImport: PendingImport?
    @State private var showImportConfirm = false
    @State private var importNotice: ImportNotice?
    @State private var sheet: SettingsSheet?
    @State private var feedback = FeedbackTrigger()
    @State private var linkFallback: LinkFallback?
    @State private var addressCopied = false

    /// What the "couldn't open" alert shows: an explanation and the address to copy.
    private struct LinkFallback {
        let body: String
        let address: String
    }

    /// A decoded file waiting for the user to confirm replacing their data.
    private struct PendingImport {
        let raw: String
        let completed: Int
        let level: Int
    }

    private enum ImportNotice { case failed, succeeded }

    private enum SettingsSheet: String, Identifiable {
        case theme, language, reminderTime
        var id: String { rawValue }
    }

    var body: some View {
        let settings = state.settings

        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text(strings.settingsTitle)
                    .oddlyFont(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .accessibilityHeading()
                    .padding(.top, 20)

                profileCard
                    .padding(.top, 20)

                SectionLabel(strings.sectionAppearance)
                    .padding(.top, 24)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(
                        icon: .palette,
                        title: strings.theme,
                        value: settings.themeMode.title.of(strings),
                        action: { sheet = .theme }
                    )
                    SettingsRow(
                        icon: .globe,
                        title: strings.languageRow,
                        value: settings.language.title,
                        action: { sheet = .language }
                    )
                }

                SectionLabel(strings.sectionReminders)
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(
                        icon: .bell,
                        title: strings.dailyReminder,
                        showChevron: false,
                        hidesTitleFromVoiceOver: true
                    ) {
                        OddlyToggle(isOn: Binding(
                            get: { state.settings.reminderEnabled },
                            set: { enabled in
                                state.settings = state.settings.with(reminderEnabled: enabled)
                                guard enabled else { return }
                                Task {
                                    notificationsAllowed = await ReminderScheduler.requestAuthorization()
                                    // Announced here, not via onChange: the tab is rebuilt on every switch and the
                                    // initial permission check would otherwise re-announce each time (Android's
                                    // live region stays silent for a node present at first composition).
                                    if !notificationsAllowed { announce(strings.notificationsDisabledNotice) }
                                }
                            }
                        ), label: strings.dailyReminder)
                        .accessibilityIdentifier(UiTestTags.shared.SETTINGS_REMINDER)
                    }
                    SettingsRow(
                        icon: .clock,
                        title: strings.reminderTime,
                        value: DateFormat.shared.time(time: settings.reminderTime),
                        action: { sheet = .reminderTime }
                    )
                    SettingsRow(
                        icon: .volume,
                        title: strings.soundAndHaptics,
                        showChevron: false,
                        hidesTitleFromVoiceOver: true
                    ) {
                        OddlyToggle(isOn: Binding(
                            get: { state.settings.soundEnabled },
                            // One switch for in-app sound/haptics and the reminder's sound
                            // (ReminderScheduler reads soundEnabled).
                            set: {
                                state.settings = state.settings.with(soundEnabled: $0, hapticsEnabled: $0)
                                if $0 { feedback.fire(FeedbackRules.shared.preview(settings: state.settings)) }
                            }
                        ), label: strings.soundAndHaptics)
                    }
                }

                if settings.reminderEnabled && !notificationsAllowed {
                    Button {
                        openSystemSettings()
                    } label: {
                        HStack(spacing: 10) {
                            OddlyIconView(.info, size: 18, tint: palette.warning)
                            Text(strings.notificationsDisabledNotice)
                                .oddlyFont(OddlyFont.bodySmall)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Text(strings.openSystemSettings)
                                .oddlyFont(OddlyFont.labelMedium)
                        }
                        .foregroundStyle(palette.warning)
                        .padding(14)
                        .background(
                            OddlyColors.warning.opacity(0.12),
                            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                        )
                    }
                    .buttonStyle(PressableStyle(pressedScale: 0.99))
                    .accessibilityIdentifier(UiTestTags.shared.SETTINGS_NOTIFICATIONS_NOTICE)
                    .padding(.top, 10)
                }

                SectionLabel(strings.sectionData)
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(icon: .download, title: strings.exportData, action: { exportBackup() })
                    SettingsRow(icon: .upload, title: strings.importData, action: {
                        addressCopied = false
                        importNotice = nil
                        exportFailed = false
                        showImporter = true
                    })
                }

                if exportFailed {
                    Text(strings.exportFailed)
                        .oddlyFont(OddlyFont.bodySmall)
                        .foregroundStyle(palette.warning)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(14)
                        .background(
                            OddlyColors.warning.opacity(0.12),
                            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                        )
                        .padding(.top, 10)
                }

                if let notice = importNotice {
                    let color = notice == .succeeded ? palette.success : palette.warning
                    Text(notice == .succeeded ? strings.importSucceeded : strings.importFailed)
                        .oddlyFont(OddlyFont.bodySmall)
                        .foregroundStyle(color)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(14)
                        .background(
                            color.opacity(0.12),
                            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                        )
                        .padding(.top, 10)
                }

                SectionLabel(strings.sectionSupport)
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                SettingsGroup {
                    // Hidden until Brand.APP_STORE_ID is filled in (T-17).
                    if let reviewUrl = ExternalLinks.shared.appStoreReviewUrl {
                        SettingsRow(
                            icon: .star,
                            title: strings.rateApp,
                            action: { open(reviewUrl, fallbackBody: strings.browserFallbackBody, address: reviewUrl) }
                        )
                        .accessibilityHint(strings.a11yOpensStore)
                        .accessibilityIdentifier(UiTestTags.shared.SETTINGS_RATE)
                    }
                    SettingsRow(
                        icon: .mail,
                        title: strings.contactSupport,
                        subtitle: Brand.shared.SUPPORT_EMAIL,
                        action: {
                            let subject = strings.supportEmailSubject(version: AppVersion.marketing, platform: "iOS")
                            open(
                                ExternalLinks.shared.mailto(address: Brand.shared.SUPPORT_EMAIL, subject: subject),
                                fallbackBody: strings.emailFallbackBody,
                                address: Brand.shared.SUPPORT_EMAIL
                            )
                        }
                    )
                    .accessibilityHint(strings.a11yOpensEmail)
                    .accessibilityIdentifier(UiTestTags.shared.SETTINGS_CONTACT)
                    SettingsRow(
                        icon: .shield,
                        title: strings.privacyPolicy,
                        action: {
                            open(
                                Brand.shared.PRIVACY_POLICY_URL,
                                fallbackBody: strings.browserFallbackBody,
                                address: Brand.shared.PRIVACY_POLICY_URL
                            )
                        }
                    )
                    .accessibilityHint(strings.a11yOpensBrowser)
                    .accessibilityIdentifier(UiTestTags.shared.SETTINGS_PRIVACY)
                    SettingsRow(icon: .info, title: strings.about, action: { showAboutDialog = true })
                        .accessibilityIdentifier(UiTestTags.shared.SETTINGS_ABOUT)
                }

                if addressCopied {
                    Text(strings.addressCopied)
                        .oddlyFont(OddlyFont.bodySmall)
                        .foregroundStyle(palette.success)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(14)
                        .background(
                            palette.success.opacity(0.12),
                            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                        )
                        .padding(.top, 10)
                }

                SettingsGroup {
                    SettingsRow(
                        icon: .trash,
                        title: strings.eraseAllData,
                        tint: palette.danger,
                        showChevron: false,
                        action: { showResetDialog = true }
                    )
                }
                .padding(.top, 20)

                Text(strings.settingsFooter(version: AppVersion.marketing))
                    .oddlyFont(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textTertiary)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 20)
                    .padding(.bottom, 32)
            }
            .padding(.horizontal, 20)
        }
        .background(palette.background)
        .statusBarScrim(palette.background)
        .oddlyFeedback(feedback)
        .task { notificationsAllowed = await ReminderScheduler.isAuthorized() }
        // Android uses live regions on these notices; here each is announced when it appears.
        .onChange(of: exportFailed) { _, failed in
            if failed { announce(strings.exportFailed) }
        }
        .onChange(of: addressCopied) { _, copied in
            if copied { announce(strings.addressCopied) }
        }
        .onChange(of: importNotice) { _, notice in
            guard let notice else { return }
            announce(notice == .succeeded ? strings.importSucceeded : strings.importFailed)
        }
        .sheet(item: $sheet) { which in
            sheetContent(for: which)
                .oddlyPalette(palette)
                .environment(\.strings, strings)
        }
        .fileImporter(
            isPresented: $showImporter,
            allowedContentTypes: [.json, .plainText]
        ) { result in
            handleImportPick(result)
        }
        .alert(strings.importConfirmTitle, isPresented: $showImportConfirm) {
            Button(strings.cancel, role: .cancel) { pendingImport = nil }
            Button(strings.importReplace, role: .destructive) {
                if let pending = pendingImport {
                    importNotice = state.importBackup(pending.raw) ? .succeeded : .failed
                }
                pendingImport = nil
            }
        } message: {
            if let pending = pendingImport {
                Text(strings.importConfirmBody(completed: Int32(pending.completed), level: Int32(pending.level)))
            }
        }
        .alert(strings.eraseAllDataConfirm, isPresented: $showResetDialog) {
            Button(strings.cancel, role: .cancel) {}
            Button(strings.erase, role: .destructive) { state.resetAllData() }
        } message: {
            Text(strings.eraseAllDataBody)
        }
        .alert(
            strings.linkFallbackTitle,
            isPresented: Binding(
                get: { linkFallback != nil },
                set: { if !$0 { linkFallback = nil } }
            ),
            presenting: linkFallback
        ) { fallback in
            Button(strings.copy) {
                UIPasteboard.general.string = fallback.address
                addressCopied = true
            }
            Button(strings.close, role: .cancel) {}
        } message: { fallback in
            Text(fallback.body + "\n\n" + fallback.address)
        }
        .alert("1% HUMAN", isPresented: $showAboutDialog) {
            Button(strings.close, role: .cancel) {}
        } message: {
            Text(strings.aboutBody(version: AppVersion.marketing))
        }
    }

    /// Opens [urlString] with the system; when nothing handles it, offers the address as text instead.
    private func open(_ urlString: String, fallbackBody: String, address: String) {
        addressCopied = false
        importNotice = nil
        exportFailed = false
        let fallback = LinkFallback(body: fallbackBody, address: address)
        guard let url = URL(string: urlString) else {
            linkFallback = fallback
            return
        }
        openURL(url) { accepted in
            if !accepted { linkFallback = fallback }
        }
    }

    @ViewBuilder
    private func sheetContent(for which: SettingsSheet) -> some View {
        switch which {
        case .theme:
            OptionPickerSheet(
                title: strings.theme,
                options: ThemeMode.entries,
                selected: state.settings.themeMode,
                label: { $0.title.of(strings) },
                onSelect: {
                    state.settings = state.settings.with(themeMode: $0)
                    sheet = nil
                }
            )

        case .language:
            OptionPickerSheet(
                title: strings.languageRow,
                options: AppLanguage.entries,
                selected: state.settings.language,
                label: { $0.title },
                onSelect: {
                    state.settings = state.settings.with(language: $0)
                    sheet = nil
                }
            )

        case .reminderTime:
            ReminderTimeSheet(
                initial: state.settings.reminderTime,
                onSave: {
                    state.settings = state.settings.with(reminderEnabled: true, reminderTime: $0)
                    sheet = nil
                },
                onCancel: { sheet = nil }
            )
        }
    }

    /// Writes the saved document to a file and offers it to the share sheet
    /// (spec §S22). The exported file is exactly what the app reads back on
    /// launch, so there is no separate backup format to keep in step.
    private func exportBackup() {
        addressCopied = false
        importNotice = nil
        let url = FileManager.default.temporaryDirectory
            .appendingPathComponent("one-percent-human-backup.json")
        do {
            try state.exportJson().write(to: url, atomically: true, encoding: .utf8)
        } catch {
            exportFailed = true
            return
        }
        exportFailed = false
        present(activityItems: [url])
    }

    /// Reads and decodes the picked file, then asks before replacing anything.
    /// Cancelling the picker is a `.failure`, but not one worth reporting.
    private func handleImportPick(_ result: Result<URL, Error>) {
        guard case .success(let url) = result else { return }
        guard
            let raw = readBackupText(at: url),
            let decoded = state.readBackup(raw)
        else {
            importNotice = .failed
            return
        }
        pendingImport = PendingImport(
            raw: raw,
            completed: decoded.completions.count,
            level: Int(decoded.profile.level)
        )
        // An alert raised while the picker is still dismissing is dropped, so
        // wait a beat for the sheet to finish closing.
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            showImportConfirm = true
        }
    }

    private func readBackupText(at url: URL) -> String? {
        let scoped = url.startAccessingSecurityScopedResource()
        defer { if scoped { url.stopAccessingSecurityScopedResource() } }
        let limit = Int(OddlyRepository.companion.MAX_BACKUP_BYTES)
        if let size = try? url.resourceValues(forKeys: [.fileSizeKey]).fileSize, size > limit {
            return nil
        }
        // Some file providers report no size, so also cap the bytes actually read.
        guard let handle = try? FileHandle(forReadingFrom: url) else { return nil }
        defer { try? handle.close() }
        guard let data = try? handle.read(upToCount: limit + 1), data.count <= limit else { return nil }
        return String(data: data, encoding: .utf8)
    }

    /// UIKit's share sheet: `ShareLink` needs its item at construction, and this
    /// one only exists once the user has asked for it.
    private func present(activityItems: [Any]) {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let root = scene.keyWindow?.rootViewController
        else { return }

        let controller = UIActivityViewController(
            activityItems: activityItems,
            applicationActivities: nil
        )
        // iPad presents this as a popover and requires an anchor.
        controller.popoverPresentationController?.sourceView = root.view
        controller.popoverPresentationController?.sourceRect = CGRect(
            x: root.view.bounds.midX, y: root.view.bounds.midY, width: 0, height: 0
        )
        root.present(controller, animated: true)
    }

    private func openSystemSettings() {
        guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
        UIApplication.shared.open(url)
    }

    private var profileCard: some View {
        HStack(spacing: 14) {
            Astronaut(size: 44, animated: false)
                .frame(width: 52, height: 52)
                .background(
                    OddlyColors.purple.opacity(0.15),
                    in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
                )

            VStack(alignment: .leading, spacing: 4) {
                Text(state.profile.displayName)
                    .oddlyFont(OddlyFont.titleMedium)
                    .foregroundStyle(palette.textPrimary)
                Text(strings.levelWithXp(
                    level: state.profile.level,
                    current: state.profile.xpInLevel,
                    total: state.profile.xpForNextLevel
                ))
                .oddlyFont(OddlyFont.bodySmall)
                .foregroundStyle(palette.textTertiary)
                GradientProgressBar(progress: Double(state.profile.levelProgress), height: 5)
                    .padding(.top, 4)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(16)
        .background(
            palette.surfaceElevated,
            in: RoundedRectangle(cornerRadius: OddlyRadius.large, style: .continuous)
        )
    }
}

/// A single-choice bottom sheet over a small, fixed option set.
///
/// Generic over the option type so theme and language share one implementation —
/// they differ only in how an option is labelled.
private struct OptionPickerSheet<Option: AnyObject & Equatable>: View {
    @Environment(\.palette) private var palette

    let title: String
    let options: [Option]
    let selected: Option
    let label: (Option) -> String
    let onSelect: (Option) -> Void

    var body: some View {
        SheetShell(title: title) {
            VStack(spacing: 4) {
                ForEach(Array(options.enumerated()), id: \.offset) { _, option in
                    let isSelected = option == selected
                    Button {
                        onSelect(option)
                    } label: {
                        HStack {
                            Text(label(option))
                                .oddlyFont(OddlyFont.bodyLarge)
                                .foregroundStyle(isSelected ? OddlyColors.purple : palette.textPrimary)
                            Spacer()
                            if isSelected {
                                OddlyIconView(.check, size: 14, tint: Color(rgb: 0x0B0B12), lineWidth: 2)
                                    .frame(width: 22, height: 22)
                                    .background(OddlyColors.purple, in: Circle())
                                    .accessibilityHidden(true)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 16)
                        .background(
                            isSelected ? OddlyColors.purple.opacity(0.16) : .clear,
                            in: RoundedRectangle(cornerRadius: OddlyRadius.small, style: .continuous)
                        )
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(PressableStyle(pressedScale: 0.99))
                    .accessibilitySelected(isSelected)
                }
            }
        }
    }
}

/// Free-form reminder time, rather than a handful of preset hours.
private struct ReminderTimeSheet: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let initial: LocalTime
    let onSave: (LocalTime) -> Void
    let onCancel: () -> Void

    @State private var picked: Date

    init(initial: LocalTime, onSave: @escaping (LocalTime) -> Void, onCancel: @escaping () -> Void) {
        self.initial = initial
        self.onSave = onSave
        self.onCancel = onCancel
        var components = DateComponents()
        components.hour = Int(initial.hour)
        components.minute = Int(initial.minute)
        _picked = State(initialValue: Calendar.current.date(from: components) ?? Date())
    }

    var body: some View {
        SheetShell(title: strings.reminderTime) {
            VStack(spacing: 20) {
                DatePicker(
                    strings.reminderTime,
                    selection: $picked,
                    displayedComponents: .hourAndMinute
                )
                .datePickerStyle(.wheel)
                .labelsHidden()
                .colorMultiply(palette.isDark ? Color(rgb: 0xC9C9D6) : .white)

                HStack(spacing: 12) {
                    SecondaryButton(strings.cancel, action: onCancel)
                    GradientButton(strings.save) {
                        let parts = Calendar.current.dateComponents([.hour, .minute], from: picked)
                        onSave(
                            LocalTime(
                                hour: Int32(parts.hour ?? 9),
                                minute: Int32(parts.minute ?? 0),
                                second: 0,
                                nanosecond: 0
                            )
                        )
                    }
                }
            }
        }
    }
}

/// Shared chrome for the settings sheets: a title, the app's own surface, and a
/// height that fits the content rather than a full-screen takeover.
private struct SheetShell<Content: View>: View {
    @Environment(\.palette) private var palette

    let title: String
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .oddlyFont(OddlyFont.headlineSmall.weight(.bold))
                .foregroundStyle(palette.textPrimary)
                .accessibilityHeading()
                .padding(.bottom, 16)

            content()

            Spacer(minLength: 0)
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(palette.surface)
        .presentationDetents([.height(360)])
        .presentationDragIndicator(.visible)
        .presentationBackground(palette.surface)
    }
}
