#if DEBUG
import UIKit
import SharedLogic

/// Test-only start-up hook for the `iosAppUITests` target.
///
/// A UI test runs in a separate process and cannot reach into the app, so the
/// environment it launches the app with is the only channel: it asks for a clean
/// slate and a pinned "now". `ODDLY_UI_TEST_SCENARIO=storeShots` then seeds the demo data of
/// `StoreShotScenario` (language from `ODDLY_UI_TEST_LANGUAGE`, `en` or default Vietnamese) for the
/// store screenshots. Env: `ODDLY_UI_TEST`, `_KEEP_DATA`, `_NOW`, `_SCENARIO`, `_LANGUAGE`. Without `ODDLY_UI_TEST=1` this does nothing, and the
/// whole type is compiled out of Release builds.
///
/// Must run before `OddlyAppState()` reads the store, hence the call from `iOSApp.init()`.
enum UITestHooks {
    static func apply() {
        let env = ProcessInfo.processInfo.environment
        guard env["ODDLY_UI_TEST"] == "1" else { return }

        // KEEP_DATA lets a test relaunch the app and see what the first run saved.
        if env["ODDLY_UI_TEST_KEEP_DATA"] != "1" {
            OddlyRepository(store: UserDefaultsStore()).clear()
            if let domain = Bundle.main.bundleIdentifier {
                UserDefaults.standard.removePersistentDomain(forName: domain)
            }
            // Pending reminders from an earlier run would outlive the wiped settings.
            ReminderScheduler.cancelAll()
        }

        if let now = env["ODDLY_UI_TEST_NOW"], !now.isEmpty {
            Today.shared.pinForTesting(start: now)
        }

        if env["ODDLY_UI_TEST_SCENARIO"] == "storeShots" {
            let language: AppLanguage = env["ODDLY_UI_TEST_LANGUAGE"] == "en" ? .english : .vietnamese
            OddlyRepository(store: UserDefaultsStore()).save(
                state: StoreShotScenario.shared.state(today: Today.shared.local(), language: language)
            )
        }

        // Looping illustrations keep XCUITest from ever seeing the app as idle.
        UIView.setAnimationsEnabled(false)
    }
}
#endif
