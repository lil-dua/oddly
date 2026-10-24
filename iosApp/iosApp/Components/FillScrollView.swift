import SwiftUI

/// Scrolls when content outgrows the screen but otherwise fills the height, so `Spacer`s inside
/// keep spreading content exactly like the old non-scrolling layouts. Bounce is disabled while the
/// content fits, so at the default text size the screen looks unchanged.
struct FillScrollView<Content: View>: View {
    @ViewBuilder var content: () -> Content

    var body: some View {
        GeometryReader { geo in
            ScrollView {
                content()
                    .frame(maxWidth: .infinity, minHeight: geo.size.height)
            }
            .scrollBounceBehavior(.basedOnSize)
        }
    }
}
