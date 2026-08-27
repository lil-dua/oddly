import SwiftUI
import UIKit

/// Restores the edge-swipe back gesture on a `NavigationStack` whose bar is
/// hidden.
///
/// UIKit turns the interactive pop gesture off whenever the navigation bar is
/// hidden, because it normally has no back button to mirror. Every screen here
/// draws its own header instead, so without this the app would silently lose
/// the swipe iOS users expect on every pushed screen.
extension View {
    func enableSwipeBack() -> some View {
        background(SwipeBackEnabler().frame(width: 0, height: 0))
    }
}

private struct SwipeBackEnabler: UIViewControllerRepresentable {

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = Enabler()
        controller.coordinator = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    /// Owns the gesture delegate, which UIKit holds only weakly.
    final class Coordinator: NSObject, UIGestureRecognizerDelegate {
        weak var navigationController: UINavigationController?

        /// Only pop when there is something to pop back to; at the root the
        /// swipe must do nothing rather than leave an empty stack.
        func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
            (navigationController?.viewControllers.count ?? 0) > 1
        }
    }

    private final class Enabler: UIViewController {
        var coordinator: Coordinator?

        override func didMove(toParent parent: UIViewController?) {
            super.didMove(toParent: parent)
            guard
                let navigationController,
                let gesture = navigationController.interactivePopGestureRecognizer
            else { return }

            coordinator?.navigationController = navigationController
            gesture.delegate = coordinator
            gesture.isEnabled = true
        }
    }
}
