import SwiftUI
import SharedLogic

private enum ShareLayout: String, CaseIterable, Identifiable {
    case story
    case square

    var id: String { rawValue }

    func label(_ strings: any Strings) -> String {
        switch self {
        case .story: return strings.shareLayoutStory
        case .square: return strings.shareLayoutSquare
        }
    }

    /// Width / height.
    var ratio: CGFloat {
        switch self {
        case .story: return 9.0 / 16.0
        case .square: return 1
        }
    }
}

/// S18 — the shareable achievement card. Rendered entirely on-device and showing
/// only aggregate achievements, never notes or private data (spec §16).
struct ShareCardScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let challenge: LocalizedChallenge?
    let onClose: () -> Void

    @State private var layout: ShareLayout = .story
    @State private var rendered: Image?

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    header

                    HStack(spacing: 8) {
                        ForEach(ShareLayout.allCases) { entry in
                            let active = entry == layout
                            Button {
                                layout = entry
                            } label: {
                                Text(entry.label(strings))
                                    .font(OddlyFont.labelMedium)
                                    .foregroundStyle(active ? OddlyColors.purple : palette.textTertiary)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(
                                        active ? OddlyColors.purple.opacity(0.2) : palette.surfaceElevated,
                                        in: RoundedRectangle(cornerRadius: OddlyRadius.small, style: .continuous)
                                    )
                            }
                            .buttonStyle(PressableStyle())
                        }
                    }

                    ShareCardPreview(state: state, challenge: challenge, ratio: layout.ratio)
                        .padding(.top, 20)

                    // The system share sheet is also where "Save Image" lives on
                    // iOS, so both actions route through it rather than asking
                    // for a photo-library permission the app does not otherwise
                    // need.
                    Group {
                        if let rendered {
                            ShareLink(
                                item: rendered,
                                preview: SharePreview(
                                    "1% HUMAN · \(strings.challengeCount(count: Int32(state.totalCompleted)))",
                                    image: rendered
                                )
                            ) {
                                shareButtonLabel(strings.shareNow, icon: .share)
                            }
                            .buttonStyle(PressableStyle())
                        } else {
                            shareButtonLabel(strings.shareNow, icon: .share)
                                .opacity(0.4)
                        }
                    }
                    .padding(.top, 24)

                    Text(strings.sharePrivacyNote)
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textTertiary)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .padding(.top, 20)
                        .padding(.bottom, 32)
                }
                .padding(.horizontal, 24)
            }
        }
        .statusBarScrim(palette.background)
        .task(id: layout) { renderCard() }
    }

    private var header: some View {
        HStack {
            Button(action: onClose) {
                OddlyIconView(.close, size: 22, tint: palette.textSecondary)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(PressableStyle())
            Spacer()
            Text(strings.shareCardTitle)
                .font(OddlyFont.titleMedium)
                .foregroundStyle(palette.textPrimary)
            Spacer()
            Color.clear.frame(width: 44, height: 44)
        }
        .padding(.vertical, 12)
    }

    private func shareButtonLabel(_ title: String, icon: OddlyIcon) -> some View {
        HStack(spacing: 8) {
            OddlyIconView(icon, size: 18, tint: OddlyColors.onNeon)
            Text(title)
                .font(OddlyFont.labelLarge)
                .foregroundStyle(OddlyColors.onNeon)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 54)
        .background(OddlyGradients.primaryButton, in: Capsule())
    }

    /// Renders the card composition to a bitmap so it can be handed to the OS
    /// share sheet — the local image generation the spec calls for (§S18).
    @MainActor
    private func renderCard() {
        let width: CGFloat = 360
        // The card's rounded corners are transparent and its fill is layered,
        // so it is composited onto the app background before capture — without
        // that the receiving app's backdrop shows through and washes it out.
        let card = ZStack {
            OddlyColors.background
            ShareCardPreview(state: state, challenge: challenge, ratio: layout.ratio)
        }
        .frame(width: width, height: width / layout.ratio)
        .environment(\.palette, palette)

        let renderer = ImageRenderer(content: card)
        renderer.scale = 3
        // Without this the card exports with its translucency intact and the
        // receiving app's background shows through, washing it out.
        renderer.isOpaque = true
        if let uiImage = renderer.uiImage {
            rendered = Image(uiImage: uiImage)
        }
    }
}

/// The card itself — this composition is what the image renderer captures.
private struct ShareCardPreview: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let challenge: LocalizedChallenge?
    let ratio: CGFloat

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    OddlyColors.background,
                    OddlyColors.composite(OddlyColors.purple, over: OddlyColors.background, alpha: 0.25),
                    OddlyColors.background,
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            StarField(starCount: 70, seed: 53)

            VStack(spacing: 0) {
                GradientText("1% HUMAN", font: OddlyFont.titleLarge)

                Spacer(minLength: 0)

                Text(strings.shareICompleted)
                    .font(OddlyFont.bodyLarge)
                    .foregroundStyle(palette.textSecondary)
                    .multilineTextAlignment(.center)

                GradientText(strings.challengeCount(count: Int32(state.totalCompleted)), font: OddlyFont.displaySmall)
                    .padding(.top, 8)

                Text(strings.keptStreakFor(count: state.streak.current))
                    .font(OddlyFont.bodyLarge)
                    .foregroundStyle(palette.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 8)

                if let challenge {
                    Text("“\(challenge.title)”")
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 16)
                }

                Spacer(minLength: 0)

                Planet(size: 96)

                Text("#1PercentHuman")
                    .font(OddlyFont.labelMedium)
                    .foregroundStyle(OddlyColors.purple)
                    .padding(.top, 16)
            }
            .padding(24)
        }
        .aspectRatio(ratio, contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: OddlyRadius.extraLarge, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: OddlyRadius.extraLarge, style: .continuous)
                .stroke(OddlyColors.purple.opacity(0.3), lineWidth: 1)
        )
    }
}
