import SwiftUI

private struct TopSafeAreaInsetKey: EnvironmentKey {
    static let defaultValue: CGFloat = 0
}

extension EnvironmentValues {
    /// Height of the status-bar strip, measured once at the app root.
    var topSafeAreaInset: CGFloat {
        get { self[TopSafeAreaInsetKey.self] }
        set { self[TopSafeAreaInsetKey.self] = newValue }
    }
}

extension View {
    /// Measures the top safe area and publishes it to the view tree, so screens
    /// deeper down can size a scrim without each doing its own geometry read.
    func measuringTopSafeArea() -> some View {
        modifier(TopSafeAreaReader())
    }

    /// Fills the status-bar strip with the app background so scrolling content
    /// passes behind it rather than colliding with the clock.
    ///
    /// The SwiftUI equivalent of the `statusBarsPadding()` the Android screens
    /// apply outside their scroll container. Needed only where a scroll view
    /// reaches the top of the screen — screens with a top bar above the scroll
    /// already clip there.
    func statusBarScrim(_ color: Color) -> some View {
        modifier(StatusBarScrim(color: color))
    }
}

private struct TopSafeAreaReader: ViewModifier {
    @State private var inset: CGFloat = 0

    func body(content: Content) -> some View {
        content
            .background {
                GeometryReader { proxy in
                    Color.clear
                        .onAppear { inset = proxy.safeAreaInsets.top }
                        .onChange(of: proxy.safeAreaInsets.top) { _, new in inset = new }
                }
            }
            .environment(\.topSafeAreaInset, inset)
    }
}

private struct StatusBarScrim: ViewModifier {
    @Environment(\.topSafeAreaInset) private var inset

    let color: Color

    func body(content: Content) -> some View {
        content.overlay(alignment: .top) {
            color
                .frame(height: inset)
                .ignoresSafeArea(edges: .top)
                .allowsHitTesting(false)
        }
    }
}
