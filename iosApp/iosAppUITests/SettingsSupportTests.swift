import XCTest

/// T-12: the "Support & legal" rows exist in Settings. Links are not tapped (leaving the app is flaky).
final class SettingsSupportTests: XCTestCase {
    private let ui = OddlyAppLauncher()

    override func setUp() {
        continueAfterFailure = false
    }

    func testSettingsShowsSupportRowsAndHidesRateWithoutStoreId() {
        ui.launch(now: "2026-03-10T09:00:00")
        ui.skipOnboarding()
        ui.waitFor(UITestIds.homeTodayTitle)
        ui.tap(UITestIds.tabSettings)
        ui.waitFor(UITestIds.settingsContact)

        var swipes = 0
        while !ui.exists(UITestIds.settingsPrivacy) && swipes < 6 {
            ui.app.swipeUp()
            swipes += 1
        }
        XCTAssertTrue(ui.exists(UITestIds.settingsPrivacy), "privacy row missing")
        XCTAssertTrue(ui.exists(UITestIds.settingsContact), "contact row missing")
        XCTAssertTrue(ui.exists(UITestIds.settingsAbout), "about row missing")
        // Brand.APP_STORE_ID is nil until T-17, so the rate row must stay hidden; flip this assert then.
        XCTAssertFalse(ui.exists(UITestIds.settingsRate), "rate row must be hidden without an App Store id")
    }
}
