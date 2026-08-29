import SwiftUI

/// Four-destination bottom navigation (spec §5). Everything deeper is a child
/// route, so this bar never grows.
///
/// Hand-rolled rather than `TabView` so the neon pill, the hand-drawn icons and
/// the exact spacing match the Android bar and the design mockup.
struct OddlyBottomBar: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let selected: TabDestination
    let onSelect: (TabDestination) -> Void

    var body: some View {
        VStack(spacing: 0) {
            // A hairline divider so content scrolling under the bar stays legible.
            Rectangle()
                .fill(palette.outline.opacity(0.5))
                .frame(height: 1)

            HStack(spacing: 0) {
                ForEach(TabDestination.allCases, id: \.self) { tab in
                    let active = tab == selected
                    let tint = active ? OddlyColors.purple : palette.textTertiary

                    Button {
                        onSelect(tab)
                    } label: {
                        VStack(spacing: 4) {
                            OddlyIconView(tab.icon, size: 20, tint: tint)
                                .frame(width: 40, height: 28)
                                .background(
                                    active ? OddlyColors.purple.opacity(0.16) : .clear,
                                    in: RoundedRectangle(cornerRadius: 10, style: .continuous)
                                )
                            Text(tab.label(strings))
                                .font(OddlyFont.labelSmall)
                                .foregroundStyle(tint)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 6)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(PressableStyle())
                    .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 12)
            .padding(.top, 10)
            .padding(.bottom, 6)
            .animation(.easeOut(duration: 0.18), value: selected)
        }
        .background(palette.surface)
    }
}
