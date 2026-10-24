import SwiftUI

@main
struct iOSApp: App {
    init() {
        #if DEBUG
        UITestHooks.apply()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            OddlyRootView()
        }
    }
}
