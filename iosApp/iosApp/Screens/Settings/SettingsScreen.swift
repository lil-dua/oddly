import SwiftUI
import SharedLogic

/// S15 — configuration and local-data controls. Reset is confirmation-gated per
/// spec §16.
struct SettingsScreen: View {
    @Environment(\.palette) private var palette

    let state: OddlyAppState

    @State private var showResetDialog = false
    @State private var showAboutDialog = false

    var body: some View {
        let settings = state.settings

        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Cài đặt")
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 20)

                profileCard
                    .padding(.top, 20)

                SectionLabel("Giao diện")
                    .padding(.top, 24)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(
                        icon: .palette,
                        title: "Chủ đề",
                        value: settings.themeMode.title,
                        action: {
                            // Cycle through the three modes.
                            let next: ThemeMode
                            switch settings.themeMode {
                            case ThemeMode.dark: next = .light
                            case ThemeMode.light: next = .system
                            default: next = .dark
                            }
                            state.settings = settings.with(themeMode: next)
                        }
                    )
                    SettingsRow(
                        icon: .globe,
                        title: "Ngôn ngữ",
                        value: settings.language.title,
                        action: {
                            let next: AppLanguage = settings.language == AppLanguage.vietnamese
                                ? .english
                                : .vietnamese
                            state.settings = settings.with(language: next)
                        }
                    )
                }

                SectionLabel("Nhắc nhở")
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(icon: .bell, title: "Lời nhắc hằng ngày", showChevron: false) {
                        OddlyToggle(isOn: Binding(
                            get: { state.settings.reminderEnabled },
                            set: { state.settings = state.settings.with(reminderEnabled: $0) }
                        ))
                    }
                    SettingsRow(
                        icon: .clock,
                        title: "Giờ nhắc",
                        value: DateFormat.shared.time(time: settings.reminderTime),
                        action: {}
                    )
                    SettingsRow(icon: .volume, title: "Âm thanh & rung", showChevron: false) {
                        OddlyToggle(isOn: Binding(
                            get: { state.settings.soundEnabled },
                            set: {
                                state.settings = state.settings.with(soundEnabled: $0, hapticsEnabled: $0)
                            }
                        ))
                    }
                }

                SectionLabel("Dữ liệu")
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                SettingsGroup {
                    SettingsRow(icon: .download, title: "Sao lưu dữ liệu", action: {})
                    SettingsRow(icon: .share, title: "Xuất dữ liệu (JSON)", action: {})
                    SettingsRow(icon: .info, title: "Giới thiệu", action: { showAboutDialog = true })
                }

                SettingsGroup {
                    SettingsRow(
                        icon: .trash,
                        title: "Xóa tất cả dữ liệu",
                        tint: OddlyColors.danger,
                        showChevron: false,
                        action: { showResetDialog = true }
                    )
                }
                .padding(.top, 20)

                Text("1% HUMAN · phiên bản 1.0\nDữ liệu của bạn được lưu an toàn trên thiết bị.")
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
        .alert("Xóa tất cả dữ liệu?", isPresented: $showResetDialog) {
            Button("Hủy", role: .cancel) {}
            Button("Xóa", role: .destructive) { state.resetAllData() }
        } message: {
            Text("Toàn bộ lịch sử, streak và cấp độ sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.")
        }
        .alert("1% HUMAN", isPresented: $showAboutDialog) {
            Button("Đóng", role: .cancel) {}
        } message: {
            Text("""
            Phiên bản 1.0

            Mỗi ngày một điều nhỏ. Một phiên bản tốt hơn.

            Ứng dụng hoạt động hoàn toàn offline. Không tài khoản, không thu thập vị trí, không gửi dữ liệu lên máy chủ.
            """)
        }
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
                Text("Level \(state.profile.level) · \(state.profile.xpInLevel)/\(state.profile.xpForNextLevel) XP")
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
