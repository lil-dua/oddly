import SwiftUI
import UIKit
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

    let state: OddlyAppState

    @State private var notificationsAllowed = true
    @State private var showResetDialog = false
    @State private var showAboutDialog = false
    @State private var sheet: SettingsSheet?

    private enum SettingsSheet: String, Identifiable {
        case theme, language, reminderTime
        var id: String { rawValue }
    }

    var body: some View {
        let settings = state.settings

        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text(strings.settingsTitle)
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
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
                    SettingsRow(icon: .bell, title: strings.dailyReminder, showChevron: false) {
                        OddlyToggle(isOn: Binding(
                            get: { state.settings.reminderEnabled },
                            set: { enabled in
                                state.settings = state.settings.with(reminderEnabled: enabled)
                                guard enabled else { return }
                                Task {
                                    notificationsAllowed = await ReminderScheduler.requestAuthorization()
                                }
                            }
                        ))
                    }
                    SettingsRow(
                        icon: .clock,
                        title: strings.reminderTime,
                        value: DateFormat.shared.time(time: settings.reminderTime),
                        action: { sheet = .reminderTime }
                    )
                    SettingsRow(icon: .volume, title: strings.soundAndHaptics, showChevron: false) {
                        OddlyToggle(isOn: Binding(
                            get: { state.settings.soundEnabled },
                            set: {
                                state.settings = state.settings.with(soundEnabled: $0, hapticsEnabled: $0)
                            }
                        ))
                    }
                }

                if settings.reminderEnabled && !notificationsAllowed {
                    Button {
                        openSystemSettings()
                    } label: {
                        HStack(spacing: 10) {
                            OddlyIconView(.info, size: 18, tint: OddlyColors.warning)
                            Text(strings.notificationsDisabledNotice)
                                .font(OddlyFont.bodySmall)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Text(strings.openSystemSettings)
                                .font(OddlyFont.labelMedium)
                        }
                        .foregroundStyle(OddlyColors.warning)
                        .padding(14)
                        .background(
                            OddlyColors.warning.opacity(0.12),
                            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                        )
                    }
                    .buttonStyle(PressableStyle(pressedScale: 0.99))
                    .padding(.top, 10)
                }

                SectionLabel(strings.sectionData)
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(icon: .download, title: strings.backupData, action: {})
                    SettingsRow(icon: .share, title: strings.exportData, action: {})
                    SettingsRow(icon: .info, title: strings.about, action: { showAboutDialog = true })
                }

                SettingsGroup {
                    SettingsRow(
                        icon: .trash,
                        title: strings.eraseAllData,
                        tint: OddlyColors.danger,
                        showChevron: false,
                        action: { showResetDialog = true }
                    )
                }
                .padding(.top, 20)

                Text(strings.settingsFooter)
                    .font(OddlyFont.bodySmall)
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
        .task { notificationsAllowed = await ReminderScheduler.isAuthorized() }
        .sheet(item: $sheet) { which in
            sheetContent(for: which)
                .oddlyPalette(palette)
                .environment(\.strings, strings)
        }
        .alert(strings.eraseAllDataConfirm, isPresented: $showResetDialog) {
            Button(strings.cancel, role: .cancel) {}
            Button(strings.erase, role: .destructive) { state.resetAllData() }
        } message: {
            Text(strings.eraseAllDataBody)
        }
        .alert("1% HUMAN", isPresented: $showAboutDialog) {
            Button(strings.close, role: .cancel) {}
        } message: {
            Text(strings.aboutBody)
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
                    .font(OddlyFont.titleMedium)
                    .foregroundStyle(palette.textPrimary)
                Text(strings.levelWithXp(
                    level: state.profile.level,
                    current: state.profile.xpInLevel,
                    total: state.profile.xpForNextLevel
                ))
                .font(OddlyFont.bodySmall)
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
                                .font(OddlyFont.bodyLarge)
                                .foregroundStyle(isSelected ? OddlyColors.purple : palette.textPrimary)
                            Spacer()
                            if isSelected {
                                OddlyIconView(.check, size: 14, tint: Color(rgb: 0x0B0B12), lineWidth: 2)
                                    .frame(width: 22, height: 22)
                                    .background(OddlyColors.purple, in: Circle())
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
                .font(OddlyFont.headlineSmall.weight(.bold))
                .foregroundStyle(palette.textPrimary)
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
